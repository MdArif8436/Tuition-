package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentUser
import com.example.ui.theme.*

/**
 * Official Attendance Debarment Warning Notice Generator for HOD & Faculty.
 *
 * Implements:
 * - Formal MAKAUT university attendance warning notice formatting
 * - Calculation of classes needed to reach 75%
 * - One-tap copy to clipboard for WhatsApp/SMS communication
 */
@Composable
fun AttendanceNoticeDialog(
    student: StudentUser,
    attendancePercentage: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Calculation: Assuming 40 classes held, how many consecutive classes to reach 75%?
    val classesHeld = 40
    val attended = (attendancePercentage / 100.0 * classesHeld).toInt()
    // Target: (attended + x) / (classesHeld + x) >= 0.75
    // attended + x >= 0.75 * classesHeld + 0.75 * x
    // 0.25 * x >= 0.75 * classesHeld - attended
    // x >= (0.75 * classesHeld - attended) / 0.25
    val classesNeeded = ((0.75 * classesHeld - attended) / 0.25).toInt().coerceAtLeast(0)

    val formalNoticeText = """
OFFICIAL NOTICE: MAKAUT STATUTORY ATTENDANCE DEFICIT WARNING
Department of Computer Applications (BCA)

To: Guardian of ${student.name} (${student.enrollmentNo})
Batch: ${student.batchId} | Academic Session 2024-2027

Dear Parent / Guardian,
This is to formally notify that your ward's cumulative academic attendance stands at ${String.format("%.1f", attendancePercentage)}%, which is critically below the statutory minimum requirement of 75.0% mandated by MAKAUT regulations.

Consequence of Non-Compliance:
Students with attendance below 75% are liable to be DEBARRED from sitting for the upcoming Semester University Theory & Practical Examinations.

Action Required:
The student must attend the next $classesNeeded consecutive scheduled lectures and practical laboratory sessions without absence to restore compliance.

Issued by:
Prof. S. Sengupta (Head of Department)
Department of BCA & Computer Applications
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = BrandSurfaceCard, // #E8F2F6
            border = BorderStroke(1.5.dp, BrandTerracotta),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandTerracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFF4A1A12),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Formal Defaulter Notice",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOceanDeep
                            )
                            Text(
                                text = "${student.name} • ${String.format("%.1f", attendancePercentage)}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandOceanDeep)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats highlight
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWarmRose.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, BrandTerracotta.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Attendance", fontSize = 11.sp, color = BrandTextSecondary)
                            Text(
                                text = "${String.format("%.1f", attendancePercentage)}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(36.dp), color = BrandTerracotta)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Classes to Recover", fontSize = 11.sp, color = BrandTextSecondary)
                            Text(
                                text = "$classesNeeded consecutive",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOceanDeep
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Formal Letter Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandInputBackground,
                    border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formalNoticeText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BrandTextPrimary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Attendance Notice", formalNoticeText))
                            Toast.makeText(context, "Notice copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, BrandOceanDeep),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOceanDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Attendance Notice", formalNoticeText))
                            Toast.makeText(context, "Ready to send via WhatsApp / SMS", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Notice", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
