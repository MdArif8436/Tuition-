package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import com.example.util.MakautCalculator

@Composable
fun FeesScreen(
    viewModel: TuitionViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val feeRecords by viewModel.feeRecords.collectAsState()

    var showReminderSentAlert by remember { mutableStateOf(false) }

    val myRecords = if (currentRole == UserRole.ADMIN) {
        feeRecords
    } else {
        feeRecords.filter { it.studentId == currentUser.id }
    }

    val totalDues = myRecords.sumOf { it.pendingBalance }
    val totalPaid = myRecords.sumOf { it.paidAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("fees_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Financial Overview Card
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
                                text = "Tuition & Assessment Accounts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Razorpay Gateway Integration • Secure Transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AcademicPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = AcademicPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Total Paid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = MakautCalculator.formatCurrency(totalPaid),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaid
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Outstanding Balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = MakautCalculator.formatCurrency(totalDues),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDues > 0) StatusPending else StatusPaid
                                )
                            }
                        }
                    }

                    if (currentRole == UserRole.ADMIN) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerAutomatedFeeDueNotice()
                                showReminderSentAlert = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showReminderSentAlert) "Reminders Dispatched to Defaulters ✓" else "Broadcast Automated Fee Reminders")
                        }
                    }
                }
            }
        }

        // Fee Records List
        item {
            Text(
                text = if (currentRole == UserRole.ADMIN) "Batch Fee Invoices & Receipts" else "My Tuition Fee Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(myRecords) { record ->
            FeeRecordItem(
                record = record,
                isStudent = currentRole != UserRole.ADMIN,
                onPayClick = { viewModel.openPaymentCheckout(record) }
            )
        }
    }
}

@Composable
private fun FeeRecordItem(
    record: FeeRecord,
    isStudent: Boolean,
    onPayClick: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.feeMonth,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.studentName} (${record.enrollmentNo})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (record.status) {
                        FeeStatus.PAID -> StatusPaid.copy(alpha = 0.15f)
                        FeeStatus.PENDING -> StatusPending.copy(alpha = 0.15f)
                        FeeStatus.PARTIAL -> StatusLate.copy(alpha = 0.15f)
                        FeeStatus.OVERDUE -> StatusOverdue.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = record.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (record.status) {
                            FeeStatus.PAID -> StatusPaid
                            FeeStatus.PENDING -> StatusPending
                            FeeStatus.PARTIAL -> StatusLate
                            FeeStatus.OVERDUE -> StatusOverdue
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = MakautCalculator.formatCurrency(record.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Paid Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = MakautCalculator.formatCurrency(record.paidAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusPaid
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pending Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = MakautCalculator.formatCurrency(record.pendingBalance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (record.pendingBalance > 0) StatusPending else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Transactions History
            if (record.transactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                record.transactions.forEach { txn ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${txn.paymentMethod} • ${txn.timestamp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = MakautCalculator.formatCurrency(txn.amount),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    }
                }
            }

            // Action Pay button
            if (record.pendingBalance > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicPrimary)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay ${MakautCalculator.formatCurrency(record.pendingBalance)} via Razorpay")
                }
            }
        }
    }
}
