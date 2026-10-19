package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.MakautCalculator
import java.util.Locale

/**
 * Interactive MAKAUT (WBUT) Statutory CGPA & SGPA Target Planner.
 *
 * Implements:
 * - Official MAKAUT conversion: Percentage = (CGPA - 0.75) * 10
 * - Placement Cutoff Calculator (>=6.75 CGPA = 60.00% corporate campus threshold)
 * - Remaining Semesters SGPA Target Planner
 * - Styled with the balanced brand palette
 */
@Composable
fun MakautCgpaCalculatorDialog(
    onDismiss: () -> Unit
) {
    var currentSemester by remember { mutableIntStateOf(3) }
    var currentCgpaText by remember { mutableStateOf("7.80") }
    var targetCgpaText by remember { mutableStateOf("8.50") }

    val currentCgpa = currentCgpaText.toDoubleOrNull() ?: 0.0
    val targetCgpa = targetCgpaText.toDoubleOrNull() ?: 0.0

    val currentPercentage = MakautCalculator.cgpaToPercentage(currentCgpa)
    val targetPercentage = MakautCalculator.cgpaToPercentage(targetCgpa)

    val (currentGrade, currentGradeTitle) = MakautCalculator.getGradeLetter(currentCgpa)

    // Calculate required average SGPA in remaining semesters
    val totalSemesters = 6
    val completedSemesters = currentSemester - 1
    val remainingSemesters = (totalSemesters - completedSemesters).coerceAtLeast(1)

    val totalPointsNeeded = targetCgpa * totalSemesters
    val currentPoints = currentCgpa * completedSemesters
    val remainingPointsNeeded = totalPointsNeeded - currentPoints
    val requiredSgpa = if (remainingSemesters > 0) {
        remainingPointsNeeded / remainingSemesters
    } else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = BrandSurfaceCard, // Balanced non-white surface #E8F2F6
            border = BorderStroke(1.5.dp, BrandSkyTeal.copy(alpha = 0.5f)),
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
                                .background(BrandOceanDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAKAUT CGPA Target",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOceanDeep
                            )
                            Text(
                                text = "Statutory Regulation (CGPA - 0.75) * 10",
                                fontSize = 11.sp,
                                color = BrandTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandOceanDeep)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Semester selector
                Text(
                    text = "Current Semester: Sem $currentSemester",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..6).forEach { sem ->
                        val isSelected = currentSemester == sem
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentSemester = sem },
                            label = { Text("S$sem", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandOceanDeep,
                                selectedLabelColor = Color.White,
                                containerColor = BrandMutedCanvas,
                                labelColor = BrandOceanDeep
                            ),
                            border = if (isSelected) null else BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Row: Current CGPA & Target CGPA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentCgpaText,
                        onValueChange = { currentCgpaText = it },
                        label = { Text("Current CGPA", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_current_cgpa"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOceanDeep,
                            unfocusedBorderColor = BrandSkyTeal.copy(alpha = 0.6f),
                            focusedContainerColor = BrandInputBackground,
                            unfocusedContainerColor = BrandInputBackground
                        )
                    )

                    OutlinedTextField(
                        value = targetCgpaText,
                        onValueChange = { targetCgpaText = it },
                        label = { Text("Target CGPA", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_target_cgpa"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOceanDeep,
                            unfocusedBorderColor = BrandSkyTeal.copy(alpha = 0.6f),
                            focusedContainerColor = BrandInputBackground,
                            unfocusedContainerColor = BrandInputBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Conversion Stats Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSoftSky.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Equivalent Percentage", fontSize = 11.sp, color = BrandTextSecondary)
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", currentPercentage)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOceanDeep
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MAKAUT Grade", fontSize = 11.sp, color = BrandTextSecondary)
                                Text(
                                    text = "$currentGrade ($currentGradeTitle)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTextPrimary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = BrandSkyTeal.copy(alpha = 0.35f)
                        )

                        Text(
                            text = MakautCalculator.getPlacementEligibility(currentCgpa),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Required SGPA Target Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (requiredSgpa <= 10.0) BrandWarmRose.copy(alpha = 0.45f) else Color(0xFFFFEAEA)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (requiredSgpa <= 10.0) BrandTerracotta else MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Target Roadmap ($remainingSemesters remaining ${if (remainingSemesters == 1) "semester" else "semesters"})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (requiredSgpa <= 0.0) {
                            Text(
                                text = "Target already achieved with current performance!",
                                fontSize = 13.sp,
                                color = StatusPaid,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (requiredSgpa <= 10.0) {
                            Text(
                                text = "Need average ${String.format(Locale.US, "%.2f", requiredSgpa)} SGPA per remaining semester to graduate with ${String.format(Locale.US, "%.2f", targetCgpa)} CGPA (${String.format(Locale.US, "%.1f", targetPercentage)}%).",
                                fontSize = 12.sp,
                                color = BrandTextPrimary
                            )
                        } else {
                            Text(
                                text = "Mathematically unreachable (>10.0 SGPA required). Target a realistic CGPA of ${(currentPoints + remainingSemesters * 10.0) / totalSemesters} max.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
