package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.AttendanceNoticeDialog
import com.example.ui.components.DigitalIdCardDialog
import com.example.ui.components.MakautCgpaCalculatorDialog
import com.example.ui.components.QrAttendanceDialog
import com.example.ui.components.RazorpayPaymentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthenticationViewModel
import com.example.ui.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TuitionViewModel,
    authViewModel: AuthenticationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()

    // Composable Auth Gate: Render LoginScreen when unauthenticated
    if (authState is AuthUiState.Unauthenticated) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { /* Automatically flips authState to Authenticated or NeedsRegistration */ }
        )
        return
    }

    // New User Profile Setup Gate: Enforce one-time registration before allowing dashboard access
    if (authState is AuthUiState.NeedsRegistration) {
        RegistrationFlowScreen(
            authViewModel = authViewModel,
            tuitionViewModel = viewModel,
            onRegistrationComplete = { /* Flips authState to Authenticated and routes to dashboard */ }
        )
        return
    }

    // Authenticated role & user session
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()

    // Navigation State
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeSubModule by remember { mutableStateOf<Int?>(null) } // null means show tab screen

    // Bottom Sheet for Quick Actions (+)
    var showActionBottomSheet by remember { mutableStateOf(false) }

    // Dialog states
    var showIdCardDialog by remember { mutableStateOf(false) }
    var showQrAttendanceDialog by remember { mutableStateOf(false) }
    var showCgpaPlannerDialog by remember { mutableStateOf(false) }
    var showAttendanceNoticeDialog by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    // Synchronize auth role with tuition view model
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Authenticated) {
            val user = (authState as AuthUiState.Authenticated).user
            val role = (authState as AuthUiState.Authenticated).role
            viewModel.setRole(role)
            viewModel.updateActiveUserProfile(user)
        }
    }

    // Back handler: pop sub-module first, then return to Tab 0 (Home/Dashboard)
    BackHandler(enabled = activeSubModule != null || selectedTab != 0) {
        if (activeSubModule != null) {
            activeSubModule = null
        } else {
            selectedTab = 0
        }
    }

    // Razorpay Checkout Modal
    if (paymentState.isOpen && paymentState.feeRecord != null) {
        RazorpayPaymentDialog(
            feeRecord = paymentState.feeRecord!!,
            amountToPay = paymentState.amountToPay,
            isProcessing = paymentState.isProcessing,
            completedTransaction = paymentState.lastCompletedTransaction,
            onConfirm = { method -> viewModel.confirmPayment(method) },
            onDismiss = { viewModel.dismissPaymentCheckout() }
        )
    }

    // Digital ID Card Dialog
    if (showIdCardDialog) {
        DigitalIdCardDialog(
            student = currentUser,
            onDismiss = { showIdCardDialog = false }
        )
    }

    // QR Attendance Dialog
    if (showQrAttendanceDialog) {
        QrAttendanceDialog(
            userRole = currentRole,
            onAttendanceMarked = { className ->
                viewModel.markNotificationRead("notif-1")
                Toast.makeText(context, "Attendance marked for $className", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showQrAttendanceDialog = false }
        )
    }

    // MAKAUT Target CGPA Planner Dialog
    if (showCgpaPlannerDialog) {
        MakautCgpaCalculatorDialog(
            onDismiss = { showCgpaPlannerDialog = false }
        )
    }

    // Attendance Defaulter Warning Notice Dialog
    if (showAttendanceNoticeDialog) {
        AttendanceNoticeDialog(
            student = currentUser,
            attendancePercentage = 68.5,
            onDismiss = { showAttendanceNoticeDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(BrandOceanDeep, BrandSkyTeal)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BCA Tuition Portal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandOceanDeep
                            )
                            Text(
                                text = when (currentRole) {
                                    UserRole.ADMIN, UserRole.SUPER_ADMIN -> "HOD Administration • MAKAUT CBCS"
                                    UserRole.TEACHER -> "Faculty: ${currentUser.name}"
                                    UserRole.STUDENT -> "Student: ${currentUser.name}"
                                    UserRole.PARENT -> "Guardian: Rohan Mukherjee"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // ID Card Quick Action Button (palette styled)
                    IconButton(
                        onClick = { showIdCardDialog = true },
                        modifier = Modifier.testTag("id_card_top_button")
                    ) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = "Digital ID Card",
                            tint = BrandOceanDeep
                        )
                    }

                    // QR Attendance Quick Button
                    IconButton(
                        onClick = { showQrAttendanceDialog = true },
                        modifier = Modifier.testTag("qr_attendance_top_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "QR Attendance",
                            tint = BrandOceanDeep
                        )
                    }

                    // MAKAUT CGPA Target Planner Quick Button
                    IconButton(
                        onClick = { showCgpaPlannerDialog = true },
                        modifier = Modifier.testTag("cgpa_calculator_top_button")
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "CGPA Target Planner",
                            tint = BrandOceanDeep
                        )
                    }

                    // Role Switcher Pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BrandSoftSky.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { showRoleMenu = true }
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (currentRole) {
                                        UserRole.ADMIN, UserRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                                        UserRole.TEACHER -> Icons.Default.School
                                        UserRole.STUDENT -> Icons.Default.Person
                                        UserRole.PARENT -> Icons.Default.FamilyRestroom
                                    },
                                    contentDescription = null,
                                    tint = BrandOceanDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentRole.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOceanDeep
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = BrandOceanDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Student Mode", fontWeight = FontWeight.Bold)
                                        Text("Academic dashboard, assignments, quizzes, fees", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.setRole(UserRole.STUDENT)
                                    authViewModel.switchRole(UserRole.STUDENT)
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandOceanDeep) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Faculty / HOD Mode", fontWeight = FontWeight.Bold)
                                        Text("Attendance, evaluations, announcements, reports", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.setRole(UserRole.ADMIN)
                                    authViewModel.switchRole(UserRole.ADMIN)
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandTerracotta) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Parent / Guardian Mode", fontWeight = FontWeight.Bold)
                                        Text("Child progress, attendance alerts, verified receipts", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.setRole(UserRole.PARENT)
                                    authViewModel.switchRole(UserRole.PARENT)
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = BrandSkyTeal) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    authViewModel.signOut()
                                    showRoleMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }

                    // Notification bell with badge
                    IconButton(
                        onClick = { activeSubModule = 10 /* Notifications */ },
                        modifier = Modifier.testTag("notification_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = BrandTerracotta) { Text("$unreadNotifs") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = BrandOceanDeep)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Facebook/Instagram-style fluid 5-item bottom bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandSurfaceCard,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (currentRole) {
                        UserRole.STUDENT -> {
                            // 1. Home
                            SocialNavItem(
                                label = "Home",
                                icon = Icons.Default.Home,
                                isSelected = selectedTab == 0 && activeSubModule == null,
                                onClick = { selectedTab = 0; activeSubModule = null }
                            )
                            // 2. Classes
                            SocialNavItem(
                                label = "Classes",
                                icon = Icons.Default.MenuBook,
                                isSelected = selectedTab == 1 && activeSubModule == null,
                                onClick = { selectedTab = 1; activeSubModule = null }
                            )
                            // 3. Add / Quick Actions (Center Action Button)
                            SocialCenterActionButton(
                                onClick = { showActionBottomSheet = true }
                            )
                            // 4. Community
                            SocialNavItem(
                                label = "Community",
                                icon = Icons.Default.Forum,
                                isSelected = selectedTab == 3 && activeSubModule == null,
                                onClick = { selectedTab = 3; activeSubModule = null }
                            )
                            // 5. Profile
                            SocialNavItem(
                                label = "Profile",
                                icon = Icons.Default.Person,
                                isSelected = selectedTab == 4 && activeSubModule == null,
                                onClick = { selectedTab = 4; activeSubModule = null }
                            )
                        }
                        UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.TEACHER -> {
                            // 1. Dashboard
                            SocialNavItem(
                                label = "Dashboard",
                                icon = Icons.Default.Dashboard,
                                isSelected = selectedTab == 0 && activeSubModule == null,
                                onClick = { selectedTab = 0; activeSubModule = null }
                            )
                            // 2. Students
                            SocialNavItem(
                                label = "Students",
                                icon = Icons.Default.Groups,
                                isSelected = selectedTab == 1 && activeSubModule == null,
                                onClick = { selectedTab = 1; activeSubModule = null }
                            )
                            // 3. Create / Manage (+)
                            SocialCenterActionButton(
                                onClick = { showActionBottomSheet = true }
                            )
                            // 4. Community
                            SocialNavItem(
                                label = "Community",
                                icon = Icons.Default.Forum,
                                isSelected = selectedTab == 3 && activeSubModule == null,
                                onClick = { selectedTab = 3; activeSubModule = null }
                            )
                            // 5. Admin
                            SocialNavItem(
                                label = "Admin",
                                icon = Icons.Default.AdminPanelSettings,
                                isSelected = selectedTab == 4 && activeSubModule == null,
                                onClick = { selectedTab = 4; activeSubModule = null }
                            )
                        }
                        UserRole.PARENT -> {
                            // 1. Home
                            SocialNavItem(
                                label = "Home",
                                icon = Icons.Default.Home,
                                isSelected = selectedTab == 0 && activeSubModule == null,
                                onClick = { selectedTab = 0; activeSubModule = null }
                            )
                            // 2. Progress
                            SocialNavItem(
                                label = "Progress",
                                icon = Icons.Default.TrendingUp,
                                isSelected = selectedTab == 1 && activeSubModule == null,
                                onClick = { selectedTab = 1; activeSubModule = null }
                            )
                            // 3. Fees
                            SocialNavItem(
                                label = "Fees",
                                icon = Icons.Default.AccountBalanceWallet,
                                isSelected = selectedTab == 2 && activeSubModule == null,
                                onClick = { selectedTab = 2; activeSubModule = null }
                            )
                            // 4. Updates
                            SocialNavItem(
                                label = "Updates",
                                icon = Icons.Default.Campaign,
                                isSelected = selectedTab == 3 && activeSubModule == null,
                                onClick = { selectedTab = 3; activeSubModule = null }
                            )
                            // 5. Profile
                            SocialNavItem(
                                label = "Profile",
                                icon = Icons.Default.Person,
                                isSelected = selectedTab == 4 && activeSubModule == null,
                                onClick = { selectedTab = 4; activeSubModule = null }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Quick Filter / Module Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickModulePill("Dashboard", activeSubModule == null && selectedTab == 0) {
                    activeSubModule = null; selectedTab = 0
                }
                QuickModulePill("Attendance", activeSubModule == 1) { activeSubModule = 1 }
                QuickModulePill("Fees & UPI", activeSubModule == 2) { activeSubModule = 2 }
                QuickModulePill("Syllabus", activeSubModule == 3) { activeSubModule = 3 }
                QuickModulePill("Assignments", activeSubModule == 6) { activeSubModule = 6 }
                QuickModulePill("Quizzes", activeSubModule == 8) { activeSubModule = 8 }
                QuickModulePill("Calendar", activeSubModule == 4) { activeSubModule = 4 }
                QuickModulePill("Marks & Stats", activeSubModule == 5) { activeSubModule = 5 }
                QuickModulePill("Daily Updates", activeSubModule == 7) { activeSubModule = 7 }
                QuickModulePill("Forum", activeSubModule == 9) { activeSubModule = 9 }
                QuickModulePill("Doubt AI & Chat", activeSubModule == 11) { activeSubModule = 11 }
                QuickModulePill("Settings", activeSubModule == 12) { activeSubModule = 12 }
            }

            // Main Content Area with fluid animated switching
            Box(modifier = Modifier.weight(1f)) {
                if (activeSubModule != null) {
                    when (activeSubModule) {
                        1 -> AttendanceScreen(viewModel = viewModel)
                        2 -> FeesScreen(viewModel = viewModel)
                        3 -> SyllabusScreen(viewModel = viewModel)
                        4 -> CalendarScreen(viewModel = viewModel)
                        5 -> AnalyticsScreen(viewModel = viewModel)
                        6 -> AssignmentsScreen(viewModel = viewModel)
                        7 -> DailyUpdatesScreen(viewModel = viewModel)
                        8 -> QuizScreen(viewModel = viewModel)
                        9 -> ForumScreen(viewModel = viewModel)
                        10 -> NotificationsScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                        11 -> DoubtChatScreen(viewModel = viewModel)
                        12 -> ProfileSettingsScreen(viewModel = viewModel, onNavigateBack = { activeSubModule = null })
                    }
                } else {
                    // Role-Based Screen Tabs
                    when (currentRole) {
                        UserRole.STUDENT -> {
                            when (selectedTab) {
                                0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                                1 -> SyllabusScreen(viewModel = viewModel) // Classes tab
                                3 -> ForumScreen(viewModel = viewModel)   // Community tab
                                4 -> ProfileSettingsScreen(viewModel = viewModel, onNavigateBack = { selectedTab = 0 })
                                else -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                            }
                        }
                        UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.TEACHER -> {
                            when (selectedTab) {
                                0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                                1 -> AttendanceScreen(viewModel = viewModel) // Students management
                                3 -> ForumScreen(viewModel = viewModel)      // Community moderation
                                4 -> AnalyticsScreen(viewModel = viewModel)  // Admin reports & metrics
                                else -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                            }
                        }
                        UserRole.PARENT -> {
                            when (selectedTab) {
                                0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                                1 -> AnalyticsScreen(viewModel = viewModel) // Progress
                                2 -> FeesScreen(viewModel = viewModel)      // Fees
                                3 -> DailyUpdatesScreen(viewModel = viewModel) // Updates
                                4 -> ProfileSettingsScreen(viewModel = viewModel, onNavigateBack = { selectedTab = 0 })
                                else -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeSubModule = it })
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add / Quick Actions (+)
    if (showActionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionBottomSheet = false },
            containerColor = BrandSurfaceCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentRole == UserRole.ADMIN) "Create & Manage Actions" else "Quick Student Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOceanDeep
                    )
                    IconButton(onClick = { showActionBottomSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandOceanDeep)
                    }
                }

                HorizontalDivider(color = BrandSkyTeal.copy(alpha = 0.35f))

                if (currentRole == UserRole.ADMIN) {
                    // HOD Actions
                    ActionSheetItem("Mark Batch Attendance", Icons.Default.FactCheck, BrandOceanDeep) {
                        showActionBottomSheet = false; activeSubModule = 1
                    }
                    ActionSheetItem("Generate Dynamic Attendance QR", Icons.Default.QrCodeScanner, BrandTerracotta) {
                        showActionBottomSheet = false; showQrAttendanceDialog = true
                    }
                    ActionSheetItem("Issue Attendance Defaulter Warning Notice", Icons.Default.Warning, BrandTerracotta) {
                        showActionBottomSheet = false; showAttendanceNoticeDialog = true
                    }
                    ActionSheetItem("Create New Assignment", Icons.Default.PostAdd, BrandSkyTeal) {
                        showActionBottomSheet = false; activeSubModule = 6
                    }
                    ActionSheetItem("Upload Test Marks (CSV / Manual)", Icons.Default.UploadFile, BrandOceanDeep) {
                        showActionBottomSheet = false; activeSubModule = 5
                    }
                    ActionSheetItem("Create Timed Quiz", Icons.Default.Quiz, BrandTerracotta) {
                        showActionBottomSheet = false; activeSubModule = 8
                    }
                    ActionSheetItem("Post Daily Update & Code Note", Icons.Default.PostAdd, BrandSkyTeal) {
                        showActionBottomSheet = false; activeSubModule = 7
                    }
                } else {
                    // Student Actions
                    ActionSheetItem("MAKAUT Target CGPA Planner", Icons.Default.Calculate, BrandOceanDeep) {
                        showActionBottomSheet = false; showCgpaPlannerDialog = true
                    }
                    ActionSheetItem("Mark Attendance via QR", Icons.Default.QrCodeScanner, BrandOceanDeep) {
                        showActionBottomSheet = false; showQrAttendanceDialog = true
                    }
                    ActionSheetItem("View Digital ID Card", Icons.Default.Badge, BrandTerracotta) {
                        showActionBottomSheet = false; showIdCardDialog = true
                    }
                    ActionSheetItem("Submit Homework Solution", Icons.Default.AssignmentTurnedIn, BrandSkyTeal) {
                        showActionBottomSheet = false; activeSubModule = 6
                    }
                    ActionSheetItem("Attempt Timed Quiz", Icons.Default.Quiz, BrandOceanDeep) {
                        showActionBottomSheet = false; activeSubModule = 8
                    }
                    ActionSheetItem("Pay Tuition Fees (UPI / Razorpay)", Icons.Default.AccountBalanceWallet, BrandTerracotta) {
                        showActionBottomSheet = false; activeSubModule = 2
                    }
                    ActionSheetItem("Ask Doubt to AI & Teacher", Icons.Default.QuestionAnswer, BrandSkyTeal) {
                        showActionBottomSheet = false; activeSubModule = 11
                    }
                    ActionSheetItem("View Class Calendar", Icons.Default.CalendarMonth, BrandOceanDeep) {
                        showActionBottomSheet = false; activeSubModule = 4
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SocialNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BrandOceanDeep else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) BrandOceanDeep else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SocialCenterActionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(BrandOceanDeep, BrandTerracotta)
                )
            )
            .clickable(onClick = onClick)
            .testTag("center_action_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Quick Actions",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun QuickModulePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandOceanDeep,
            selectedLabelColor = Color.White,
            containerColor = BrandSoftSky.copy(alpha = 0.35f),
            labelColor = BrandOceanDeep
        ),
        border = if (isSelected) null else BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.4f))
    )
}

@Composable
private fun ActionSheetItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = BrandSoftSky.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandSkyTeal)
        }
    }
}
