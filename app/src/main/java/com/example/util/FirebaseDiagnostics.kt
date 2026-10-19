package com.example.util

import android.content.Context
import android.util.Log
import com.example.FirebaseInitializer
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SystemDiagnosticsInfo(
    val isFirebaseInitialized: Boolean = false,
    val firebaseInitSource: String = "Pending",
    val projectId: String = "",
    val appId: String = "",
    val initializationError: String? = null,
    val hwuiStatus: String = "RGBA_8888 Pinned",
    val eglStatus: String = "Standard Swap Buffer Enabled",
    val diagnosticLogs: List<String> = emptyList()
)

object FirebaseDiagnostics {
    private const val TAG = "FirebaseDiagnostics"

    private val _diagnosticsState = MutableStateFlow(SystemDiagnosticsInfo())
    val diagnosticsState: StateFlow<SystemDiagnosticsInfo> = _diagnosticsState.asStateFlow()

    /**
     * Inspects FirebaseApp status without attempting duplicate initialization.
     */
    fun initialize(context: Context) {
        val logs = mutableListOf<String>()
        var isInit = false
        var source = "None"
        var projId = "bca-tuition-portal"
        var appId = "1:277483881388:android:a1b2c3d4e5f67890"

        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                isInit = true
                source = "Single ContentProvider / App Initializer"
                projId = app.options.projectId ?: projId
                appId = app.options.applicationId
                val logMsg = "Firebase default app active (Project: $projId, AppId: $appId)"
                Log.i(TAG, logMsg)
                logs.add(logMsg)
            } else {
                isInit = FirebaseInitializer.isInitialized.value
                source = if (isInit) "Asynchronous Background Initializer" else "Initializing..."
                logs.add("Firebase initialization status: $source")
            }
        } catch (e: Exception) {
            val err = "Firebase diagnostic check notice: ${e.message}"
            Log.w(TAG, err, e)
            logs.add(err)
            isInit = false
            source = "Diagnostic: ${e.localizedMessage}"
        }

        _diagnosticsState.value = SystemDiagnosticsInfo(
            isFirebaseInitialized = isInit,
            firebaseInitSource = source,
            projectId = projId,
            appId = appId,
            initializationError = FirebaseInitializer.initializationError.value,
            hwuiStatus = "Standard HWUI Pipeline (8-bit sRGB)",
            eglStatus = "System Default EGL Buffer Configuration",
            diagnosticLogs = logs
        )
    }

    fun refreshDiagnostics(context: Context) {
        initialize(context)
    }
}
