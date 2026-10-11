package com.homeworkout.app.controller

import com.homeworkout.app.events.AppEvent
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.Exercise
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.OfflineRepository
import com.homeworkout.app.model.WorkoutPlanRepository

class WorkoutController(
    private val eventBus: EventBus
) {

    fun selectWorkout(workoutId: String) {
        eventBus.publish(
            AppEvent.WorkoutSelected(workoutId)
        )
    }

    fun startWorkout(workoutId: String) {
        eventBus.publish(
            AppEvent.WorkoutStarted(workoutId)
        )
    }

    fun startPlan(planId: String): Boolean {
        val plan = WorkoutPlanRepository.findPlanById(planId)
            ?: OfflineRepository.getPlan(planId)
            ?: return false

        selectWorkout(plan.id)
        startWorkout(plan.id)

        return true
    }

    fun getExercisesForPlan(planId: String): List<Exercise> {
        val plan = WorkoutPlanRepository.findPlanById(planId)
            ?: OfflineRepository.getPlan(planId)
            ?: return emptyList()

        val exercises = OfflineRepository.getCachedExercises().ifEmpty {
            ExerciseRepository.exercises
        }

        return plan.exerciseIds.mapNotNull { exerciseId ->
            exercises.firstOrNull { it.id == exerciseId }
        }
    }

    fun completeWorkout(workoutId: String) {
        eventBus.publish(
            AppEvent.WorkoutCompleted(workoutId)
        )
    }
}
