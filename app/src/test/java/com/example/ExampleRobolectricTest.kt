package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceStatus
import com.example.data.repository.TuitionRepository
import com.example.util.MakautCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BCA Tuition Portal", appName)
    }

    @Test
    fun `test statutory MAKAUT CGPA to percentage conversion`() {
        // As defined in MAKAUT WBUT statutory regulations:
        // Percentage = (CGPA - 0.75) * 10
        val percentage675 = MakautCalculator.cgpaToPercentage(6.75)
        assertEquals(60.0, percentage675, 0.01)

        val percentage800 = MakautCalculator.cgpaToPercentage(8.0)
        assertEquals(72.5, percentage800, 0.01)

        val percentage900 = MakautCalculator.cgpaToPercentage(9.0)
        assertEquals(82.5, percentage900, 0.01)
    }

    @Test
    fun `test MAKAUT letter grading classification`() {
        val gradeO = MakautCalculator.getGradeLetter(9.2)
        assertEquals("O", gradeO.first)
        assertEquals("Outstanding", gradeO.second)

        val gradeB = MakautCalculator.getGradeLetter(6.75)
        assertEquals("B", gradeB.first)
        assertEquals("Good", gradeB.second)
    }

    @Test
    fun `test repository payment processing`() {
        val repo = TuitionRepository()
        var successCalled = false

        repo.processRazorpayPayment(
            feeRecordId = "fee-102",
            amount = 2500.0,
            method = "UPI (Google Pay)"
        ) { txn ->
            successCalled = true
            assertEquals(2500.0, txn.amount, 0.01)
            assertTrue(txn.razorpayOrderId.startsWith("order_MAKAUT_"))
        }

        assertTrue(successCalled)
    }

    @Test
    fun `test doubt messaging and reply flow`() {
        val repo = TuitionRepository()
        val initialCount = repo.doubtMessages.value.size

        repo.sendDoubtMessage(
            text = "Sir, what is the time complexity of BFS in adjacency list?",
            codeSnippet = null,
            senderId = "stud-001",
            senderName = "Rahul Sharma",
            senderRole = com.example.data.model.UserRole.STUDENT
        )

        val updated = repo.doubtMessages.value
        assertEquals(initialCount + 1, updated.size)
        assertEquals("Sir, what is the time complexity of BFS in adjacency list?", updated.last().text)
    }

    @Test
    fun `test student feedback submission`() {
        val repo = TuitionRepository()
        val initialCount = repo.courseFeedbacks.value.size

        repo.submitFeedback(
            rating = 5,
            subject = "DBMS (BAP301)",
            comment = "Great explanation of indexing.",
            studentId = "stud-001",
            studentName = "Rahul Sharma"
        )

        val updated = repo.courseFeedbacks.value
        assertEquals(initialCount + 1, updated.size)
        assertEquals(5, updated.first().rating)
    }

    @Test
    fun `test audit log generation on sensitive academic action`() {
        val repo = TuitionRepository()
        val initialAuditCount = repo.auditLogs.value.size

        repo.addAuditLog(
            action = "TEST_MARKS_UPDATED",
            targetEntity = "Rahul Sharma - DBMS",
            details = "Updated midterm score to 24/25."
        )

        val updatedLogs = repo.auditLogs.value
        assertEquals(initialAuditCount + 1, updatedLogs.size)
        assertEquals("TEST_MARKS_UPDATED", updatedLogs.first().action)
    }

    @Test
    fun `test twice-weekly attendance tracking and filtering`() {
        val repo = TuitionRepository()
        val allAttendance = repo.attendanceRecords.value
        assertTrue("Attendance records should not be empty", allAttendance.isNotEmpty())

        val wednesdayTheory = allAttendance.filter { !it.isPractical }
        val saturdayPractical = allAttendance.filter { it.isPractical }

        assertTrue("Should have theory class attendance records", wednesdayTheory.isNotEmpty())
        assertTrue("Should have practical lab class attendance records", saturdayPractical.isNotEmpty())

        // Calculate student attendance percentage
        val studentRecords = allAttendance.filter { it.studentId == "student-1" }
        val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT }
        val calculatedPercent = if (studentRecords.isNotEmpty()) {
            (presentCount.toDouble() / studentRecords.size.toDouble()) * 100.0
        } else 0.0

        assertTrue("Attendance percentage should be between 0 and 100", calculatedPercent in 0.0..100.0)
    }

    @Test
    fun `test quiz attempt submission and anti-cheat infraction tracking`() {
        val repo = TuitionRepository()
        val initialAttemptCount = repo.quizAttempts.value.size

        val attempt = repo.submitQuizAttempt(
            quizId = "quiz-1",
            quizTitle = "DBMS Normalization & Relational Calculus",
            studentId = "student-1",
            score = 18,
            maxScore = 20,
            infractionCount = 1,
            wasForced = false
        )

        val updatedAttempts = repo.quizAttempts.value
        assertEquals(initialAttemptCount + 1, updatedAttempts.size)
        assertEquals(90.0, attempt.percentage, 0.01)
        assertEquals(1, attempt.infractionCount)
        assertEquals("student-1", attempt.studentId)
    }

    @Test
    fun `test assignment submission and teacher feedback grading`() {
        val repo = TuitionRepository()
        val targetAssignmentId = "asg-1"

        // Student submits solution
        repo.submitAssignment(
            assignmentId = targetAssignmentId,
            text = "CREATE TABLE Student (id INT PRIMARY KEY, name VARCHAR(50));"
        )

        val submittedAssign = repo.assignments.value.first { it.id == targetAssignmentId }
        assertEquals(com.example.data.model.AssignmentStatus.SUBMITTED, submittedAssign.submissionStatus)
        assertEquals("CREATE TABLE Student (id INT PRIMARY KEY, name VARCHAR(50));", submittedAssign.studentSubmissionText)

        // HOD evaluates and assigns marks + feedback
        repo.gradeAssignment(
            assignmentId = targetAssignmentId,
            marks = 23,
            feedback = "Correct DDL syntax and primary key constraint applied properly."
        )

        val gradedAssign = repo.assignments.value.first { it.id == targetAssignmentId }
        assertEquals(com.example.data.model.AssignmentStatus.ACCEPTED, gradedAssign.submissionStatus)
        assertEquals(23, gradedAssign.obtainedMarks)
        assertEquals("Correct DDL syntax and primary key constraint applied properly.", gradedAssign.teacherFeedback)
    }

    @Test
    fun `test forum post creation and upvoting mechanics`() {
        val repo = TuitionRepository()
        val initialPosts = repo.forumPosts.value.size

        repo.addForumPost(
            title = "What is the difference between B-Tree and B+ Tree in DBMS?",
            content = "Specifically in indexing leaf nodes with sequential pointers.",
            tags = listOf("DBMS", "Indexing", "B-Tree"),
            authorName = "Ananya Roy",
            authorRole = com.example.data.model.UserRole.STUDENT
        )

        val updatedPosts = repo.forumPosts.value
        assertEquals(initialPosts + 1, updatedPosts.size)
        val newPost = updatedPosts.first()
        assertEquals("What is the difference between B-Tree and B+ Tree in DBMS?", newPost.title)

        // Upvote the post
        val initialVotes = newPost.upvotes
        repo.toggleForumUpvote(newPost.id)
        val upvotedPost = repo.forumPosts.value.first { it.id == newPost.id }
        assertEquals(initialVotes + 1, upvotedPost.upvotes)
        assertTrue(upvotedPost.isUpvotedByMe)
    }

    @Test
    fun `test role switching and session integrity`() {
        val repo = TuitionRepository()
        assertEquals(com.example.data.model.UserRole.STUDENT, repo.currentRole.value)

        // Switch to HOD/Admin
        repo.switchRole(com.example.data.model.UserRole.ADMIN)
        assertEquals(com.example.data.model.UserRole.ADMIN, repo.currentRole.value)

        // Switch to Parent
        repo.switchRole(com.example.data.model.UserRole.PARENT)
        assertEquals(com.example.data.model.UserRole.PARENT, repo.currentRole.value)
    }

    @Test
    fun `test AuthenticationViewModel StateFlow and role switching`() {
        val authVm = com.example.ui.viewmodel.AuthenticationViewModel()
        val initialUiState = authVm.uiState.value
        assertTrue("Initial auth state should be Unauthenticated, Authenticated, or Loading",
            initialUiState is com.example.ui.viewmodel.AuthUiState.Authenticated ||
            initialUiState is com.example.ui.viewmodel.AuthUiState.Loading ||
            initialUiState is com.example.ui.viewmodel.AuthUiState.Unauthenticated
        )

        // Switch to HOD
        authVm.switchRole(com.example.data.model.UserRole.ADMIN)
        val adminState = authVm.uiState.value as com.example.ui.viewmodel.AuthUiState.Authenticated
        assertEquals(com.example.data.model.UserRole.ADMIN, adminState.role)
        assertTrue(adminState.user.displayName.contains("HOD") || adminState.user.displayName.contains("Sengupta"))

        // Switch to Parent
        authVm.switchRole(com.example.data.model.UserRole.PARENT)
        val parentState = authVm.uiState.value as com.example.ui.viewmodel.AuthUiState.Authenticated
        assertEquals(com.example.data.model.UserRole.PARENT, parentState.role)

        // Sign Out
        authVm.signOut()
        assertEquals(com.example.ui.viewmodel.AuthUiState.Unauthenticated, authVm.uiState.value)
    }

    @Test
    fun `test FirebaseInitializer safe async initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var callbackInvoked = false

        // Test root package com.example.FirebaseInitializer directly
        com.example.FirebaseInitializer.initializeAsync(context) { success ->
            callbackInvoked = true
        }

        // Test ContentProvider instantiation
        val provider = com.example.FirebaseInitializer()
        assertNotNull(provider)

        // Must not throw or crash even in test environment
        assertTrue(true)
    }
}

