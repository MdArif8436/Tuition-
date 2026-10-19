package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyTopic
import com.example.data.model.TopicStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun SyllabusScreen(
    viewModel: TuitionViewModel
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val studyTopics by viewModel.studyTopics.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTopics = studyTopics.filter { topic ->
        when (selectedFilter) {
            "ONGOING" -> topic.status == TopicStatus.ONGOING
            "UPCOMING" -> topic.status == TopicStatus.UPCOMING
            "COMPLETED" -> topic.status == TopicStatus.COMPLETED
            "PRACTICAL" -> topic.isPractical
            else -> true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("syllabus_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header card
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
                                text = "MAKAUT BCA Syllabus Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choice Based Credit System (CBCS) • Theory & Labs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentRole == UserRole.ADMIN) {
                            FilledTonalButton(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Topic")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "ONGOING", "UPCOMING", "COMPLETED", "PRACTICAL").forEach { f ->
                            FilterChip(
                                selected = selectedFilter == f,
                                onClick = { selectedFilter = f },
                                label = { Text(f) }
                            )
                        }
                    }
                }
            }
        }

        // Topics list
        items(filteredTopics) { topic ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (topic.isPractical) AcademicSecondary.copy(alpha = 0.15f)
                                        else AcademicPrimary.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (topic.isPractical) Icons.Default.Computer else Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (topic.isPractical) AcademicSecondary else AcademicPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = topic.subject,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = topic.makautUnit,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (topic.status) {
                                TopicStatus.ONGOING -> AcademicPrimary.copy(alpha = 0.15f)
                                TopicStatus.UPCOMING -> AcademicTertiary.copy(alpha = 0.15f)
                                TopicStatus.COMPLETED -> StatusPaid.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = topic.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (topic.status) {
                                    TopicStatus.ONGOING -> AcademicPrimary
                                    TopicStatus.UPCOMING -> AcademicTertiary
                                    TopicStatus.COMPLETED -> StatusPaid
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = topic.description,
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
                            text = "Timeline: ${topic.startDate} → ${topic.expectedCompletionDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(topic.referenceNotesUrl))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lecture Notes")
                        }
                    }

                    if (currentRole == UserRole.ADMIN) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Change Status: ", style = MaterialTheme.typography.bodySmall)
                            TopicStatus.values().forEach { st ->
                                if (st != topic.status) {
                                    OutlinedButton(
                                        onClick = { viewModel.updateTopicStatus(topic.id, st) },
                                        modifier = Modifier.padding(start = 4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(st.name, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSubj by remember { mutableStateOf("DBMS (BAP301)") }
        var newDesc by remember { mutableStateOf("") }
        var newUnit by remember { mutableStateOf("Unit 4 - Advanced Topics") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Study Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubj,
                        onValueChange = { newSubj = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUnit,
                        onValueChange = { newUnit = it },
                        label = { Text("MAKAUT Unit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTitle.isNotBlank()) {
                        viewModel.addStudyTopic(
                            StudyTopic(
                                batchId = "batch-2",
                                subject = newSubj,
                                title = newTitle,
                                description = newDesc,
                                status = TopicStatus.UPCOMING,
                                startDate = "2026-10-20",
                                expectedCompletionDate = "2026-10-30",
                                makautUnit = newUnit
                            )
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Add Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
