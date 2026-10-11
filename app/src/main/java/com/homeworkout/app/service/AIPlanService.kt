package com.homeworkout.app.service

import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.AIWorkoutDay
import com.homeworkout.app.model.AIWorkoutPlan
import com.homeworkout.app.model.Exercise
import com.homeworkout.app.model.WorkoutHistory
import org.json.JSONObject

class AIPlanService {

    suspend fun generatePlan(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        availableExercises: List<Exercise>,
        history: List<WorkoutHistory>
    ): AIWorkoutPlan {
        if (SupabaseClient.currentUser == null) {
            throw IllegalStateException("Please sign in to your account to generate an AI workout plan.")
        }

        val result = SupabaseClient.Functions.callAIWorkoutPlan(
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout,
            availableExercises = availableExercises,
            history = history
        )

        val json = result.getOrElse { ex ->
            val msg = ex.message?.takeIf { it.isNotBlank() }
                ?: "Unable to reach the Supabase AI Workout Plan service. Please check your connection and try again."
            throw IllegalStateException(msg, ex)
        }

        return parsePlan(
            json = json,
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout,
            availableExercises = availableExercises
        )
    }

    private fun parsePlan(
        json: JSONObject,
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        availableExercises: List<Exercise>
    ): AIWorkoutPlan {

        val allowedIds = availableExercises.map { it.id }.toSet()
        val daysJson = json.getJSONArray("days")

        val days = mutableListOf<AIWorkoutDay>()

        for (index in 0 until daysJson.length()) {
            val dayJson = daysJson.getJSONObject(index)
            val exerciseJson = dayJson.getJSONArray("exerciseIds")
            val exerciseIds = mutableListOf<String>()

            for (exerciseIndex in 0 until exerciseJson.length()) {
                val id = exerciseJson.optString(exerciseIndex).trim()
                if (id.isNotEmpty() && allowedIds.contains(id) && !exerciseIds.contains(id)) {
                    exerciseIds.add(id)
                }
            }

            if (exerciseIds.isEmpty()) {
                exerciseIds.addAll(availableExercises.take(3).map { it.id })
            }

            days.add(
                AIWorkoutDay(
                    dayNumber = dayJson.optInt("day", index + 1),
                    focus = dayJson.optString("focus", "Full Body").ifBlank { "Full Body" },
                    exerciseIds = exerciseIds
                )
            )
        }

        if (days.isEmpty()) {
            throw IllegalStateException("AI Workout Plan returned an empty schedule. Please try generating again.")
        }

        val basePlan = AIWorkoutPlan(
            title = json.optString("title", "Personalized 30-Day Program").ifBlank { "Personalized 30-Day Program" },
            description = json.optString(
                "description",
                "Progressive 30-day training program tailored to your goals and schedule."
            ),
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout,
            days = days,
            generatedByAI = true
        )

        return com.homeworkout.app.model.ThirtyDayProgramGenerator.ensureThirtyDayPlan(basePlan)
    }
}
