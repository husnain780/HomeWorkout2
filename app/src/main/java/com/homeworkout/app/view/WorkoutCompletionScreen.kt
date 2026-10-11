package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.NotificationRepository
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwMetricCard
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwOrange
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary

@Composable
fun WorkoutCompletionScreen(
    workoutId: String,
    summary: CompletedWorkoutSummary? = null,
    onContinue: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onContinue)

    val fallbackPlan = remember(workoutId) {
        WorkoutPlanRepository.findPlanById(workoutId)
            ?: WorkoutPlanRepository.catalogPlans.first()
    }

    val effectiveSummary = remember(workoutId, summary) {
        summary ?: run {
            val estCal = fallbackPlan.exerciseIds.sumOf { id ->
                ExerciseRepository.exercises.firstOrNull { it.id == id }?.calories ?: 8
            }
            CompletedWorkoutSummary(
                recordId = "session_${workoutId}_${System.currentTimeMillis() / 10000}",
                workoutId = fallbackPlan.id,
                workoutName = fallbackPlan.title,
                durationMinutes = fallbackPlan.durationMinutes,
                elapsedSeconds = fallbackPlan.durationMinutes * 60,
                exercisesCompleted = fallbackPlan.exerciseCount,
                totalExercises = fallbackPlan.exerciseCount,
                caloriesBurned = estCal,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    var hasRecordedLocally by rememberSaveable(effectiveSummary.recordId) { mutableStateOf(false) }

    val record = remember(effectiveSummary) {
        WorkoutHistory(
            id = effectiveSummary.recordId,
            workoutId = effectiveSummary.workoutId,
            workoutName = effectiveSummary.workoutName,
            durationMinutes = effectiveSummary.durationMinutes,
            exercisesCompleted = effectiveSummary.exercisesCompleted,
            caloriesBurned = effectiveSummary.caloriesBurned,
            completedAt = effectiveSummary.completedAt
        )
    }

    LaunchedEffect(effectiveSummary.recordId) {
        if (!hasRecordedLocally) {
            WorkoutHistoryRepository.add(record = record, syncToCloud = true)
            val updatedHistory = WorkoutHistoryRepository.getAll()
            val newStreak = WorkoutAnalytics.currentStreak(updatedHistory)
            NotificationRepository.recordWorkoutCompletedNotification(
                workoutId = record.id,
                workoutName = record.workoutName,
                durationMinutes = record.durationMinutes,
                calories = record.caloriesBurned,
                streakDays = newStreak
            )
            hasRecordedLocally = true
        }
    }

    val allHistory = remember { WorkoutHistoryRepository.getAll() }
    val currentStreak = remember(allHistory) { WorkoutAnalytics.currentStreak(allHistory) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HwAccent, HwCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = HwTextOnAccent,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HwBadge(
                    text = "WORKOUT COMPLETE",
                    color = HwAccent,
                    containerColor = HwAccentSoft
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Great Job!",
                    color = HwTextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.6).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You finished ${effectiveSummary.workoutName}.",
                    color = HwTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HwMetricCard(
                        label = "Exercises",
                        value = "${effectiveSummary.exercisesCompleted}",
                        icon = Icons.Outlined.FitnessCenter,
                        accent = HwAccent,
                        modifier = Modifier.weight(1f)
                    )
                    HwMetricCard(
                        label = "Duration",
                        value = "${effectiveSummary.durationMinutes}m",
                        icon = Icons.Outlined.Schedule,
                        accent = HwCyan,
                        modifier = Modifier.weight(1f)
                    )
                    HwMetricCard(
                        label = "Burned",
                        value = "~${effectiveSummary.caloriesBurned} kcal",
                        icon = Icons.Outlined.Bolt,
                        accent = HwOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Streak",
                            color = HwTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$currentStreak day${if (currentStreak != 1) "s" else ""} 🔥",
                            color = HwOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            HwPrimaryButton(
                text = "Continue",
                onClick = onContinue,
                icon = Icons.Outlined.Check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
