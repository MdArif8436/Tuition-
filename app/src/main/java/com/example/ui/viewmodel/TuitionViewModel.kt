package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TuitionRepository
import com.example.util.MakautCalculator
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveQuizState(
    val quiz: Quiz? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> optionIndex
    val secondsRemaining: Int = 300,
    val infractionCount: Int = 0,
    val maxInfractionsAllowed: Int = 3,
    val showAntiCheatWarning: Boolean = false,
    val isFinished: Boolean = false,
    val latestAttemptResult: QuizAttempt? = null
)

data class PaymentCheckoutState(
    val isOpen: Boolean = false,
    val feeRecord: FeeRecord? = null,
    val amountToPay: Double = 0.0,
    val paymentMethod: String = "UPI (Google Pay)",
    val isProcessing: Boolean = false,
    val lastCompletedTransaction: FeeTransaction? = null
)

class TuitionViewModel(
    private val repository: TuitionRepository = TuitionRepository()
) : ViewModel() {

    val currentRole = repository.currentRole
    val currentUser = repository.currentUser
    val batches = repository.batches
    val students = repository.students
    val attendanceRecords = repository.attendanceRecords
    val feeRecords = repository.feeRecords
    val studyTopics = repository.studyTopics
    val quizzes = repository.quizzes
    val quizAttempts = repository.quizAttempts
    val testMarks = repository.testMarks
    val schedules = repository.schedules
    val notifications = repository.notifications
    val dailyUpdates = repository.dailyUpdates
    val assignments = repository.assignments
    val forumPosts = repository.forumPosts
    val forumComments = repository.forumComments
    val doubtMessages = repository.doubtMessages
    val courseFeedbacks = repository.courseFeedbacks
    val studyResources = repository.studyResources
    val auditLogs = repository.auditLogs
    val notificationPreferences = repository.notificationPreferences
    val appLanguage = repository.appLanguage

    // Active Quiz Session
    private val _quizState = MutableStateFlow(ActiveQuizState())
    val quizState: StateFlow<ActiveQuizState> = _quizState.asStateFlow()
    private var quizTimerJob: Job? = null

    // Payment Checkout State
    private val _paymentState = MutableStateFlow(PaymentCheckoutState())
    val paymentState: StateFlow<PaymentCheckoutState> = _paymentState.asStateFlow()

    // Forum search query
    private val _forumSearchQuery = MutableStateFlow("")
    val forumSearchQuery: StateFlow<String> = _forumSearchQuery.asStateFlow()

    private val _selectedForumTag = MutableStateFlow<String?>(null)
    val selectedForumTag: StateFlow<String?> = _selectedForumTag.asStateFlow()

    // Filtered forum posts
    val filteredForumPosts: StateFlow<List<ForumPost>> = combine(
        forumPosts,
        _forumSearchQuery,
        _selectedForumTag
    ) { posts, query, tag ->
        posts.filter { post ->
            val matchesQuery = query.isBlank() ||
                post.title.contains(query, ignoreCase = true) ||
                post.content.contains(query, ignoreCase = true) ||
                post.tags.any { it.contains(query, ignoreCase = true) }
            val matchesTag = tag == null || post.tags.contains(tag)
            matchesQuery && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Student Attendance Statistics
    val currentStudentAttendancePercentage: StateFlow<Double> = combine(
        currentUser,
        attendanceRecords
    ) { student, records ->
        val studentRecords = records.filter { it.studentId == student.id }
        if (studentRecords.isEmpty()) 85.0 // default representative
        else {
            val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
            (presentCount.toDouble() / studentRecords.size.toDouble()) * 100.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85.0)

    // Defaulters (< 75% attendance) for HOD command center
    val lowAttendanceStudents: StateFlow<List<Pair<StudentUser, Double>>> = combine(
        students,
        attendanceRecords
    ) { studentList, records ->
        studentList.mapNotNull { student ->
            val recs = records.filter { it.studentId == student.id }
            val pct = if (recs.isEmpty()) 70.0 else {
                val present = recs.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
                (present.toDouble() / recs.size.toDouble()) * 100.0
            }
            if (pct < 75.0) student to pct else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending fee dues for current student
    val currentStudentPendingFees: StateFlow<List<FeeRecord>> = combine(
        currentUser,
        feeRecords
    ) { student, records ->
        records.filter { it.studentId == student.id && (it.status == FeeStatus.PENDING || it.status == FeeStatus.PARTIAL || it.status == FeeStatus.OVERDUE) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unread notifications count
    val unreadNotificationsCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Role switcher
    fun setRole(role: UserRole) {
        repository.switchRole(role)
    }

    fun updateActiveUserProfile(user: com.example.ui.viewmodel.AuthUserProfile) {
        repository.updateActiveUserProfile(
            uid = user.uid,
            name = user.displayName,
            email = user.email,
            rollNumber = user.rollNumber,
            batch = user.batch,
            phone = user.phone,
            role = user.role
        )
    }

    fun selectStudent(student: StudentUser) {
        repository.selectStudent(student)
    }

    // Payment Actions
    fun openPaymentCheckout(feeRecord: FeeRecord) {
        _paymentState.value = PaymentCheckoutState(
            isOpen = true,
            feeRecord = feeRecord,
            amountToPay = feeRecord.pendingBalance,
            paymentMethod = "UPI (Google Pay)",
            isProcessing = false
        )
    }

    fun dismissPaymentCheckout() {
        _paymentState.value = _paymentState.value.copy(isOpen = false)
    }

    fun confirmPayment(method: String) {
        val current = _paymentState.value
        val feeRecord = current.feeRecord ?: return

        _paymentState.value = current.copy(isProcessing = true, paymentMethod = method)

        viewModelScope.launch {
            // Simulate cryptographic verification and Razorpay webhook delay
            delay(1200)
            repository.processRazorpayPayment(
                feeRecordId = feeRecord.id,
                amount = current.amountToPay,
                method = method
            ) { transaction ->
                _paymentState.value = _paymentState.value.copy(
                    isProcessing = false,
                    lastCompletedTransaction = transaction
                )
            }
        }
    }

    // Attendance Bulk Marking
    fun markBatchAttendance(subject: String, date: String, statusMap: Map<String, AttendanceStatus>) {
        val records = statusMap.mapNotNull { (studentId, status) ->
            val student = students.value.find { it.id == studentId } ?: return@mapNotNull null
            AttendanceRecord(
                studentId = student.id,
                studentName = student.name,
                enrollmentNo = student.enrollmentNo,
                batchId = student.batchId,
                subject = subject,
                date = date,
                status = status
            )
        }
        repository.markBatchAttendance(records)
    }

    // Quiz Session Management
    fun startQuiz(quiz: Quiz) {
        quizTimerJob?.cancel()
        _quizState.value = ActiveQuizState(
            quiz = quiz,
            currentQuestionIndex = 0,
            selectedAnswers = emptyMap(),
            secondsRemaining = quiz.timeLimitMinutes * 60,
            infractionCount = 0,
            showAntiCheatWarning = false,
            isFinished = false,
            latestAttemptResult = null
        )

        // Start countdown timer
        quizTimerJob = viewModelScope.launch {
            while (_quizState.value.secondsRemaining > 0 && !_quizState.value.isFinished) {
                delay(1000)
                _quizState.value = _quizState.value.copy(
                    secondsRemaining = _quizState.value.secondsRemaining - 1
                )
            }
            if (!_quizState.value.isFinished) {
                finishQuiz(wasForced = true)
            }
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val answers = _quizState.value.selectedAnswers.toMutableMap()
        answers[questionIndex] = optionIndex
        _quizState.value = _quizState.value.copy(selectedAnswers = answers)
    }

    fun nextQuizQuestion() {
        val state = _quizState.value
        val quiz = state.quiz ?: return
        if (state.currentQuestionIndex < quiz.questions.size - 1) {
            _quizState.value = state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
        }
    }

    fun prevQuizQuestion() {
        val state = _quizState.value
        if (state.currentQuestionIndex > 0) {
            _quizState.value = state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
        }
    }

    /**
     * Anti-cheat heuristic: Triggered when app loses focus, user switches away,
     * or screen moves to background during active assessment.
     */
    fun recordAntiCheatInfraction() {
        val state = _quizState.value
        if (state.quiz == null || state.isFinished) return

        val newCount = state.infractionCount + 1
        if (newCount >= state.maxInfractionsAllowed) {
            // Forcibly lock out and auto-submit
            finishQuiz(wasForced = true, finalInfractionCount = newCount)
        } else {
            // Display high-priority warning
            _quizState.value = state.copy(
                infractionCount = newCount,
                showAntiCheatWarning = true
            )
        }
    }

    fun dismissAntiCheatWarning() {
        _quizState.value = _quizState.value.copy(showAntiCheatWarning = false)
    }

    fun finishQuiz(wasForced: Boolean = false, finalInfractionCount: Int? = null) {
        quizTimerJob?.cancel()
        val state = _quizState.value
        val quiz = state.quiz ?: return

        var score = 0
        quiz.questions.forEachIndexed { idx, q ->
            val chosen = state.selectedAnswers[idx]
            if (chosen != null && chosen == q.correctOptionIndex) {
                score += (quiz.maxMarks / quiz.questions.size).coerceAtLeast(1)
            }
        }

        val infractions = finalInfractionCount ?: state.infractionCount
        val attempt = repository.submitQuizAttempt(
            quizId = quiz.id,
            quizTitle = quiz.title,
            studentId = currentUser.value.id,
            score = score,
            maxScore = quiz.maxMarks,
            infractionCount = infractions,
            wasForced = wasForced
        )

        _quizState.value = state.copy(
            isFinished = true,
            showAntiCheatWarning = false,
            infractionCount = infractions,
            latestAttemptResult = attempt
        )
    }

    fun exitQuizView() {
        quizTimerJob?.cancel()
        _quizState.value = ActiveQuizState()
    }

    // Assignments
    fun submitAssignment(assignmentId: String, text: String) {
        repository.submitAssignment(assignmentId, text)
    }

    fun gradeAssignment(assignmentId: String, marks: Int, feedback: String) {
        repository.gradeAssignment(assignmentId, marks, feedback)
    }

    // Study topics
    fun updateTopicStatus(topicId: String, status: TopicStatus) {
        repository.updateTopicStatus(topicId, status)
    }

    fun addStudyTopic(topic: StudyTopic) {
        repository.addStudyTopic(topic)
    }

    // Daily updates
    fun addDailyUpdate(update: DailyUpdate) {
        repository.addDailyUpdate(update)
    }

    // Forum
    fun setForumSearch(query: String) {
        _forumSearchQuery.value = query
    }

    fun selectForumTag(tag: String?) {
        _selectedForumTag.value = tag
    }

    fun toggleForumUpvote(postId: String) {
        repository.toggleForumUpvote(postId)
    }

    fun addForumPost(title: String, content: String, tags: List<String>) {
        val user = currentUser.value
        val role = currentRole.value
        val authorName = if (role == UserRole.ADMIN) "Prof. S. Sengupta (HOD)" else user.name
        repository.addForumPost(title, content, tags, authorName, role)
    }

    fun addForumComment(postId: String, content: String) {
        val user = currentUser.value
        val role = currentRole.value
        val authorName = if (role == UserRole.ADMIN) "Prof. S. Sengupta (HOD)" else user.name
        repository.addForumComment(postId, content, authorName, role)
    }

    // Notifications
    fun markNotificationRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun triggerAutomatedClassReminder() {
        repository.addNotification(
            title = "Automated: Upcoming Class Reminder",
            message = "Reminder: Python & SQL Practical Lab begins in 30 minutes in Lab 2.",
            type = NotificationType.UPCOMING_CLASS,
            actionText = "View Lab Room"
        )
    }

    fun triggerAutomatedFeeDueNotice() {
        val student = currentUser.value
        repository.addNotification(
            title = "Automated: Fee Due Alert",
            message = "Attention ${student.name}: Outstanding balance of ₹2,500 due on 25th Oct. Please settle promptly.",
            type = NotificationType.OUTSTANDING_BALANCE,
            actionText = "Pay Now"
        )
    }

    // Doubt Chat Actions
    fun sendDoubtMessage(
        text: String,
        codeSnippet: String? = null,
        isFromTeacher: Boolean? = null,
        senderName: String? = null
    ) {
        val user = currentUser.value
        val role = currentRole.value
        val resolvedRole = if (isFromTeacher == true) UserRole.ADMIN else role
        val resolvedName = senderName ?: if (resolvedRole == UserRole.ADMIN) "Prof. S. Sengupta (HOD)" else user.name
        val senderId = if (resolvedRole == UserRole.ADMIN) "prof-sengupta-hod" else user.id
        repository.sendDoubtMessage(text, codeSnippet, senderId, resolvedName, resolvedRole)
    }

    // Student Course Feedback
    fun submitFeedback(rating: Int, subject: String, comment: String) {
        val user = currentUser.value
        repository.submitFeedback(rating, subject, comment, user.id, user.name)
    }

    // Preferences & Settings
    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        repository.updateNotificationPreferences(prefs)
    }

    fun setAppLanguage(lang: AppLanguage) {
        repository.setAppLanguage(lang)
    }

    fun toggleResourceDownload(resourceId: String) {
        repository.toggleResourceDownload(resourceId)
    }

    // Bulk marks entry / import
    fun addBulkTestMarks(marks: List<TestMark>) {
        repository.bulkImportMarks(marks)
    }

    fun addSingleTestMark(mark: TestMark) {
        repository.addTestMark(mark)
    }

    // Attendance Save Helper
    fun recordBulkAttendance(records: List<AttendanceRecord>) {
        repository.markBatchAttendance(records)
    }

    // Export Helpers (CSV text generation)
    fun exportAttendanceCsv(): String {
        val records = attendanceRecords.value
        val sb = StringBuilder("Date,Student Name,Enrollment No,Subject,Status,Remarks\n")
        records.forEach { r ->
            sb.append("${r.date},\"${r.studentName}\",${r.enrollmentNo},\"${r.subject}\",${r.status.name},\"${r.remarks}\"\n")
        }
        return sb.toString()
    }

    fun exportFeeCsv(): String {
        val records = feeRecords.value
        val sb = StringBuilder("Student Name,Enrollment No,Fee Month,Total (INR),Paid (INR),Pending (INR),Due Date,Status\n")
        records.forEach { r ->
            sb.append("\"${r.studentName}\",${r.enrollmentNo},\"${r.feeMonth}\",${r.totalAmount},${r.paidAmount},${r.pendingBalance},${r.dueDate},${r.status.name}\n")
        }
        return sb.toString()
    }

    fun exportMarksCsv(): String {
        val marks = testMarks.value
        val sb = StringBuilder("Date,Student Name,Enrollment No,Subject,Assessment Name,Marks Obtained,Max Marks,Percentage\n")
        marks.forEach { m ->
            sb.append("${m.date},\"${m.studentName}\",${m.enrollmentNo},\"${m.subject}\",\"${m.testName}\",${m.obtainedMarks},${m.maxMarks},${String.format(Locale.US, "%.1f", m.percentage)}%\n")
        }
        return sb.toString()
    }
}
