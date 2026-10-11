package com.homeworkout.app.service

import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.WorkoutHistory
import java.io.IOException

sealed class AICoachResult {
    data class Success(val text: String) : AICoachResult()
    data class Error(
        val message: String,
        val errorType: AICoachErrorType,
        val canRetry: Boolean = true
    ) : AICoachResult()
}

enum class AICoachErrorType {
    APP_CHECK_UNREGISTERED,
    NETWORK_ERROR,
    RATE_LIMIT,
    SAFETY_BLOCKED,
    GENERIC_ERROR
}

class AICoachService {

    suspend fun sendMessage(
        userMessage: String,
        history: List<WorkoutHistory>
    ): AICoachResult {
        val trimmed = userMessage.trim()
        if (trimmed.isEmpty()) return AICoachResult.Success("")

        if (SupabaseClient.currentUser == null) {
            return AICoachResult.Error(
                message = "Please sign in to your account to use the AI Coach.",
                errorType = AICoachErrorType.APP_CHECK_UNREGISTERED,
                canRetry = false
            )
        }

        val result = SupabaseClient.Functions.callAICoach(
            message = trimmed,
            history = history
        )

        return result.fold(
            onSuccess = { reply ->
                if (reply.isNotBlank()) {
                    AICoachResult.Success(reply)
                } else {
                    AICoachResult.Error(
                        message = "I couldn't generate a response for that prompt. Please try asking again.",
                        errorType = AICoachErrorType.GENERIC_ERROR,
                        canRetry = true
                    )
                }
            },
            onFailure = { exception ->
                classifyException(exception)
            }
        )
    }

    private fun classifyException(exception: Throwable): AICoachResult {
        val errorMsg = exception.message ?: ""

        val isAuthError =
            errorMsg.contains("unauthorized", ignoreCase = true) ||
            errorMsg.contains("session", ignoreCase = true) ||
            errorMsg.contains("sign in", ignoreCase = true) ||
            errorMsg.contains("401", ignoreCase = true)

        val isNetworkError =
            exception is IOException ||
            errorMsg.contains("network", ignoreCase = true) ||
            errorMsg.contains("connect", ignoreCase = true) ||
            errorMsg.contains("timeout", ignoreCase = true) ||
            errorMsg.contains("host", ignoreCase = true)

        val isRateLimit =
            errorMsg.contains("quota", ignoreCase = true) ||
            errorMsg.contains("rate limit", ignoreCase = true) ||
            errorMsg.contains("429", ignoreCase = true)

        val isKeyMissing =
            errorMsg.contains("GEMINI_API_KEY", ignoreCase = true)

        return when {
            isAuthError -> {
                AICoachResult.Error(
                    message = "Your session has expired or requires authentication. Please sign in or retry.",
                    errorType = AICoachErrorType.APP_CHECK_UNREGISTERED,
                    canRetry = true
                )
            }

            isKeyMissing -> {
                AICoachResult.Error(
                    message = "Supabase Edge Function requires GEMINI_API_KEY. Please set the secret in Supabase Dashboard (Project Settings > Edge Functions > Secrets).",
                    errorType = AICoachErrorType.GENERIC_ERROR,
                    canRetry = true
                )
            }

            isNetworkError -> {
                AICoachResult.Error(
                    message = "Unable to connect to the Supabase AI service. Please verify your internet connection and try again.",
                    errorType = AICoachErrorType.NETWORK_ERROR,
                    canRetry = true
                )
            }

            isRateLimit -> {
                AICoachResult.Error(
                    message = "AI request quota or rate limit temporarily reached. Please wait a moment and try again.",
                    errorType = AICoachErrorType.RATE_LIMIT,
                    canRetry = true
                )
            }

            else -> {
                AICoachResult.Error(
                    message = "AI service temporarily unavailable: ${errorMsg.take(120)}. Please try again shortly.",
                    errorType = AICoachErrorType.GENERIC_ERROR,
                    canRetry = true
                )
            }
        }
    }
}
