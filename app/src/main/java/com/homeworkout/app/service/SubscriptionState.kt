package com.homeworkout.app.service

data class SubscriptionState(
    val isReady: Boolean = false,
    val isPremium: Boolean = false,
    val productTitle: String = "HomeWorkout Premium",
    val price: String = "",
    val message: String = "Connecting to Google Play..."
)
