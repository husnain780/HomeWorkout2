package com.homeworkout.app.model

data class WorkoutHistory(
    val id: String,
    val workoutId: String,
    val workoutName: String,
    val durationMinutes: Int,
    val exercisesCompleted: Int,
    val caloriesBurned: Int,
    val completedAt: Long = System.currentTimeMillis()
)
