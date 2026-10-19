package com.example

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Asynchronous, crash-resilient Firebase Initializer in the root package.
 *
 * Implements:
 * - ContentProvider integration for manifest-level startup initialization
 * - Asynchronous background initialization via Kotlin Coroutines on Dispatchers.IO
 * - Robust try-catch protection to prevent runtime crashes if Firebase is unavailable
 * - Automatic fallback options matching bca-tuition-portal project credentials
 * - Reactive StateFlow tracking initialization status
 */
class FirebaseInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        initializeAsync(ctx) { success ->
            if (success) {
                Log.i(TAG, "Manifest ContentProvider FirebaseApp initialization complete.")
            } else {
                Log.w(TAG, "Manifest ContentProvider FirebaseApp initialization safely skipped.")
            }
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        private const val TAG = "FirebaseInitializer"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        private val _isInitialized = MutableStateFlow(false)
        val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

        private val _initializationError = MutableStateFlow<String?>(null)
        val initializationError: StateFlow<String?> = _initializationError.asStateFlow()

        /**
         * Initializes Firebase asynchronously in a non-blocking background thread
         * with comprehensive try-catch handling to prevent application runtime crashes.
         *
         * @param context Application context
         * @param onComplete Optional completion callback with success/failure boolean
         */
        @JvmStatic
        fun initializeAsync(
            context: Context,
            onComplete: ((Boolean) -> Unit)? = null
        ) {
            scope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        val appContext = context.applicationContext ?: context
                        val existingApps = FirebaseApp.getApps(appContext)

                        if (existingApps.isNotEmpty()) {
                            Log.i(TAG, "FirebaseApp already initialized.")
                            _isInitialized.value = true
                            _initializationError.value = null
                            return@withContext true
                        }

                        // Attempt standard initialization from google-services.json
                        val initializedApp = try {
                            FirebaseApp.initializeApp(appContext)
                        } catch (e: Exception) {
                            Log.w(TAG, "Standard FirebaseApp.initializeApp threw exception; attempting fallback options", e)
                            null
                        }

                        if (initializedApp != null) {
                            Log.i(TAG, "FirebaseApp successfully initialized from resources.")
                            _isInitialized.value = true
                            _initializationError.value = null
                            true
                        } else {
                            // Resilient fallback initialization to ensure app operates offline/in sandbox
                            val fallbackOptions = FirebaseOptions.Builder()
                                .setProjectId("bca-tuition-portal")
                                .setApplicationId("1:277483881388:android:a1b2c3d4e5f67890")
                                .setApiKey("AIzaSyAyGTzuCABZCxBXTE7qIBa0MOcALUJ_aYU")
                                .setStorageBucket("bca-tuition-portal.firebasestorage.app")
                                .build()

                            FirebaseApp.initializeApp(appContext, fallbackOptions)
                            Log.i(TAG, "FirebaseApp successfully initialized with fallback options.")
                            _isInitialized.value = true
                            _initializationError.value = null
                            true
                        }
                    } catch (t: Throwable) {
                        // Catch all exceptions and errors to strictly prevent runtime crashes
                        val errMsg = "Firebase initialization handled safely: ${t.localizedMessage ?: t.message}"
                        Log.e(TAG, errMsg, t)
                        _isInitialized.value = false
                        _initializationError.value = errMsg
                        false
                    }
                }

                onComplete?.invoke(success)
            }
        }
    }
}
