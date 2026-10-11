package com.homeworkout.app.model

data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val durationSeconds: Int,
    val calories: Int,
    val description: String,
    val instructions: List<String>,
    val imageRes: Int
)
