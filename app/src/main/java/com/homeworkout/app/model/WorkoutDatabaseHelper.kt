package com.homeworkout.app.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WorkoutDatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {
        createTablesV4(db)
    }

    private fun createTablesV4(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_history (
                id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                workout_id TEXT NOT NULL,
                workout_name TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                exercises_completed INTEGER NOT NULL,
                calories_burned INTEGER NOT NULL,
                completed_at INTEGER NOT NULL,
                synced INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (user_id, id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS saved_workout_plans (
                id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                goal TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                days_per_week INTEGER NOT NULL,
                minutes_per_workout INTEGER NOT NULL,
                exercise_ids TEXT NOT NULL,
                days_json TEXT NOT NULL,
                generated_by_ai INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                start_date TEXT NOT NULL DEFAULT '',
                current_day INTEGER NOT NULL DEFAULT 1,
                completed_days_json TEXT NOT NULL DEFAULT '[]',
                PRIMARY KEY (user_id, id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ai_coach_messages (
                id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                message_text TEXT NOT NULL,
                is_user INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                PRIMARY KEY (user_id, id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_notifications (
                id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'general',
                is_read INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                PRIMARY KEY (user_id, id)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                ALTER TABLE workout_history
                ADD COLUMN completed_at INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )

            db.execSQL(
                """
                UPDATE workout_history
                SET completed_at = CAST(id AS INTEGER)
                WHERE completed_at = 0
                """.trimIndent()
            )
        }

        if (oldVersion < 3) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS workout_history_v3 (
                    id TEXT NOT NULL,
                    user_id TEXT NOT NULL,
                    workout_id TEXT NOT NULL,
                    workout_name TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    exercises_completed INTEGER NOT NULL,
                    calories_burned INTEGER NOT NULL,
                    completed_at INTEGER NOT NULL,
                    synced INTEGER NOT NULL DEFAULT 1,
                    PRIMARY KEY (user_id, id)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT OR IGNORE INTO workout_history_v3 (
                    id, user_id, workout_id, workout_name,
                    duration_minutes, exercises_completed, calories_burned,
                    completed_at, synced
                )
                SELECT
                    id,
                    '$LEGACY_OWNER_USER_ID',
                    workout_id,
                    workout_name,
                    duration_minutes,
                    exercises_completed,
                    calories_burned,
                    completed_at,
                    1
                FROM workout_history
                """.trimIndent()
            )

            db.execSQL("DROP TABLE IF EXISTS workout_history")
            db.execSQL("ALTER TABLE workout_history_v3 RENAME TO workout_history")

            createTablesV4(db)
        }

        if (oldVersion in 3 until 4) {
            runCatching {
                db.execSQL("ALTER TABLE saved_workout_plans ADD COLUMN start_date TEXT NOT NULL DEFAULT ''")
            }
            runCatching {
                db.execSQL("ALTER TABLE saved_workout_plans ADD COLUMN current_day INTEGER NOT NULL DEFAULT 1")
            }
            runCatching {
                db.execSQL("ALTER TABLE saved_workout_plans ADD COLUMN completed_days_json TEXT NOT NULL DEFAULT '[]'")
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "workout_history.db"
        private const val DATABASE_VERSION = 4
        private const val LEGACY_OWNER_USER_ID = "e67967ee-07e1-4d43-af6d-e0a02d43a3cc"

        @Volatile
        private var instance: WorkoutDatabaseHelper? = null

        fun getInstance(context: Context): WorkoutDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: WorkoutDatabaseHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
