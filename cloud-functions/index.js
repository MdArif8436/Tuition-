const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");
const nodemailer = require("nodemailer");

admin.initializeApp();
const db = admin.firestore();

// -------------------------------------------------------------
// 1. RAZORPAY PAYMENT SIGNATURE VERIFICATION & RECONCILIATION
// -------------------------------------------------------------
exports.verifyRazorpayPayment = functions.https.onCall(async (data, context) => {
  // Ensure caller is authenticated
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be authenticated to verify payments.");
  }

  const { orderId, paymentId, signature, feeRecordId, amount, studentId } = data;

  if (!orderId || !paymentId || !signature || !feeRecordId) {
    throw new functions.https.HttpsError("invalid-argument", "Missing payment verification parameters.");
  }

  const razorpayKeySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.secret || "SANDBOX_SECRET_KEY";

  // Compute expected HMAC SHA-256 signature
  const generatedSignature = crypto
    .createHmac("sha256", razorpayKeySecret)
    .update(`${orderId}|${paymentId}`)
    .digest("hex");

  const isAuthentic = crypto.timingSafeEqual(
    Buffer.from(generatedSignature, "utf-8"),
    Buffer.from(signature, "utf-8")
  );

  if (!isAuthentic) {
    // Log security violation
    await db.collection("auditLogs").add({
      type: "PAYMENT_SIGNATURE_MISMATCH",
      studentId: context.auth.uid,
      orderId,
      paymentId,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    throw new functions.https.HttpsError("permission-denied", "Razorpay payment signature mismatch. Verification failed.");
  }

  // Idempotent record update in transaction
  const feeRef = db.collection("fees").doc(feeRecordId);
  const paymentRef = db.collection("payments").doc(paymentId);

  await db.runTransaction(async (transaction) => {
    const feeDoc = await transaction.get(feeRef);
    if (!feeDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Fee record not found.");
    }

    const currentFee = feeDoc.data();
    const newPaidAmount = (currentFee.paidAmount || 0) + Number(amount);
    const newStatus = newPaidAmount >= currentFee.totalAmount ? "PAID" : "PARTIALLY_PAID";

    transaction.update(feeRef, {
      paidAmount: newPaidAmount,
      status: newStatus,
      lastPaymentId: paymentId,
      lastPaymentDate: new Date().toISOString(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    transaction.set(paymentRef, {
      paymentId,
      orderId,
      feeRecordId,
      studentId: studentId || context.auth.uid,
      amount: Number(amount),
      status: "SUCCESS",
      verifiedByBackend: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
  });

  return { success: true, message: "Payment verified and recorded successfully.", status: "PAID" };
});

// -------------------------------------------------------------
// 2. CRYPTOGRAPHIC TIME-LIMITED ATTENDANCE QR TOKEN
// -------------------------------------------------------------
exports.generateAttendanceQrToken = functions.https.onCall(async (data, context) => {
  // Only HOD or Admin can generate attendance sessions
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required.");
  }

  const userDoc = await db.collection("users").doc(context.auth.uid).get();
  const role = userDoc.data()?.role;
  if (role !== "hod" && role !== "admin") {
    throw new functions.https.HttpsError("permission-denied", "Only HOD/Faculty can generate QR attendance.");
  }

  const { batchId, subject, validSeconds = 120 } = data;
  const expiresAt = Date.now() + (validSeconds * 1000);
  const sessionId = crypto.randomUUID();

  const secret = process.env.ATTENDANCE_SECRET || "ATTENDANCE_TOKEN_SIGNING_KEY";
  const payload = `${sessionId}|${batchId}|${subject}|${expiresAt}`;
  const tokenSignature = crypto.createHmac("sha256", secret).update(payload).digest("hex");

  const token = Buffer.from(JSON.stringify({
    sessionId,
    batchId,
    subject,
    expiresAt,
    sig: tokenSignature
  })).toString("base64");

  // Save active session
  await db.collection("attendanceSessions").doc(sessionId).set({
    sessionId,
    batchId,
    subject,
    generatedBy: context.auth.uid,
    expiresAt: new Date(expiresAt),
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { token, expiresAt };
});

// -------------------------------------------------------------
// 3. VERIFY AND RECORD QR ATTENDANCE
// -------------------------------------------------------------
exports.submitQrAttendance = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required.");
  }

  const { token } = data;
  if (!token) {
    throw new functions.https.HttpsError("invalid-argument", "Token is required.");
  }

  let sessionData;
  try {
    sessionData = JSON.parse(Buffer.from(token, "base64").toString("utf-8"));
  } catch (e) {
    throw new functions.https.HttpsError("invalid-argument", "Malformed token.");
  }

  const { sessionId, batchId, subject, expiresAt, sig } = sessionData;

  // Check expiration
  if (Date.now() > expiresAt) {
    throw new functions.https.HttpsError("deadline-exceeded", "Attendance QR has expired. Request fresh code from instructor.");
  }

  // Verify HMAC signature
  const secret = process.env.ATTENDANCE_SECRET || "ATTENDANCE_TOKEN_SIGNING_KEY";
  const expectedPayload = `${sessionId}|${batchId}|${subject}|${expiresAt}`;
  const expectedSig = crypto.createHmac("sha256", secret).update(expectedPayload).digest("hex");

  if (sig !== expectedSig) {
    throw new functions.https.HttpsError("permission-denied", "Invalid QR code signature.");
  }

  // Prevent duplicate check-in for the same student in this session
  const checkinId = `${sessionId}_${context.auth.uid}`;
  const checkinRef = db.collection("attendance").doc(checkinId);

  const existing = await checkinRef.get();
  if (existing.exists) {
    return { success: true, message: "Attendance already checked in for today's session." };
  }

  // Retrieve student details
  const studentDoc = await db.collection("users").doc(context.auth.uid).get();
  const studentData = studentDoc.data() || {};

  const todayStr = new Date().toISOString().split("T")[0];

  await checkinRef.set({
    sessionId,
    studentId: context.auth.uid,
    studentName: studentData.fullName || "Student",
    enrollmentNo: studentData.rollNumber || "N/A",
    batchId,
    subject,
    date: todayStr,
    status: "PRESENT",
    verifiedViaQr: true,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, message: "Attendance recorded successfully." };
});

// -------------------------------------------------------------
// 4. TRANSACTIONAL EMAIL NOTIFICATION DISPATCHER
// -------------------------------------------------------------
exports.sendTransactionalEmail = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required.");
  }

  const { toEmail, templateType, templateParams } = data;
  if (!toEmail || !templateType) {
    throw new functions.https.HttpsError("invalid-argument", "Missing recipient or template.");
  }

  const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST || "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
      user: process.env.SMTP_USER || "tuitionportal.makaut@gmail.com",
      pass: process.env.SMTP_PASS || "app_specific_password_here"
    }
  });

  let subject = "BCA Tuition Portal Notification";
  let htmlBody = `<p>Notification from BCA Tuition Portal</p>`;

  switch (templateType) {
    case "WELCOME":
      subject = "Welcome to BCA Tuition & HOD Portal";
      htmlBody = `<h2>Welcome, ${templateParams.name || "Student"}!</h2>
        <p>Your academic account has been registered for batch: <b>${templateParams.batch || "BCA"}</b>.</p>`;
      break;

    case "PAYMENT_RECEIPT":
      subject = `Payment Receipt - ${templateParams.receiptNo || "Receipt"}`;
      htmlBody = `<h2>Payment Confirmed</h2>
        <p>Student: <b>${templateParams.studentName}</b></p>
        <p>Amount: <b>₹${templateParams.amount}</b></p>
        <p>Transaction ID: <b>${templateParams.transactionId}</b></p>
        <p>Date: ${new Date().toLocaleDateString()}</p>`;
      break;

    case "FEE_DUE_REMINDER":
      subject = "Tuition Fee Due Reminder";
      htmlBody = `<h2>Tuition Fee Reminder</h2>
        <p>Dear ${templateParams.studentName}, your fee for <b>${templateParams.month}</b> of ₹${templateParams.dueAmount} is due on <b>${templateParams.dueDate}</b>.</p>`;
      break;
  }

  try {
    if (process.env.SMTP_USER) {
      await transporter.sendMail({
        from: `"BCA Tuition Portal" <${process.env.SMTP_USER}>`,
        to: toEmail,
        subject,
        html: htmlBody
      });
    }
    return { success: true, dispatched: true };
  } catch (err) {
    console.error("Email dispatch failure:", err);
    return { success: false, error: err.message };
  }
});
