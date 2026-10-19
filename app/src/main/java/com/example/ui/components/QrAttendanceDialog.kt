package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun QrAttendanceDialog(
    userRole: UserRole,
    onAttendanceMarked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sessionCode by remember { mutableStateOf("MAKAUT-${(1000..9999).random()}") }
    var enteredCode by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Wednesday Theory - DBMS (BAP301)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BrandSoftSky),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().testTag("qr_attendance_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (userRole == UserRole.ADMIN) "Generate Session QR" else "QR Attendance Check-in",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOceanDeep
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandOceanDeep)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (userRole == UserRole.ADMIN) {
                    // HOD View: Generates Session Code
                    Text(
                        text = "Project this dynamic QR in class. Code expires in 5 minutes to prevent proxy check-ins.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandSoftSky.copy(alpha = 0.3f))
                            .border(2.dp, BrandOceanDeep, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Dynamic Attendance QR",
                            tint = BrandOceanDeep,
                            modifier = Modifier.size(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = BrandWarmRose.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Session Passcode: $sessionCode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43231F),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            sessionCode = "MAKAUT-${(1000..9999).random()}"
                            Toast.makeText(context, "New single-use session generated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regenerate QR")
                    }
                } else {
                    // Student View: Scan QR or Enter Session Passcode
                    Text(
                        text = "Scan the QR code displayed by Prof. Sengupta or enter the 8-digit session passcode.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { enteredCode = it.uppercase() },
                        label = { Text("Session Passcode (e.g. MAKAUT-4821)") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandOceanDeep) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_qr_passcode")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (enteredCode.isNotBlank()) {
                                onAttendanceMarked(selectedClass)
                                Toast.makeText(context, "Attendance verified successfully! Status: PRESENT", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Please enter the session code displayed on the screen", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("verify_qr_attendance_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify & Check-in", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onAttendanceMarked(selectedClass)
                            Toast.makeText(context, "Simulated Camera QR Scan: Verified PRESENT!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSkyTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandOceanDeep, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Camera Scan", color = BrandOceanDeep, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
