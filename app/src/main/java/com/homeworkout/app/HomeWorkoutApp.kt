package com.homeworkout.app

import android.app.Application
import android.util.Log
import com.homeworkout.app.data.supabase.SupabaseClient

class HomeWorkoutApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "FATAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.d(TAG, "Initializing Supabase Client...")
        SupabaseClient.initialize(this)
        try {
            com.google.android.filament.Filament.init()
            com.google.android.filament.gltfio.Gltfio.init()
            Log.d(TAG, "Filament and Gltfio native libraries initialized successfully.")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize Filament/Gltfio native libraries", t)
        }
    }

    companion object {
        private const val TAG = "HomeWorkoutApp"
    }
}
