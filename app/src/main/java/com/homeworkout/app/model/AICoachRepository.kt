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

object AICoachRepository {

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

    fun getMessages(userId: String? = currentUserIdOrNull()): List<AICoachMessage> {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return emptyList()
        val db = databaseHelper?.readableDatabase ?: return emptyList()

        val list = mutableListOf<AICoachMessage>()
        val cursor = db.query(
            "ai_coach_messages",
            null,
            "user_id = ?",
            arrayOf(uid),
            null,
            null,
            "created_at ASC, id ASC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val textIdx = it.getColumnIndexOrThrow("message_text")
            val isUserIdx = it.getColumnIndexOrThrow("is_user")

            while (it.moveToNext()) {
                list.add(
                    AICoachMessage(
                        id = it.getString(idIdx),
                        text = it.getString(textIdx),
                        isUser = it.getInt(isUserIdx) == 1,
                        isError = false,
                        canRetry = false
                    )
                )
            }
        }
        return list
    }

    fun saveMessage(
        message: AICoachMessage,
        userId: String? = currentUserIdOrNull(),
        createdAtMs: Long = System.currentTimeMillis(),
        syncToCloud: Boolean = true
    ) {
        if (message.isError) return
        val uid = userId?.takeIf { it.isNotBlank() } ?: return
        val db = databaseHelper?.writableDatabase ?: return

        val values = ContentValues().apply {
            put("id", message.id)
            put("user_id", uid)
            put("message_text", message.text)
            put("is_user", if (message.isUser) 1 else 0)
            put("created_at", createdAtMs)
        }
        db.insertWithOnConflict(
            "ai_coach_messages",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        revision++

        if (syncToCloud) {
            scope.launch {
                val body = JSONObject().apply {
                    put("id", message.id)
                    put("user_id", uid)
                    put("message_text", message.text)
                    put("is_user", message.isUser)
                    put("created_at", Instant.ofEpochMilli(createdAtMs).toString())
                }
                SupabaseClient.Database.upsertAICoachMessage(body)
            }
        }
    }

    fun clearHistory(userId: String? = currentUserIdOrNull()) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return
        val db = databaseHelper?.writableDatabase ?: return
        db.delete("ai_coach_messages", "user_id = ?", arrayOf(uid))
        revision++

        scope.launch {
            SupabaseClient.Database.clearAICoachMessages(uid)
        }
    }

    suspend fun syncWithCloud(userId: String) {
        val remoteResult = SupabaseClient.Database.getAICoachMessages(userId)
        val remoteList = remoteResult.getOrNull() ?: return
        val remoteIds = mutableSetOf<String>()

        remoteList.forEach { obj ->
            val id = obj.optString("id")
            if (id.isNotBlank()) {
                remoteIds.add(id)
                val createdIso = obj.optString("created_at")
                val createdAtMs = runCatching { Instant.parse(createdIso).toEpochMilli() }
                    .getOrDefault(System.currentTimeMillis())
                saveMessage(
                    message = AICoachMessage(
                        id = id,
                        text = obj.optString("message_text", ""),
                        isUser = obj.optBoolean("is_user", true)
                    ),
                    userId = userId,
                    createdAtMs = createdAtMs,
                    syncToCloud = false
                )
            }
        }

        val db = databaseHelper?.readableDatabase ?: return
        val cursor = db.query(
            "ai_coach_messages",
            null,
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "created_at ASC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val textIdx = it.getColumnIndexOrThrow("message_text")
            val isUserIdx = it.getColumnIndexOrThrow("is_user")
            val createdIdx = it.getColumnIndexOrThrow("created_at")
            while (it.moveToNext()) {
                val id = it.getString(idIdx)
                if (!remoteIds.contains(id)) {
                    val body = JSONObject().apply {
                        put("id", id)
                        put("user_id", userId)
                        put("message_text", it.getString(textIdx))
                        put("is_user", it.getInt(isUserIdx) == 1)
                        put("created_at", Instant.ofEpochMilli(it.getLong(createdIdx)).toString())
                    }
                    SupabaseClient.Database.upsertAICoachMessage(body)
                }
            }
        }
        revision++
    }
}
