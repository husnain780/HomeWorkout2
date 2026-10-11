package com.homeworkout.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutAnalyticsTest {

    private fun record(
        id: String,
        dayOffset: Long,
        calories: Int = 100,
        minutes: Int = 20
    ): WorkoutHistory {

        val timestamp =
            System.currentTimeMillis() -
                (dayOffset * 24L * 60L * 60L * 1000L)

        return WorkoutHistory(
            id = id,
            workoutId = "test_workout",
            workoutName = "Test Workout",
            durationMinutes = minutes,
            exercisesCompleted = 4,
            caloriesBurned = calories,
            completedAt = timestamp
        )
    }

    @Test
    fun totalWorkouts_isCorrect() {

        val history =
            listOf(
                record("1", 0),
                record("2", 1),
                record("3", 2)
            )

        assertEquals(
            3,
            WorkoutAnalytics.totalWorkouts(history)
        )
    }

    @Test
    fun totalCalories_isCorrect() {

        val history =
            listOf(
                record("1", 0, calories = 100),
                record("2", 1, calories = 120)
            )

        assertEquals(
            220,
            WorkoutAnalytics.totalCalories(history)
        )
    }

    @Test
    fun totalMinutes_isCorrect() {

        val history =
            listOf(
                record("1", 0, minutes = 20),
                record("2", 1, minutes = 30)
            )

        assertEquals(
            50,
            WorkoutAnalytics.totalMinutes(history)
        )
    }

    @Test
    fun currentStreak_countsConsecutiveDays() {

        val history =
            listOf(
                record("1", 0),
                record("2", 1),
                record("3", 2)
            )

        assertEquals(
            3,
            WorkoutAnalytics.currentStreak(history)
        )
    }

    @Test
    fun emptyHistory_returnsZeroStreak() {

        assertEquals(
            0,
            WorkoutAnalytics.currentStreak(emptyList())
        )
    }
}
