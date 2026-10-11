package com.homeworkout.app.controller

import com.homeworkout.app.events.AIEvent
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.AICoachMessage
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.service.AICoachErrorType
import com.homeworkout.app.service.AICoachResult
import com.homeworkout.app.service.AICoachService

class AICoachController(
    private val eventBus: EventBus,
    private val service: AICoachService =
        AICoachService()
) {

    suspend fun ask(
        message: String,
        history: List<WorkoutHistory>
    ): AICoachResult {

        eventBus.publish(
            AIEvent.MessageSent(message)
        )

        return try {
            val result =
                service.sendMessage(
                    userMessage = message,
                    history = history
                )

            when (result) {
                is AICoachResult.Success -> {
                    eventBus.publish(
                        AIEvent.ResponseReceived(result.text)
                    )
                }
                is AICoachResult.Error -> {
                    eventBus.publish(
                        AIEvent.Error(result.message)
                    )
                }
            }

            result
        } catch (exception: Exception) {
            val error =
                exception.message
                    ?: "AI Coach error"

            eventBus.publish(
                AIEvent.Error(error)
            )

            AICoachResult.Error(
                message = "I couldn't process that request right now.",
                errorType = AICoachErrorType.GENERIC_ERROR,
                canRetry = true
            )
        }
    }

    fun userMessage(
        text: String
    ): AICoachMessage {
        return AICoachMessage(
            id = "user_${System.currentTimeMillis()}",
            text = text,
            isUser = true
        )
    }

    fun coachMessage(
        text: String
    ): AICoachMessage {
        return AICoachMessage(
            id = "coach_${System.currentTimeMillis()}",
            text = text,
            isUser = false
        )
    }

    fun coachErrorMessage(
        text: String,
        canRetry: Boolean = true
    ): AICoachMessage {
        return AICoachMessage(
            id = "coach_err_${System.currentTimeMillis()}",
            text = text,
            isUser = false,
            isError = true,
            canRetry = canRetry
        )
    }
}
