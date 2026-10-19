package com.example.data.model

import java.util.UUID

enum class UserRole {
    SUPER_ADMIN, // Platform Owner
    ADMIN,       // College HOD / College Admin
    TEACHER,     // Subject Teacher / Faculty
    STUDENT,     // Student
    PARENT       // Parent / Guardian
}

enum class TeacherApprovalStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CHANGES_REQUESTED
}

data class College(
    val id: String,
    val code: String,
    val name: String,
    val shortName: String,
    val location: String,
    val officialEmail: String,
    val courses: List<String> = listOf("BCA", "MCA", "B.Tech CSE", "B.Sc IT"),
    val batches: List<String> = listOf("Batch 2024–27, Section A", "Batch 2025–28, Section B"),
    val isVerified: Boolean = true
)

object CollegeCatalog {
    val sampleColleges = listOf(
        College(
            id = "col-iem-kolkata",
            code = "IEM-KOL",
            name = "Institute of Engineering & Management (IEM)",
            shortName = "IEM Kolkata",
            location = "Sector V, Salt Lake, Kolkata",
            officialEmail = "hod.bca@iemcal.com"
        ),
        College(
            id = "col-heritage-tech",
            code = "HIT-KOL",
            name = "Heritage Institute of Technology",
            shortName = "Heritage Kolkata",
            location = "Anandapur, Kolkata",
            officialEmail = "hod.ca@heritageit.edu"
        ),
        College(
            id = "col-makaut-main",
            code = "MAKAUT-WB",
            name = "MAKAUT State University Campus",
            shortName = "MAKAUT Main",
            location = "Haringhata / Salt Lake, West Bengal",
            officialEmail = "dept.bca@makaut-wb.edu"
        ),
        College(
            id = "col-sxc-calcutta",
            code = "SXC-CAL",
            name = "St. Xavier's College (Autonomous)",
            shortName = "St. Xavier's",
            location = "Park Street, Kolkata",
            officialEmail = "ca.dept@sxccal.edu"
        )
    )

    fun findByCode(code: String): College? {
        val clean = code.trim().uppercase()
        return sampleColleges.firstOrNull { it.code.equals(clean, ignoreCase = true) }
    }

    fun findById(id: String): College? {
        return sampleColleges.firstOrNull { it.id == id } ?: sampleColleges.first()
    }
}

data class TeacherProfile(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val email: String,
    val mobile: String,
    val designation: String = "Assistant Professor",
    val collegeId: String = "col-iem-kolkata",
    val collegeName: String = "Institute of Engineering & Management (IEM)",
    val assignedSubjects: List<String> = listOf("DBMS", "Data Structures"),
    val assignedBatches: List<String> = listOf("Batch 2024–27, Section A"),
    val qualifications: String = "M.Tech in CSE (Jadavpur Univ), MCA",
    val experienceYears: Int = 6,
    val bio: String = "Passionate educator specializing in database normalization and distributed systems.",
    val cvFileName: String = "prof_resume_2026.pdf",
    val cvFileUrl: String? = null,
    val status: TeacherApprovalStatus = TeacherApprovalStatus.APPROVED,
    val rejectionReason: String? = null,
    val appliedDate: String = "2026-10-10"
)

data class StudentUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val enrollmentNo: String,
    val batchId: String,
    val batchName: String,
    val contactNo: String,
    val parentContactNo: String,
    val currentSemester: Int = 3,
    val cgpa: Double = 8.12,
    val collegeId: String = "col-iem-kolkata",
    val collegeName: String = "Institute of Engineering & Management (IEM)",
    val courseName: String = "BCA",
    val referralCode: String = "REF-${UUID.randomUUID().toString().take(6).uppercase()}",
    val photoUrl: String? = null
)

data class BatchInfo(
    val id: String,
    val name: String,
    val academicYear: String,
    val semester: Int,
    val collegeId: String = "col-iem-kolkata",
    val isActive: Boolean = true
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
}

data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val enrollmentNo: String,
    val batchId: String,
    val collegeId: String = "col-iem-kolkata",
    val subject: String,
    val date: String, // e.g. "2026-10-18"
    val status: AttendanceStatus,
    val isPractical: Boolean = false,
    val remarks: String = ""
)

enum class FeeStatus {
    PAID,
    PENDING,
    PARTIAL,
    OVERDUE
}

data class FeeTransaction(
    val id: String = UUID.randomUUID().toString(),
    val feeRecordId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val amount: Double,
    val status: String = "SUCCESS",
    val paymentMethod: String = "UPI (Google Pay)",
    val timestamp: String = "2026-10-18 10:30"
)

data class FeeRecord(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val enrollmentNo: String,
    val batchName: String,
    val collegeId: String = "col-iem-kolkata",
    val feeMonth: String, // e.g. "October 2026"
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String, // e.g. "2026-10-25"
    val status: FeeStatus,
    val transactions: List<FeeTransaction> = emptyList()
) {
    val pendingBalance: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}

enum class TopicStatus {
    UPCOMING,
    ONGOING,
    COMPLETED
}

data class StudyTopic(
    val id: String = UUID.randomUUID().toString(),
    val batchId: String,
    val collegeId: String = "col-iem-kolkata",
    val subject: String,
    val title: String,
    val description: String,
    val status: TopicStatus,
    val isPractical: Boolean = false,
    val startDate: String,
    val completionDate: String? = null,
    val expectedCompletionDate: String = "2026-10-25",
    val makautUnit: String = "Unit 1 - Fundamentals",
    val referenceNotesUrl: String = "",
    val lectureCount: Int = 1,
    val notesUrl: String? = referenceNotesUrl
)

data class QuizQuestion(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val questionText: String = text,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class Quiz(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val batchId: String,
    val collegeId: String = "col-iem-kolkata",
    val durationMinutes: Int = 15,
    val timeLimitMinutes: Int = durationMinutes,
    val totalMarks: Int = 20,
    val maxMarks: Int = totalMarks,
    val questions: List<QuizQuestion>,
    val deadlineDate: String = "2026-10-25",
    val isStrictAntiCheat: Boolean = true
)

data class QuizAttempt(
    val id: String = UUID.randomUUID().toString(),
    val quizId: String,
    val quizTitle: String,
    val studentId: String,
    val score: Int,
    val maxScore: Int,
    val completedAt: String,
    val infractionCount: Int = 0,
    val wasForced: Boolean = false,
    val wasForcedSubmitted: Boolean = wasForced,
    val percentage: Double = if (maxScore > 0) (score.toDouble() / maxScore) * 100.0 else 0.0
)

data class TestMark(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val enrollmentNo: String,
    val collegeId: String = "col-iem-kolkata",
    val subject: String,
    val testName: String, // e.g. "Continuous Assessment 1 (CA1)"
    val obtainedMarks: Double,
    val maxMarks: Double = 25.0,
    val date: String,
    val semester: Int = 3
) {
    val percentage: Double
        get() = if (maxMarks > 0) (obtainedMarks / maxMarks) * 100.0 else 0.0
}

enum class ScheduleType {
    LECTURE,
    LAB,
    TEST,
    SEMINAR,
    HOLIDAY
}

data class ScheduleEvent(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: String,
    val timeSlot: String,
    val subject: String,
    val roomOrLab: String,
    val type: ScheduleType = ScheduleType.LECTURE,
    val batch: String = "BCA 2nd Year",
    val instructor: String = "Prof. S. Sengupta"
)

data class ClassSchedule(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val topic: String,
    val teacherName: String,
    val dayOfWeek: String, // "Monday", "Wednesday", etc.
    val startTime: String, // "10:30 AM"
    val endTime: String,   // "12:00 PM"
    val isLabSession: Boolean = false,
    val roomOrLink: String = "Lab 402 / Google Meet"
)

enum class NotificationType {
    ATTENDANCE_ALERT,
    FEE_REMINDER,
    QUIZ_ANNOUNCEMENT,
    MARKS_UPDATE,
    DAILY_SUMMARY,
    EXAM_ROUTINE,
    UPCOMING_CLASS,
    OUTSTANDING_BALANCE,
    ASSIGNMENT_DEADLINE,
    ANNOUNCEMENT
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actionText: String? = null,
    val relatedEntityId: String? = null,
    val actionDeepLink: String? = null
)

data class TuitionNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actionDeepLink: String? = null
)

data class DailyUpdate(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val subject: String,
    val topicTitle: String,
    val summary: String = "",
    val teacherSummary: String = summary,
    val homeworkAssigned: String = "",
    val nextClassAgenda: String = "",
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val batch: String = "BCA 2nd Year",
    val collegeId: String = "col-iem-kolkata"
)

enum class AssignmentStatus {
    NOT_SUBMITTED,
    PENDING,
    SUBMITTED,
    UNDER_REVIEW,
    ACCEPTED,
    GRADED,
    NEEDS_REVISION
}

data class Assignment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val batchId: String,
    val collegeId: String = "col-iem-kolkata",
    val description: String,
    val assignedDate: String = "2026-10-15",
    val dueDate: String,
    val maxMarks: Int = 25,
    val submissionStatus: AssignmentStatus = AssignmentStatus.NOT_SUBMITTED,
    val studentSubmissionText: String? = null,
    val submittedDate: String? = null,
    val teacherFeedback: String? = null,
    val obtainedMarks: Int? = null
)

data class ForumPost(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String = "",
    val title: String,
    val content: String,
    val tags: List<String>,
    val authorName: String,
    val authorRole: UserRole,
    val collegeId: String = "col-iem-kolkata",
    val timestamp: String = "",
    val createdAt: String = timestamp,
    val upvotes: Int = 0,
    val commentCount: Int = 0,
    val commentsCount: Int = commentCount,
    val isUpvotedByMe: Boolean = false,
    val isAnswered: Boolean = false,
    val isSolved: Boolean = isAnswered
)

data class ForumComment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val authorName: String,
    val authorRole: UserRole,
    val content: String,
    val timestamp: String = "",
    val createdAt: String = timestamp,
    val isAcceptedAnswer: Boolean = false
)

data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val action: String,
    val actorName: String = "Prof. S. Sengupta (HOD)",
    val actorRole: UserRole = UserRole.ADMIN,
    val targetEntity: String = "",
    val details: String = "",
    val target: String = targetEntity,
    val timestamp: String
)

data class NotificationPreferences(
    val attendanceAlerts: Boolean = true,
    val feeReminders: Boolean = true,
    val quizNotifications: Boolean = true,
    val dailyDigest: Boolean = true,
    val scheduleEnabled: Boolean = true,
    val feesEnabled: Boolean = true,
    val assignmentsEnabled: Boolean = true,
    val quizzesEnabled: Boolean = true,
    val updatesEnabled: Boolean = true
)

enum class AppLanguage {
    ENGLISH,
    BENGALI,
    HINDI
}

data class SubjectProgress(
    val subject: String,
    val totalTopics: Int,
    val completedTopics: Int,
    val pendingAssignments: Int
) {
    val progressPercentage: Double
        get() = if (totalTopics > 0) (completedTopics.toDouble() / totalTopics) * 100.0 else 0.0
}

data class DoubtMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val text: String,
    val timestamp: String,
    val codeSnippet: String? = null,
    val isFromTeacher: Boolean = false,
    val isAiResponse: Boolean = false,
    val isGroundedInNotes: Boolean = true
)

data class CourseFeedback(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val rating: Int, // 1 to 5
    val subject: String,
    val comment: String,
    val timestamp: String,
    val isAnonymous: Boolean = false
)

data class CertificateInfo(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val enrollmentNo: String,
    val collegeName: String = "Institute of Engineering & Management (IEM)",
    val courseName: String = "Bachelor of Computer Application (BCA)",
    val issueDate: String,
    val cgpa: Double,
    val grade: String,
    val certificateType: String = "Academic Excellence & Semester Progression"
)

data class LeaderboardEntry(
    val rank: Int,
    val studentId: String,
    val studentName: String,
    val enrollmentNo: String,
    val collegeName: String,
    val attendancePercentage: Double,
    val assignmentCompletionPercentage: Double,
    val quizAveragePercentage: Double,
    val compositeScore: Double, // (Attendance * 30%) + (Assignment Completion * 30%) + (Quiz * 40%)
    val badge: String? = null
)

data class AnnouncementItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val postedBy: String,
    val collegeId: String,
    val targetRole: String = "ALL", // "ALL", "STUDENT", "TEACHER"
    val timestamp: String,
    val readCount: Int = 18,
    val reactionCount: Int = 12
)

enum class ResourceType {
    LECTURE_NOTE,
    PYQ,
    LAB_MANUAL,
    SYLLABUS_PDF,
    CODE_SAMPLE
}

data class StudyResource(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val type: ResourceType,
    val fileSizeMb: Double,
    val semester: Int,
    val isDownloaded: Boolean = false,
    val downloadUrl: String? = null
)
