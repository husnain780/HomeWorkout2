package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.controller.AIPlanController
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.AIWorkoutDay
import com.homeworkout.app.model.AIWorkoutPlan
import com.homeworkout.app.model.OfflineRepository
import com.homeworkout.app.ui.HwBackButton
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import com.homeworkout.app.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

import com.homeworkout.app.model.SavedWorkoutPlan
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutPlanRepository

@Composable
fun AIPlanGenerationScreen(
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit = {}
) {
    val controller = remember { AIPlanController(EventBus()) }
    val plansRev = WorkoutPlanRepository.revision

    val savedPlans = remember(plansRev) {
        WorkoutPlanRepository.getSavedWorkoutPlans()
    }

    var goal by remember {
        mutableStateOf(
            when (val pref = UserPreferencesRepository.getFitnessGoal()) {
                "Full Body" -> "General Fitness"
                else -> pref
            }
        )
    }
    var difficulty by remember {
        mutableStateOf(
            UserPreferencesRepository.getPreferredDifficulty().let {
                if (it in listOf("Beginner", "Intermediate", "Advanced")) it else "Intermediate"
            }
        )
    }
    var daysText by remember {
        mutableStateOf(UserPreferencesRepository.getWeeklyGoal().coerceIn(2, 6).toString())
    }
    var minutesText by remember { mutableStateOf("20") }
    var generatedPlan by remember {
        mutableStateOf<AIWorkoutPlan?>(savedPlans.firstOrNull()?.toAIWorkoutPlan())
    }
    var activeSavedPlanId by remember {
        mutableStateOf<String?>(savedPlans.firstOrNull()?.id)
    }
    var isGenerating by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveStatusMessage by remember {
        mutableStateOf<String?>(
            if (savedPlans.isNotEmpty()) "Active 30-day plan loaded" else null
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PlanHeader(onBack = onBack)
        }

        item {
            IntroCard()
        }

        item {
            PreferenceSection(
                title = "PRIMARY GOAL",
                subtitle = "Choose what you want to focus on"
            ) {
                OptionGrid(
                    options = listOf("Fitness", "Strength", "Cardio", "Core"),
                    selected = when (goal) {
                        "General Fitness" -> "Fitness"
                        else -> goal
                    },
                    onSelected = { option ->
                        goal = if (option == "Fitness") "General Fitness" else option
                    }
                )
            }
        }

        item {
            PreferenceSection(
                title = "TRAINING EXPERIENCE",
                subtitle = "Set your current experience baseline"
            ) {
                OptionGrid(
                    options = listOf("Beginner", "Intermediate", "Advanced"),
                    selected = difficulty,
                    onSelected = { difficulty = it }
                )
            }
        }

        item {
            PreferenceSection(
                title = "WEEKLY SCHEDULE",
                subtitle = "Set your realistic frequency and time target"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScheduleField(
                        value = daysText,
                        label = "DAYS / WEEK",
                        modifier = Modifier.weight(1f),
                        onValueChange = { value ->
                            daysText = value.filter { it.isDigit() }.take(1)
                        }
                    )

                    ScheduleField(
                        value = minutesText,
                        label = "MINUTES",
                        modifier = Modifier.weight(1f),
                        onValueChange = { value ->
                            minutesText = value.filter { it.isDigit() }.take(2)
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val days = daysText.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    val minutes = minutesText.toIntOrNull()?.coerceIn(10, 45) ?: 20
                    isGenerating = true
                    errorMessage = null
                    saveStatusMessage = null
                    activeSavedPlanId = null

                    scope.launch {
                        try {
                            val plan = controller.generate(
                                goal = goal,
                                difficulty = difficulty,
                                daysPerWeek = days,
                                minutesPerWorkout = minutes
                            )
                            generatedPlan = plan
                        } catch (exception: Exception) {
                            errorMessage = exception.message ?: "Could not generate AI workout plan."
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFD5DED9),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (isGenerating) "GENERATING PLAN..." else "GENERATE AI PLAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )
            }
        }

        if (isGenerating) {
            item { GeneratingCard() }
        }

        if (errorMessage != null) {
            item { ErrorCard(message = errorMessage ?: "Something went wrong") }
        }

        generatedPlan?.let { plan ->
            item {
                GeneratedPlanHeader(plan = plan)
            }

            item {
                PlanStats(plan = plan)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (activeSavedPlanId == null && !isSaving) {
                                isSaving = true
                                scope.launch {
                                    val result = WorkoutPlanRepository.saveAIPlan(plan)
                                    isSaving = false
                                    result.onSuccess { saved ->
                                        activeSavedPlanId = saved.id
                                        saveStatusMessage = "Plan saved"
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Failed to save plan."
                                    }
                                }
                            }
                        },
                        enabled = activeSavedPlanId == null && !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandNavy,
                            contentColor = Color.White,
                            disabledContainerColor = BrandGreenSoft,
                            disabledContentColor = BrandGreenDark
                        )
                    ) {
                        Text(
                            text = when {
                                isSaving -> "SAVING..."
                                activeSavedPlanId != null -> "SAVED TO PLANS ✓"
                                else -> "SAVE PLAN"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val planIdToStart = activeSavedPlanId ?: run {
                                    val saved = WorkoutPlanRepository.saveAIPlan(plan).getOrNull()
                                    activeSavedPlanId = saved?.id
                                    saved?.id
                                }
                                if (!planIdToStart.isNullOrBlank()) {
                                    onStartWorkout(planIdToStart)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "START ROUTINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            if (!saveStatusMessage.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BrandGreenSoft
                    ) {
                        Text(
                            text = saveStatusMessage!!,
                            color = BrandGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "30-DAY SCHEDULE",
                    color = BrandNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )
            }

            items(
                count = plan.days.size,
                key = { index -> plan.days[index].dayNumber }
            ) { index ->
                GeneratedDayCard(day = plan.days[index])
            }

            item {
                GenerationSource(plan = plan)
            }
        }
    }
}

@Composable
private fun PlanHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HwBackButton(onClick = onBack)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SMART TRAINING",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )
            Text(
                text = "AI Workout Plan",
                color = BrandNavy,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(BrandGreen.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "ADAPTIVE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Personalized\nto your life.",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Select your target, level, and schedule. Our intelligent engine arranges an optimal weekly routine.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PreferenceSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )
            Text(
                text = subtitle,
                color = BrandMuted,
                fontSize = 10.sp
            )
        }
        content()
    }
}

@Composable
private fun OptionGrid(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(option) },
                shape = RoundedCornerShape(14.dp),
                color = if (active) BrandNavy else SurfaceWhite,
                border = if (active) null else BorderStroke(1.dp, BorderSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (active) Color.White else BrandMuted,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleField(
    value: String,
    label: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            focusedBorderColor = BrandGreen,
            unfocusedBorderColor = BorderSoft,
            focusedLabelColor = BrandGreenDark,
            unfocusedLabelColor = BrandMuted,
            cursorColor = BrandGreenDark
        )
    )
}

@Composable
private fun GeneratingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = BrandGreenSoft
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandGreenDark,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "GENERATING PLAN",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )
                Text(
                    text = "Structuring exercises and recovery windows...",
                    color = BrandNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF1F1),
        border = BorderStroke(1.dp, Color(0xFFF2CACA))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = Color(0xFFFFDCDC)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "!",
                        color = Color(0xFFC94646),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = BrandText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GeneratedPlanHeader(
    plan: AIWorkoutPlan
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GENERATED PLAN",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.title,
                        color = BrandNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = "AI READY",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
            }

            Text(
                text = plan.description,
                color = BrandMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PlanStats(
    plan: AIWorkoutPlan
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlanStat(value = plan.goal, label = "GOAL", modifier = Modifier.weight(1f))
        PlanStat(value = plan.difficulty, label = "LEVEL", modifier = Modifier.weight(1f))
        PlanStat(value = "${plan.daysPerWeek}d", label = "PER WEEK", modifier = Modifier.weight(1f))
        PlanStat(value = "${plan.minutesPerWorkout}m", label = "SESSION", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PlanStat(
    value: String,
    label: String,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = label,
                color = BrandMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GeneratedDayCard(
    day: AIWorkoutDay
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = BrandNavy
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = day.dayNumber.toString(),
                            color = BrandGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAY ${day.dayNumber}",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = day.focus,
                        color = BrandNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (day.isRestDay) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = day.warmup,
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                day.exerciseIds.forEachIndexed { index, exerciseId ->
                    val exercise = OfflineRepository.getExercise(exerciseId)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AppBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = BrandGreenDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = exercise?.name ?: exerciseId,
                                color = BrandText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerationSource(
    plan: AIWorkoutPlan
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BrandGreenSoft
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "AI",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Personalized 30-day program (${plan.goal} • ${plan.difficulty}).",
                color = BrandNavy,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
