package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.MakautCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: TuitionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val notifPrefs by viewModel.notificationPreferences.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showAuditLogs by remember { mutableStateOf(false) }
    var showLoginSwitchDialog by remember { mutableStateOf(false) }

    // Feedback inputs
    var feedbackRating by remember { mutableIntStateOf(5) }
    var feedbackSubject by remember { mutableStateOf("DBMS (BAP301)") }
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSubmittedSnackbar by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // User Profile Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AcademicPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    UserRole.ADMIN, UserRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                                    UserRole.TEACHER -> Icons.Default.School
                                    UserRole.STUDENT -> Icons.Default.Person
                                    UserRole.PARENT -> Icons.Default.FamilyRestroom
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "Prof. S. Sengupta (HOD)" else currentUser.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "Head of Department • BCA Program" else "Enrollment: ${currentUser.enrollmentNo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Batch: ${currentUser.batchName} (Semester ${currentUser.currentSemester})",
                                style = MaterialTheme.typography.labelMedium,
                                color = AcademicPrimary
                            )
                        }

                        IconButton(onClick = { showLoginSwitchDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Account / Role", tint = AcademicPrimary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current CGPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%.2f", currentUser.cgpa),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AcademicPrimary
                            )
                        }
                        Column {
                            Text("MAKAUT %", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", MakautCalculator.cgpaToPercentage(currentUser.cgpa))}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                        }
                        Column {
                            Text("Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "87.5%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                        }
                        Column {
                            Text("Fee Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Verified",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                        }
                    }
                }
            }
        }

        // Academic Certificate & Credentials Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Semester Completion Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Digitally signed academic credential verified under MAKAUT guidelines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCertificateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimary)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View", fontSize = 12.sp)
                    }
                }
            }
        }

        // Notification Preferences Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automated Alerts & Push Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "FCM push reminders for classes, fees, assignments, and test schedules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Upcoming Class Reminders",
                        subtitle = "Alert 30 mins before theory and lab sessions",
                        checked = notifPrefs.scheduleEnabled,
                        onCheckedChange = { viewModel.updateNotificationPreferences(notifPrefs.copy(scheduleEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Fee Outstanding & Due-Date Reminders",
                        subtitle = "Alert 3 days and 1 day prior to monthly fee deadline",
                        checked = notifPrefs.feesEnabled,
                        onCheckedChange = { viewModel.updateNotificationPreferences(notifPrefs.copy(feesEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Assignment Deadlines & Reviews",
                        subtitle = "Alerts on new assignments and faculty marks reviews",
                        checked = notifPrefs.assignmentsEnabled,
                        onCheckedChange = { viewModel.updateNotificationPreferences(notifPrefs.copy(assignmentsEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Quizzes & Anti-Cheat Notices",
                        subtitle = "Notifications when mock tests and evaluations open",
                        checked = notifPrefs.quizzesEnabled,
                        onCheckedChange = { viewModel.updateNotificationPreferences(notifPrefs.copy(quizzesEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Daily Summary & Code Snippets",
                        subtitle = "Evening bulletin with programming syntax updates",
                        checked = notifPrefs.updatesEnabled,
                        onCheckedChange = { viewModel.updateNotificationPreferences(notifPrefs.copy(updatesEnabled = it)) }
                    )
                }
            }
        }

        // Language & Localization Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Language & Regional Localization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LanguageOptionCard(
                            label = "English (UK/IN)",
                            isSelected = appLanguage == AppLanguage.ENGLISH,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAppLanguage(AppLanguage.ENGLISH) }
                        )

                        LanguageOptionCard(
                            label = "हिंदी (Hindi)",
                            isSelected = appLanguage == AppLanguage.HINDI,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAppLanguage(AppLanguage.HINDI) }
                        )
                    }
                }
            }
        }

        // Course Feedback & Quality Evaluation
        item {
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
                        Column {
                            Text(
                                text = "Student Feedback & Rating",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rate teaching pedagogy, practical labs, and course delivery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(onClick = { showFeedbackDialog = true }) {
                            Text("Rate")
                        }
                    }
                }
            }
        }

        // Administrative Audit Trail (HOD / Compliance)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAuditLogs = !showAuditLogs },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Institutional Security & Audit Trail",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Immutable audit log of grade recordings, assignments, and payments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = if (showAuditLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AcademicPrimary
                        )
                    }

                    AnimatedVisibility(visible = showAuditLogs) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            auditLogs.take(5).forEach { entry ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = entry.action,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AcademicPrimary
                                            )
                                            Text(
                                                text = entry.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${entry.targetEntity} — ${entry.details}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Information & Policies
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Application & Regulatory Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyPolicy = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AcademicPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Data Privacy & FERPA/UGC Compliance", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("v1.0.0 (Build 2026.10-release)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Affiliation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("MAKAUT WBUT BCA Curriculum", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Academic Mentor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Prof. S. Sengupta (HOD)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Submit Course Feedback") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Subject: $feedbackSubject", fontWeight = FontWeight.Bold)

                    Text("Rating (1-5 stars):", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { feedbackRating = star }) {
                                Icon(
                                    imageVector = if (star <= feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= feedbackRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Your Review / Suggestion") },
                        placeholder = { Text("e.g. Practical lab exercises on SQL subqueries were clear...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            viewModel.submitFeedback(feedbackRating, feedbackSubject, feedbackText)
                            showFeedbackDialog = false
                            feedbackText = ""
                            feedbackSubmittedSnackbar = true
                        }
                    }
                ) {
                    Text("Submit Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Certificate Preview Modal
    if (showCertificateDialog) {
        AlertDialog(
            onDismissRequest = { showCertificateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Academic Certificate")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AcademicPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DEPARTMENT OF COMPUTER APPLICATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcademicPrimary
                    )
                    Text(
                        text = "CERTIFICATE OF ACADEMIC EXCELLENCE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This is to certify that",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = currentUser.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcademicPrimary
                    )
                    Text(
                        text = "Enrollment: ${currentUser.enrollmentNo} • Batch: ${currentUser.batchName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "has successfully attained a Cumulative Grade Point Average of ${String.format(Locale.US, "%.2f", currentUser.cgpa)} (${String.format(Locale.US, "%.1f", MakautCalculator.cgpaToPercentage(currentUser.cgpa))}%), classified as Grade 'O' (Outstanding) under MAKAUT statutory norms.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Issued on: ${SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())}\nMentor: Prof. S. Sengupta (HOD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCertificateDialog = false }) {
                    Text("Download PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCertificateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Privacy Policy Modal
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text("Privacy Policy & Data Security") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    item {
                        Text(
                            text = "BCA Tuition Portal is built strictly for pedagogical and academic administration under MAKAUT university guidelines.\n\n" +
                                    "1. Data Privacy: Student attendance, fees, marks, and personal contact details are stored securely. Role-based security rules prevent unauthorized access.\n\n" +
                                    "2. Payments: All fee payment transactions are conducted using industry-standard 256-bit SSL encryption via Razorpay order simulation.\n\n" +
                                    "3. Anti-Cheat Monitoring: The assessment engine monitors window focus loss and blur events solely during active test attempts to maintain academic integrity.\n\n" +
                                    "4. Contact Mentor: If you have queries, contact Prof. S. Sengupta directly via the in-app Doubt Clearing thread.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicy = false }) {
                    Text("I Understand")
                }
            }
        )
    }

    // Switch Account Modal
    if (showLoginSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showLoginSwitchDialog = false },
            title = { Text("Switch User Account / Role") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select identity mode for this session:", style = MaterialTheme.typography.bodySmall)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentRole == UserRole.STUDENT) AcademicPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setRole(UserRole.STUDENT)
                                showLoginSwitchDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AcademicPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Rahul Sharma (Student)", fontWeight = FontWeight.Bold)
                                Text("Enrollment: MAKAUT/BCA/2024/042", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentRole == UserRole.ADMIN) AcademicPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setRole(UserRole.ADMIN)
                                showLoginSwitchDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AcademicPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Prof. S. Sengupta (Faculty & HOD)", fontWeight = FontWeight.Bold)
                                Text("Full Administrative & Bulk Access", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentRole == UserRole.PARENT) AcademicPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setRole(UserRole.PARENT)
                                showLoginSwitchDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = AcademicPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Mr. R. Sharma (Parent/Guardian)", fontWeight = FontWeight.Bold)
                                Text("Read-only attendance, marks & fee oversight", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoginSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LanguageOptionCard(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AcademicPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AcademicPrimary) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) AcademicPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) AcademicPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
