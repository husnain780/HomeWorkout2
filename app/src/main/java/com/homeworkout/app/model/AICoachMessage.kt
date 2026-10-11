package com.homeworkout.app.model

data class AICoachMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val canRetry: Boolean = false
)
