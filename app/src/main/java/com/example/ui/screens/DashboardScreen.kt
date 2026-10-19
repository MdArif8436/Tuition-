package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.CodeSnippetView
import com.example.ui.components.MakautFormulaCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.MakautCalculator
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TuitionViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val attendancePct by viewModel.currentStudentAttendancePercentage.collectAsState()
    val pendingFees by viewModel.currentStudentPendingFees.collectAsState()
    val lowAttendanceList by viewModel.lowAttendanceStudents.collectAsState()
    val dailyUpdates by viewModel.dailyUpdates.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val studyTopics by viewModel.studyTopics.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    var showLandingInfo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AcademicPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = when (currentRole) {
                                        UserRole.ADMIN, UserRole.SUPER_ADMIN -> "HOD & Faculty Command Center"
                                        UserRole.TEACHER -> "Teacher Academic Portal"
                                        UserRole.STUDENT -> "BCA Student Academic Portal"
                                        UserRole.PARENT -> "Parent / Guardian Oversight"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (currentRole) {
                                    UserRole.ADMIN, UserRole.SUPER_ADMIN -> "Welcome, Prof. S. Sengupta"
                                    UserRole.TEACHER -> "Welcome, Faculty"
                                    UserRole.STUDENT -> "Hello, ${currentUser.name}"
                                    UserRole.PARENT -> "Guardian Portal for ${currentUser.name}"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "MAKAUT BCA Syllabus • Kolkata Cohort",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showLandingInfo = !showLandingInfo },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = if (showLandingInfo) Icons.Default.Close else Icons.Default.School,
                                contentDescription = "Faculty Bio & Pedagogy",
                                tint = AcademicPrimary
                            )
                        }
                    }

                    if (showLandingInfo) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(14.dp))
                        HODProfileSection()
                    }
                }
            }
        }

        // Temporary Firebase Diagnostics Banner
        item {
            var showDashboardDiagnostics by remember { mutableStateOf(true) }
            val isFirebaseInit by com.example.FirebaseInitializer.isInitialized.collectAsState()
            val firebaseInitErr by com.example.FirebaseInitializer.initializationError.collectAsState()

            if (showDashboardDiagnostics) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandSoftSky.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFirebaseInit) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isFirebaseInit) BrandOceanDeep else Color(0xFFD39E00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase: ${if (isFirebaseInit) "Active & Synced" else "Initializing"} (gen-lang-client-0915184420)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandOceanDeep
                            )
                        }
                        IconButton(
                            onClick = { showDashboardDiagnostics = false },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = BrandTextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // ROLE SPECIFIC CONTENT
        when (currentRole) {
            UserRole.STUDENT -> {
                // Quick Summary Stats for Student
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Attendance",
                            value = "${String.format(Locale.US, "%.0f", attendancePct)}%",
                            icon = Icons.Default.FactCheck,
                            iconTint = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                            badgeText = if (attendancePct >= 75.0) "Good" else "Warning <75%",
                            badgeColor = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "CGPA Score",
                            value = String.format(Locale.US, "%.2f", currentUser.cgpa),
                            icon = Icons.Default.Grade,
                            iconTint = AcademicTertiary,
                            badgeText = "${String.format(Locale.US, "%.1f", MakautCalculator.cgpaToPercentage(currentUser.cgpa))}%",
                            badgeColor = AcademicTertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Attendance Warning banner if < 75%
                if (attendancePct < 75.0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Attendance Deficit Alert",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Your attendance is ${String.format(Locale.US, "%.1f", attendancePct)}%, which is below the statutory 75% MAKAUT requirement to sit for final semester examinations.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Outstanding Fees Card with Razorpay action
                if (pendingFees.isNotEmpty()) {
                    item {
                        val fee = pendingFees.first()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusPending.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = StatusPending
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Outstanding Balance",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusPending.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Due: ${fee.dueDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusPending,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = fee.feeMonth,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = MakautCalculator.formatCurrency(fee.pendingBalance),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.openPaymentCheckout(fee) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pay_due_fee_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimary)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pay Now via Razorpay")
                                }
                            }
                        }
                    }
                }

                // Interactive MAKAUT CGPA Calculator
                item {
                    MakautFormulaCard(initialCgpa = currentUser.cgpa)
                }

                // Next scheduled class
                item {
                    val nextClass = schedules.firstOrNull()
                    if (nextClass != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Next Upcoming Class",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = { onNavigateToTab(4) /* Calendar */ }) {
                                        Text("Full Schedule")
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (nextClass.type == ScheduleType.LAB) AcademicSecondary else AcademicPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (nextClass.type == ScheduleType.LAB) Icons.Default.Computer else Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = nextClass.subject,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${nextClass.dayOfWeek} • ${nextClass.timeSlot} • ${nextClass.roomOrLab}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily Update Code Snippet Preview
                if (dailyUpdates.isNotEmpty()) {
                    item {
                        val latestUpdate = dailyUpdates.first()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Latest HOD Class Update",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { onNavigateToTab(7) /* Updates */ }) {
                                    Text("View All")
                                }
                            }
                            Text(
                                text = "${latestUpdate.subject}: ${latestUpdate.topicTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (latestUpdate.codeSnippet != null) {
                                CodeSnippetView(
                                    code = latestUpdate.codeSnippet,
                                    language = latestUpdate.codeLanguage ?: "c"
                                )
                            }
                        }
                    }
                }
            }

            UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.TEACHER -> {
                // Teacher / HOD Command Center
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Active Students",
                            value = "5 Enrolled",
                            icon = Icons.Default.People,
                            iconTint = AcademicPrimary,
                            badgeText = "BCA Sem 3",
                            badgeColor = AcademicPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Avg Attendance",
                            value = "79.4%",
                            icon = Icons.Default.FactCheck,
                            iconTint = AcademicSecondary,
                            badgeText = "MAKAUT Req 75%",
                            badgeColor = AcademicSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Defaulter Anomalies Warning Box
                if (lowAttendanceList.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Attendance Defaulters (<75%)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = "${lowAttendanceList.size} Students",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                lowAttendanceList.forEach { (student, pct) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = student.enrollmentNo,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", pct)}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        viewModel.triggerAutomatedClassReminder()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Broadcast Attendance Warning to Parents")
                                }
                            }
                        }
                    }
                }

                // Rapid Actions Grid for HOD
                item {
                    Text(
                        text = "Teacher Command Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CommandActionButton(
                                icon = Icons.Default.FactCheck,
                                label = "Mark Bulk Attendance",
                                onClick = { onNavigateToTab(1) /* Attendance */ },
                                modifier = Modifier.weight(1f)
                            )
                            CommandActionButton(
                                icon = Icons.Default.AttachMoney,
                                label = "Fee Records & Defaulters",
                                onClick = { onNavigateToTab(2) /* Fees */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CommandActionButton(
                                icon = Icons.Default.Analytics,
                                label = "Marks & CGPA Analytics",
                                onClick = { onNavigateToTab(5) /* Analytics */ },
                                modifier = Modifier.weight(1f)
                            )
                            CommandActionButton(
                                icon = Icons.Default.Assignment,
                                label = "Review Submissions",
                                onClick = { onNavigateToTab(6) /* Assignments */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // MAKAUT Formula Card
                item {
                    MakautFormulaCard(initialCgpa = 7.5)
                }
            }

            UserRole.PARENT -> {
                // Parent / Guardian Portal
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Ward Academic Oversight",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Official records verified by HOD Prof. S. Sengupta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    title = "Ward Attendance",
                                    value = "${String.format(Locale.US, "%.0f", attendancePct)}%",
                                    icon = Icons.Default.FactCheck,
                                    iconTint = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                                    badgeText = if (attendancePct >= 75.0) "Compliant" else "Deficit Alert",
                                    badgeColor = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Current CGPA",
                                    value = String.format(Locale.US, "%.2f", currentUser.cgpa),
                                    icon = Icons.Default.Grade,
                                    iconTint = AcademicTertiary,
                                    badgeText = "${String.format(Locale.US, "%.1f", MakautCalculator.cgpaToPercentage(currentUser.cgpa))}%",
                                    badgeColor = AcademicTertiary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Fee Payment Verification",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (pendingFees.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusPaid.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusPaid)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "All tuition and assessment fees are up-to-date.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusPaid
                                        )
                                    }
                                }
                            } else {
                                pendingFees.forEach { fee ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fee.feeMonth, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${MakautCalculator.formatCurrency(fee.pendingBalance)} Due",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPending
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    MakautFormulaCard(initialCgpa = currentUser.cgpa)
                }
            }
        }
    }
}

@Composable
private fun CommandActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AcademicPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HODProfileSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "About Faculty & MAKAUT Pedagogy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Prof. S. Sengupta (M.Tech in Computer Science, B.Tech, MAKAUT Academic Advisory Board Member) has mentored over 800+ BCA graduates into leading IT companies (TCS, Cognizant, Wipro, Infosys).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Curriculum Highlights: Rigorous focus on C, Python, PostgreSQL, Data Structures, OOP Java, and modern Web Technologies aligned with the MAKAUT Choice Based Credit System (CBCS).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "📍 Salt Lake Sector V, Kolkata",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "📞 +91 98300 12345",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
