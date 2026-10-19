package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthenticationViewModel
import com.example.ui.viewmodel.TuitionViewModel

/**
 * Clean, multi-step Registration & Profile Setup Flow.
 *
 * Steps:
 * 1. Role Selection Screen (Student, Parent, HOD/Teacher)
 * 2. Role-Specific Profile Details:
 *    - Student: Roll Number validation, Course, Batch, Semester, Mobile
 *    - Parent: Relationship, Child Roll Number, Verification Code linking
 *    - HOD/Teacher: Secure Invite Code, Designation, Subjects & Batches
 * 3. Completion & Transition to Role Dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFlowScreen(
    authViewModel: AuthenticationViewModel,
    tuitionViewModel: TuitionViewModel,
    onRegistrationComplete: () -> Unit
) {
    val context = LocalContext.current
    val currentStudents by tuitionViewModel.students.collectAsState()

    var currentStep by remember { mutableIntStateOf(1) } // 1: Role, 2: Details
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    // Student form state
    var studentName by remember { mutableStateOf("Rahul Sharma") }
    var rollNumber by remember { mutableStateOf("") }
    var studentCourse by remember { mutableStateOf("BCA") }
    var studentBatch by remember { mutableStateOf("BCA 2024–27, Section A") }
    var studentSemester by remember { mutableStateOf("Semester 3 (2nd Year)") }
    var studentMobile by remember { mutableStateOf("") }

    // Parent form state
    var parentName by remember { mutableStateOf("Debabrata Mukherjee") }
    var parentMobile by remember { mutableStateOf("") }
    var parentRelationship by remember { mutableStateOf("Father") }
    var childRollNumber by remember { mutableStateOf("") }
    var childVerificationCode by remember { mutableStateOf("") }

    // HOD form state
    var hodName by remember { mutableStateOf("Prof. S. Sengupta") }
    var hodDesignation by remember { mutableStateOf("Head of Department (HOD)") }
    var hodMobile by remember { mutableStateOf("") }
    var hodInviteCode by remember { mutableStateOf("") }

    // Inline errors & feedback
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandMutedCanvas // #D9E9F0
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header with Back button & Step Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = {
                            currentStep--
                            errorMessage = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandOceanDeep
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandSoftSky.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Step $currentStep of 2",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOceanDeep,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Titles
            Text(
                text = if (currentStep == 1) "Select Your Role" else when (selectedRole) {
                    UserRole.STUDENT -> "Student Profile Setup"
                    UserRole.PARENT -> "Parent & Child Linking"
                    UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.TEACHER -> "Faculty Verification & Setup"
                    null -> "Profile Setup"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandOceanDeep
            )
            Text(
                text = if (currentStep == 1) {
                    "Choose how you will participate in the BCA Department Portal"
                } else {
                    "Complete your one-time academic registration to activate your dashboard"
                },
                fontSize = 13.sp,
                color = BrandTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner if present
            errorMessage?.let { err ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEAEA)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ================= STEP 1: ROLE SELECTION =================
            if (currentStep == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RoleSelectionCard(
                        title = "I am a Student",
                        description = "Attend classes, submit assignments, take quizzes, and track progress.",
                        icon = Icons.Default.School,
                        accentColor = BrandOceanDeep,
                        isSelected = selectedRole == UserRole.STUDENT,
                        onClick = {
                            selectedRole = UserRole.STUDENT
                            errorMessage = null
                        }
                    )

                    RoleSelectionCard(
                        title = "I am a Parent",
                        description = "Track your child’s attendance, fees, marks, and announcements.",
                        icon = Icons.Default.FamilyRestroom,
                        accentColor = BrandSkyTeal,
                        isSelected = selectedRole == UserRole.PARENT,
                        onClick = {
                            selectedRole = UserRole.PARENT
                            errorMessage = null
                        }
                    )

                    RoleSelectionCard(
                        title = "I am HOD / Teacher",
                        description = "Manage students, attendance, fees, quizzes, assignments, and reports.",
                        icon = Icons.Default.AdminPanelSettings,
                        accentColor = BrandTerracotta,
                        badge = "Invite Code Required",
                        isSelected = selectedRole == UserRole.ADMIN,
                        onClick = {
                            selectedRole = UserRole.ADMIN
                            errorMessage = null
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (selectedRole != null) {
                                currentStep = 2
                                errorMessage = null
                            }
                        },
                        enabled = selectedRole != null,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_to_step_2_button")
                    ) {
                        Text("Continue to Profile Setup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }

            // ================= STEP 2: PROFILE DETAILS =================
            if (currentStep == 2) {
                when (selectedRole) {
                    UserRole.STUDENT -> {
                        StudentRegistrationContent(
                            fullName = studentName,
                            onNameChange = { studentName = it },
                            rollNumber = rollNumber,
                            onRollChange = { rollNumber = it },
                            course = studentCourse,
                            batch = studentBatch,
                            onBatchChange = { studentBatch = it },
                            semester = studentSemester,
                            onSemesterChange = { studentSemester = it },
                            mobile = studentMobile,
                            onMobileChange = { studentMobile = it },
                            isSubmitting = isSubmitting,
                            onSubmit = {
                                isSubmitting = true
                                errorMessage = null
                                authViewModel.completeStudentRegistration(
                                    fullName = studentName,
                                    rollNumber = rollNumber,
                                    className = studentCourse,
                                    batch = studentBatch,
                                    semester = studentSemester,
                                    mobileNumber = studentMobile,
                                    subjects = listOf("Data Structures", "DBMS", "Java Programming", "Computer Architecture"),
                                    onSuccess = {
                                        isSubmitting = false
                                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                        onRegistrationComplete()
                                    },
                                    onError = { err ->
                                        isSubmitting = false
                                        errorMessage = err
                                    }
                                )
                            }
                        )
                    }

                    UserRole.PARENT -> {
                        ParentRegistrationContent(
                            fullName = parentName,
                            onNameChange = { parentName = it },
                            mobile = parentMobile,
                            onMobileChange = { parentMobile = it },
                            relationship = parentRelationship,
                            onRelationshipChange = { parentRelationship = it },
                            childRollNumber = childRollNumber,
                            onChildRollChange = { childRollNumber = it },
                            verificationCode = childVerificationCode,
                            onVerificationCodeChange = { childVerificationCode = it },
                            isSubmitting = isSubmitting,
                            onSubmit = {
                                isSubmitting = true
                                errorMessage = null
                                authViewModel.completeParentRegistration(
                                    fullName = parentName,
                                    mobileNumber = parentMobile,
                                    relationship = parentRelationship,
                                    childRollNumber = childRollNumber,
                                    verificationInput = childVerificationCode,
                                    studentsCatalog = currentStudents,
                                    onSuccess = {
                                        isSubmitting = false
                                        Toast.makeText(context, "Parent linking successful!", Toast.LENGTH_SHORT).show()
                                        onRegistrationComplete()
                                    },
                                    onError = { err ->
                                        isSubmitting = false
                                        errorMessage = err
                                    }
                                )
                            }
                        )
                    }

                    UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.TEACHER -> {
                        HodRegistrationContent(
                            fullName = hodName,
                            onNameChange = { hodName = it },
                            designation = hodDesignation,
                            onDesignationChange = { hodDesignation = it },
                            mobile = hodMobile,
                            onMobileChange = { hodMobile = it },
                            inviteCode = hodInviteCode,
                            onInviteCodeChange = { hodInviteCode = it },
                            isSubmitting = isSubmitting,
                            onSubmit = {
                                isSubmitting = true
                                errorMessage = null
                                authViewModel.completeHodRegistration(
                                    fullName = hodName,
                                    designation = hodDesignation,
                                    mobileNumber = hodMobile,
                                    inviteCode = hodInviteCode,
                                    subjects = listOf("DBMS", "Data Structures", "Java", "Python Lab"),
                                    batches = listOf("BCA 2024–27, Section A", "BCA 2025–28, Section B"),
                                    onSuccess = {
                                        isSubmitting = false
                                        Toast.makeText(context, "HOD verification approved!", Toast.LENGTH_SHORT).show()
                                        onRegistrationComplete()
                                    },
                                    onError = { err ->
                                        isSubmitting = false
                                        errorMessage = err
                                    }
                                )
                            }
                        )
                    }

                    null -> {}
                }
            }
        }
    }
}

// ---------------- SUB-COMPONENTS ----------------

@Composable
private fun RoleSelectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandSurfaceCard else BrandSurfaceCard.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else BrandSkyTeal.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOceanDeep
                    )
                    badge?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandTerracotta.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = it,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A1A12),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = BrandTextSecondary,
                    lineHeight = 16.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
        }
    }
}

@Composable
private fun StudentRegistrationContent(
    fullName: String,
    onNameChange: (String) -> Unit,
    rollNumber: String,
    onRollChange: (String) -> Unit,
    course: String,
    batch: String,
    onBatchChange: (String) -> Unit,
    semester: String,
    onSemesterChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    val isFormValid = fullName.isNotBlank() &&
            rollNumber.isNotBlank() &&
            mobile.filter { it.isDigit() }.length >= 10

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurfaceCard),
        border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = onNameChange,
                label = { Text("Full Name (from Google account)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_student_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            // Roll Number
            Column {
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = onRollChange,
                    label = { Text("Roll Number / Student ID *") },
                    placeholder = { Text("e.g. MAKAUT/BCA/24001 or BCA24001") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_student_roll"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOceanDeep,
                        focusedContainerColor = BrandInputBackground,
                        unfocusedContainerColor = BrandInputBackground
                    )
                )
                Text(
                    text = "Must be unique across the department",
                    fontSize = 11.sp,
                    color = BrandTextSecondary,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                )
            }

            // Course & Batch Dropdowns
            OutlinedTextField(
                value = course,
                onValueChange = {},
                readOnly = true,
                label = { Text("Class / Course") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            // Batch selection
            Text("Select Batch & Section", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandTextPrimary)
            listOf("BCA 2024–27, Section A", "BCA 2025–28, Section B", "BCA 2023–26, Section A").forEach { b ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBatchChange(b) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = batch == b,
                        onClick = { onBatchChange(b) },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandOceanDeep)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(b, fontSize = 13.sp, color = BrandTextPrimary)
                }
            }

            // Mobile number
            OutlinedTextField(
                value = mobile,
                onValueChange = onMobileChange,
                label = { Text("Mobile Number (10 digits) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_student_mobile"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            // Assigned Subjects preview
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSoftSky.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Assigned Semester Subjects (MAKAUT Syllabus):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandOceanDeep)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Data Structures & Algorithms\n• Database Management Systems (DBMS)\n• Java Programming & Object-Oriented Lab\n• Computer Architecture", fontSize = 11.sp, color = BrandTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                enabled = isFormValid && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_student_registration_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Complete Registration & Enter Portal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParentRegistrationContent(
    fullName: String,
    onNameChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    relationship: String,
    onRelationshipChange: (String) -> Unit,
    childRollNumber: String,
    onChildRollChange: (String) -> Unit,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    val isFormValid = fullName.isNotBlank() &&
            mobile.filter { it.isDigit() }.length >= 10 &&
            childRollNumber.isNotBlank() &&
            verificationCode.isNotBlank()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurfaceCard),
        border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = onNameChange,
                label = { Text("Parent / Guardian Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_parent_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = onMobileChange,
                label = { Text("Parent Mobile Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_parent_mobile"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            // Relationship
            Text("Relationship to Student", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandTextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Father", "Mother", "Guardian").forEach { rel ->
                    FilterChip(
                        selected = relationship == rel,
                        onClick = { onRelationshipChange(rel) },
                        label = { Text(rel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOceanDeep,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            HorizontalDivider(color = BrandSkyTeal.copy(alpha = 0.3f))

            Text("Link Your Enrolled Child", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandOceanDeep)

            OutlinedTextField(
                value = childRollNumber,
                onValueChange = onChildRollChange,
                label = { Text("Child's Roll Number / Student ID *") },
                placeholder = { Text("e.g. MAKAUT/BCA/24001") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_parent_child_roll"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            OutlinedTextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                label = { Text("Child's Registered Phone or Invite Code *") },
                placeholder = { Text("Last 4 digits of phone or code") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_parent_verify_code"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            Button(
                onClick = onSubmit,
                enabled = isFormValid && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_parent_registration_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Verify & Link Child Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HodRegistrationContent(
    fullName: String,
    onNameChange: (String) -> Unit,
    designation: String,
    onDesignationChange: (String) -> Unit,
    mobile: String,
    onMobileChange: (String) -> Unit,
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    val isFormValid = fullName.isNotBlank() &&
            inviteCode.isNotBlank() &&
            mobile.filter { it.isDigit() }.length >= 10

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurfaceCard),
        border = BorderStroke(1.dp, BrandTerracotta.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrandWarmRose.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BrandOceanDeep, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Faculty security verification required to prevent unauthorized access.", fontSize = 11.sp, color = BrandTextPrimary)
                }
            }

            OutlinedTextField(
                value = inviteCode,
                onValueChange = onInviteCodeChange,
                label = { Text("HOD Master Invite Code *") },
                placeholder = { Text("e.g. MAKAUT-HOD-2025") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_hod_invite_code"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = onNameChange,
                label = { Text("Faculty Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_hod_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            OutlinedTextField(
                value = designation,
                onValueChange = onDesignationChange,
                label = { Text("Designation") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = onMobileChange,
                label = { Text("Official Mobile Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_hod_mobile"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOceanDeep,
                    focusedContainerColor = BrandInputBackground,
                    unfocusedContainerColor = BrandInputBackground
                )
            )

            Button(
                onClick = onSubmit,
                enabled = isFormValid && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTerracotta),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_hod_registration_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF3E1D18))
                } else {
                    Text("Verify Invite Code & Enter HOD Desk", color = Color(0xFF3E1D18), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
