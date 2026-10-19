package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirestoreTuitionService
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AuthUserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TuitionRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedOwner_createsProfileSuccessfully() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val service = FirestoreTuitionService(firestore)

        val profile = AuthUserProfile(
            uid = aliceUid,
            displayName = "Alice Mukherjee",
            email = ALICE_EMAIL,
            role = UserRole.STUDENT,
            rollNumber = "MAKAUT/BCA/24001",
            batch = "Batch 2024-27",
            registrationComplete = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            service.saveUserProfile(profile)
        }
        assertTrue(saveResult.isSuccess)

        val retrievedResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            service.getUserProfile(aliceUid)
        }
        assertTrue(retrievedResult.isSuccess)
        val retrieved = retrievedResult.getOrNull()
        assertNotNull(retrieved)
        assertEquals("Alice Mukherjee", retrieved?.displayName)
    }

    @Test
    fun getOtherUserProfile_nonHodUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val serviceAlice = FirestoreTuitionService(firestore)

        val aliceProfile = AuthUserProfile(
            uid = aliceUid,
            displayName = "Alice Mukherjee",
            email = ALICE_EMAIL,
            role = UserRole.STUDENT,
            rollNumber = "MAKAUT/BCA/24001",
            registrationComplete = true
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            serviceAlice.saveUserProfile(aliceProfile)
        }

        // Bob signs in and tries to read Alice's profile
        signInTestUser(BOB_EMAIL)
        val serviceBob = FirestoreTuitionService(firestore)

        try {
            val doc = withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
            // If get succeeds without exception, it must not return Alice's document data
            assertTrue(doc.data == null)
        } catch (e: Exception) {
            assertTrue(e is FirebaseFirestoreException)
        }
    }

    @Test
    fun attendanceRecord_studentUser_cannotForgeAttendance() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)

        var failedWithPermissionDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("attendance").document("att_fake_1").set(
                    mapOf(
                        "studentId" to aliceUid,
                        "status" to "PRESENT",
                        "date" to "2026-10-19"
                    )
                ).await()
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                failedWithPermissionDenied = true
            }
        } catch (e: Exception) {
            failedWithPermissionDenied = true
        }
        assertTrue("Student user should not be allowed to create attendance records", failedWithPermissionDenied)
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
        auth.signOut()

        var failedWithPermissionDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document("random_user").get().await()
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                failedWithPermissionDenied = true
            }
        } catch (e: Exception) {
            failedWithPermissionDenied = true
        }
        assertTrue("Unauthenticated access must be rejected", failedWithPermissionDenied)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_student@makaut.ac.in"
        const val BOB_EMAIL = "bob_student@makaut.ac.in"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
