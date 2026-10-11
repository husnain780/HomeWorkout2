package com.homeworkout.app.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class CompletedWorkoutSummary(
    val recordId: String,
    val workoutId: String,
    val workoutName: String,
    val durationMinutes: Int,
    val elapsedSeconds: Int,
    val exercisesCompleted: Int,
    val totalExercises: Int,
    val caloriesBurned: Int,
    val completedAt: Long
)

private data class WorkoutExerciseStep(
    val id: String,
    val name: String,
    val category: String,
    val durationSeconds: Int,
    val caloriesPerRepOrSet: Int,
    val instruction: String
)

data class WorkoutSessionState(
    val currentIndex: Int = 0,
    val totalSteps: Int,
    val stepDurationSeconds: List<Int>,
    val remainingSeconds: Int = stepDurationSeconds.firstOrNull() ?: 30,
    val isPaused: Boolean = false,
    val activeSessionSeconds: Int = 0,
    val completedIndices: Set<Int> = emptySet(),
    val isFinished: Boolean = false
) {
    fun tickOneSecond(): WorkoutSessionState {
        if (isPaused || isFinished || totalSteps <= 0) return this
        val nextActive = activeSessionSeconds + 1
        return if (remainingSeconds > 1) {
            copy(
                remainingSeconds = remainingSeconds - 1,
                activeSessionSeconds = nextActive
            )
        } else {
            val updatedCompleted = completedIndices + currentIndex
            if (currentIndex < totalSteps - 1) {
                val nextIdx = currentIndex + 1
                copy(
                    currentIndex = nextIdx,
                    remainingSeconds = stepDurationSeconds.getOrElse(nextIdx) { 30 },
                    activeSessionSeconds = nextActive,
                    completedIndices = updatedCompleted
                )
            } else {
                copy(
                    remainingSeconds = 0,
                    activeSessionSeconds = nextActive,
                    completedIndices = updatedCompleted,
                    isFinished = true
                )
            }
        }
    }

    fun nextExercise(): WorkoutSessionState {
        if (isFinished || totalSteps <= 0) return this
        val updatedCompleted = completedIndices + currentIndex
        return if (currentIndex < totalSteps - 1) {
            val nextIdx = currentIndex + 1
            copy(
                currentIndex = nextIdx,
                remainingSeconds = stepDurationSeconds.getOrElse(nextIdx) { 30 },
                completedIndices = updatedCompleted
            )
        } else {
            copy(
                remainingSeconds = 0,
                completedIndices = updatedCompleted,
                isFinished = true
            )
        }
    }

    fun previousExercise(): WorkoutSessionState {
        if (isFinished || totalSteps <= 0) return this
        val prevIdx = (currentIndex - 1).coerceAtLeast(0)
        return copy(
            currentIndex = prevIdx,
            remainingSeconds = stepDurationSeconds.getOrElse(prevIdx) { 30 }
        )
    }

    fun togglePause(): WorkoutSessionState = copy(isPaused = !isPaused)
}

@Composable
fun WorkoutScreen(
    workoutId: String,
    onCompleteWorkout: (CompletedWorkoutSummary) -> Unit,
    onCancelWorkout: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onCancelWorkout)

    val plan = remember(workoutId) {
        val single = ExerciseRepository.find(workoutId)
        if (single != null) {
            com.homeworkout.app.model.WorkoutPlan(
                id = single.id,
                title = single.name,
                description = single.description.ifBlank { "Targeted drill: ${single.name}" },
                durationMinutes = ((single.durationSeconds + 59) / 60).coerceAtLeast(1),
                exerciseCount = 1,
                difficulty = single.difficulty,
                exerciseIds = listOf(single.id),
                category = single.category,
                isAiGenerated = false
            )
        } else {
            WorkoutPlanRepository.findPlanById(workoutId)
                ?: WorkoutPlanRepository.catalogPlans.first()
        }
    }

    val exercises = remember(plan.id) {
        val mapped = plan.exerciseIds.mapNotNull { exerciseId ->
            val found = ExerciseRepository.find(exerciseId)
            found?.let { ex ->
                WorkoutExerciseStep(
                    id = ex.id,
                    name = ex.name,
                    category = ex.category,
                    durationSeconds = ex.durationSeconds.coerceAtLeast(15),
                    caloriesPerRepOrSet = ex.calories.coerceAtLeast(4),
                    instruction = ex.instructions.firstOrNull()
                        ?: ex.description.ifBlank { "Move with steady control and keep your core engaged." }
                )
            }
        }
        mapped.ifEmpty {
            ExerciseRepository.exercises.take(4).map { ex ->
                WorkoutExerciseStep(
                    id = ex.id,
                    name = ex.name,
                    category = ex.category,
                    durationSeconds = ex.durationSeconds.coerceAtLeast(15),
                    caloriesPerRepOrSet = ex.calories.coerceAtLeast(4),
                    instruction = ex.instructions.firstOrNull()
                        ?: "Move with steady control and keep your core engaged."
                )
            }
        }
    }

    val sessionKey = remember(workoutId) { "${workoutId}_${System.currentTimeMillis()}" }

    var sessionState by rememberSaveable(
        sessionKey,
        stateSaver = androidx.compose.runtime.saveable.Saver(
            save = { state ->
                listOf(
                    state.currentIndex,
                    state.totalSteps,
                    state.remainingSeconds,
                    state.isPaused,
                    state.activeSessionSeconds,
                    state.completedIndices.toList(),
                    state.isFinished
                )
            },
            restore = { list ->
                @Suppress("UNCHECKED_CAST")
                val items = list as List<Any>
                WorkoutSessionState(
                    currentIndex = (items[0] as Number).toInt(),
                    totalSteps = (items[1] as Number).toInt(),
                    stepDurationSeconds = exercises.map { it.durationSeconds },
                    remainingSeconds = (items[2] as Number).toInt(),
                    isPaused = items[3] as Boolean,
                    activeSessionSeconds = (items[4] as Number).toInt(),
                    completedIndices = @Suppress("UNCHECKED_CAST") (items[5] as List<Int>).toSet(),
                    isFinished = items[6] as Boolean
                )
            }
        )
    ) {
        mutableStateOf(
            WorkoutSessionState(
                currentIndex = 0,
                totalSteps = exercises.size,
                stepDurationSeconds = exercises.map { it.durationSeconds },
                remainingSeconds = exercises.first().durationSeconds,
                isPaused = false,
                activeSessionSeconds = 0,
                completedIndices = emptySet(),
                isFinished = false
            )
        )
    }

    var hasEmittedFinish by rememberSaveable(sessionKey) { mutableStateOf(false) }
    var sessionRecordId by rememberSaveable(sessionKey) {
        mutableStateOf("workout_${System.currentTimeMillis()}")
    }

    val safeIndex = sessionState.currentIndex.coerceIn(0, exercises.lastIndex)
    val currentExercise = exercises[safeIndex]
    val nextExercise = exercises.getOrNull(safeIndex + 1)
    val isPaused = sessionState.isPaused
    val remainingSeconds = sessionState.remainingSeconds

    fun finishWorkoutSession(finalState: WorkoutSessionState) {
        if (hasEmittedFinish) return
        hasEmittedFinish = true

        val completedIndices = finalState.completedIndices.ifEmpty {
            (0..finalState.currentIndex).toSet()
        }
        val completedCount = completedIndices.size.coerceAtLeast(1)
        val baseCalories = completedIndices.sumOf { idx ->
            exercises.getOrNull(idx)?.caloriesPerRepOrSet ?: 8
        }

        val plannedTotalSec = exercises.sumOf { it.durationSeconds }.coerceAtLeast(60)
        val durationFactor = (finalState.activeSessionSeconds.toFloat() / plannedTotalSec.toFloat())
            .coerceIn(0.6f, 1.4f)
        val estimatedCalories = (baseCalories * durationFactor).roundToInt().coerceAtLeast(completedCount * 4)
        val actualMinutes = ((finalState.activeSessionSeconds + 30) / 60).coerceAtLeast(1)

        // Update 30-day program progress if this workout belongs to a saved 30-day plan
        WorkoutPlanRepository.markPlanDayCompleted(workoutId)

        onCompleteWorkout(
            CompletedWorkoutSummary(
                recordId = sessionRecordId,
                workoutId = plan.id,
                workoutName = plan.title,
                durationMinutes = actualMinutes,
                elapsedSeconds = finalState.activeSessionSeconds.coerceAtLeast(1),
                exercisesCompleted = completedCount,
                totalExercises = exercises.size,
                caloriesBurned = estimatedCalories,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    LaunchedEffect(sessionState.isPaused, sessionState.isFinished) {
        while (!sessionState.isPaused && !sessionState.isFinished) {
            delay(1000L)
            sessionState = sessionState.tickOneSecond()
        }
    }

    LaunchedEffect(sessionState.isFinished) {
        if (sessionState.isFinished && !hasEmittedFinish) {
            finishWorkoutSession(sessionState)
        }
    }

    val stepProgress by animateFloatAsState(
        targetValue = (
            (currentExercise.durationSeconds - remainingSeconds).toFloat() /
                currentExercise.durationSeconds.coerceAtLeast(1).toFloat()
            ).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "exerciseProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Small session header with back/exit and exercise progress ("3 of 8")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCancelWorkout),
                shape = CircleShape,
                color = HwCard,
                border = BorderStroke(1.dp, HwBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Exit workout",
                        tint = HwTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = plan.title,
                color = HwTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = HwCardElevated,
                border = BorderStroke(1.dp, HwBorder)
            ) {
                Text(
                    text = "${safeIndex + 1} of ${exercises.size}",
                    color = HwAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Large animated character demonstrating the current exercise
        AnimatedExerciseCharacter(
            exerciseId = currentExercise.id,
            isPaused = isPaused,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Exercise name & 4. One short, helpful instruction in plain English
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentExercise.name,
                color = HwTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentExercise.instruction,
                color = HwTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Workout timer/countdown with clear progress indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = HwCard,
            border = BorderStroke(1.dp, HwBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (isPaused) "PAUSED" else "TIME REMAINING",
                        color = if (isPaused) HwCyan else HwTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatCountdown(remainingSeconds),
                        color = HwAccent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = HwAccent,
                    trackColor = HwCardElevated,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Upcoming exercise pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = HwCard,
            border = BorderStroke(1.dp, HwBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (nextExercise != null) {
                    Surface(
                        modifier = Modifier
                            .size(width = 44.dp, height = 36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF4F7F9)
                    ) {
                        Image(
                            painter = painterResource(id = ExerciseRepository.imageRes(nextExercise.id)),
                            contentDescription = nextExercise.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (nextExercise != null) {
                        "Next: ${nextExercise.name}"
                    } else {
                        "Final exercise in this workout"
                    },
                    color = HwTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (nextExercise != null) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = HwAccentSoft
                    ) {
                        Text(
                            text = "${nextExercise.durationSeconds}s",
                            color = HwAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. Simple controls: Previous, Pause/Resume, and Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = safeIndex > 0) {
                        sessionState = sessionState.previousExercise()
                    },
                shape = RoundedCornerShape(16.dp),
                color = HwCard,
                border = BorderStroke(1.dp, HwBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (safeIndex > 0) HwTextPrimary else HwTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Previous",
                        color = if (safeIndex > 0) HwTextPrimary else HwTextTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pause / Resume Button
            Surface(
                modifier = Modifier
                    .weight(1.2f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { sessionState = sessionState.togglePause() },
                shape = RoundedCornerShape(16.dp),
                color = if (isPaused) HwAccent else HwCardElevated,
                border = BorderStroke(1.dp, if (isPaused) HwAccent else HwBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = if (isPaused) HwTextOnAccent else HwTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        color = if (isPaused) HwTextOnAccent else HwTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Next / Finish Button
            val isLast = safeIndex >= exercises.lastIndex
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (!isLast) {
                            sessionState = sessionState.nextExercise()
                        } else {
                            val finishedState = sessionState.copy(isFinished = true)
                            sessionState = finishedState
                            finishWorkoutSession(finishedState)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (isLast) HwAccent else HwAccentSoft,
                border = BorderStroke(1.dp, HwAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLast) "Finish" else "Next",
                        color = if (isLast) HwTextOnAccent else HwAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Outlined.Check else Icons.Outlined.SkipNext,
                        contentDescription = if (isLast) "Finish" else "Next",
                        tint = if (isLast) HwTextOnAccent else HwAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatCountdown(totalSeconds: Int): String {
    val mins = (totalSeconds / 60).coerceAtLeast(0)
    val secs = (totalSeconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(mins, secs)
}
