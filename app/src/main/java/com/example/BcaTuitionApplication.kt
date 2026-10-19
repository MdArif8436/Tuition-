package com.example

import android.app.Application
import android.util.Log
import com.example.util.FirebaseDiagnostics

class BcaTuitionApplication : Application() {

    companion object {
        lateinit var instance: BcaTuitionApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Verify/trigger single asynchronous Firebase initialization
        FirebaseInitializer.initializeAsync(this) { success ->
            if (success) {
                Log.i("BcaTuitionApp", "Firebase ready.")
            } else {
                Log.w("BcaTuitionApp", "Firebase unavailable; operating in crash-safe offline mode.")
            }
            FirebaseDiagnostics.initialize(this)
        }
    }
}
