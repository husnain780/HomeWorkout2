package com.homeworkout.app.model

data class FormAnalysisState(
    val exercise: String = "Squat",
    val reps: Int = 0,
    val score: Int = 0,
    val feedback: String = "Stand in front of the camera.",
    val phase: String = "Ready",
    val leftKneeAngle: Double = 180.0,
    val rightKneeAngle: Double = 180.0,
    val leftElbowAngle: Double = 180.0,
    val rightElbowAngle: Double = 180.0,
    val isPersonDetected: Boolean = false
)
