package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.R
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import kotlinx.coroutines.delay

private enum class WorkoutPhase {
    WORK,
    REST
}

private data class WorkoutExercise(
    val id: String,
    val name: String,
    val category: String,
    val durationSeconds: Int,
    val calories: Int,
    val imageRes: Int
)

@Composable
fun WorkoutScreen(
    workoutId: String = "beginner_workout",
    onCompleteWorkout: () -> Unit
) {
    val exercises = remember(workoutId) {
        val plan = WorkoutPlanRepository.plans.firstOrNull { it.id == workoutId }
        val ids = plan?.exerciseIds ?: listOf(
            "jumping_jack",
            "squat",
            "push_up",
            "plank"
        )

        ids.mapNotNull { id ->
            ExerciseRepository.find(id)?.let { exercise ->
                WorkoutExercise(
                    id = exercise.id,
                    name = exercise.name,
                    category = exercise.category,
                    durationSeconds = exercise.durationSeconds,
                    calories = exercise.calories,
                    imageRes = exercise.imageRes
                )
            }
        }.ifEmpty {
            listOf(
                WorkoutExercise(
                    "push_up",
                    "Push Up",
                    "Chest",
                    30,
                    8,
                    R.drawable.push_up
                )
            )
        }
    }

    var currentIndex by remember(workoutId) {
        mutableStateOf(0)
    }

    var phase by remember(workoutId) {
        mutableStateOf(WorkoutPhase.WORK)
    }

    var secondsLeft by remember(workoutId) {
        mutableStateOf(exercises[0].durationSeconds)
    }

    var paused by remember(workoutId) {
        mutableStateOf(false)
    }

    val current = exercises[currentIndex]

    fun startNextExercise() {
        if (currentIndex >= exercises.lastIndex) {
            onCompleteWorkout()
        } else {
            currentIndex += 1
            phase = WorkoutPhase.WORK
            secondsLeft = exercises[currentIndex].durationSeconds
            paused = false
        }
    }

    fun startRest() {
        if (currentIndex >= exercises.lastIndex) {
            onCompleteWorkout()
        } else {
            phase = WorkoutPhase.REST
            secondsLeft = 10
            paused = false
        }
    }

    fun skipCurrent() {
        if (phase == WorkoutPhase.WORK) {
            startRest()
        } else {
            startNextExercise()
        }
    }

    LaunchedEffect(
        currentIndex,
        phase,
        paused,
        secondsLeft
    ) {
        if (paused) {
            return@LaunchedEffect
        }

        delay(1000)

        if (secondsLeft > 1) {
            secondsLeft -= 1
            return@LaunchedEffect
        }

        secondsLeft = 0

        when (phase) {
            WorkoutPhase.WORK -> {
                startRest()
            }
            WorkoutPhase.REST -> {
                startNextExercise()
            }
        }
    }

    val totalSeconds = if (phase == WorkoutPhase.WORK) {
        current.durationSeconds.coerceAtLeast(1)
    } else {
        10
    }

    val phaseProgress = (1f - secondsLeft.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val overallProgress = (
        currentIndex.toFloat() +
            if (phase == WorkoutPhase.REST) 1f else phaseProgress
    ) / exercises.size.toFloat()

    val workoutTitle = when (workoutId) {
        "strength_builder" -> "Strength Builder"
        "quick_home_workout" -> "Quick Home Workout"
        else -> "Beginner Full Body"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 108.dp)
        ) {
            Header(
                workoutTitle = workoutTitle,
                currentIndex = currentIndex,
                exerciseCount = exercises.size,
                phase = phase
            )

            LinearProgressIndicator(
                progress = { overallProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = if (phase == WorkoutPhase.REST) BrandNavy else BrandGreen,
                trackColor = Color(0xFFE2E9E5)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HeroExercise(
                exercise = current,
                phase = phase
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimerPanel(
                secondsLeft = secondsLeft,
                phase = phase,
                phaseProgress = phaseProgress
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExerciseStrip(
                exercises = exercises,
                currentIndex = currentIndex,
                onExerciseSelected = { index ->
                    currentIndex = index
                    phase = WorkoutPhase.WORK
                    secondsLeft = exercises[index].durationSeconds
                    paused = false
                }
            )
        }

        WorkoutControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            phase = phase,
            paused = paused,
            lastExercise = currentIndex == exercises.lastIndex,
            onPauseResume = { paused = !paused },
            onSkip = { skipCurrent() },
            onSkipRest = { startNextExercise() },
            onNext = { startNextExercise() }
        )
    }
}

@Composable
private fun Header(
    workoutTitle: String,
    currentIndex: Int,
    exerciseCount: Int,
    phase: WorkoutPhase
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (phase == WorkoutPhase.WORK) "ACTIVE SESSION" else "RECOVERY PHASE",
                color = if (phase == WorkoutPhase.WORK) BrandGreenDark else BrandNavy,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = workoutTitle,
                color = BrandNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderSoft),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${currentIndex + 1}",
                    color = BrandNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "OF $exerciseCount",
                    color = BrandMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun HeroExercise(
    exercise: WorkoutExercise,
    phase: WorkoutPhase
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(26.dp))
        ) {
            Image(
                painter = painterResource(exercise.imageRes),
                contentDescription = exercise.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                shape = RoundedCornerShape(50.dp),
                color = if (phase == WorkoutPhase.WORK) BrandGreen else Color.White
            ) {
                Text(
                    text = if (phase == WorkoutPhase.WORK) "ACTIVE WORK" else "REST BREAK",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (phase == WorkoutPhase.WORK) Color.White else BrandNavy,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (phase == WorkoutPhase.WORK) exercise.category.uppercase() else "RECOVERY",
                    color = BrandGreenSoft,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = if (phase == WorkoutPhase.WORK) exercise.name else "Take a Breather",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (phase == WorkoutPhase.WORK) {
                        "${exercise.durationSeconds}s target • ${exercise.calories} kcal estimated"
                    } else {
                        "Catch your breath before next movement"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TimerPanel(
    secondsLeft: Int,
    phase: WorkoutPhase,
    phaseProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (phase == WorkoutPhase.WORK) "TIME REMAINING" else "REST TIME",
                color = BrandMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "%02d".format(secondsLeft),
                color = if (phase == WorkoutPhase.WORK) BrandNavy else BrandGreenDark,
                fontSize = 52.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "SECONDS",
                color = BrandMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { phaseProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = if (phase == WorkoutPhase.WORK) BrandGreen else BrandNavy,
                trackColor = Color(0xFFE7ECE9)
            )
        }
    }
}

@Composable
private fun ExerciseStrip(
    exercises: List<WorkoutExercise>,
    currentIndex: Int,
    onExerciseSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WORKOUT SEQUENCE",
                color = BrandNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${currentIndex + 1}/${exercises.size}",
                color = BrandGreenDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(exercises) { index, exercise ->
                ExerciseThumb(
                    exercise = exercise,
                    selected = index == currentIndex,
                    completed = index < currentIndex,
                    onClick = { onExerciseSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseThumb(
    exercise: WorkoutExercise,
    selected: Boolean,
    completed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(96.dp)
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandGreenSoft else Color.White
        ),
        border = BorderStroke(
            1.dp,
            when {
                selected -> BrandGreen
                completed -> Color(0xFFC9E5D6)
                else -> BorderSoft
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (completed) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        shape = CircleShape,
                        color = BrandGreen
                    ) {
                        Box(
                            modifier = Modifier.size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                val stroke = Stroke(width = 2f, cap = StrokeCap.Round)
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width * 0.2f, size.height * 0.5f),
                                    end = Offset(size.width * 0.45f, size.height * 0.8f),
                                    strokeWidth = stroke.width,
                                    cap = stroke.cap
                                )
                                drawLine(
                                    color = Color.White,
                                    start = Offset(size.width * 0.45f, size.height * 0.8f),
                                    end = Offset(size.width * 0.85f, size.height * 0.2f),
                                    strokeWidth = stroke.width,
                                    cap = stroke.cap
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exercise.name,
                modifier = Modifier.fillMaxWidth(),
                color = BrandNavy,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WorkoutControls(
    modifier: Modifier,
    phase: WorkoutPhase,
    paused: Boolean,
    lastExercise: Boolean,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onSkipRest: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        if (phase == WorkoutPhase.REST) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSkipRest,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSoft),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy)
                ) {
                    Text(
                        text = "SKIP REST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (lastExercise) "FINISH WORKOUT" else "NEXT EXERCISE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPauseResume,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSoft),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy)
                ) {
                    Text(
                        text = if (paused) "RESUME" else "PAUSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (lastExercise) "FINISH WORKOUT" else "SKIP EXERCISE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
