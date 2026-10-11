package com.homeworkout.app.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.homeworkout.app.data.supabase.SupabaseClient

object WorkoutHistoryRepository {

    private var databaseHelper: WorkoutDatabaseHelper? = null

    var revision by mutableIntStateOf(0)
        private set

    fun initialize(context: android.content.Context) {
        if (databaseHelper == null) {
            databaseHelper = WorkoutDatabaseHelper.getInstance(context.applicationContext)
            revision++
        }
    }

    private fun database(): SQLiteDatabase {
        return databaseHelper?.writableDatabase
            ?: error("WorkoutHistoryRepository is not initialized")
    }

    private fun currentUserIdOrNull(): String? {
        return SupabaseClient.currentUser?.id?.takeIf { it.isNotBlank() }
    }

    fun add(
        record: WorkoutHistory,
        userId: String? = currentUserIdOrNull(),
        syncToCloud: Boolean = true
    ) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return
        saveLocal(record = record, userId = uid, synced = false)
        revision++

        if (syncToCloud) {
            try {
                com.homeworkout.app.service.CloudSyncRepository.syncWorkout(record)
            } catch (_: Exception) {
            }
        }
    }

    fun saveLocalIfMissing(
        record: WorkoutHistory,
        userId: String? = currentUserIdOrNull(),
        synced: Boolean = true
    ) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return
        saveLocal(record = record, userId = uid, synced = synced)
        revision++
    }

    fun markSynced(id: String, userId: String) {
        val db = databaseHelper?.writableDatabase ?: return
        val values = ContentValues().apply {
            put("synced", 1)
        }
        db.update(
            "workout_history",
            values,
            "user_id = ? AND id = ?",
            arrayOf(userId, id)
        )
        revision++
    }

    fun isSynced(id: String, userId: String? = currentUserIdOrNull()): Boolean {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return false
        val db = databaseHelper?.readableDatabase ?: return false
        val cursor = db.query(
            "workout_history",
            arrayOf("synced"),
            "user_id = ? AND id = ?",
            arrayOf(uid, id),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0) == 1
            }
        }
        return false
    }

    private fun saveLocal(
        record: WorkoutHistory,
        userId: String,
        synced: Boolean
    ) {
        val values = ContentValues().apply {
            put("id", record.id)
            put("user_id", userId)
            put("workout_id", record.workoutId)
            put("workout_name", record.workoutName)
            put("duration_minutes", record.durationMinutes)
            put("exercises_completed", record.exercisesCompleted)
            put("calories_burned", record.caloriesBurned)
            put("completed_at", record.completedAt)
            put("synced", if (synced) 1 else 0)
        }

        database().insertWithOnConflict(
            "workout_history",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getUnsynced(userId: String): List<WorkoutHistory> {
        if (userId.isBlank() || databaseHelper == null) return emptyList()
        val result = mutableListOf<WorkoutHistory>()
        val cursor = database().query(
            "workout_history",
            null,
            "user_id = ? AND synced = 0",
            arrayOf(userId),
            null,
            null,
            "completed_at ASC, id ASC"
        )
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow("id")
            val workoutIdIndex = it.getColumnIndexOrThrow("workout_id")
            val workoutNameIndex = it.getColumnIndexOrThrow("workout_name")
            val durationIndex = it.getColumnIndexOrThrow("duration_minutes")
            val exercisesIndex = it.getColumnIndexOrThrow("exercises_completed")
            val caloriesIndex = it.getColumnIndexOrThrow("calories_burned")
            val completedAtIndex = it.getColumnIndexOrThrow("completed_at")

            while (it.moveToNext()) {
                result.add(
                    WorkoutHistory(
                        id = it.getString(idIndex),
                        workoutId = it.getString(workoutIdIndex),
                        workoutName = it.getString(workoutNameIndex),
                        durationMinutes = it.getInt(durationIndex),
                        exercisesCompleted = it.getInt(exercisesIndex),
                        caloriesBurned = it.getInt(caloriesIndex),
                        completedAt = it.getLong(completedAtIndex)
                    )
                )
            }
        }
        return result
    }

    fun getAll(userId: String? = currentUserIdOrNull()): List<WorkoutHistory> {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return emptyList()
        if (databaseHelper == null) return emptyList()

        val result = mutableListOf<WorkoutHistory>()

        val cursor = database().query(
            "workout_history",
            null,
            "user_id = ?",
            arrayOf(uid),
            null,
            null,
            "completed_at DESC, id DESC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow("id")
            val workoutIdIndex = it.getColumnIndexOrThrow("workout_id")
            val workoutNameIndex = it.getColumnIndexOrThrow("workout_name")
            val durationIndex = it.getColumnIndexOrThrow("duration_minutes")
            val exercisesIndex = it.getColumnIndexOrThrow("exercises_completed")
            val caloriesIndex = it.getColumnIndexOrThrow("calories_burned")
            val completedAtIndex = it.getColumnIndexOrThrow("completed_at")

            while (it.moveToNext()) {
                result.add(
                    WorkoutHistory(
                        id = it.getString(idIndex),
                        workoutId = it.getString(workoutIdIndex),
                        workoutName = it.getString(workoutNameIndex),
                        durationMinutes = it.getInt(durationIndex),
                        exercisesCompleted = it.getInt(exercisesIndex),
                        caloriesBurned = it.getInt(caloriesIndex),
                        completedAt = it.getLong(completedAtIndex)
                    )
                )
            }
        }

        return result
    }

    fun getTotalWorkouts(): Int {
        return getAll().size
    }

    fun getTotalCalories(): Int {
        return getAll().sumOf { it.caloriesBurned }
    }

    fun getTotalMinutes(): Int {
        return getAll().sumOf { it.durationMinutes }
    }

    fun notifyChanged() {
        revision++
    }

    fun clearForCurrentUser() {
        val uid = currentUserIdOrNull() ?: return
        database().delete("workout_history", "user_id = ?", arrayOf(uid))
        revision++
    }
}
