package com.homeworkout.app.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.homeworkout.app.data.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant

data class UserNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val category: String,
    val isRead: Boolean,
    val createdAt: Long
)

object NotificationRepository {

    private var databaseHelper: WorkoutDatabaseHelper? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var revision by mutableIntStateOf(0)
        private set

    fun initialize(context: Context) {
        if (databaseHelper == null) {
            databaseHelper = WorkoutDatabaseHelper.getInstance(context.applicationContext)
            revision++
        }
    }

    private fun currentUserIdOrNull(): String? {
        return SupabaseClient.currentUser?.id?.takeIf { it.isNotBlank() }
    }

    fun getAll(userId: String? = currentUserIdOrNull()): List<UserNotification> {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return emptyList()
        val db = databaseHelper?.readableDatabase ?: return emptyList()

        val list = mutableListOf<UserNotification>()
        val cursor = db.query(
            "user_notifications",
            null,
            "user_id = ?",
            arrayOf(uid),
            null,
            null,
            "created_at DESC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val userIdx = it.getColumnIndexOrThrow("user_id")
            val titleIdx = it.getColumnIndexOrThrow("title")
            val msgIdx = it.getColumnIndexOrThrow("message")
            val catIdx = it.getColumnIndexOrThrow("category")
            val readIdx = it.getColumnIndexOrThrow("is_read")
            val createdIdx = it.getColumnIndexOrThrow("created_at")

            while (it.moveToNext()) {
                list.add(
                    UserNotification(
                        id = it.getString(idIdx),
                        userId = it.getString(userIdx),
                        title = it.getString(titleIdx),
                        message = it.getString(msgIdx),
                        category = it.getString(catIdx),
                        isRead = it.getInt(readIdx) == 1,
                        createdAt = it.getLong(createdIdx)
                    )
                )
            }
        }
        return list
    }

    fun saveLocal(notification: UserNotification) {
        val db = databaseHelper?.writableDatabase ?: return
        val values = ContentValues().apply {
            put("id", notification.id)
            put("user_id", notification.userId)
            put("title", notification.title)
            put("message", notification.message)
            put("category", notification.category)
            put("is_read", if (notification.isRead) 1 else 0)
            put("created_at", notification.createdAt)
        }
        db.insertWithOnConflict(
            "user_notifications",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        revision++
    }

    fun addNotification(
        title: String,
        message: String,
        category: String = "general",
        id: String = "notif_${System.currentTimeMillis()}",
        syncToCloud: Boolean = true
    ) {
        val uid = currentUserIdOrNull() ?: return
        if (!UserPreferencesRepository.isNotificationsEnabled(uid) && category != "account") {
            return
        }

        val now = System.currentTimeMillis()
        val item = UserNotification(
            id = id,
            userId = uid,
            title = title,
            message = message,
            category = category,
            isRead = false,
            createdAt = now
        )
        saveLocal(item)

        if (syncToCloud) {
            scope.launch {
                val body = JSONObject().apply {
                    put("id", item.id)
                    put("user_id", item.userId)
                    put("title", item.title)
                    put("message", item.message)
                    put("category", item.category)
                    put("is_read", item.isRead)
                    put("created_at", Instant.ofEpochMilli(item.createdAt).toString())
                }
                SupabaseClient.Database.upsertUserNotification(body)
            }
        }
    }

    fun recordWorkoutCompletedNotification(
        workoutId: String,
        workoutName: String,
        durationMinutes: Int,
        calories: Int,
        streakDays: Int
    ) {
        val uid = currentUserIdOrNull() ?: return
        if (!UserPreferencesRepository.isNotificationsEnabled(uid)) return

        addNotification(
            id = "workout_$workoutId",
            title = "Workout Completed: $workoutName",
            message = "Logged $durationMinutes min session (~$calories est. kcal burned).",
            category = "workout",
            syncToCloud = true
        )

        if (streakDays >= 1) {
            val todayStr = java.time.LocalDate.now().toString()
            addNotification(
                id = "streak_${todayStr}_${streakDays}",
                title = "Active Streak: $streakDays ${if (streakDays == 1) "Day" else "Days"}",
                message = "You have completed workouts on $streakDays consecutive ${if (streakDays == 1) "day" else "days"} based on your saved workout history.",
                category = "streak",
                syncToCloud = true
            )
        }
    }

    fun recordAIPlanSavedNotification(
        planId: String,
        planTitle: String,
        daysPerWeek: Int
    ) {
        val uid = currentUserIdOrNull() ?: return
        if (!UserPreferencesRepository.isNotificationsEnabled(uid)) return

        addNotification(
            id = "plan_$planId",
            title = "AI Routine Saved",
            message = "\"$planTitle\" ($daysPerWeek days/week) is now saved in your Workout Plans.",
            category = "ai_plan",
            syncToCloud = true
        )
    }

    fun markAllRead() {
        val uid = currentUserIdOrNull() ?: return
        val db = databaseHelper?.writableDatabase ?: return
        val currentItems = getAll(uid)
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        db.update("user_notifications", values, "user_id = ?", arrayOf(uid))
        revision++

        scope.launch {
            currentItems.filter { !it.isRead }.forEach { item ->
                val body = JSONObject().apply {
                    put("id", item.id)
                    put("user_id", item.userId)
                    put("title", item.title)
                    put("message", item.message)
                    put("category", item.category)
                    put("is_read", true)
                    put("created_at", Instant.ofEpochMilli(item.createdAt).toString())
                }
                SupabaseClient.Database.upsertUserNotification(body)
            }
        }
    }

    fun deleteNotification(id: String) {
        val uid = currentUserIdOrNull() ?: return
        val db = databaseHelper?.writableDatabase ?: return
        db.delete("user_notifications", "user_id = ? AND id = ?", arrayOf(uid, id))
        revision++

        scope.launch {
            SupabaseClient.Database.deleteUserNotification(uid, id)
        }
    }

    suspend fun syncWithCloud(userId: String) {
        val remoteResult = SupabaseClient.Database.getUserNotifications(userId)
        val remoteList = remoteResult.getOrNull() ?: return
        val remoteIds = mutableSetOf<String>()

        remoteList.forEach { obj ->
            val id = obj.optString("id")
            if (id.isNotBlank()) {
                remoteIds.add(id)
                val createdIso = obj.optString("created_at")
                val createdAtMs = runCatching { Instant.parse(createdIso).toEpochMilli() }
                    .getOrDefault(System.currentTimeMillis())
                saveLocal(
                    UserNotification(
                        id = id,
                        userId = userId,
                        title = obj.optString("title", "Notification"),
                        message = obj.optString("message", ""),
                        category = obj.optString("category", "general"),
                        isRead = obj.optBoolean("is_read", false),
                        createdAt = createdAtMs
                    )
                )
            }
        }

        // Upload any local notifications not yet in remote
        val localItems = getAll(userId)
        localItems.filter { !remoteIds.contains(it.id) }.forEach { local ->
            val body = JSONObject().apply {
                put("id", local.id)
                put("user_id", local.userId)
                put("title", local.title)
                put("message", local.message)
                put("category", local.category)
                put("is_read", local.isRead)
                put("created_at", Instant.ofEpochMilli(local.createdAt).toString())
            }
            SupabaseClient.Database.upsertUserNotification(body)
        }

        // Ensure at least a welcome notification exists if the user has 0 notifications
        if (getAll(userId).isEmpty()) {
            val welcome = UserNotification(
                id = "welcome_$userId",
                userId = userId,
                title = "Account Ready",
                message = "Welcome to HomeWorkout! Complete a workout or generate an AI plan to track your real progress.",
                category = "account",
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            saveLocal(welcome)
            val body = JSONObject().apply {
                put("id", welcome.id)
                put("user_id", welcome.userId)
                put("title", welcome.title)
                put("message", welcome.message)
                put("category", welcome.category)
                put("is_read", welcome.isRead)
                put("created_at", Instant.ofEpochMilli(welcome.createdAt).toString())
            }
            SupabaseClient.Database.upsertUserNotification(body)
        }
        revision++
    }
}
