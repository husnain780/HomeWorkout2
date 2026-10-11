package com.homeworkout.app.events

sealed class AppEvent {

    data class WorkoutSelected(
        val workoutId: String
    ) : AppEvent()

    data class WorkoutStarted(
        val workoutId: String
    ) : AppEvent()

    data class WorkoutCompleted(
        val workoutId: String
    ) : AppEvent()
}
