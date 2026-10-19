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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudentUser
import com.example.ui.theme.*

@Composable
fun DigitalIdCardDialog(
    student: StudentUser,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("digital_id_card_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with institutional gradient using requested palette #579FB6 and #77B7D0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(BrandOceanDeep, BrandSkyTeal)
                            )
                        )
                        .padding(vertical = 16.dp, horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MAKAUT AFFILIATED INSTITUTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSoftSky,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "DEPARTMENT OF COMPUTER APPLICATIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "BCA STUDENT IDENTIFICATION CARD",
                            fontSize = 10.sp,
                            color = BrandSoftSky.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar with border
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(BrandSoftSky.copy(alpha = 0.5f))
                        .border(3.dp, BrandOceanDeep, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student Photo",
                        tint = BrandOceanDeep,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = student.name,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandOceanDeep
                )
                Text(
                    text = student.batchName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Table
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSoftSky.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, BrandSoftSky)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IdFieldRow("Enrollment No.", student.enrollmentNo)
                        IdFieldRow("University Roll", "MAKAUT/ROLL/${student.enrollmentNo.takeLast(5)}")
                        IdFieldRow("Mobile Contact", student.contactNo)
                        IdFieldRow("Academic Year", "2024 - 2027 (Sem ${student.currentSemester})")
                        IdFieldRow("Card Status", "ACTIVE / VERIFIED", highlightColor = StatusPresent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verification QR Simulation
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, BrandSkyTeal, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Verification QR",
                        tint = BrandOceanDeep,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Text(
                    text = "Scan to verify departmental enrollment",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Download & Close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSkyTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = BrandOceanDeep, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "ID Card PDF saved to Downloads folder", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdFieldRow(label: String, value: String, highlightColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
