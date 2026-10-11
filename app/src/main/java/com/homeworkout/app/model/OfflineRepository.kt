package com.homeworkout.app.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.homeworkout.app.R
object OfflineRepository {

    private var databaseHelper: OfflineDatabaseHelper? = null

    fun initialize(context: Context) {
        if (databaseHelper != null) {
            return
        }

        databaseHelper =
            OfflineDatabaseHelper(
                context.applicationContext
            )

        runCatching {
            seedCatalog()
        }.onFailure { ex ->
            android.util.Log.e("OfflineRepository", "Error seeding offline catalog", ex)
        }
    }

    private fun database(): SQLiteDatabase {
        return databaseHelper?.writableDatabase
            ?: error(
                "OfflineRepository is not initialized"
            )
    }

    private fun seedCatalog() {
        val db = database()

        val exerciseCount = runCatching {
            db.rawQuery(
                "SELECT COUNT(*) FROM cached_exercises",
                null
            ).use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            }
        }.getOrDefault(0)

        if (exerciseCount == 0) {
            db.beginTransaction()
            try {
                ExerciseRepository.exercises.forEach { exercise ->

                val values =
                    ContentValues().apply {
                        put("id", exercise.id)
                        put("name", exercise.name)
                        put("category", exercise.category)
                        put(
                            "difficulty",
                            exercise.difficulty
                        )
                        put(
                            "duration_seconds",
                            exercise.durationSeconds
                        )
                        put(
                            "calories",
                            exercise.calories
                        )
                        put(
                            "description",
                            exercise.description
                        )
                        put(
                            "instructions",
                            exercise.instructions.joinToString(
                                "\u001F"
                            )
                        )
                    }

                db.insert(
                    "cached_exercises",
                    null,
                    values
                )
            }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        db.beginTransaction()
        try {
            WorkoutPlanRepository.catalogPlans.forEach { plan ->
                val values =
                    ContentValues().apply {
                        put("id", plan.id)
                        put("title", plan.title)
                        put(
                            "description",
                            plan.description
                        )
                        put(
                            "duration_minutes",
                            plan.durationMinutes
                        )
                        put(
                            "exercise_count",
                            plan.exerciseCount
                        )
                        put(
                            "difficulty",
                            plan.difficulty
                        )
                        put(
                            "exercise_ids",
                            plan.exerciseIds.joinToString(",")
                        )
                    }

                db.insertWithOnConflict(
                    "cached_plans",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            val metadata =
                ContentValues().apply {
                    put("key", "catalog_version")
                    put("value", "1")
                }

            db.insertWithOnConflict(
                "cache_metadata",
                null,
                metadata,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedExercises(): List<Exercise> {

        val result =
            mutableListOf<Exercise>()

        val cursor =
            database().query(
                "cached_exercises",
                null,
                null,
                null,
                null,
                null,
                "name ASC"
            )

        cursor.use {

            while (it.moveToNext()) {

                val instructions =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            "instructions"
                        )
                    )
                        .split("\u001F")
                        .filter { value ->
                            value.isNotBlank()
                        }

                result.add(
                    Exercise(
                        id =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "id"
                                )
                            ),
                        name =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "name"
                                )
                            ),
                        category =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "category"
                                )
                            ),
                        difficulty =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "difficulty"
                                )
                            ),
                        durationSeconds =
                            it.getInt(
                                it.getColumnIndexOrThrow(
                                    "duration_seconds"
                                )
                            ),
                        calories =
                            it.getInt(
                                it.getColumnIndexOrThrow(
                                    "calories"
                                )
                            ),
                        description =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "description"
                                )
                            ),
                        instructions = instructions,
                        imageRes = ExerciseRepository.imageRes(
                            it.getString(it.getColumnIndexOrThrow("id"))
                        )
                    )
                )
            }
        }

        return result
    }

    fun getCachedPlans(): List<WorkoutPlan> {

        val result =
            mutableListOf<WorkoutPlan>()

        val cursor =
            database().query(
                "cached_plans",
                null,
                null,
                null,
                null,
                null,
                "title ASC"
            )

        cursor.use {

            while (it.moveToNext()) {

                val exerciseIds =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            "exercise_ids"
                        )
                    )
                        .split(",")
                        .filter { value ->
                            value.isNotBlank()
                        }

                result.add(
                    WorkoutPlan(
                        id =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "id"
                                )
                            ),
                        title =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "title"
                                )
                            ),
                        description =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "description"
                                )
                            ),
                        durationMinutes =
                            it.getInt(
                                it.getColumnIndexOrThrow(
                                    "duration_minutes"
                                )
                            ),
                        exerciseCount =
                            it.getInt(
                                it.getColumnIndexOrThrow(
                                    "exercise_count"
                                )
                            ),
                        difficulty =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    "difficulty"
                                )
                            ),
                        exerciseIds = exerciseIds
                    )
                )
            }
        }

        return result
    }

    fun getExercise(
        exerciseId: String
    ): Exercise? {
        return getCachedExercises()
            .firstOrNull {
                it.id == exerciseId
            }
    }

    fun getPlan(
        planId: String
    ): WorkoutPlan? {
        return getCachedPlans()
            .firstOrNull {
                it.id == planId
            }
    }

    fun getCachedExerciseCount(): Int {
        return getCachedExercises().size
    }

    fun getCachedPlanCount(): Int {
        return getCachedPlans().size
    }
}
