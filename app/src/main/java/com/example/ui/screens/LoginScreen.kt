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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthenticationViewModel

/**
 * Clean, professional Sign-In Screen meeting all production requirements:
 * - App logo and app name
 * - Tagline: "Learn. Submit. Track. Succeed."
 * - Primary Action: "Continue with Google" via Credential Manager
 * - Secondary Option: Email/Password login with validation
 * - Terms & Conditions and Privacy Policy dialogs
 * - Smooth loading states and clear error feedback
 * - Balanced non-glare palette styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthenticationViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authState by authViewModel.uiState.collectAsState()
    val authError by authViewModel.authError.collectAsState()

    var showEmailLogin by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val isLoading = authState is AuthUiState.Loading

    // Temporary Firebase Diagnostics UI state
    val isFirebaseInitialized by com.example.FirebaseInitializer.isInitialized.collectAsState()
    val firebaseInitError by com.example.FirebaseInitializer.initializationError.collectAsState()
    var showDiagnostics by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandMutedCanvas // #D9E9F0 balanced mid-tone
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // App Brand Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandOceanDeep, BrandSkyTeal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "BCA Portal Logo",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "BCA Tuition Portal",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandOceanDeep
            )

            // Required Tagline
            Text(
                text = "Learn. Submit. Track. Succeed.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandTextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Department of Computer Applications • MAKAUT",
                fontSize = 12.sp,
                color = BrandTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Notice Banner if Google Sign-In fails or cancels
            authError?.let { err ->
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
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { authViewModel.clearError() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Temporary Firebase Diagnostics UI (Can be dismissed after verification)
            if (showDiagnostics) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFirebaseInitialized) BrandSoftSky.copy(alpha = 0.5f) else Color(0xFFFFF3CD)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isFirebaseInitialized) BrandOceanDeep.copy(alpha = 0.5f) else Color(0xFFFFC107)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_diagnostics_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFirebaseInitialized) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isFirebaseInitialized) BrandOceanDeep else Color(0xFFD39E00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Firebase Initialization Diagnostics (Temporary)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTextPrimary
                                )
                            }
                            IconButton(
                                onClick = { showDiagnostics = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide diagnostics",
                                    tint = BrandTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("isInitialized:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandTextPrimary)
                            Text(
                                text = "$isFirebaseInitialized",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirebaseInitialized) Color(0xFF155724) else Color(0xFF721C24)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Project ID:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandTextPrimary)
                            Text("bca-tuition-portal", fontSize = 11.sp, color = BrandTextSecondary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("initializationError:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandTextPrimary)
                            Text(
                                text = firebaseInitError ?: "null (None / Clean)",
                                fontSize = 11.sp,
                                color = if (firebaseInitError != null) MaterialTheme.colorScheme.error else BrandTextSecondary
                            )
                        }

                        if (firebaseInitError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notice: $firebaseInitError",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Main Action Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceCard), // #E8F2F6
                border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in to access your dashboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandTextPrimary
                    )

                    // Primary Button: Continue with Google
                    Button(
                        onClick = {
                            if (!isLoading) {
                                authViewModel.signInWithGoogle(context)
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("primary_google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = BrandOceanDeep
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Divider with OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BrandSkyTeal.copy(alpha = 0.4f))
                        Text(
                            text = " OR ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BrandSkyTeal.copy(alpha = 0.4f))
                    }

                    // Secondary Option: Email / Password Toggle
                    OutlinedButton(
                        onClick = { showEmailLogin = !showEmailLogin },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSkyTeal),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrandSoftSky.copy(alpha = 0.3f),
                            contentColor = BrandOceanDeep
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("toggle_email_login_button")
                    ) {
                        Icon(
                            imageVector = if (showEmailLogin) Icons.Default.ExpandLess else Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showEmailLogin) "Hide Email Sign In" else "Sign in with Email & Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Expandable Email/Password Form
                    AnimatedVisibility(visible = showEmailLogin) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Academic Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandOceanDeep) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandOceanDeep,
                                    focusedContainerColor = BrandInputBackground,
                                    unfocusedContainerColor = BrandInputBackground
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandOceanDeep) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = BrandOceanDeep
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    authViewModel.signInWithEmail(email, password, selectedRole)
                                }),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandOceanDeep,
                                    focusedContainerColor = BrandInputBackground,
                                    unfocusedContainerColor = BrandInputBackground
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        resetEmail = email
                                        showForgotPasswordDialog = true
                                    }
                                ) {
                                    Text("Forgot Password?", fontSize = 12.sp, color = BrandOceanDeep)
                                }

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        authViewModel.signInWithEmail(email, password, selectedRole)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Log In", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fast Demo Role Access
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWarmRose.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, BrandTerracotta.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandOceanDeep, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instant Demo Role Login (One-Tap Test)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                authViewModel.quickDemoLogin(UserRole.STUDENT)
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_demo_student")
                        ) {
                            Text("Student", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                authViewModel.quickDemoLogin(UserRole.ADMIN)
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTerracotta),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_demo_hod")
                        ) {
                            Text("HOD", fontSize = 11.sp, color = Color(0xFF3E1D18), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                authViewModel.quickDemoLogin(UserRole.PARENT)
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSkyTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("quick_demo_parent")
                        ) {
                            Text("Parent", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Links: Terms & Conditions and Privacy Policy
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terms & Conditions",
                    fontSize = 12.sp,
                    color = BrandOceanDeep,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
                Text(" • ", fontSize = 12.sp, color = BrandTextSecondary)
                Text(
                    text = "Privacy Policy",
                    fontSize = 12.sp,
                    color = BrandOceanDeep,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = BrandSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = BrandOceanDeep) },
            text = {
                Column {
                    Text("Enter your registered academic email to receive reset instructions.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            authViewModel.sendPasswordReset(resetEmail)
                            Toast.makeText(context, "Reset link sent to $resetEmail", Toast.LENGTH_SHORT).show()
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep)
                ) {
                    Text("Send Link", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Terms & Conditions Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = BrandSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = BrandOceanDeep) },
            text = {
                Text(
                    text = "1. Acceptance of Terms: By logging in, students, faculty, and guardians agree to comply with MAKAUT departmental regulations.\n\n" +
                            "2. Attendance & Conduct: Minimum 75% attendance is statutory for exam eligibility.\n\n" +
                            "3. Security: All submissions and records are verified by the HOD faculty desk.",
                    fontSize = 12.sp,
                    color = BrandTextPrimary
                )
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep)) {
                    Text("I Understand", color = Color.White)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = BrandSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = BrandOceanDeep) },
            text = {
                Text(
                    text = "1. Student Data Protection: Academic marks, attendance, and fee invoices are stored securely using Cloud Firestore.\n\n" +
                            "2. Role-Based Access: Students access only their records. Parents access only linked child records. Faculty access authorized batches.\n\n" +
                            "3. Google Authentication: Profile credentials are authenticated via Google Identity Services.",
                    fontSize = 12.sp,
                    color = BrandTextPrimary
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep)) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}
