Security Guide
This document explains the security model, required configuration, and best practices for the BCA Tuition & HOD Management Platform.

1. Security Principles
Deny access by default.

Authenticate every user before granting access.

Use role-based authorization for HOD, Teacher, Student, Parent, and Super Admin.

Keep all secret keys on the server/Cloud Functions, never in the Android app or frontend.

Validate every input and file upload.

Log sensitive actions for auditing.

Use Firebase App Check to reduce abuse from unauthorized clients.

Firebase API keys identify a project/app; authorization is enforced through Firebase Security Rules, IAM, and App Check. [Firebase Security Checklist]

2. User Roles
Role	Access
Super Admin	Manages all colleges, approves HOD accounts, manages platform settings
HOD / College Admin	Manages one college, teachers, students, batches, fees, reports
Teacher	Manages assigned subjects, batches, attendance, assignments, quizzes, marks
Student	Accesses only their own academic data
Parent	Accesses only linked child data
3. Authentication
Supported Methods
Google Sign-In

Email/password sign-in

Email verification

Password reset through Gmail/email

Optional phone OTP login

Requirements
Require email verification before granting full access.

Send password-reset links only to registered email addresses.

Invalidate old sessions after password reset where possible.

Use secure session handling and Firebase Auth token verification.

Do not store passwords in plain text.

4. Role Switching
When switching between Student, HOD, Teacher, and Parent roles:

Require an individual role-access password/PIN.

Do not use one common fixed password for all users.

Limit failed attempts.

Log every role-switch attempt.

Send a security alert when the role password changes.

Use Cloud Functions or trusted server logic for role changes.

5. Firestore Security Rules
Deploy these baseline rules:

js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    function isAuthenticated() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    function userRole() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
    }

    function isHod() {
      return isAuthenticated() && userRole() == 'hod';
    }

    function isSuperAdmin() {
      return isAuthenticated() && userRole() == 'super_admin';
    }

    match /users/{userId} {
      allow read: if isOwner(userId) || isHod() || isSuperAdmin();
      allow create: if isOwner(userId);
      allow update: if isOwner(userId) || isHod();
      allow delete: if isSuperAdmin();
    }

    match /colleges/{collegeId} {
      allow read: if isAuthenticated();
      allow write: if isSuperAdmin();

      match /students/{studentId} {
        allow read: if isHod() || isOwner(studentId);
        allow write: if isHod();
      }

      match /teachers/{teacherId} {
        allow read: if isAuthenticated();
        allow write: if isHod();
      }

      match /attendance/{attendanceId} {
        allow read: if isHod() || request.auth.uid == resource.data.studentId;
        allow write: if isHod();
      }

      match /fees/{feeId} {
        allow read: if isHod() || request.auth.uid == resource.data.studentId;
        allow write: if isHod();
      }

      match /payments/{paymentId} {
        allow read: if isHod() || request.auth.uid == resource.data.studentId;
        allow create: if isAuthenticated();
        allow update, delete: if isHod();
      }

      match /marks/{markId} {
        allow read: if isHod() || request.auth.uid == resource.data.studentId;
        allow write: if isHod();
      }

      match /submissions/{submissionId} {
        allow read: if isHod() || request.auth.uid == resource.data.studentId;
        allow create, update: if request.auth.uid == resource.data.studentId;
        allow delete: if isHod();
      }

      match /dailyUpdates/{updateId} {
        allow read: if isAuthenticated();
        allow write: if isHod();
      }

      match /announcements/{announcementId} {
        allow read: if isAuthenticated();
        allow write: if isHod();
      }
    }

    match /{document=**} {
      allow read, write: if false;
    }
  }
}
6. Firebase Storage Rules
js
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {

    function isAuthenticated() {
      return request.auth != null;
    }

    match /profilePhotos/{userId}/{file} {
      allow read: if isAuthenticated();
      allow write: if request.auth.uid == userId
        && request.resource.size < 5 * 1024 * 1024
        && request.resource.contentType.matches('image/.*');
    }

    match /studyMaterials/{file} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated()
        && request.resource.size < 25 * 1024 * 1024
        && request.resource.contentType.matches('application/pdf|image/.*|application/vnd.openxmlformats-officedocument.*');
    }

    match /submissions/{studentId}/{file} {
      allow read: if isAuthenticated();
      allow write: if request.auth.uid == studentId
        && request.resource.size < 25 * 1024 * 1024;
    }

    match /teacherCVs/{teacherId}/{file} {
      allow read: if isAuthenticated();
      allow write: if request.auth.uid == teacherId
        && request.resource.size < 20 * 1024 * 1024
        && request.resource.contentType.matches('application/pdf|application/msword|application/vnd.openxmlformats-officedocument.wordprocessingml.document');
    }

    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
7. File Upload Security
Allow only required file types.

Limit file size:

Profile photo: 5 MB

Teacher CV: 20 MB

Study material: 25 MB

Assignment submission: 25 MB

Validate MIME type and file extension.

Scan or inspect uploaded files where possible.

Generate unique file names.

Do not trust original file names.

Use signed URLs or Firebase Security Rules for access control.

8. Payment Security
Use a trusted payment gateway such as Razorpay or PayU.

Verify payment using server-side webhook, signature verification, or gateway API.

Never mark a payment as Paid only because the UPI app returns to the app.

Store transaction ID, amount, status, method, date, and student ID.

Use idempotency keys to prevent duplicate payments.

Do not store card numbers, CVV, UPI PIN, or banking passwords.

Send payment receipts only to verified email addresses.

9. AI Assistant Security
Run AI requests through Cloud Functions or a secure backend.

Never place AI API keys in the Android app or frontend.

Allow answers only from HOD/teacher-approved documents.

Use retrieval-augmented generation (RAG) with approved notes/PDFs.

Limit daily AI requests per student.

Log queries anonymously for abuse monitoring.

Do not allow users to upload arbitrary content directly into the AI knowledge base without approval.

10. API Keys and Secrets
Secret	Correct Location
Firebase Android config	app/google-services.json
Firebase Web config	Frontend environment/config file
Payment gateway public key	Frontend/environment config
Payment gateway secret key	Cloud Functions environment/secret manager
AI API key	Cloud Functions environment/secret manager
SMTP/SendGrid/Resend key	Cloud Functions environment/secret manager
Firebase service-account JSON	Server/Cloud Functions secret manager
UPI ID	Firebase Remote Config or Firestore app settings
Never commit these to GitHub or include them in the Android app source code.

11. App Check
Enable Firebase App Check for:

Firestore

Firebase Storage

Cloud Functions

Use:

Play Integrity for Android

reCAPTCHA Enterprise for Web

Custom provider only if necessary

12. Logging and Monitoring
Enable:

Firebase Crashlytics

Firebase Analytics

Cloud Functions logs

Firestore audit logs where available

Security alerts for password changes and role changes

Log sensitive actions:

Login failures

Role switching

Password changes

Fee status changes

Marks changes

Teacher approval/rejection

File deletion

Backup/restore

Payment verification

13. Android Security Checklist
Set android:exported="false" for internal components.

Do not export unnecessary activities, services, receivers, or providers.

Validate deep links.

Use HTTPS for all network communication.

Use Android Keystore and EncryptedSharedPreferences for sensitive local data.

Use BiometricPrompt for app lock.

Do not store biometric data in the app.

Enable R8/ProGuard for release builds.

Remove debug logs from release builds.

Keep dependencies updated.

14. Incident Response
If a key, service account, or secret is exposed:

Rotate the exposed key immediately.

Revoke active sessions/tokens if needed.

Review Firestore, Storage, and Cloud Function logs.

Check for unauthorized reads/writes.

Update Security Rules if a vulnerability is found.

Notify affected users if personal data was accessed.

Deploy a fixed release and verify with tests.

15. Deployment Checklist
Production Firestore rules deployed

Production Storage rules deployed

App Check enabled

Email verification enabled

Password reset configured

Payment webhook verification enabled

Secret keys stored in server environment/secret manager

Debug logs removed from release build

Crashlytics enabled

Backup and restore tested

Role-based access tested

Release AAB/APK signed properly
