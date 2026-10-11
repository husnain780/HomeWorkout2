package com.homeworkout.app.model

data class WorkoutPlan(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val exerciseCount: Int = 0,
    val difficulty: String,
    val exerciseIds: List<String>,
    val category: String = "Full Body",
    val isAiGenerated: Boolean = false
) {
    val subtitle: String
        get() = description
}
