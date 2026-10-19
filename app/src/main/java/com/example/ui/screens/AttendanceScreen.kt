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
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val students by viewModel.students.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val attendancePct by viewModel.currentStudentAttendancePercentage.collectAsState()

    var selectedSubject by remember { mutableStateOf("DBMS (BAP301)") }
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    var markingDate by remember { mutableStateOf(todayDateStr) }

    // Map for HOD bulk marking: studentId -> AttendanceStatus
    var bulkStatusMap by remember(students) {
        mutableStateOf(students.associate { it.id to AttendanceStatus.PRESENT })
    }
    var scheduleFilter by remember { mutableStateOf("ALL") } // "ALL", "WED_THEORY", "SAT_LAB"
    var showExportDialog by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val filteredRecords = attendanceRecords.filter { record ->
        when (scheduleFilter) {
            "WED_THEORY" -> record.subject.contains("DBMS", ignoreCase = true) || record.subject.contains("OS", ignoreCase = true)
            "SAT_LAB" -> record.subject.contains("Lab", ignoreCase = true) || record.subject.contains("Practical", ignoreCase = true)
            else -> true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("attendance_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Attendance Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MAKAUT Attendance Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Twice-weekly tracking • Statutory 75% cutoff required",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(CircleShape)
                                    .background(AcademicPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export Attendance CSV/PDF",
                                    tint = AcademicPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (attendancePct >= 75.0) StatusPresent.copy(alpha = 0.15f) else StatusAbsent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${attendancePct.toInt()}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attendancePct >= 75.0) StatusPresent else StatusAbsent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Twice-weekly filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = scheduleFilter == "ALL",
                            onClick = { scheduleFilter = "ALL" },
                            label = { Text("All Sessions", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = scheduleFilter == "WED_THEORY",
                            onClick = { scheduleFilter = "WED_THEORY" },
                            label = { Text("Wed (Theory)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = scheduleFilter == "SAT_LAB",
                            onClick = { scheduleFilter = "SAT_LAB" },
                            label = { Text("Sat (Practical Lab)", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (attendancePct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (attendancePct >= 75.0) "✓ Compliant with Exam Cutoff" else "⚠️ Deficit: Shortage of attendance",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (attendancePct >= 75.0) StatusPresent else StatusAbsent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Mandatory: 75%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (currentRole == UserRole.ADMIN) {
            // HOD Bulk Attendance Marking Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HOD Rapid Batch Attendance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mark presence or absence for the entire batch with one tap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subject chips
                        val subjects = listOf("DBMS (BAP301)", "Data Structures (BAP302)", "OOP Java (BAP303)", "Python Lab (BAP391)")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjects.take(2).forEach { sub ->
                                FilterChip(
                                    selected = selectedSubject == sub,
                                    onClick = { selectedSubject = sub },
                                    label = { Text(sub.split(" ")[0]) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Roster: BCA 2nd Year (${students.size} Students)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        students.forEach { student ->
                            val currentStatus = bulkStatusMap[student.id] ?: AttendanceStatus.PRESENT
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = student.enrollmentNo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Toggle Buttons P / A / L
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        StatusToggleButton(
                                            label = "P",
                                            isSelected = currentStatus == AttendanceStatus.PRESENT,
                                            selectedColor = StatusPresent,
                                            onClick = {
                                                bulkStatusMap = bulkStatusMap + (student.id to AttendanceStatus.PRESENT)
                                            }
                                        )
                                        StatusToggleButton(
                                            label = "A",
                                            isSelected = currentStatus == AttendanceStatus.ABSENT,
                                            selectedColor = StatusAbsent,
                                            onClick = {
                                                bulkStatusMap = bulkStatusMap + (student.id to AttendanceStatus.ABSENT)
                                            }
                                        )
                                        StatusToggleButton(
                                            label = "L",
                                            isSelected = currentStatus == AttendanceStatus.LATE,
                                            selectedColor = StatusLate,
                                            onClick = {
                                                bulkStatusMap = bulkStatusMap + (student.id to AttendanceStatus.LATE)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.markBatchAttendance(
                                    subject = selectedSubject,
                                    date = markingDate,
                                    statusMap = bulkStatusMap
                                )
                                showSavedSnackbar = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_attendance_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showSavedSnackbar) "Attendance Saved Successfully ✓" else "Save Batch Attendance Record")
                        }
                    }
                }
            }
        }

        // Attendance History List
        item {
            Text(
                text = "Recent Attendance Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val myRecords = if (currentRole == UserRole.ADMIN) {
            filteredRecords
        } else {
            filteredRecords.filter { it.studentId == currentUser.id }
        }

        if (myRecords.isEmpty()) {
            item {
                Text(
                    text = "No attendance logs found for current session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(myRecords) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (record.status) {
                                            AttendanceStatus.PRESENT -> StatusPresent.copy(alpha = 0.15f)
                                            AttendanceStatus.ABSENT -> StatusAbsent.copy(alpha = 0.15f)
                                            AttendanceStatus.LATE -> StatusLate.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (record.status) {
                                        AttendanceStatus.PRESENT -> Icons.Default.Check
                                        AttendanceStatus.ABSENT -> Icons.Default.Close
                                        AttendanceStatus.LATE -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                    tint = when (record.status) {
                                        AttendanceStatus.PRESENT -> StatusPresent
                                        AttendanceStatus.ABSENT -> StatusAbsent
                                        AttendanceStatus.LATE -> StatusLate
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = record.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (currentRole == UserRole.ADMIN) "${record.studentName} • ${record.date}" else record.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (record.remarks.isNotBlank()) {
                                    Text(
                                        text = "Note: ${record.remarks}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (record.status) {
                                AttendanceStatus.PRESENT -> StatusPresent.copy(alpha = 0.15f)
                                AttendanceStatus.ABSENT -> StatusAbsent.copy(alpha = 0.15f)
                                AttendanceStatus.LATE -> StatusLate.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = record.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (record.status) {
                                    AttendanceStatus.PRESENT -> StatusPresent
                                    AttendanceStatus.ABSENT -> StatusAbsent
                                    AttendanceStatus.LATE -> StatusLate
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        val exportText = viewModel.exportAttendanceCsv()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = AcademicPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Attendance Data")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Format: Comma-Separated Values (CSV / Spreadsheet)", fontWeight = FontWeight.Bold)
                    Text("Compliant with MAKAUT attendance auditing regulations.", style = MaterialTheme.typography.bodySmall)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        Text(
                            text = exportText.take(280) + if (exportText.length > 280) "\n..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showExportDialog = false }) {
                    Text("Download CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun StatusToggleButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
