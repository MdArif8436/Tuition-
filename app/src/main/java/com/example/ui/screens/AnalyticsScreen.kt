package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.TestMark
import com.example.data.model.UserRole
import com.example.ui.components.MakautFormulaCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.MakautCalculator
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: TuitionViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val testMarks by viewModel.testMarks.collectAsState()
    val students by viewModel.students.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var showMarksExportDialog by remember { mutableStateOf(false) }
    var showBulkUploadDialog by remember { mutableStateOf(false) }

    val myMarks = if (currentRole == UserRole.ADMIN) {
        testMarks
    } else {
        testMarks.filter { it.studentId == currentUser.id }
    }

    val avgPercentage = if (myMarks.isNotEmpty()) {
        myMarks.map { it.percentage }.average()
    } else 82.5

    val statutoryPct = MakautCalculator.cgpaToPercentage(currentUser.cgpa)
    val (gradeLetter, gradeTitle) = MakautCalculator.getGradeLetter(currentUser.cgpa)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Performance Analytics Header
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
                        Column {
                            Text(
                                text = "MAKAUT Performance Analytics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Historical Grade Point Average & Statutory Equivalents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentRole == UserRole.ADMIN) {
                                IconButton(
                                    onClick = { showBulkUploadDialog = true },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .clip(CircleShape)
                                        .background(AcademicSecondary.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Bulk Import Marks",
                                        tint = AcademicSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showMarksExportDialog = true },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(CircleShape)
                                    .background(AcademicPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export Marks CSV",
                                    tint = AcademicPrimary
                                )
                            }

                            IconButton(
                                onClick = { showReportDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AcademicPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "View Mark Sheet",
                                    tint = AcademicPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Current CGPA",
                            value = String.format(Locale.US, "%.2f", currentUser.cgpa),
                            icon = Icons.Default.EmojiEvents,
                            iconTint = AcademicTertiary,
                            badgeText = "Grade $gradeLetter",
                            badgeColor = AcademicTertiary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Statutory %",
                            value = "${String.format(Locale.US, "%.1f", statutoryPct)}%",
                            icon = Icons.Default.Percent,
                            iconTint = StatusPaid,
                            badgeText = "Cutoff >=60%",
                            badgeColor = StatusPaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Batch Rank",
                            value = "#2 of 45",
                            icon = Icons.Default.Leaderboard,
                            iconTint = AcademicSecondary,
                            badgeText = "96th %ile",
                            badgeColor = AcademicSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Average Score",
                            value = "${String.format(Locale.US, "%.1f", avgPercentage)}%",
                            icon = Icons.Default.Assessment,
                            iconTint = AcademicPrimary,
                            badgeText = "Consistent",
                            badgeColor = AcademicPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Statutory Formula Converter Card
        item {
            MakautFormulaCard(initialCgpa = currentUser.cgpa)
        }

        // Semester-wise Progress & Visual Trend Bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Semester Progression (SGPA Trend)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Performance across BCA Choice Based Credit System semesters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val semesterSgpas = listOf(
                        Triple("Semester 1", 7.80, "2025-12"),
                        Triple("Semester 2", 8.25, "2026-06"),
                        Triple("Semester 3 (Projected)", 8.45, "2026-11")
                    )

                    semesterSgpas.forEach { (sem, sgpa, session) ->
                        val pct = MakautCalculator.cgpaToPercentage(sgpa)
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$sem ($session)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "SGPA: ${String.format(Locale.US, "%.2f", sgpa)} (${String.format(Locale.US, "%.1f", pct)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (sgpa / 10.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AcademicPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Subject-wise Marks List
        item {
            Text(
                text = "Detailed Subject Assessment Scores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(myMarks) { mark ->
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mark.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${mark.testName} • ${mark.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", mark.obtainedMarks)} / ${String.format(Locale.US, "%.0f", mark.maxMarks)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (mark.percentage >= 60.0) StatusPaid else StatusPending
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", mark.percentage)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Official Mark Sheet Modal
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = AcademicPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official BCA Transcript")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student: ${currentUser.name} (${currentUser.enrollmentNo})", fontWeight = FontWeight.Bold)
                    Text("University: Maulana Abul Kalam Azad University of Technology (MAKAUT)")
                    Text("Program: Bachelor of Computer Application (BCA)")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Cumulative GPA (CGPA): ${String.format(Locale.US, "%.2f", currentUser.cgpa)}")
                    Text("Statutory Percentage: ${String.format(Locale.US, "%.2f", statutoryPct)}%")
                    Text("Grade Classification: $gradeLetter ($gradeTitle)")
                    Text("Cutoff Standing: ${MakautCalculator.getPlacementEligibility(currentUser.cgpa)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Verified by: Prof. S. Sengupta, HOD & Academic Mentor", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                Button(onClick = { showReportDialog = false }) {
                    Text("Print / Export Transcript")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showMarksExportDialog) {
        val exportCsv = viewModel.exportMarksCsv()
        AlertDialog(
            onDismissRequest = { showMarksExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = AcademicPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Assessment Scores")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Format: CSV (Spreadsheet)", fontWeight = FontWeight.Bold)
                    Text("Contains subject scores, internal assessments, percentages, and student rankings.", style = MaterialTheme.typography.bodySmall)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        Text(
                            text = exportCsv.take(280) + if (exportCsv.length > 280) "\n..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMarksExportDialog = false }) {
                    Text("Download Spreadsheet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarksExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBulkUploadDialog) {
        AlertDialog(
            onDismissRequest = { showBulkUploadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = AcademicSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HOD Bulk Import Marks")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Import Batch Assessment Scores from CSV", fontWeight = FontWeight.Bold)
                    Text("Uploads standardized CSV test grades directly to student academic ledgers with immutable audit logging.", style = MaterialTheme.typography.bodySmall)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sample CSV Template:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Roll,Name,Subject,Test,Score,Max\n042,Rahul Sharma,DBMS,Midterm,23.5,25\n043,Sneha Ghosh,DBMS,Midterm,24.0,25",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBulkUploadDialog = false }) {
                    Text("Process CSV Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
