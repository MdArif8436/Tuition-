package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.ScheduleEvent
import com.example.data.model.ScheduleType
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun CalendarScreen(
    viewModel: TuitionViewModel
) {
    val schedules by viewModel.schedules.collectAsState()
    var selectedDay by remember { mutableStateOf("Monday") }
    var showNotifBroadcasted by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val filteredSchedules = schedules.filter { it.dayOfWeek == selectedDay }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("calendar_screen"),
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
                                text = "BCA Lecture & Lab Calendar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automated class reminders and lab slot tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AcademicPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AcademicPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { day ->
                            val isSelected = selectedDay == day
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AcademicPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedDay = day }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.triggerAutomatedClassReminder()
                            showNotifBroadcasted = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showNotifBroadcasted) "Automated Class Alert Dispatched ✓" else "Trigger Automated Upcoming Class Alert")
                    }
                }
            }
        }

        item {
            Text(
                text = "Schedule for $selectedDay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredSchedules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No scheduled sessions on $selectedDay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSchedules) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (event.type) {
                                        ScheduleType.LAB -> AcademicSecondary
                                        ScheduleType.TEST -> AcademicTertiary
                                        ScheduleType.LECTURE -> AcademicPrimary
                                        ScheduleType.HOLIDAY -> StatusPaid
                                        ScheduleType.SEMINAR -> AcademicSecondary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (event.type) {
                                    ScheduleType.LAB -> Icons.Default.Computer
                                    ScheduleType.TEST -> Icons.Default.Quiz
                                    ScheduleType.LECTURE -> Icons.Default.MenuBook
                                    ScheduleType.HOLIDAY -> Icons.Default.Celebration
                                    ScheduleType.SEMINAR -> Icons.Default.Groups
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.subject,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${event.timeSlot} • ${event.roomOrLab}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Faculty: ${event.instructor}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (event.type) {
                                ScheduleType.LAB -> AcademicSecondary.copy(alpha = 0.15f)
                                ScheduleType.TEST -> AcademicTertiary.copy(alpha = 0.15f)
                                ScheduleType.LECTURE -> AcademicPrimary.copy(alpha = 0.15f)
                                ScheduleType.HOLIDAY -> StatusPaid.copy(alpha = 0.15f)
                                ScheduleType.SEMINAR -> AcademicSecondary.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = event.type.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (event.type) {
                                    ScheduleType.LAB -> AcademicSecondary
                                    ScheduleType.TEST -> AcademicTertiary
                                    ScheduleType.LECTURE -> AcademicPrimary
                                    ScheduleType.HOLIDAY -> StatusPaid
                                    ScheduleType.SEMINAR -> AcademicSecondary
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
