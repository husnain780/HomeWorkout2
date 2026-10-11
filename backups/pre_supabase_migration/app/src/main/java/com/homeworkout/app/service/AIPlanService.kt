package com.homeworkout.app.service

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.homeworkout.app.model.AIWorkoutDay
import com.homeworkout.app.model.AIWorkoutPlan
import com.homeworkout.app.model.Exercise
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistory
import org.json.JSONObject

class AIPlanService {

    private val model =
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-2.5-flash"
        )

    suspend fun generatePlan(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        availableExercises: List<Exercise>,
        history: List<WorkoutHistory>
    ): AIWorkoutPlan {

        val allowedExercises =
            availableExercises.joinToString("\n") { exercise ->
                "${exercise.id} | ${exercise.name} | ${exercise.category} | ${exercise.difficulty}"
            }

        val prompt =
            """
            Create a safe, age-appropriate home workout plan.

            Goal: $goal
            Difficulty: $difficulty
            Days per week: $daysPerWeek
            Minutes per workout: $minutesPerWorkout

            User progress:
            Completed workouts: ${WorkoutAnalytics.totalWorkouts(history)}
            Total workout minutes: ${WorkoutAnalytics.totalMinutes(history)}
            Current streak: ${WorkoutAnalytics.currentStreak(history)}

            ONLY use these exercise IDs:
            $allowedExercises

            Rules:
            - Use only exercise IDs from the catalog.
            - Return exactly $daysPerWeek days.
            - Use 2 to 4 exercises per day.
            - Keep the routine manageable and progressive.
            - Do not recommend dangerous challenges.
            - Do not recommend extreme exercise.
            - Do not include restrictive eating.
            - Do not include body-image goals.
            - Do not diagnose medical conditions.
            - Keep the plan suitable for a general teen user.

            Return ONLY valid JSON.
            Do not use markdown.
            Do not add explanation.

            JSON format:

            {
              "title": "string",
              "description": "string",
              "days": [
                {
                  "day": 1,
                  "focus": "string",
                  "exerciseIds": ["id1", "id2"]
                }
              ]
            }
            """.trimIndent()

        return try {

            val response =
                model.generateContent(prompt)

            val responseText =
                response.text
                    ?.trim()
                    ?: throw IllegalStateException(
                        "Empty AI response"
                    )

            parsePlan(
                raw = responseText,
                goal = goal,
                difficulty = difficulty,
                daysPerWeek = daysPerWeek,
                minutesPerWorkout = minutesPerWorkout,
                availableExercises = availableExercises
            )

        } catch (exception: Exception) {

            fallbackPlan(
                goal = goal,
                difficulty = difficulty,
                daysPerWeek = daysPerWeek,
                minutesPerWorkout = minutesPerWorkout,
                availableExercises = availableExercises
            )
        }
    }

    private fun parsePlan(
        raw: String,
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        availableExercises: List<Exercise>
    ): AIWorkoutPlan {

        val cleaned =
            raw
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

        val start =
            cleaned.indexOf("{")

        val end =
            cleaned.lastIndexOf("}")

        if (start < 0 || end <= start) {
            throw IllegalStateException(
                "Invalid JSON"
            )
        }

        val json =
            JSONObject(
                cleaned.substring(
                    start,
                    end + 1
                )
            )

        val allowedIds =
            availableExercises
                .map { it.id }
                .toSet()

        val daysJson =
            json.getJSONArray("days")

        if (daysJson.length() != daysPerWeek) {
            throw IllegalStateException(
                "Invalid number of days"
            )
        }

        val days =
            mutableListOf<AIWorkoutDay>()

        for (index in 0 until daysJson.length()) {

            val dayJson =
                daysJson.getJSONObject(index)

            val exerciseJson =
                dayJson.getJSONArray("exerciseIds")

            val exerciseIds =
                mutableListOf<String>()

            for (
                exerciseIndex in 0 until exerciseJson.length()
            ) {

                val id =
                    exerciseJson
                        .optString(exerciseIndex)
                        .trim()

                if (
                    id.isNotEmpty() &&
                    allowedIds.contains(id) &&
                    !exerciseIds.contains(id)
                ) {
                    exerciseIds.add(id)
                }
            }

            if (exerciseIds.size !in 2..4) {
                throw IllegalStateException(
                    "Invalid exercise count"
                )
            }

            days.add(
                AIWorkoutDay(
                    dayNumber =
                        dayJson.optInt(
                            "day",
                            index + 1
                        ),
                    focus =
                        dayJson.optString(
                            "focus",
                            "Full Body"
                        ).ifBlank {
                            "Full Body"
                        },
                    exerciseIds = exerciseIds
                )
            )
        }

        return AIWorkoutPlan(
            title =
                json.optString(
                    "title",
                    "My AI Workout Plan"
                ).ifBlank {
                    "My AI Workout Plan"
                },
            description =
                json.optString(
                    "description",
                    "A personalized workout plan."
                ),
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout,
            days = days,
            generatedByAI = true
        )
    }

    private fun fallbackPlan(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        availableExercises: List<Exercise>
    ): AIWorkoutPlan {

        val ids =
            availableExercises
                .map { it.id }

        val selected =
            ids.take(4)

        val days =
            (1..daysPerWeek).map { day ->

                val exercises =
                    if (selected.size >= 2) {
                        selected
                    } else {
                        ids
                    }

                AIWorkoutDay(
                    dayNumber = day,
                    focus = when {
                        goal.contains(
                            "Strength",
                            ignoreCase = true
                        ) -> "Strength"

                        goal.contains(
                            "Cardio",
                            ignoreCase = true
                        ) -> "Cardio"

                        goal.contains(
                            "Core",
                            ignoreCase = true
                        ) -> "Core"

                        else -> "Full Body"
                    },
                    exerciseIds = exercises.take(4)
                )
            }

        return AIWorkoutPlan(
            title = "Personalized Home Plan",
            description =
                "A local fallback plan built from the HomeWorkout exercise catalog.",
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout,
            days = days,
            generatedByAI = false
        )
    }
}
