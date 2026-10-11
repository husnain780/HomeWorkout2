package com.homeworkout.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class HomeWorkoutApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Installing DebugAppCheckProviderFactory for debug build")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // For production release, install PlayIntegrityAppCheckProviderFactory if present on classpath
            try {
                val factoryClass = Class.forName("com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory")
                val method = factoryClass.getMethod("getInstance")
                val factory = method.invoke(null) as AppCheckProviderFactory
                firebaseAppCheck.installAppCheckProviderFactory(factory)
                Log.d(TAG, "Installed PlayIntegrityAppCheckProviderFactory for release build")
            } catch (t: Throwable) {
                Log.w(TAG, "PlayIntegrityAppCheckProviderFactory not available on classpath: ${t.message}")
            }
        }
    }

    companion object {
        private const val TAG = "HomeWorkoutApp"
    }
}
