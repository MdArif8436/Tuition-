package com.example.data.repository

import com.example.data.model.*
import com.example.util.MakautCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TuitionRepository {

    // Current active user & role
    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUser = MutableStateFlow(
        StudentUser(
            id = "student-1",
            name = "Rohan Mukherjee",
            email = "rohan.bca24@makaut.ac.in",
            enrollmentNo = "MAKAUT/BCA/24001",
            batchId = "batch-2",
            batchName = "BCA 2nd Year (2025-2028)",
            contactNo = "+91 98301 23456",
            parentContactNo = "+91 98300 98765",
            currentSemester = 3,
            cgpa = 8.12
        )
    )
    val currentUser: StateFlow<StudentUser> = _currentUser.asStateFlow()

    // Batches
    private val _batches = MutableStateFlow(
        listOf(
            BatchInfo("batch-1", "BCA 1st Year (2026-2029)", "2026-2027", 1),
            BatchInfo("batch-2", "BCA 2nd Year (2025-2028)", "2025-2026", 3),
            BatchInfo("batch-3", "BCA 3rd Year (2024-2027)", "2024-2025", 5)
        )
    )
    val batches: StateFlow<List<BatchInfo>> = _batches.asStateFlow()

    // Student Roster
    private val _students = MutableStateFlow(
        listOf(
            StudentUser("student-1", "Rohan Mukherjee", "rohan.bca24@makaut.ac.in", "MAKAUT/BCA/24001", "batch-2", "BCA 2nd Year", "+91 98301 23456", "+91 98300 98765", 3, 8.12),
            StudentUser("student-2", "Ananya Roy", "ananya.roy@makaut.ac.in", "MAKAUT/BCA/24002", "batch-2", "BCA 2nd Year", "+91 98312 34567", "+91 98311 87654", 3, 9.15),
            StudentUser("student-3", "Sourav Das", "sourav.das@makaut.ac.in", "MAKAUT/BCA/24003", "batch-2", "BCA 2nd Year", "+91 98323 45678", "+91 98322 76543", 3, 6.40),
            StudentUser("student-4", "Debolina Ghosh", "debolina.g@makaut.ac.in", "MAKAUT/BCA/24004", "batch-2", "BCA 2nd Year", "+91 98334 56789", "+91 98333 65432", 3, 7.80),
            StudentUser("student-5", "Pritam Banerjee", "pritam.b@makaut.ac.in", "MAKAUT/BCA/24005", "batch-2", "BCA 2nd Year", "+91 98345 67890", "+91 98344 54321", 3, 6.65)
        )
    )
    val students: StateFlow<List<StudentUser>> = _students.asStateFlow()

    // Attendance
    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(
        listOf(
            AttendanceRecord(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", batchId = "batch-2", subject = "DBMS (BAP301)", date = "2026-10-17", status = AttendanceStatus.PRESENT),
            AttendanceRecord(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", batchId = "batch-2", subject = "Data Structures (BAP302)", date = "2026-10-16", status = AttendanceStatus.PRESENT),
            AttendanceRecord(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", batchId = "batch-2", subject = "OOP with Java (BAP303)", date = "2026-10-15", status = AttendanceStatus.PRESENT),
            AttendanceRecord(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", batchId = "batch-2", subject = "Computer Networks (BAP401)", date = "2026-10-14", status = AttendanceStatus.ABSENT, remarks = "Medical appointment"),
            AttendanceRecord(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", batchId = "batch-2", subject = "Python Lab (BAP391)", date = "2026-10-13", status = AttendanceStatus.PRESENT, isPractical = true),
            // Sourav Das (Attendance below 75%)
            AttendanceRecord(studentId = "student-3", studentName = "Sourav Das", enrollmentNo = "MAKAUT/BCA/24003", batchId = "batch-2", subject = "DBMS (BAP301)", date = "2026-10-17", status = AttendanceStatus.ABSENT),
            AttendanceRecord(studentId = "student-3", studentName = "Sourav Das", enrollmentNo = "MAKAUT/BCA/24003", batchId = "batch-2", subject = "Data Structures (BAP302)", date = "2026-10-16", status = AttendanceStatus.ABSENT),
            AttendanceRecord(studentId = "student-3", studentName = "Sourav Das", enrollmentNo = "MAKAUT/BCA/24003", batchId = "batch-2", subject = "OOP with Java (BAP303)", date = "2026-10-15", status = AttendanceStatus.PRESENT),
            // Ananya Roy (High attendance)
            AttendanceRecord(studentId = "student-2", studentName = "Ananya Roy", enrollmentNo = "MAKAUT/BCA/24002", batchId = "batch-2", subject = "DBMS (BAP301)", date = "2026-10-17", status = AttendanceStatus.PRESENT),
            AttendanceRecord(studentId = "student-2", studentName = "Ananya Roy", enrollmentNo = "MAKAUT/BCA/24002", batchId = "batch-2", subject = "Data Structures (BAP302)", date = "2026-10-16", status = AttendanceStatus.PRESENT)
        )
    )
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    // Fees & Payments
    private val _feeRecords = MutableStateFlow<List<FeeRecord>>(
        listOf(
            FeeRecord(
                id = "fee-101",
                studentId = "student-1",
                studentName = "Rohan Mukherjee",
                enrollmentNo = "MAKAUT/BCA/24001",
                batchName = "BCA 2nd Year",
                feeMonth = "October 2026 Monthly Tuition",
                totalAmount = 3500.0,
                paidAmount = 3500.0,
                dueDate = "2026-10-10",
                status = FeeStatus.PAID,
                transactions = listOf(
                    FeeTransaction(
                        feeRecordId = "fee-101",
                        razorpayOrderId = "order_MAKAUT_8921",
                        razorpayPaymentId = "pay_BCA_20261010_99",
                        amount = 3500.0,
                        paymentMethod = "UPI (Google Pay)",
                        timestamp = "2026-10-09 14:22"
                    )
                )
            ),
            FeeRecord(
                id = "fee-102",
                studentId = "student-1",
                studentName = "Rohan Mukherjee",
                enrollmentNo = "MAKAUT/BCA/24001",
                batchName = "BCA 2nd Year",
                feeMonth = "Semester 3 Lab & Assessment Fee",
                totalAmount = 2500.0,
                paidAmount = 0.0,
                dueDate = "2026-10-25",
                status = FeeStatus.PENDING,
                transactions = emptyList()
            ),
            FeeRecord(
                id = "fee-103",
                studentId = "student-3",
                studentName = "Sourav Das",
                enrollmentNo = "MAKAUT/BCA/24003",
                batchName = "BCA 2nd Year",
                feeMonth = "October 2026 Monthly Tuition",
                totalAmount = 3500.0,
                paidAmount = 0.0,
                dueDate = "2026-10-05",
                status = FeeStatus.OVERDUE,
                transactions = emptyList()
            ),
            FeeRecord(
                id = "fee-104",
                studentId = "student-4",
                studentName = "Debolina Ghosh",
                enrollmentNo = "MAKAUT/BCA/24004",
                batchName = "BCA 2nd Year",
                feeMonth = "October 2026 Monthly Tuition",
                totalAmount = 3500.0,
                paidAmount = 2000.0,
                dueDate = "2026-10-20",
                status = FeeStatus.PARTIAL,
                transactions = listOf(
                    FeeTransaction(
                        feeRecordId = "fee-104",
                        razorpayOrderId = "order_MAKAUT_8924",
                        razorpayPaymentId = "pay_BCA_20261012_41",
                        amount = 2000.0,
                        paymentMethod = "Debit Card",
                        timestamp = "2026-10-12 11:15"
                    )
                )
            )
        )
    )
    val feeRecords: StateFlow<List<FeeRecord>> = _feeRecords.asStateFlow()

    // Study Topics (Syllabus Tracker)
    private val _studyTopics = MutableStateFlow<List<StudyTopic>>(
        listOf(
            StudyTopic(
                id = "topic-1",
                batchId = "batch-2",
                subject = "Database Management Systems (DBMS)",
                title = "Relational Normalization (1NF, 2NF, 3NF, BCNF)",
                description = "Functional dependencies, closure of attributes, lossless join decomposition, dependency preservation, and Boyce-Codd Normal Form.",
                status = TopicStatus.ONGOING,
                isPractical = false,
                startDate = "2026-10-12",
                expectedCompletionDate = "2026-10-22",
                makautUnit = "Unit 3 - Relational Database Design",
                referenceNotesUrl = "https://makautexam.net/syllabus/bca/dbms-unit3.pdf"
            ),
            StudyTopic(
                id = "topic-2",
                batchId = "batch-2",
                subject = "Database Management Systems (DBMS)",
                title = "SQL Joins, Aggregation & Nested Queries (PostgreSQL / MySQL)",
                description = "Practical queries, inner/outer/cross joins, GROUP BY, HAVING, subqueries, and views.",
                status = TopicStatus.COMPLETED,
                isPractical = true,
                startDate = "2026-09-28",
                expectedCompletionDate = "2026-10-10",
                makautUnit = "Unit 2 - Structured Query Language",
                referenceNotesUrl = "https://makautexam.net/syllabus/bca/sql-lab.pdf"
            ),
            StudyTopic(
                id = "topic-3",
                batchId = "batch-2",
                subject = "Data Structures & Algorithms",
                title = "Binary Search Trees & AVL Rotations",
                description = "BST operations (insertion, deletion, traversal), height-balanced trees, AVL single and double rotations.",
                status = TopicStatus.ONGOING,
                isPractical = false,
                startDate = "2026-10-08",
                expectedCompletionDate = "2026-10-24",
                makautUnit = "Unit 4 - Non-Linear Data Structures",
                referenceNotesUrl = "https://makautexam.net/syllabus/bca/dsa-trees.pdf"
            ),
            StudyTopic(
                id = "topic-4",
                batchId = "batch-2",
                subject = "Object-Oriented Programming with Java",
                title = "Multithreading & Synchronization",
                description = "Thread lifecycle, runnable vs thread class, synchronized blocks, wait/notify inter-thread communication, deadlocks.",
                status = TopicStatus.UPCOMING,
                isPractical = true,
                startDate = "2026-10-25",
                expectedCompletionDate = "2026-11-08",
                makautUnit = "Unit 3 - Advanced Java Concepts",
                referenceNotesUrl = "https://makautexam.net/syllabus/bca/java-threads.pdf"
            ),
            StudyTopic(
                id = "topic-5",
                batchId = "batch-2",
                subject = "Computer Networks",
                title = "OSI vs TCP/IP Models & Subnetting (IPv4)",
                description = "Layer architectures, protocol data units, classful vs CIDR IP addressing, subnet masking calculations.",
                status = TopicStatus.COMPLETED,
                isPractical = false,
                startDate = "2026-09-15",
                expectedCompletionDate = "2026-10-02",
                makautUnit = "Unit 1 & 2 - Network Architecture",
                referenceNotesUrl = "https://makautexam.net/syllabus/bca/cn-subnetting.pdf"
            )
        )
    )
    val studyTopics: StateFlow<List<StudyTopic>> = _studyTopics.asStateFlow()

    // Quizzes with Anti-Cheat Heuristics
    private val _quizzes = MutableStateFlow<List<Quiz>>(
        listOf(
            Quiz(
                id = "quiz-1",
                title = "DBMS Normalization & Relational Calculus",
                subject = "Database Management Systems",
                batchId = "batch-2",
                timeLimitMinutes = 5,
                maxMarks = 20,
                questions = listOf(
                    QuizQuestion(
                        id = "q1",
                        text = "A relation schema R is in BCNF if for every functional dependency X -> Y:",
                        options = listOf(
                            "Y is a subset of X or X is a superkey",
                            "Y is a prime attribute",
                            "X is a candidate key only",
                            "There are no non-trivial dependencies"
                        ),
                        correctOptionIndex = 0,
                        explanation = "In Boyce-Codd Normal Form (BCNF), for every non-trivial functional dependency X -> Y, X must be a superkey."
                    ),
                    QuizQuestion(
                        id = "q2",
                        text = "Which of the following is true regarding 3NF vs BCNF?",
                        options = listOf(
                            "Every 3NF relation is guaranteed to be in BCNF",
                            "BCNF is strictly stronger than 3NF; it eliminates transitive dependencies on prime attributes",
                            "3NF does not preserve functional dependencies",
                            "BCNF always preserves functional dependencies"
                        ),
                        correctOptionIndex = 1,
                        explanation = "BCNF is stronger than 3NF. In 3NF, the RHS can be a prime attribute, which is disallowed in BCNF unless the LHS is a superkey."
                    ),
                    QuizQuestion(
                        id = "q3",
                        text = "In SQL, which clause is evaluated BEFORE the SELECT clause?",
                        options = listOf(
                            "ORDER BY",
                            "LIMIT",
                            "WHERE and HAVING",
                            "DISTINCT"
                        ),
                        correctOptionIndex = 2,
                        explanation = "SQL query execution order: FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> DISTINCT -> ORDER BY -> LIMIT."
                    ),
                    QuizQuestion(
                        id = "q4",
                        text = "Under the MAKAUT 10-point scale, what is the statutory percentage equivalent of CGPA 6.75?",
                        options = listOf(
                            "67.50%",
                            "60.00%",
                            "64.12%",
                            "59.25%"
                        ),
                        correctOptionIndex = 1,
                        explanation = "Statutory MAKAUT formula: Percentage = (CGPA - 0.75) * 10 = (6.75 - 0.75) * 10 = 60.00%."
                    )
                )
            ),
            Quiz(
                id = "quiz-2",
                title = "C Pointers, Memory Allocation & DSA",
                subject = "Data Structures & Algorithms",
                batchId = "batch-2",
                timeLimitMinutes = 6,
                maxMarks = 20,
                questions = listOf(
                    QuizQuestion(
                        id = "q21",
                        text = "What is the worst-case time complexity of searching in an AVL tree with n nodes?",
                        options = listOf("O(n)", "O(log n)", "O(n log n)", "O(1)"),
                        correctOptionIndex = 1,
                        explanation = "AVL trees maintain height balance factor in {-1, 0, 1}, ensuring maximum height is 1.44 log n, giving O(log n) worst case."
                    ),
                    QuizQuestion(
                        id = "q22",
                        text = "What does free(ptr) in C do to the pointer variable itself?",
                        options = listOf(
                            "Sets ptr to NULL automatically",
                            "Reallocates ptr to heap stack",
                            "Deallocates memory block but leaves ptr as a dangling pointer",
                            "Throws a segmentation fault"
                        ),
                        correctOptionIndex = 2,
                        explanation = "free(ptr) frees the allocated memory block on the heap, but ptr retains the memory address (dangling pointer) until explicitly set to NULL."
                    )
                )
            )
        )
    )
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _quizAttempts = MutableStateFlow<List<QuizAttempt>>(
        listOf(
            QuizAttempt(
                id = "attempt-1",
                quizId = "quiz-1",
                quizTitle = "DBMS Normalization & Relational Calculus",
                studentId = "student-1",
                score = 15,
                maxScore = 20,
                percentage = 75.0,
                infractionCount = 0,
                completedAt = "2026-10-14 18:30"
            )
        )
    )
    val quizAttempts: StateFlow<List<QuizAttempt>> = _quizAttempts.asStateFlow()

    // Test Marks & Performance Analytics
    private val _testMarks = MutableStateFlow<List<TestMark>>(
        listOf(
            // Rohan Mukherjee
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "DBMS (BAP301)", testName = "Internal Assessment 1", maxMarks = 30.0, obtainedMarks = 26.5, semester = 3, date = "2026-10-10"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "Data Structures (BAP302)", testName = "Internal Assessment 1", maxMarks = 30.0, obtainedMarks = 24.0, semester = 3, date = "2026-10-12"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "OOP with Java (BAP303)", testName = "Mid-Sem Lab Practical", maxMarks = 40.0, obtainedMarks = 36.0, semester = 3, date = "2026-10-15"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "Digital Electronics (BAP101)", testName = "Semester 1 End-Sem", maxMarks = 100.0, obtainedMarks = 84.0, semester = 1, date = "2025-12-20"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "C Programming (BAP102)", testName = "Semester 1 End-Sem", maxMarks = 100.0, obtainedMarks = 88.0, semester = 1, date = "2025-12-22"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "Computer Architecture (BAP201)", testName = "Semester 2 End-Sem", maxMarks = 100.0, obtainedMarks = 78.0, semester = 2, date = "2026-06-18"),
            TestMark(studentId = "student-1", studentName = "Rohan Mukherjee", enrollmentNo = "MAKAUT/BCA/24001", subject = "Discrete Mathematics (BAP202)", testName = "Semester 2 End-Sem", maxMarks = 100.0, obtainedMarks = 82.0, semester = 2, date = "2026-06-20"),

            // Other students for analytics ranking
            TestMark(studentId = "student-2", studentName = "Ananya Roy", enrollmentNo = "MAKAUT/BCA/24002", subject = "DBMS (BAP301)", testName = "Internal Assessment 1", maxMarks = 30.0, obtainedMarks = 29.0, semester = 3, date = "2026-10-10"),
            TestMark(studentId = "student-3", studentName = "Sourav Das", enrollmentNo = "MAKAUT/BCA/24003", subject = "DBMS (BAP301)", testName = "Internal Assessment 1", maxMarks = 30.0, obtainedMarks = 18.0, semester = 3, date = "2026-10-10")
        )
    )
    val testMarks: StateFlow<List<TestMark>> = _testMarks.asStateFlow()

    // Class Calendar Schedules
    private val _schedules = MutableStateFlow<List<ScheduleEvent>>(
        listOf(
            ScheduleEvent(dayOfWeek = "Monday", timeSlot = "10:00 AM - 11:30 AM", subject = "Database Management Systems (Theory)", roomOrLab = "Room 302", type = ScheduleType.LECTURE, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Monday", timeSlot = "12:00 PM - 02:00 PM", subject = "Python & SQL Practical Session", roomOrLab = "Lab 2 (Systems)", type = ScheduleType.LAB, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Tuesday", timeSlot = "10:00 AM - 11:30 AM", subject = "Data Structures & Algorithms", roomOrLab = "Room 302", type = ScheduleType.LECTURE, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Wednesday", timeSlot = "11:30 AM - 01:00 PM", subject = "Computer Networking & Protocols", roomOrLab = "Room 301", type = ScheduleType.LECTURE, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Thursday", timeSlot = "02:00 PM - 04:00 PM", subject = "Java OOP Practical Laboratory", roomOrLab = "Lab 1 (Software)", type = ScheduleType.LAB, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Friday", timeSlot = "10:00 AM - 11:30 AM", subject = "Discrete Mathematics & Graph Theory", roomOrLab = "Room 302", type = ScheduleType.LECTURE, batch = "BCA 2nd Year"),
            ScheduleEvent(dayOfWeek = "Saturday", timeSlot = "11:00 AM - 01:00 PM", subject = "MAKAUT Internal Assessment Mock Test", roomOrLab = "Exam Hall A", type = ScheduleType.TEST, batch = "BCA 2nd Year")
        )
    )
    val schedules: StateFlow<List<ScheduleEvent>> = _schedules.asStateFlow()

    // In-App & Automated Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = "notif-1",
                title = "Upcoming Class Tomorrow",
                message = "DBMS Theory class scheduled for Monday at 10:00 AM in Room 302. Please review 3NF Normalization notes.",
                timestamp = "Today, 18:00",
                type = NotificationType.UPCOMING_CLASS,
                actionText = "View Schedule"
            ),
            AppNotification(
                id = "notif-2",
                title = "Outstanding Fee Reminder",
                message = "Semester 3 Lab & Assessment Fee of ₹2,500 is due on 25th Oct. Pay securely via Razorpay to avoid late fee.",
                timestamp = "Yesterday, 09:30",
                type = NotificationType.OUTSTANDING_BALANCE,
                relatedEntityId = "fee-102",
                actionText = "Pay Now"
            ),
            AppNotification(
                id = "notif-3",
                title = "New Assignment Published",
                message = "Prof. Sengupta published: SQL Schema Design for E-Commerce. Deadline: Oct 24.",
                timestamp = "2 days ago",
                type = NotificationType.ASSIGNMENT_DEADLINE,
                actionText = "View Assignment"
            ),
            AppNotification(
                id = "notif-4",
                title = "Daily Update: Code Snippet Posted",
                message = "Check out the C implementation of Circular Linked List with time complexity breakdown.",
                timestamp = "3 days ago",
                type = NotificationType.ANNOUNCEMENT,
                actionText = "Read Snippet"
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Updates of the Day (HOD Feed with Code Snippets)
    private val _dailyUpdates = MutableStateFlow<List<DailyUpdate>>(
        listOf(
            DailyUpdate(
                id = "update-1",
                date = "2026-10-17",
                subject = "Data Structures & Algorithms (BAP302)",
                topicTitle = "Circular Linked List Insertion & Deletion in C",
                summary = "Today we covered circular linked lists where the last node points back to the head node. Below is the reference implementation demonstrating O(1) insertion at head using tail pointer.",
                codeSnippet = """// Circular Linked List Insertion at Head (O(1) with tail pointer)
#include <stdio.h>
#include <stdlib.h>

struct Node {
    int data;
    struct Node* next;
};

struct Node* insertAtHead(struct Node* last, int data) {
    struct Node* newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    if (last == NULL) {
        newNode->next = newNode;
        return newNode;
    }
    newNode->next = last->next;
    last->next = newNode;
    return last;
}""",
                codeLanguage = "c",
                batch = "BCA 2nd Year"
            ),
            DailyUpdate(
                id = "update-2",
                date = "2026-10-15",
                subject = "Database Management Systems (BAP301)",
                topicTitle = "PostgreSQL Complex Joins & Subqueries",
                summary = "Class recap on inner vs left joins with aggregation. Practice query to find students with average test marks higher than the batch median.",
                codeSnippet = """-- Find BCA students scoring above batch average
SELECT s.enrollment_no, s.name, AVG(m.obtained_marks) as avg_score
FROM students s
JOIN test_marks m ON s.id = m.student_id
GROUP BY s.id, s.enrollment_no, s.name
HAVING AVG(m.obtained_marks) > (
    SELECT AVG(obtained_marks) FROM test_marks
)
ORDER BY avg_score DESC;""",
                codeLanguage = "sql",
                batch = "BCA 2nd Year"
            )
        )
    )
    val dailyUpdates: StateFlow<List<DailyUpdate>> = _dailyUpdates.asStateFlow()

    // Assignments
    private val _assignments = MutableStateFlow<List<Assignment>>(
        listOf(
            Assignment(
                id = "asg-1",
                title = "SQL Schema Design for E-Commerce",
                subject = "Database Management Systems",
                batchId = "batch-2",
                description = "Design a 3NF normalized schema for an online marketplace with tables for Customers, Orders, OrderItems, Products, and Payments. Submit DDL scripts with foreign keys and index definitions.",
                dueDate = "2026-10-24",
                maxMarks = 25,
                submissionStatus = AssignmentStatus.NOT_SUBMITTED
            ),
            Assignment(
                id = "asg-2",
                title = "Java Multithreaded Bank Transaction Simulator",
                subject = "Object-Oriented Programming with Java",
                batchId = "batch-2",
                description = "Implement synchronized deposit and withdraw methods simulating concurrent account access. Prevent race conditions using synchronized blocks or ReentrantLock.",
                dueDate = "2026-10-28",
                maxMarks = 30,
                submissionStatus = AssignmentStatus.SUBMITTED,
                studentSubmissionText = "// BankAccount.java with synchronized methods\npublic synchronized void withdraw(double amt) { ... }",
                submittedDate = "2026-10-16",
                obtainedMarks = 28,
                teacherFeedback = "Excellent usage of synchronized block and clean exception handling for insufficient funds."
            )
        )
    )
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    // Forum Posts
    private val _forumPosts = MutableStateFlow<List<ForumPost>>(
        listOf(
            ForumPost(
                id = "post-1",
                authorId = "student-1",
                authorName = "Rohan Mukherjee",
                authorRole = UserRole.STUDENT,
                title = "Difference between BCNF and 3NF with a real MAKAUT exam question?",
                content = "Could someone clarify why every relation in BCNF is also in 3NF, but the reverse is not always true? Specifically regarding functional dependencies where RHS is a prime attribute.",
                tags = listOf("DBMS", "Normalization", "MAKAUT-Exam", "BCNF"),
                upvotes = 7,
                isUpvotedByMe = true,
                isSolved = true,
                commentsCount = 2,
                createdAt = "2 days ago"
            ),
            ForumPost(
                id = "post-2",
                authorId = "hod-1",
                authorName = "Prof. S. Sengupta (HOD)",
                authorRole = UserRole.ADMIN,
                title = "Important: MAKAUT Odd Semester Exam Schedule Guidelines 2026-27",
                content = "All BCA students must ensure attendance >= 75% before filling out the university examination form. Practical lab records must be signed by this Friday.",
                tags = listOf("Announcement", "MAKAUT", "Exam-Notice", "HOD"),
                upvotes = 19,
                isUpvotedByMe = false,
                isSolved = false,
                commentsCount = 4,
                createdAt = "Yesterday"
            )
        )
    )
    val forumPosts: StateFlow<List<ForumPost>> = _forumPosts.asStateFlow()

    private val _forumComments = MutableStateFlow<Map<String, List<ForumComment>>>(
        mapOf(
            "post-1" to listOf(
                ForumComment(
                    postId = "post-1",
                    authorName = "Prof. S. Sengupta (HOD)",
                    authorRole = UserRole.ADMIN,
                    content = "In 3NF, for X -> Y, if Y is a prime attribute (part of any candidate key), the dependency is permitted even if X is not a superkey. In BCNF, this relaxation is strictly removed: X MUST be a superkey. Hence BCNF eliminates transitive dependencies on prime attributes.",
                    isAcceptedAnswer = true,
                    createdAt = "1 day ago"
                ),
                ForumComment(
                    postId = "post-1",
                    authorName = "Ananya Roy",
                    authorRole = UserRole.STUDENT,
                    content = "Check standard textbook Example: R(Student, Course, Teacher) where (Student, Course) is candidate key and Teacher -> Course. It is in 3NF but not BCNF!",
                    isAcceptedAnswer = false,
                    createdAt = "1 day ago"
                )
            )
        )
    )
    val forumComments: StateFlow<Map<String, List<ForumComment>>> = _forumComments.asStateFlow()

    // --- Actions & State Mutations ---

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun updateActiveUserProfile(
        uid: String,
        name: String,
        email: String,
        rollNumber: String,
        batch: String,
        phone: String,
        role: UserRole
    ) {
        _currentRole.value = role
        _currentUser.value = StudentUser(
            id = uid,
            name = name,
            email = email,
            enrollmentNo = rollNumber.ifBlank { "MAKAUT/BCA/24001" },
            batchId = "batch-2",
            batchName = batch.ifBlank { "BCA 2nd Year" },
            contactNo = phone.ifBlank { "+91 98301 23456" },
            parentContactNo = "+91 98300 98765",
            currentSemester = 3,
            cgpa = 8.12
        )
    }

    fun selectStudent(student: StudentUser) {
        _currentUser.value = student
    }

    // Payment Processing Simulation
    fun processRazorpayPayment(
        feeRecordId: String,
        amount: Double,
        method: String,
        onSuccess: (FeeTransaction) -> Unit
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val nowStr = dateFormat.format(Date())
        val orderId = "order_MAKAUT_${(1000..9999).random()}"
        val paymentId = "pay_BCA_${System.currentTimeMillis()}"

        val transaction = FeeTransaction(
            feeRecordId = feeRecordId,
            razorpayOrderId = orderId,
            razorpayPaymentId = paymentId,
            amount = amount,
            status = "SUCCESS",
            paymentMethod = method,
            timestamp = nowStr
        )

        _feeRecords.value = _feeRecords.value.map { rec ->
            if (rec.id == feeRecordId) {
                val newPaid = rec.paidAmount + amount
                val newStatus = if (newPaid >= rec.totalAmount) FeeStatus.PAID else FeeStatus.PARTIAL
                rec.copy(
                    paidAmount = newPaid,
                    status = newStatus,
                    transactions = rec.transactions + transaction
                )
            } else rec
        }

        // Add automated notification
        addNotification(
            title = "Payment Confirmed (Razorpay)",
            message = "Your payment of ${MakautCalculator.formatCurrency(amount)} was successfully processed. Ref: $paymentId.",
            type = NotificationType.OUTSTANDING_BALANCE,
            actionText = "View Receipt"
        )

        onSuccess(transaction)
    }

    // Attendance bulk marking (by HOD)
    fun markBatchAttendance(
        records: List<AttendanceRecord>
    ) {
        val existing = _attendanceRecords.value.toMutableList()
        // Replace or add
        records.forEach { newRec ->
            val idx = existing.indexOfFirst {
                it.studentId == newRec.studentId && it.date == newRec.date && it.subject == newRec.subject
            }
            if (idx >= 0) {
                existing[idx] = newRec
            } else {
                existing.add(newRec)
            }
        }
        _attendanceRecords.value = existing
    }

    // Record Quiz Attempt
    fun submitQuizAttempt(
        quizId: String,
        quizTitle: String,
        studentId: String,
        score: Int,
        maxScore: Int,
        infractionCount: Int,
        wasForced: Boolean
    ): QuizAttempt {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val attempt = QuizAttempt(
            quizId = quizId,
            quizTitle = quizTitle,
            studentId = studentId,
            score = score,
            maxScore = maxScore,
            percentage = if (maxScore > 0) (score.toDouble() / maxScore.toDouble()) * 100.0 else 0.0,
            infractionCount = infractionCount,
            completedAt = dateFormat.format(Date()),
            wasForcedSubmitted = wasForced
        )
        _quizAttempts.value = listOf(attempt) + _quizAttempts.value
        return attempt
    }

    // Submit Assignment
    fun submitAssignment(assignmentId: String, text: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        _assignments.value = _assignments.value.map {
            if (it.id == assignmentId) {
                it.copy(
                    submissionStatus = AssignmentStatus.SUBMITTED,
                    studentSubmissionText = text,
                    submittedDate = dateFormat.format(Date())
                )
            } else it
        }
    }

    // Grade Assignment (HOD)
    fun gradeAssignment(assignmentId: String, marks: Int, feedback: String) {
        _assignments.value = _assignments.value.map {
            if (it.id == assignmentId) {
                it.copy(
                    submissionStatus = AssignmentStatus.ACCEPTED,
                    obtainedMarks = marks,
                    teacherFeedback = feedback
                )
            } else it
        }
    }

    // Study topic status transition
    fun updateTopicStatus(topicId: String, status: TopicStatus) {
        _studyTopics.value = _studyTopics.value.map {
            if (it.id == topicId) it.copy(status = status) else it
        }
    }

    // Add study topic
    fun addStudyTopic(topic: StudyTopic) {
        _studyTopics.value = listOf(topic) + _studyTopics.value
    }

    // Post Daily Update
    fun addDailyUpdate(update: DailyUpdate) {
        _dailyUpdates.value = listOf(update) + _dailyUpdates.value
        addNotification(
            title = "New Class Update Posted",
            message = "${update.subject}: ${update.topicTitle}",
            type = NotificationType.ANNOUNCEMENT,
            actionText = "Read Note"
        )
    }

    // Automated Notifications trigger
    fun addNotification(title: String, message: String, type: NotificationType, actionText: String? = null) {
        val notif = AppNotification(
            title = title,
            message = message,
            timestamp = "Just now",
            type = type,
            actionText = actionText
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    // Forum upvote
    fun toggleForumUpvote(postId: String) {
        _forumPosts.value = _forumPosts.value.map {
            if (it.id == postId) {
                val newUpvoted = !it.isUpvotedByMe
                val countChange = if (newUpvoted) 1 else -1
                it.copy(isUpvotedByMe = newUpvoted, upvotes = it.upvotes + countChange)
            } else it
        }
    }

    // Add Forum Post
    fun addForumPost(title: String, content: String, tags: List<String>, authorName: String, authorRole: UserRole) {
        val post = ForumPost(
            authorId = UUID.randomUUID().toString(),
            authorName = authorName,
            authorRole = authorRole,
            title = title,
            content = content,
            tags = tags,
            createdAt = "Just now"
        )
        _forumPosts.value = listOf(post) + _forumPosts.value
    }

    // Add Forum Comment
    fun addForumComment(postId: String, content: String, authorName: String, authorRole: UserRole) {
        val comment = ForumComment(
            postId = postId,
            authorName = authorName,
            authorRole = authorRole,
            content = content,
            createdAt = "Just now"
        )
        val currentMap = _forumComments.value.toMutableMap()
        val list = currentMap[postId]?.toMutableList() ?: mutableListOf()
        list.add(comment)
        currentMap[postId] = list
        _forumComments.value = currentMap

        // Update comment count
        _forumPosts.value = _forumPosts.value.map {
            if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
        }
    }

    // Study Resources & Offline Notes Repository
    private val _studyResources = MutableStateFlow<List<StudyResource>>(
        listOf(
            StudyResource(
                id = "res-1",
                title = "MAKAUT BCA DBMS Unit 3 Relational Normalization Notes",
                subject = "Database Management Systems",
                type = ResourceType.LECTURE_NOTE,
                fileSizeMb = 3.4,
                semester = 3,
                isDownloaded = true
            ),
            StudyResource(
                id = "res-2",
                title = "MAKAUT 2024 End-Semester Question Paper (BAP301)",
                subject = "Database Management Systems",
                type = ResourceType.PYQ,
                fileSizeMb = 1.8,
                semester = 3,
                isDownloaded = false
            ),
            StudyResource(
                id = "res-3",
                title = "Data Structures in C Practical Lab Manual with Solutions",
                subject = "Data Structures & Algorithms",
                type = ResourceType.LAB_MANUAL,
                fileSizeMb = 5.2,
                semester = 3,
                isDownloaded = true
            ),
            StudyResource(
                id = "res-4",
                title = "Java OOP Socket & Multithreading Practice Bank",
                subject = "OOP with Java",
                type = ResourceType.LECTURE_NOTE,
                fileSizeMb = 2.1,
                semester = 3,
                isDownloaded = false
            ),
            StudyResource(
                id = "res-5",
                title = "Official MAKAUT BCA Choice Based Credit Syllabus 2024-27",
                subject = "Academic Regulations",
                type = ResourceType.SYLLABUS_PDF,
                fileSizeMb = 4.0,
                semester = 3,
                isDownloaded = true
            )
        )
    )
    val studyResources: StateFlow<List<StudyResource>> = _studyResources.asStateFlow()

    // Audit Logs (Immutable HOD Activity Trail)
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(
        listOf(
            AuditLogEntry(
                action = "BULK_ATTENDANCE_RECORDED",
                targetEntity = "BCA 2nd Year (Batch 2)",
                details = "Marked 5 students for DBMS lecture. 2 absent students notified.",
                timestamp = "2026-10-18 10:15"
            ),
            AuditLogEntry(
                action = "FEE_STATUS_VERIFIED",
                targetEntity = "Rohan Mukherjee (MAKAUT/BCA/24001)",
                details = "Verified ₹3,500 Razorpay payment order_MAKAUT_8921.",
                timestamp = "2026-10-17 14:30"
            ),
            AuditLogEntry(
                action = "ASSIGNMENT_GRADED",
                targetEntity = "Java Multithreaded Bank Simulator",
                details = "Graded student submission: 28/30 with feedback notes.",
                timestamp = "2026-10-16 16:45"
            )
        )
    )
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Notification Preferences
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    val notificationPreferences: StateFlow<NotificationPreferences> = _notificationPreferences.asStateFlow()

    // Language Support
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun toggleResourceDownload(resourceId: String) {
        _studyResources.value = _studyResources.value.map {
            if (it.id == resourceId) it.copy(isDownloaded = !it.isDownloaded) else it
        }
    }

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        _notificationPreferences.value = prefs
    }

    fun setAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun addAuditLog(action: String, targetEntity: String, details: String) {
        val entry = AuditLogEntry(
            action = action,
            targetEntity = targetEntity,
            details = details,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    fun bulkImportMarks(marks: List<TestMark>) {
        _testMarks.value = marks + _testMarks.value
        addAuditLog(
            action = "BULK_MARKS_IMPORTED",
            targetEntity = "${marks.size} Student Marks Records",
            details = "Imported test scores from CSV spreadsheet."
        )
    }

    fun createBatchAssignment(assignment: Assignment, batchIds: List<String>) {
        val newAssignments = batchIds.map { bId ->
            assignment.copy(id = UUID.randomUUID().toString(), batchId = bId)
        }
        _assignments.value = newAssignments + _assignments.value
        addAuditLog(
            action = "BULK_ASSIGNMENT_CREATED",
            targetEntity = assignment.title,
            details = "Dispatched assignment to ${batchIds.size} batch(es)."
        )
    }

    // Doubt-Solving Chat Thread
    private val _doubtMessages = MutableStateFlow<List<DoubtMessage>>(
        listOf(
            DoubtMessage(
                id = "doubt-1",
                senderId = "stud-001",
                senderName = "Rahul Sharma",
                senderRole = UserRole.STUDENT,
                text = "Respected Sir, regarding BAP301 (DBMS) Boyce-Codd Normal Form: does every relation in BCNF necessarily satisfy 3NF as well?",
                timestamp = "Today 10:15 AM",
                isFromTeacher = false
            ),
            DoubtMessage(
                id = "doubt-2",
                senderId = "prof-sengupta-hod",
                senderName = "Prof. S. Sengupta (HOD)",
                senderRole = UserRole.ADMIN,
                text = "Yes Rahul, BCNF is a stricter subset of 3NF. In 3NF, for X -> A, either X is a superkey OR A is a prime attribute. In BCNF, X must strictly be a superkey for every non-trivial dependency.",
                timestamp = "Today 10:18 AM",
                codeSnippet = "// 3NF Condition:\n// X -> A holds if X is Superkey OR A is Prime Attribute.\n// BCNF Condition (Stricter):\n// X -> A strictly requires X to be a Superkey.",
                isFromTeacher = true
            ),
            DoubtMessage(
                id = "doubt-3",
                senderId = "stud-001",
                senderName = "Rahul Sharma",
                senderRole = UserRole.STUDENT,
                text = "Understood perfectly Sir! That clears the confusion on question 4 of the practice assignment.",
                timestamp = "Today 10:22 AM",
                isFromTeacher = false
            )
        )
    )
    val doubtMessages: StateFlow<List<DoubtMessage>> = _doubtMessages.asStateFlow()

    // Course Feedback
    private val _courseFeedbacks = MutableStateFlow<List<CourseFeedback>>(
        listOf(
            CourseFeedback(
                id = "fb-1",
                studentId = "stud-001",
                studentName = "Rahul Sharma",
                rating = 5,
                subject = "Database Management Systems (BAP301)",
                comment = "Exceptional conceptual clarity on query optimization and normalization. Practical labs are very helpful.",
                timestamp = "2026-10-15"
            ),
            CourseFeedback(
                id = "fb-2",
                studentId = "stud-002",
                studentName = "Sneha Ghosh",
                rating = 5,
                subject = "Object Oriented Programming with Java (BAP302)",
                comment = "Regular code reviews and anti-cheat mock tests helped me improve my score from 65% to 88%.",
                timestamp = "2026-10-16"
            )
        )
    )
    val courseFeedbacks: StateFlow<List<CourseFeedback>> = _courseFeedbacks.asStateFlow()

    fun sendDoubtMessage(text: String, codeSnippet: String?, senderId: String, senderName: String, senderRole: UserRole) {
        val newMsg = DoubtMessage(
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            text = text,
            timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date()),
            codeSnippet = codeSnippet,
            isFromTeacher = senderRole == UserRole.ADMIN
        )
        _doubtMessages.value = _doubtMessages.value + newMsg
    }

    fun submitFeedback(rating: Int, subject: String, comment: String, studentId: String, studentName: String) {
        val fb = CourseFeedback(
            studentId = studentId,
            studentName = studentName,
            rating = rating,
            subject = subject,
            comment = comment,
            timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
        _courseFeedbacks.value = listOf(fb) + _courseFeedbacks.value
    }

    // Upload / Add Test Marks (CSV Bulk or Form)
    fun addTestMark(mark: TestMark) {
        _testMarks.value = listOf(mark) + _testMarks.value
        addAuditLog(
            action = "TEST_MARK_RECORDED",
            targetEntity = "${mark.studentName} - ${mark.subject}",
            details = "Recorded ${mark.obtainedMarks}/${mark.maxMarks} on ${mark.testName}"
        )
    }

    // ================= MULTI-COLLEGE & TEACHER APPROVAL ENGINE =================

    private val _colleges = MutableStateFlow(CollegeCatalog.sampleColleges)
    val colleges: StateFlow<List<College>> = _colleges.asStateFlow()

    private val _activeCollegeId = MutableStateFlow("col-iem-kolkata")
    val activeCollegeId: StateFlow<String> = _activeCollegeId.asStateFlow()

    fun setActiveCollege(collegeId: String) {
        _activeCollegeId.value = collegeId
        addAuditLog(
            action = "COLLEGE_SWITCHED",
            targetEntity = collegeId,
            details = "Switched active college context"
        )
    }

    // Teacher Profiles & Approvals
    private val _teachers = MutableStateFlow(
        listOf(
            TeacherProfile(
                id = "teacher-1",
                fullName = "Dr. Anirban Banerjee",
                email = "anirban.b@iemcal.com",
                mobile = "+91 98310 44211",
                designation = "Assistant Professor",
                collegeId = "col-iem-kolkata",
                collegeName = "Institute of Engineering & Management (IEM)",
                assignedSubjects = listOf("DBMS", "Design & Analysis of Algorithms"),
                assignedBatches = listOf("Batch 2024–27, Section A"),
                qualifications = "Ph.D. in Computer Science (ISI Kolkata)",
                experienceYears = 8,
                bio = "Specializing in distributed database optimization and relational calculus.",
                cvFileName = "dr_anirban_banerjee_cv.pdf",
                status = TeacherApprovalStatus.APPROVED,
                appliedDate = "2026-10-01"
            ),
            TeacherProfile(
                id = "teacher-2",
                fullName = "Prof. Moumita Mukherjee",
                email = "moumita.m@iemcal.com",
                mobile = "+91 98305 11982",
                designation = "Associate Professor",
                collegeId = "col-iem-kolkata",
                assignedSubjects = listOf("Object Oriented Programming with Java", "Python Lab"),
                assignedBatches = listOf("Batch 2024–27, Section A", "Batch 2025–28, Section B"),
                qualifications = "M.Tech in Software Engineering (Jadavpur University)",
                experienceYears = 6,
                bio = "Passionate educator focusing on Java multithreading and clean code architectures.",
                cvFileName = "moumita_mukherjee_resume.pdf",
                status = TeacherApprovalStatus.APPROVED,
                appliedDate = "2026-10-05"
            ),
            TeacherProfile(
                id = "teacher-3",
                fullName = "Subhajit Karmakar",
                email = "subhajit.k@gmail.com",
                mobile = "+91 94331 77652",
                designation = "Lecturer / Industry Mentor",
                collegeId = "col-iem-kolkata",
                assignedSubjects = listOf("Computer Architecture & Organization", "Digital Logic"),
                assignedBatches = listOf("Batch 2024–27, Section A"),
                qualifications = "M.Sc in Computer Science (Calcutta University), GATE Qualified",
                experienceYears = 3,
                bio = "Industry developer with 3+ years in microarchitecture synthesis and FPGA labs.",
                cvFileName = "subhajit_karmakar_cv.pdf",
                status = TeacherApprovalStatus.SUBMITTED,
                appliedDate = "2026-10-18"
            )
        )
    )
    val teachers: StateFlow<List<TeacherProfile>> = _teachers.asStateFlow()

    fun approveTeacher(teacherId: String) {
        _teachers.value = _teachers.value.map { t ->
            if (t.id == teacherId) t.copy(status = TeacherApprovalStatus.APPROVED, rejectionReason = null) else t
        }
        addAuditLog(
            action = "TEACHER_APPROVED",
            targetEntity = teacherId,
            details = "HOD approved faculty appointment and assigned subject access."
        )
    }

    fun rejectTeacher(teacherId: String, reason: String) {
        _teachers.value = _teachers.value.map { t ->
            if (t.id == teacherId) t.copy(status = TeacherApprovalStatus.REJECTED, rejectionReason = reason) else t
        }
        addAuditLog(
            action = "TEACHER_REJECTED",
            targetEntity = teacherId,
            details = "Faculty application rejected: $reason"
        )
    }

    fun requestTeacherChanges(teacherId: String, note: String) {
        _teachers.value = _teachers.value.map { t ->
            if (t.id == teacherId) t.copy(status = TeacherApprovalStatus.CHANGES_REQUESTED, rejectionReason = note) else t
        }
    }

    // Announcements
    private val _announcements = MutableStateFlow(
        listOf(
            AnnouncementItem(
                id = "ann-1",
                title = "Mid-Semester Practical CA Exam Notice",
                message = "The Continuous Assessment (CA2) Lab Exam for DBMS and Java will be held on Monday 24th October in Lab 402.",
                postedBy = "Prof. S. Sengupta (HOD)",
                collegeId = "col-iem-kolkata",
                timestamp = "Yesterday 4:00 PM",
                readCount = 42,
                reactionCount = 19
            ),
            AnnouncementItem(
                id = "ann-2",
                title = "MAKAUT Technical Symposium & Hackathon",
                message = "Registrations are now open for the Annual State CodeFest. Submit team details by Friday.",
                postedBy = "Dr. Anirban Banerjee",
                collegeId = "col-iem-kolkata",
                timestamp = "2 days ago",
                readCount = 58,
                reactionCount = 27
            )
        )
    )
    val announcements: StateFlow<List<AnnouncementItem>> = _announcements.asStateFlow()

    fun postAnnouncement(title: String, message: String, targetRole: String) {
        val item = AnnouncementItem(
            title = title,
            message = message,
            postedBy = if (_currentRole.value == UserRole.ADMIN) "Prof. S. Sengupta (HOD)" else "Faculty Member",
            collegeId = _activeCollegeId.value,
            targetRole = targetRole,
            timestamp = "Just now",
            readCount = 1,
            reactionCount = 0
        )
        _announcements.value = listOf(item) + _announcements.value
    }

    // Leaderboard Computation: Score = (Attendance * 30%) + (Assignment Completion * 30%) + (Quiz Performance * 40%)
    val leaderboard: StateFlow<List<LeaderboardEntry>> = MutableStateFlow(
        listOf(
            LeaderboardEntry(
                rank = 1,
                studentId = "student-2",
                studentName = "Ananya Roy",
                enrollmentNo = "MAKAUT/BCA/24002",
                collegeName = "Institute of Engineering & Management (IEM)",
                attendancePercentage = 95.0,
                assignmentCompletionPercentage = 100.0,
                quizAveragePercentage = 92.5,
                compositeScore = (95.0 * 0.3) + (100.0 * 0.3) + (92.5 * 0.4), // 95.5
                badge = "Gold Scholar"
            ),
            LeaderboardEntry(
                rank = 2,
                studentId = "student-1",
                studentName = "Rohan Mukherjee",
                enrollmentNo = "MAKAUT/BCA/24001",
                collegeName = "Institute of Engineering & Management (IEM)",
                attendancePercentage = 80.0,
                assignmentCompletionPercentage = 85.0,
                quizAveragePercentage = 90.0,
                compositeScore = (80.0 * 0.3) + (85.0 * 0.3) + (90.0 * 0.4), // 85.5
                badge = "Silver Achiever"
            ),
            LeaderboardEntry(
                rank = 3,
                studentId = "student-4",
                studentName = "Debolina Ghosh",
                enrollmentNo = "MAKAUT/BCA/24004",
                collegeName = "Institute of Engineering & Management (IEM)",
                attendancePercentage = 82.0,
                assignmentCompletionPercentage = 80.0,
                quizAveragePercentage = 86.0,
                compositeScore = (82.0 * 0.3) + (80.0 * 0.3) + (86.0 * 0.4), // 83.0
                badge = "Bronze Honoree"
            ),
            LeaderboardEntry(
                rank = 4,
                studentId = "student-5",
                studentName = "Pritam Banerjee",
                enrollmentNo = "MAKAUT/BCA/24005",
                collegeName = "Institute of Engineering & Management (IEM)",
                attendancePercentage = 76.0,
                assignmentCompletionPercentage = 70.0,
                quizAveragePercentage = 75.0,
                compositeScore = (76.0 * 0.3) + (70.0 * 0.3) + (75.0 * 0.4) // 73.8
            ),
            LeaderboardEntry(
                rank = 5,
                studentId = "student-3",
                studentName = "Sourav Das",
                enrollmentNo = "MAKAUT/BCA/24003",
                collegeName = "Institute of Engineering & Management (IEM)",
                attendancePercentage = 33.3,
                assignmentCompletionPercentage = 50.0,
                quizAveragePercentage = 60.0,
                compositeScore = (33.3 * 0.3) + (50.0 * 0.3) + (60.0 * 0.4) // 49.0
            )
        )
    ).asStateFlow()

    // App Lock (PIN / Biometric protection)
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _appPin = MutableStateFlow("1234")

    fun verifyPin(enteredPin: String): Boolean {
        val matches = enteredPin == _appPin.value
        if (matches) {
            _isAppLocked.value = false
        }
        return matches
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    // AI Doubt Assistant - Grounded in College & Teacher Notes Only
    fun askAiAssistantGrounded(question: String): DoubtMessage {
        val cleanQ = question.lowercase()

        val (answerText, codeSnippet, isGrounded) = when {
            cleanQ.contains("bcnf") || cleanQ.contains("boyce") || cleanQ.contains("3nf") -> {
                Triple(
                    "According to Module 3 (DBMS Normalization): Boyce-Codd Normal Form (BCNF) strictly requires that for every functional dependency X -> Y, X must be a superkey. In 3NF, Y could alternatively be a prime attribute.",
                    "// BCNF Rule: If X -> Y is non-trivial, then X MUST be a Superkey.\n// Table: Student_Advisor(student_id, subject, advisor_id)\n// If advisor -> subject, Advisor is NOT superkey => Violates BCNF.",
                    true
                )
            }
            cleanQ.contains("inheritance") || cleanQ.contains("polymorphism") || cleanQ.contains("java") -> {
                Triple(
                    "According to Module 2 (OOP with Java): Method overriding enables runtime polymorphism using dynamic method dispatch. In Java, methods are virtual by default unless declared final.",
                    "class Animal { void sound() { System.out.println(\"Sound\"); } }\nclass Dog extends Animal { void sound() { System.out.println(\"Bark\"); } }",
                    true
                )
            }
            cleanQ.contains("b-tree") || cleanQ.contains("indexing") || cleanQ.contains("tree") -> {
                Triple(
                    "According to Database Indexing Notes: In a B+ Tree, data records and pointers reside strictly in leaf nodes linked sequentially for fast range scans. In B-Trees, data keys reside across all levels.",
                    "// B+ Tree Structure:\n// Internal Nodes: Routing keys only\n// Leaf Nodes: Data records + next-leaf pointer [L1] -> [L2] -> [L3]",
                    true
                )
            }
            cleanQ.contains("stack") || cleanQ.contains("queue") || cleanQ.contains("data structure") -> {
                Triple(
                    "According to Data Structures Notes: Stack operates on LIFO (Last In First Out) principle using push/pop, queue operates on FIFO (First In First Out) using enqueue/dequeue.",
                    "int stack[100], top = -1;\nvoid push(int x) { stack[++top] = x; }\nint pop() { return stack[top--]; }",
                    true
                )
            }
            else -> {
                Triple(
                    "I don’t have enough information in the uploaded college notes for this query. Please ask your teacher or post this doubt in the discussion forum.",
                    null,
                    false
                )
            }
        }

        val aiMsg = DoubtMessage(
            senderId = "ai-doubt-assistant",
            senderName = "AI Academic Assistant (Grounded Notes)",
            senderRole = UserRole.ADMIN,
            text = answerText,
            timestamp = "Just now",
            codeSnippet = codeSnippet,
            isFromTeacher = true,
            isAiResponse = true,
            isGroundedInNotes = isGrounded
        )

        _doubtMessages.value = _doubtMessages.value + aiMsg
        return aiMsg
    }
}

