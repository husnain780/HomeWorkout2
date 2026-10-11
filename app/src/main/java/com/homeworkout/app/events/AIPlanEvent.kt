package com.homeworkout.app.events

sealed class AIPlanEvent {

    data class GenerationRequested(
        val goal: String,
        val difficulty: String,
        val daysPerWeek: Int,
        val minutesPerWorkout: Int
    ) : AIPlanEvent()

    data class PlanGenerated(
        val title: String
    ) : AIPlanEvent()

    data class GenerationFailed(
        val message: String
    ) : AIPlanEvent()
}
