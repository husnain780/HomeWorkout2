package com.homeworkout.app.controller

import com.homeworkout.app.events.AIPlanEvent
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.AIWorkoutPlan
import com.homeworkout.app.model.OfflineRepository
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.service.AIPlanService

class AIPlanController(
    private val eventBus: EventBus,
    private val service: AIPlanService =
        AIPlanService()
) {

    suspend fun generate(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int
    ): AIWorkoutPlan {

        eventBus.publish(
            AIPlanEvent.GenerationRequested(
                goal = goal,
                difficulty = difficulty,
                daysPerWeek = daysPerWeek,
                minutesPerWorkout = minutesPerWorkout
            )
        )

        return try {

            val plan =
                service.generatePlan(
                    goal = goal,
                    difficulty = difficulty,
                    daysPerWeek = daysPerWeek,
                    minutesPerWorkout = minutesPerWorkout,
                    availableExercises =
                        OfflineRepository.getCachedExercises(),
                    history =
                        WorkoutHistoryRepository.getAll()
                )

            eventBus.publish(
                AIPlanEvent.PlanGenerated(
                    title = plan.title
                )
            )

            plan

        } catch (exception: Exception) {

            eventBus.publish(
                AIPlanEvent.GenerationFailed(
                    message =
                        exception.message
                            ?: "Generation failed"
                )
            )

            throw exception
        }
    }
}
