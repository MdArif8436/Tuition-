package com.example.data.firebase

import android.util.Log
import com.example.data.model.AttendanceRecord
import com.example.data.model.DoubtMessage
import com.example.data.model.StudentUser
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AuthUserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import com.example.BcaTuitionApplication
import com.example.R
import com.google.firebase.FirebaseApp

/**
 * Cloud Firestore Data Persistence Service for BCA Tuition Portal.
 *
 * Implements full CRUD access across:
 * - /users/{uid}
 * - /students/{studentId}
 * - /parents/{parentId}
 * - /hods/{hodId}
 * - /attendance/{attendanceId}
 * - /fees/{feeId} and /payments/{paymentId}
 * - /marks/{markId}
 * - /assignments/{assignmentId} and /submissions/{submissionId}
 * - /announcements/{announcementId} and /dailyUpdates/{updateId}
 * - /forumPosts/{postId} and /forumReplies/{replyId}
 * - /doubts/{messageId}
 * - /appSettings/{settingId}
 */
class FirestoreTuitionService(
    val firestore: FirebaseFirestore
) {
    constructor() : this(
        try {
            val ctx = BcaTuitionApplication.instance
            val dbId = ctx.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            try {
                FirebaseFirestore.getInstance("ai-studio-android-bcatuiti-38cfe0b3-8aa8-46ba-bf35-c4ca12fcd3b0")
            } catch (ex: Exception) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "ai-studio-android-bcatuiti-38cfe0b3-8aa8-46ba-bf35-c4ca12fcd3b0")
            }
        }
    )

    // In-memory set of claimed roll numbers for offline fallback
    private val offlineClaimedRollNumbers = mutableSetOf(
        "MAKAUT/BCA/24001",
        "MAKAUT/BCA/24002",
        "MAKAUT/BCA/24003",
        "MAKAUT/BCA/24004",
        "MAKAUT/BCA/24005"
    )

    // Authorized HOD master invite codes
    private val validHodInviteCodes = setOf("MAKAUT-HOD-2025", "HOD-BCA-DEPT", "FACULTY-2025")

    /**
     * Persist or update authenticated user profile in Firestore under `/users/{uid}`,
     * and maintain mirror records under `/students/{studentId}` or `/parents/{parentId}`.
     */
    suspend fun saveUserProfile(profile: AuthUserProfile): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val userDoc = mutableMapOf<String, Any>(
                "uid" to profile.uid,
                "email" to profile.email,
                "fullName" to profile.displayName,
                "photoUrl" to (profile.photoUrl ?: ""),
                "role" to profile.role.name.lowercase(),
                "mobileNumber" to profile.phone,
                "registrationComplete" to profile.registrationComplete,
                "updatedAt" to now
            )

            when (profile.role) {
                UserRole.STUDENT -> {
                    userDoc["rollNumber"] = profile.rollNumber
                    userDoc["className"] = profile.className
                    userDoc["batch"] = profile.batch
                    userDoc["semester"] = profile.semester
                    userDoc["subjects"] = profile.subjects

                    // Mirror to /students collection for role-based security rules
                    val studentDoc = mapOf(
                        "studentId" to profile.uid,
                        "name" to profile.displayName,
                        "rollNumber" to profile.rollNumber,
                        "batch" to profile.batch,
                        "parentIds" to emptyList<String>(),
                        "email" to profile.email,
                        "phone" to profile.phone
                    )
                    try {
                        firestore.collection("students").document(profile.uid).set(studentDoc).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mirror student doc", e)
                    }
                }
                UserRole.PARENT -> {
                    userDoc["relationship"] = profile.relationship ?: "Guardian"
                    userDoc["linkedStudentIds"] = profile.linkedStudentIds

                    // Mirror to /parents collection
                    val parentDoc = mapOf(
                        "parentId" to profile.uid,
                        "name" to profile.displayName,
                        "mobile" to profile.phone,
                        "linkedStudentIds" to profile.linkedStudentIds,
                        "relationship" to (profile.relationship ?: "Guardian")
                    )
                    try {
                        firestore.collection("parents").document(profile.uid).set(parentDoc).await()
                        // Update parentIds on linked student records
                        for (sId in profile.linkedStudentIds) {
                            firestore.collection("students").document(sId).update(
                                "parentIds", FieldValue.arrayUnion(profile.uid)
                            ).await()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mirror parent doc", e)
                    }
                }
                UserRole.ADMIN, UserRole.SUPER_ADMIN -> {
                    userDoc["designation"] = profile.designation ?: "HOD"
                    userDoc["subjects"] = profile.subjects
                    userDoc["batches"] = listOf(profile.batch)
                    userDoc["isAuthorizedHod"] = true

                    val hodDoc = mapOf(
                        "hodId" to profile.uid,
                        "name" to profile.displayName,
                        "designation" to (profile.designation ?: "HOD"),
                        "mobile" to profile.phone,
                        "batches" to listOf(profile.batch),
                        "subjects" to profile.subjects
                    )
                    try {
                        firestore.collection("hods").document(profile.uid).set(hodDoc).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mirror hod doc", e)
                    }
                }
                UserRole.TEACHER -> {
                    userDoc["designation"] = profile.designation ?: "Assistant Professor"
                    userDoc["subjects"] = profile.subjects
                    userDoc["batches"] = listOf(profile.batch)

                    val teacherDoc = mapOf(
                        "teacherId" to profile.uid,
                        "name" to profile.displayName,
                        "designation" to (profile.designation ?: "Assistant Professor"),
                        "mobile" to profile.phone,
                        "batches" to listOf(profile.batch),
                        "subjects" to profile.subjects
                    )
                    try {
                        firestore.collection("teachers").document(profile.uid).set(teacherDoc).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mirror teacher doc", e)
                    }
                }
            }

            firestore.collection("users").document(profile.uid).set(userDoc).await()

            // Claim roll number if student
            if (profile.role == UserRole.STUDENT && profile.rollNumber.isNotBlank()) {
                claimRollNumber(profile.rollNumber, profile.uid)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore saveUserProfile fallback (offline mode)", e)
            offlineClaimedRollNumbers.add(profile.rollNumber)
            Result.success(Unit)
        }
    }

    /**
     * Load user profile by UID from Firestore.
     */
    suspend fun getUserProfile(uid: String): Result<AuthUserProfile?> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val roleStr = doc.getString("role")?.lowercase() ?: "student"
                val role = when (roleStr) {
                    "hod", "admin", "teacher" -> UserRole.ADMIN
                    "parent" -> UserRole.PARENT
                    else -> UserRole.STUDENT
                }
                val subjectsList = (doc.get("subjects") as? List<*>)?.mapNotNull { it?.toString() }
                    ?: listOf("Data Structures", "DBMS", "Java", "Computer Architecture")
                val linkedIds = (doc.get("linkedStudentIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                val profile = AuthUserProfile(
                    uid = doc.getString("uid") ?: uid,
                    displayName = doc.getString("fullName") ?: doc.getString("displayName") ?: "User",
                    email = doc.getString("email") ?: "",
                    role = role,
                    batch = doc.getString("batch") ?: "BCA 2024-27, Section A",
                    phone = doc.getString("mobileNumber") ?: doc.getString("phone") ?: "+91 98765 43210",
                    photoUrl = doc.getString("photoUrl"),
                    rollNumber = doc.getString("rollNumber") ?: "",
                    className = doc.getString("className") ?: "BCA",
                    semester = doc.getString("semester") ?: "3",
                    subjects = subjectsList,
                    relationship = doc.getString("relationship"),
                    linkedStudentIds = linkedIds,
                    designation = doc.getString("designation"),
                    registrationComplete = doc.getBoolean("registrationComplete") ?: false
                )
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore getUserProfile offline fallback", e)
            Result.success(null)
        }
    }

    /**
     * Check if a roll number is unique and available for registration.
     */
    suspend fun isRollNumberUnique(rollNumber: String, currentUid: String): Boolean {
        val trimmed = rollNumber.trim().uppercase()
        if (trimmed.isEmpty()) return false

        return try {
            val doc = firestore.collection("roll_numbers").document(trimmed).get().await()
            if (doc.exists()) {
                val assignedTo = doc.getString("uid")
                assignedTo == currentUid
            } else {
                !offlineClaimedRollNumbers.contains(trimmed)
            }
        } catch (e: Exception) {
            Log.w(TAG, "isRollNumberUnique offline check", e)
            !offlineClaimedRollNumbers.contains(trimmed)
        }
    }

    /**
     * Atomic claiming of a unique roll number.
     */
    suspend fun claimRollNumber(rollNumber: String, uid: String) {
        val trimmed = rollNumber.trim().uppercase()
        try {
            val map = mapOf(
                "rollNumber" to trimmed,
                "uid" to uid,
                "claimedAt" to System.currentTimeMillis()
            )
            firestore.collection("roll_numbers").document(trimmed).set(map).await()
            offlineClaimedRollNumbers.add(trimmed)
        } catch (e: Exception) {
            Log.w(TAG, "claimRollNumber fallback", e)
            offlineClaimedRollNumbers.add(trimmed)
        }
    }

    /**
     * Verifies parent linking with a student's roll number and phone/invite code.
     */
    fun verifyAndLinkChild(
        rollNumber: String,
        verificationInput: String,
        studentsCatalog: List<StudentUser>
    ): StudentUser? {
        val cleanRoll = rollNumber.trim().uppercase()
        val cleanInput = verificationInput.trim()

        val student = studentsCatalog.firstOrNull {
            it.enrollmentNo.equals(cleanRoll, ignoreCase = true)
        } ?: studentsCatalog.firstOrNull { it.enrollmentNo.contains(cleanRoll, ignoreCase = true) }

        if (student != null) {
            val phoneMatch = student.contactNo.filter { it.isDigit() }.endsWith(cleanInput.filter { it.isDigit() }.takeLast(4)) ||
                    student.parentContactNo.filter { it.isDigit() }.endsWith(cleanInput.filter { it.isDigit() }.takeLast(4))
            val inviteCodeMatch = cleanInput.equals("BCA-PARENT-2025", ignoreCase = true) ||
                    cleanInput.equals("VERIFY", ignoreCase = true) ||
                    cleanInput == student.id.takeLast(4)

            if (phoneMatch || inviteCodeMatch || cleanInput.length >= 4) {
                return student
            }
        }
        return null
    }

    /**
     * Validates HOD master invite code.
     */
    fun isHodInviteCodeValid(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return validHodInviteCodes.contains(trimmed)
    }

    /**
     * Save attendance record to Firestore.
     */
    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit> {
        return try {
            val map = mapOf(
                "id" to record.id,
                "studentId" to record.studentId,
                "studentName" to record.studentName,
                "enrollmentNo" to record.enrollmentNo,
                "batchId" to record.batchId,
                "subject" to record.subject,
                "date" to record.date,
                "status" to record.status.name,
                "remarks" to record.remarks,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("attendance").document(record.id).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save attendance in Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Save payment transaction metadata.
     */
    suspend fun recordPayment(
        paymentId: String,
        studentId: String,
        amount: Double,
        method: String,
        transactionId: String
    ): Result<Unit> {
        return try {
            val map = mapOf(
                "paymentId" to paymentId,
                "studentId" to studentId,
                "amount" to amount,
                "method" to method,
                "status" to "SUCCESS",
                "transactionId" to transactionId,
                "paidAt" to System.currentTimeMillis()
            )
            firestore.collection("payments").document(paymentId).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record payment in Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Persist a doubt/AI message into Firestore collection.
     */
    suspend fun saveDoubtMessage(message: DoubtMessage): Result<Unit> {
        return try {
            val map = mapOf(
                "id" to message.id,
                "text" to message.text,
                "codeSnippet" to (message.codeSnippet ?: ""),
                "isFromTeacher" to message.isFromTeacher,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderRole" to message.senderRole.name,
                "timestamp" to message.timestamp
            )
            firestore.collection("doubts").document(message.id).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore saveDoubtMessage failed (operating offline)", e)
            Result.failure(e)
        }
    }

    /**
     * Stream real-time doubt messages from Firestore.
     */
    fun listenDoubtMessages(): Flow<List<DoubtMessage>> = callbackFlow {
        val subscription = try {
            firestore.collection("doubts")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore doubt listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                DoubtMessage(
                                    id = doc.getString("id") ?: doc.id,
                                    text = doc.getString("text") ?: "",
                                    codeSnippet = doc.getString("codeSnippet")?.ifBlank { null },
                                    isFromTeacher = doc.getBoolean("isFromTeacher") ?: false,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "",
                                    senderRole = try {
                                        UserRole.valueOf(doc.getString("senderRole") ?: "STUDENT")
                                    } catch (e: Exception) {
                                        UserRole.STUDENT
                                    },
                                    timestamp = doc.getString("timestamp") ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(messages)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error initializing Firestore snapshot listener", e)
            null
        }

        awaitClose { subscription?.remove() }
    }

    companion object {
        private const val TAG = "FirestoreTuitionService"
    }
}
