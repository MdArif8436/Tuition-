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
import com.example.data.model.Assignment
import com.example.data.model.AssignmentStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun AssignmentsScreen(
    viewModel: TuitionViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val assignments by viewModel.assignments.collectAsState()

    var activeSubmissionAssignment by remember { mutableStateOf<Assignment?>(null) }
    var activeGradingAssignment by remember { mutableStateOf<Assignment?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("assignments_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
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
                                text = "BCA Practical & Theory Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Submit code, SQL scripts, and practical lab assignments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AcademicSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = AcademicSecondary)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Coursework Submissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(assignments) { assignment ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcademicPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = assignment.subject,
                                style = MaterialTheme.typography.labelSmall,
                                color = AcademicPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (assignment.submissionStatus) {
                                AssignmentStatus.NOT_SUBMITTED, AssignmentStatus.PENDING -> StatusPending.copy(alpha = 0.15f)
                                AssignmentStatus.SUBMITTED -> AcademicSecondary.copy(alpha = 0.15f)
                                AssignmentStatus.UNDER_REVIEW -> AcademicTertiary.copy(alpha = 0.15f)
                                AssignmentStatus.ACCEPTED, AssignmentStatus.GRADED -> StatusPaid.copy(alpha = 0.15f)
                                AssignmentStatus.NEEDS_REVISION -> StatusAbsent.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = assignment.submissionStatus.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (assignment.submissionStatus) {
                                    AssignmentStatus.NOT_SUBMITTED, AssignmentStatus.PENDING -> StatusPending
                                    AssignmentStatus.SUBMITTED -> AcademicSecondary
                                    AssignmentStatus.UNDER_REVIEW -> AcademicTertiary
                                    AssignmentStatus.ACCEPTED, AssignmentStatus.GRADED -> StatusPaid
                                    AssignmentStatus.NEEDS_REVISION -> StatusAbsent
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = assignment.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due: ${assignment.dueDate} • Max: ${assignment.maxMarks} Marks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (assignment.obtainedMarks != null) {
                            Text(
                                text = "Score: ${assignment.obtainedMarks} / ${assignment.maxMarks}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                        }
                    }

                    if (assignment.teacherFeedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Faculty Feedback:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = assignment.teacherFeedback,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentRole == UserRole.ADMIN) {
                        Button(
                            onClick = { activeGradingAssignment = assignment },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grade & Provide Feedback")
                        }
                    } else {
                        if (assignment.submissionStatus == AssignmentStatus.NOT_SUBMITTED) {
                            Button(
                                onClick = { activeSubmissionAssignment = assignment },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Solution / Code")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { activeSubmissionAssignment = assignment },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View My Submission")
                            }
                        }
                    }
                }
            }
        }
    }

    // Student Submit Dialog
    if (activeSubmissionAssignment != null) {
        val asg = activeSubmissionAssignment!!
        var submissionText by remember { mutableStateOf(asg.studentSubmissionText ?: "") }

        AlertDialog(
            onDismissRequest = { activeSubmissionAssignment = null },
            title = { Text(asg.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your SQL script, C/Java code, or explanation:")
                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = { submissionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("// Paste source code or answer here...") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.submitAssignment(asg.id, submissionText)
                    activeSubmissionAssignment = null
                }) {
                    Text("Submit Solution")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeSubmissionAssignment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Teacher Grade Dialog
    if (activeGradingAssignment != null) {
        val asg = activeGradingAssignment!!
        var marksText by remember { mutableStateOf((asg.obtainedMarks ?: 25).toString()) }
        var feedbackText by remember { mutableStateOf(asg.teacherFeedback ?: "Good logic and proper normalization schema.") }

        AlertDialog(
            onDismissRequest = { activeGradingAssignment = null },
            title = { Text("Grade: ${asg.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = marksText,
                        onValueChange = { marksText = it },
                        label = { Text("Awarded Marks (Max ${asg.maxMarks})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Faculty Evaluation Feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val m = marksText.toIntOrNull() ?: asg.maxMarks
                    viewModel.gradeAssignment(asg.id, m, feedbackText)
                    activeGradingAssignment = null
                }) {
                    Text("Save Grade")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeGradingAssignment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
