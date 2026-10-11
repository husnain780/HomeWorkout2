package com.homeworkout.app.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OfflineDatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE cached_exercises (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                duration_seconds INTEGER NOT NULL,
                calories INTEGER NOT NULL,
                description TEXT NOT NULL,
                instructions TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cached_plans (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                exercise_count INTEGER NOT NULL,
                difficulty TEXT NOT NULL,
                exercise_ids TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cache_metadata (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS cached_exercises")
        db.execSQL("DROP TABLE IF EXISTS cached_plans")
        db.execSQL("DROP TABLE IF EXISTS cache_metadata")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "offline_cache.db"
        private const val DATABASE_VERSION = 1
    }
}
