package com.homeworkout.app.service

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistory
import java.io.IOException
import java.time.LocalDate

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

    private val model =
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content {
                text(
                    """
                    You are a versatile, highly intelligent, general-purpose AI assistant and coach integrated into the HomeWorkout app.

                    You have broad, comprehensive expertise and excel at:
                    - Software engineering, coding, syntax, architecture, and debugging (Kotlin, Android, Jetpack Compose, Python, JavaScript, Java, C++, algorithms, data structures)
                    - Mathematics, physics, logic, problem-solving, and step-by-step solutions
                    - Academic learning, study techniques, science, history, and conceptual explanations
                    - Writing, proofreading, creative editing, summarization, and business communications
                    - Productivity, habits, time management, and mental focus
                    - Comprehensive fitness, biomechanics, exercise technique, training splits, progressive overload, recovery, and home workouts.

                    Guidelines:
                    1. Answer all user questions thoroughly, accurately, and articulately across all subjects without refusing non-fitness requests.
                    2. When answering programming or math queries, format explanations clearly with readable code or mathematical steps.
                    3. For fitness or health topics, provide safe, practical, evidence-based recommendations. Never encourage dangerous physical challenges or exercising through severe pain. Do not provide medical diagnoses.
                    4. If user workout telemetry context is provided, you may use it to inform workout-related guidance, but do not inject workout topics into unrelated discussions (like programming or math).
                    5. Maintain a friendly, supportive, intelligent, and professional tone.
                    """.trimIndent()
                )
            }
        )

    private val chat =
        model.startChat()

    suspend fun sendMessage(
        userMessage: String,
        history: List<WorkoutHistory>
    ): AICoachResult {
        val trimmed = userMessage.trim()
        if (trimmed.isEmpty()) return AICoachResult.Success("")

        val isFitnessRelated = trimmed.lowercase().let { msg ->
            msg.contains("workout") || msg.contains("exercise") || msg.contains("fitness") ||
            msg.contains("training") || msg.contains("muscle") || msg.contains("rep") ||
            msg.contains("set") || msg.contains("streak") || msg.contains("progress") ||
            msg.contains("calorie") || msg.contains("cardio") || msg.contains("squat") ||
            msg.contains("pushup") || msg.contains("push-up") || msg.contains("plank") ||
            msg.contains("routine") || msg.contains("recovery") || msg.contains("sore") ||
            msg.contains("diet") || msg.contains("nutrition") || msg.contains("weight")
        }

        val prompt = if (isFitnessRelated && history.isNotEmpty()) {
            val totalWorkouts = WorkoutAnalytics.totalWorkouts(history)
            val totalMinutes = WorkoutAnalytics.totalMinutes(history)
            val totalCalories = WorkoutAnalytics.totalCalories(history)
            val streak = WorkoutAnalytics.currentStreak(history)
            val lastWorkout = history.lastOrNull()?.workoutName ?: "None yet"
            """
            [User Workout Telemetry: $totalWorkouts completed sessions, $totalMinutes mins active, $totalCalories kcal burned, $streak-day streak, Last session: $lastWorkout, Date: ${LocalDate.now()}]

            User question:
            $trimmed
            """.trimIndent()
        } else {
            trimmed
        }

        return try {
            val response = chat.sendMessage(prompt)
            val reply = response.text?.trim()
            if (!reply.isNullOrBlank()) {
                AICoachResult.Success(reply)
            } else {
                AICoachResult.Error(
                    message = "I couldn't generate a response for that prompt. Please try asking again.",
                    errorType = AICoachErrorType.GENERIC_ERROR,
                    canRetry = true
                )
            }
        } catch (exception: Exception) {
            classifyException(exception)
        }
    }

    private fun classifyException(exception: Exception): AICoachResult {
        val errorMsg = exception.message ?: ""
        val className = exception.javaClass.simpleName

        val isAppCheckError =
            errorMsg.contains("app check", ignoreCase = true) ||
            errorMsg.contains("appcheck", ignoreCase = true) ||
            errorMsg.contains("token invalid", ignoreCase = true) ||
            errorMsg.contains("permission denied", ignoreCase = true) ||
            errorMsg.contains("403", ignoreCase = true) ||
            className.contains("AppCheck", ignoreCase = true)

        val isNetworkError =
            exception is IOException ||
            errorMsg.contains("network", ignoreCase = true) ||
            errorMsg.contains("connect", ignoreCase = true) ||
            errorMsg.contains("timeout", ignoreCase = true) ||
            errorMsg.contains("host", ignoreCase = true)

        val isRateLimit =
            errorMsg.contains("quota", ignoreCase = true) ||
            errorMsg.contains("rate limit", ignoreCase = true) ||
            errorMsg.contains("resource exhausted", ignoreCase = true) ||
            errorMsg.contains("429", ignoreCase = true)

        val isSafetyBlock =
            errorMsg.contains("safety", ignoreCase = true) ||
            errorMsg.contains("blocked", ignoreCase = true)

        return when {
            isAppCheckError -> {
                AICoachResult.Error(
                    message = "Firebase App Check verification required.\n\n" +
                        "Debug provider is active. To enable AI requests for this build:\n" +
                        "1. Open Logcat and find the debug secret logged by DebugAppCheckProviderFactory.\n" +
                        "2. In Firebase Console, go to App Check > Apps > Manage debug tokens.\n" +
                        "3. Add the debug token to allow requests from this device.\n\n" +
                        "Once registered in Firebase Console, tap Retry.",
                    errorType = AICoachErrorType.APP_CHECK_UNREGISTERED,
                    canRetry = true
                )
            }

            isNetworkError -> {
                AICoachResult.Error(
                    message = "Unable to connect to the AI service. Please verify your internet connection and try again.",
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

            isSafetyBlock -> {
                AICoachResult.Error(
                    message = "This prompt could not be processed due to safety guidelines. Please rephrase your question.",
                    errorType = AICoachErrorType.SAFETY_BLOCKED,
                    canRetry = false
                )
            }

            else -> {
                AICoachResult.Error(
                    message = "AI service temporarily unavailable. Please try again shortly.",
                    errorType = AICoachErrorType.GENERIC_ERROR,
                    canRetry = true
                )
            }
        }
    }
}
