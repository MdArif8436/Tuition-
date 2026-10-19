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
import com.example.data.model.NotificationType
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun NotificationsScreen(
    viewModel: TuitionViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("notifications_screen"),
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
                                text = "Automated Notification Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time alerts for scheduled classes & pending tuition dues",
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
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = AcademicPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.triggerAutomatedClassReminder() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simulate Class Alert", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerAutomatedFeeDueNotice() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simulate Fee Alert", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent Notifications (${notifications.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(notifications) { notif ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.markNotificationRead(notif.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(if (!notif.isRead) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when (notif.type) {
                                    NotificationType.UPCOMING_CLASS -> AcademicPrimary.copy(alpha = 0.15f)
                                    NotificationType.OUTSTANDING_BALANCE -> StatusPending.copy(alpha = 0.15f)
                                    NotificationType.ASSIGNMENT_DEADLINE -> AcademicTertiary.copy(alpha = 0.15f)
                                    NotificationType.ANNOUNCEMENT -> AcademicSecondary.copy(alpha = 0.15f)
                                    else -> AcademicPrimary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (notif.type) {
                                NotificationType.UPCOMING_CLASS -> Icons.Default.School
                                NotificationType.OUTSTANDING_BALANCE -> Icons.Default.AccountBalanceWallet
                                NotificationType.ASSIGNMENT_DEADLINE -> Icons.Default.Alarm
                                NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = when (notif.type) {
                                NotificationType.UPCOMING_CLASS -> AcademicPrimary
                                NotificationType.OUTSTANDING_BALANCE -> StatusPending
                                NotificationType.ASSIGNMENT_DEADLINE -> AcademicTertiary
                                NotificationType.ANNOUNCEMENT -> AcademicSecondary
                                else -> AcademicPrimary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold
                            )
                            Text(
                                text = notif.timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (notif.actionText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    when (notif.type) {
                                        NotificationType.UPCOMING_CLASS -> onNavigateToTab(4) // Calendar
                                        NotificationType.OUTSTANDING_BALANCE -> onNavigateToTab(2) // Fees
                                        NotificationType.ASSIGNMENT_DEADLINE -> onNavigateToTab(6) // Assignments
                                        NotificationType.ANNOUNCEMENT -> onNavigateToTab(7) // Daily Updates
                                        else -> onNavigateToTab(0)
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "${notif.actionText} →",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
