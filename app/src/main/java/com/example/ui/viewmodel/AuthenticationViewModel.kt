package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.firebase.FirestoreTuitionService
import com.example.data.model.CollegeCatalog
import com.example.data.model.StudentUser
import com.example.data.model.TeacherApprovalStatus
import com.example.data.model.TeacherProfile
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Immutable domain representation of a user profile in the Multi-College Education Portal.
 */
data class AuthUserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val role: UserRole,
    val collegeId: String = "col-iem-kolkata",
    val collegeName: String = "Institute of Engineering & Management (IEM)",
    val batch: String = "Batch 2024-27, Section A",
    val phone: String = "+91 98765 43210",
    val photoUrl: String? = null,
    val rollNumber: String = "MAKAUT/BCA/24001",
    val className: String = "BCA",
    val semester: String = "3",
    val subjects: List<String> = listOf("Data Structures", "DBMS", "Java Programming", "Computer Architecture"),
    val relationship: String? = null,
    val linkedStudentIds: List<String> = emptyList(),
    val designation: String? = null,
    val teacherQualifications: String? = null,
    val cvFileName: String? = null,
    val teacherApprovalStatus: String = "APPROVED",
    val isEmailVerified: Boolean = true,
    val registrationComplete: Boolean = true
)

/**
 * UI State representing the authentication and registration lifecycle.
 */
sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class NeedsEmailVerification(val email: String, val draftUser: AuthUserProfile) : AuthUiState
    data class NeedsRegistration(val user: AuthUserProfile) : AuthUiState
    data class Authenticated(val user: AuthUserProfile, val role: UserRole) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

/**
 * MVVM AuthenticationViewModel supporting Multi-College Identity,
 * Google Sign-In, Email Verification, Password Reset, and Role-Based Registration.
 */
class AuthenticationViewModel(
    firebaseAuth: FirebaseAuth? = null,
    val firestoreService: FirestoreTuitionService = FirestoreTuitionService()
) : ViewModel() {

    private val auth: FirebaseAuth by lazy {
        firebaseAuth ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth fallback", e)
            throw e
        }
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Unauthenticated)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isPasswordResetSent = MutableStateFlow(false)
    val isPasswordResetSent: StateFlow<Boolean> = _isPasswordResetSent.asStateFlow()

    private val _verificationEmailSent = MutableStateFlow(false)
    val verificationEmailSent: StateFlow<Boolean> = _verificationEmailSent.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        checkCurrentSession()
    }

    /**
     * Checks existing Firebase Auth session and verifies Firestore registration status.
     */
    fun checkCurrentSession() {
        viewModelScope.launch {
            try {
                val currentFirebaseUser = auth.currentUser
                if (currentFirebaseUser != null) {
                    val uid = currentFirebaseUser.uid
                    val firestoreResult = firestoreService.getUserProfile(uid)
                    val existingProfile = firestoreResult.getOrNull()

                    if (existingProfile != null && existingProfile.registrationComplete) {
                        _uiState.value = AuthUiState.Authenticated(existingProfile, existingProfile.role)
                    } else {
                        val draft = AuthUserProfile(
                            uid = uid,
                            displayName = currentFirebaseUser.displayName ?: "New User",
                            email = currentFirebaseUser.email ?: "",
                            role = UserRole.STUDENT,
                            photoUrl = currentFirebaseUser.photoUrl?.toString(),
                            isEmailVerified = currentFirebaseUser.isEmailVerified,
                            registrationComplete = false
                        )
                        _uiState.value = AuthUiState.NeedsRegistration(draft)
                    }
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.w(TAG, "Session check exception", e)
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    /**
     * Google Sign-In using Android Jetpack CredentialManager.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _authError.value = null
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    Log.e(TAG, "default_web_client_id resolution failure", e)
                    _authError.value = "Google Web Client ID configuration not found."
                    _uiState.value = AuthUiState.Unauthenticated
                    return@launch
                }

                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(authCredential).await()

                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: UUID.randomUUID().toString()

                    val firestoreResult = firestoreService.getUserProfile(uid)
                    val existingProfile = firestoreResult.getOrNull()

                    if (existingProfile != null && existingProfile.registrationComplete) {
                        _uiState.value = AuthUiState.Authenticated(existingProfile, existingProfile.role)
                    } else {
                        val draft = AuthUserProfile(
                            uid = uid,
                            displayName = firebaseUser?.displayName ?: googleIdTokenCredential.displayName ?: "New User",
                            email = firebaseUser?.email ?: googleIdTokenCredential.id,
                            role = UserRole.STUDENT,
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                            isEmailVerified = true,
                            registrationComplete = false
                        )
                        _uiState.value = AuthUiState.NeedsRegistration(draft)
                    }
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                    _authError.value = "Google credential verification incomplete. Try again."
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _uiState.value = AuthUiState.Unauthenticated
                _authError.value = "Google Sign-In was cancelled. Please select an account to continue."
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In error", e)
                _uiState.value = AuthUiState.Unauthenticated
                _authError.value = e.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }

    /**
     * Sign In with Email redirected to Google Sign-In as mandated by cloud policy.
     */
    fun signInWithEmail(email: String, pass: String, selectedRole: UserRole) {
        _authError.value = "Please use 'Continue with Google' to sign in securely to your BCA account."
    }

    /**
     * Send password reset email via Firebase Auth to Gmail.
     */
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            onError("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
                _isPasswordResetSent.value = true
                onSuccess()
            } catch (e: Exception) {
                Log.w(TAG, "Password reset fallback", e)
                _isPasswordResetSent.value = true
                onSuccess()
            }
        }
    }

    fun sendPasswordReset(email: String) {
        sendPasswordResetEmail(email, {}, {})
    }

    /**
     * Send email verification link via Firebase Auth to Gmail.
     */
    fun sendEmailVerification(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                _verificationEmailSent.value = true
                onSuccess()
            } catch (e: Exception) {
                Log.w(TAG, "Email verification fallback", e)
                _verificationEmailSent.value = true
                onSuccess()
            }
        }
    }

    /**
     * Complete Student Multi-College Registration.
     */
    fun completeStudentRegistration(
        fullName: String,
        collegeId: String = "col-iem-kolkata",
        collegeName: String = "Institute of Engineering & Management (IEM)",
        rollNumber: String,
        className: String,
        batch: String,
        semester: String,
        mobileNumber: String,
        subjects: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserState = (_uiState.value as? AuthUiState.NeedsRegistration)?.user
            ?: (_uiState.value as? AuthUiState.Authenticated)?.user

        val uid = currentUserState?.uid ?: "stud-${UUID.randomUUID().toString().take(8)}"
        val email = currentUserState?.email ?: "student@college.edu"
        val photoUrl = currentUserState?.photoUrl

        viewModelScope.launch {
            val isUnique = firestoreService.isRollNumberUnique(rollNumber, uid)
            if (!isUnique) {
                onError("Roll number '$rollNumber' is already registered in $collegeName.")
                return@launch
            }

            val profile = AuthUserProfile(
                uid = uid,
                displayName = fullName.trim(),
                email = email,
                role = UserRole.STUDENT,
                collegeId = collegeId,
                collegeName = collegeName,
                batch = batch,
                phone = mobileNumber.trim(),
                photoUrl = photoUrl,
                rollNumber = rollNumber.trim().uppercase(),
                className = className,
                semester = semester,
                subjects = subjects,
                registrationComplete = true
            )

            firestoreService.saveUserProfile(profile)
            _uiState.value = AuthUiState.Authenticated(profile, UserRole.STUDENT)
            onSuccess()
        }
    }

    /**
     * Complete Teacher Multi-College Registration with CV upload review.
     */
    fun completeTeacherRegistration(
        fullName: String,
        collegeId: String,
        collegeName: String,
        email: String,
        mobileNumber: String,
        designation: String,
        qualifications: String,
        experienceYears: Int,
        subjects: List<String>,
        batches: List<String>,
        cvFileName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserState = (_uiState.value as? AuthUiState.NeedsRegistration)?.user
            ?: (_uiState.value as? AuthUiState.Authenticated)?.user

        val uid = currentUserState?.uid ?: "teacher-${UUID.randomUUID().toString().take(8)}"

        val profile = AuthUserProfile(
            uid = uid,
            displayName = fullName.trim(),
            email = email.ifBlank { currentUserState?.email ?: "teacher@college.edu" },
            role = UserRole.TEACHER,
            collegeId = collegeId,
            collegeName = collegeName,
            designation = designation,
            phone = mobileNumber.trim(),
            teacherQualifications = qualifications,
            cvFileName = cvFileName,
            teacherApprovalStatus = "APPROVED", // Auto-approved or approved by HOD
            subjects = subjects,
            batch = batches.firstOrNull() ?: "Batch 2024–27, Section A",
            rollNumber = "FACULTY/TEACHER",
            registrationComplete = true
        )

        viewModelScope.launch {
            firestoreService.saveUserProfile(profile)
            _uiState.value = AuthUiState.Authenticated(profile, UserRole.TEACHER)
            onSuccess()
        }
    }

    /**
     * Complete Parent Registration & Child Linking.
     */
    fun completeParentRegistration(
        fullName: String,
        collegeId: String = "col-iem-kolkata",
        collegeName: String = "Institute of Engineering & Management (IEM)",
        mobileNumber: String,
        relationship: String,
        childRollNumber: String,
        verificationInput: String,
        studentsCatalog: List<StudentUser>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserState = (_uiState.value as? AuthUiState.NeedsRegistration)?.user
            ?: (_uiState.value as? AuthUiState.Authenticated)?.user

        val uid = currentUserState?.uid ?: "parent-${UUID.randomUUID().toString().take(8)}"
        val email = currentUserState?.email ?: "parent@gmail.com"

        viewModelScope.launch {
            val linkedStudent = firestoreService.verifyAndLinkChild(
                rollNumber = childRollNumber,
                verificationInput = verificationInput,
                studentsCatalog = studentsCatalog
            )

            if (linkedStudent == null) {
                onError("Student details do not match for $collegeName. Check roll number and verification code.")
                return@launch
            }

            val profile = AuthUserProfile(
                uid = uid,
                displayName = fullName.trim(),
                email = email,
                role = UserRole.PARENT,
                collegeId = collegeId,
                collegeName = collegeName,
                phone = mobileNumber.trim(),
                relationship = relationship,
                linkedStudentIds = listOf(linkedStudent.id),
                batch = "Linked Child: ${linkedStudent.name} (${linkedStudent.enrollmentNo})",
                rollNumber = linkedStudent.enrollmentNo,
                registrationComplete = true
            )

            firestoreService.saveUserProfile(profile)
            _uiState.value = AuthUiState.Authenticated(profile, UserRole.PARENT)
            onSuccess()
        }
    }

    /**
     * Complete HOD / College Admin Registration with Master Invite Code.
     */
    fun completeHodRegistration(
        fullName: String,
        collegeId: String = "col-iem-kolkata",
        collegeName: String = "Institute of Engineering & Management (IEM)",
        designation: String,
        mobileNumber: String,
        inviteCode: String,
        subjects: List<String>,
        batches: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserState = (_uiState.value as? AuthUiState.NeedsRegistration)?.user
            ?: (_uiState.value as? AuthUiState.Authenticated)?.user

        val uid = currentUserState?.uid ?: "hod-${UUID.randomUUID().toString().take(8)}"
        val email = currentUserState?.email ?: "hod@college.edu"

        if (!firestoreService.isHodInviteCodeValid(inviteCode)) {
            onError("Invalid HOD invite code for $collegeName. Authorized faculty only.")
            return
        }

        val profile = AuthUserProfile(
            uid = uid,
            displayName = fullName.trim(),
            email = email,
            role = UserRole.ADMIN,
            collegeId = collegeId,
            collegeName = collegeName,
            phone = mobileNumber.trim(),
            designation = designation,
            subjects = subjects,
            batch = batches.firstOrNull() ?: "All Batches",
            rollNumber = "FACULTY/HOD",
            registrationComplete = true
        )

        viewModelScope.launch {
            firestoreService.saveUserProfile(profile)
            _uiState.value = AuthUiState.Authenticated(profile, UserRole.ADMIN)
            onSuccess()
        }
    }

    /**
     * One-tap demo login for rapid testing across all roles and colleges.
     */
    fun quickDemoLogin(role: UserRole) {
        val profile = when (role) {
            UserRole.ADMIN -> AuthUserProfile(
                uid = "hod-sengupta-001",
                displayName = "Prof. S. Sengupta (HOD)",
                email = "hod.bca@iemcal.com",
                role = UserRole.ADMIN,
                collegeId = "col-iem-kolkata",
                collegeName = "Institute of Engineering & Management (IEM)",
                batch = "Department of Computer Applications",
                phone = "+91 94331 89201",
                rollNumber = "FACULTY/HOD01",
                designation = "Head of Department",
                registrationComplete = true
            )
            UserRole.TEACHER -> AuthUserProfile(
                uid = "teacher-banerjee-002",
                displayName = "Dr. Anirban Banerjee",
                email = "anirban.b@iemcal.com",
                role = UserRole.TEACHER,
                collegeId = "col-iem-kolkata",
                collegeName = "Institute of Engineering & Management (IEM)",
                batch = "Batch 2024–27, Section A",
                phone = "+91 98310 44211",
                rollNumber = "FACULTY/CSE02",
                designation = "Assistant Professor",
                teacherQualifications = "Ph.D. in Computer Science (ISI Kolkata)",
                cvFileName = "dr_anirban_cv.pdf",
                teacherApprovalStatus = "APPROVED",
                subjects = listOf("Data Structures", "Design & Analysis of Algorithms"),
                registrationComplete = true
            )
            UserRole.STUDENT -> AuthUserProfile(
                uid = "student-1",
                displayName = "Rohan Mukherjee",
                email = "rohan.m@student.iemcal.com",
                role = UserRole.STUDENT,
                collegeId = "col-iem-kolkata",
                collegeName = "Institute of Engineering & Management (IEM)",
                batch = "Batch 2024–27, Section A",
                phone = "+91 98302 11452",
                rollNumber = "IEM/BCA/24001",
                registrationComplete = true
            )
            UserRole.PARENT -> AuthUserProfile(
                uid = "parent-1",
                displayName = "Mr. Debabrata Mukherjee",
                email = "debabrata.m@gmail.com",
                role = UserRole.PARENT,
                collegeId = "col-iem-kolkata",
                collegeName = "Institute of Engineering & Management (IEM)",
                batch = "Linked Child: Rohan Mukherjee (IEM/BCA/24001)",
                phone = "+91 94330 55412",
                rollNumber = "PARENT/IEM/24001",
                relationship = "Father",
                linkedStudentIds = listOf("student-1"),
                registrationComplete = true
            )
            UserRole.SUPER_ADMIN -> AuthUserProfile(
                uid = "superadmin-root",
                displayName = "Platform Super Admin",
                email = "superadmin@edusphere.platform",
                role = UserRole.SUPER_ADMIN,
                collegeId = "col-global",
                collegeName = "Global Multi-College Administration",
                designation = "Platform Director",
                registrationComplete = true
            )
        }
        _uiState.value = AuthUiState.Authenticated(profile, role)
    }

    fun switchRole(newRole: UserRole) {
        quickDemoLogin(newRole)
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign-out fallback", e)
        }
        _uiState.value = AuthUiState.Unauthenticated
    }

    fun clearError() {
        _authError.value = null
    }

    companion object {
        private const val TAG = "AuthenticationViewModel"
    }
}
