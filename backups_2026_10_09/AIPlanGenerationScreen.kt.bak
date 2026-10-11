package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import kotlinx.coroutines.launch

@Composable
fun AIPlanGenerationScreen(
    onBack: () -> Unit
) {
    val controller = remember {
        AIPlanController(EventBus())
    }

    var goal by remember {
        mutableStateOf("General Fitness")
    }

    var difficulty by remember {
        mutableStateOf("Beginner")
    }

    var daysText by remember {
        mutableStateOf("3")
    }

    var minutesText by remember {
        mutableStateOf("20")
    }

    var generatedPlan by remember {
        mutableStateOf<AIWorkoutPlan?>(null)
    }

    var isGenerating by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            PlanHeader(
                onBack = onBack
            )
        }

        item {
            IntroCard()
        }

        item {
            PreferenceSection(
                title = "YOUR GOAL",
                subtitle = "What do you want to improve?"
            ) {
                OptionGrid(
                    options = listOf(
                        "Fitness",
                        "Strength",
                        "Cardio",
                        "Core"
                    ),
                    selected = when (goal) {
                        "General Fitness" -> "Fitness"
                        else -> goal
                    },
                    onSelected = { option ->
                        goal = when (option) {
                            "Fitness" -> "General Fitness"
                            else -> option
                        }
                    }
                )
            }
        }

        item {
            PreferenceSection(
                title = "TRAINING LEVEL",
                subtitle = "Choose the intensity that fits you"
            ) {
                OptionGrid(
                    options = listOf(
                        "Beginner",
                        "Intermediate"
                    ),
                    selected = difficulty,
                    onSelected = {
                        difficulty = it
                    }
                )
            }
        }

        item {
            PreferenceSection(
                title = "YOUR SCHEDULE",
                subtitle = "Set your weekly commitment"
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
                            daysText = value
                                .filter { it.isDigit() }
                                .take(1)
                        }
                    )

                    ScheduleField(
                        value = minutesText,
                        label = "MINUTES",
                        modifier = Modifier.weight(1f),
                        onValueChange = { value ->
                            minutesText = value
                                .filter { it.isDigit() }
                                .take(2)
                        }
                    )
                }
            }
        }

        item {

            Button(
                onClick = {

                    val days = daysText
                        .toIntOrNull()
                        ?.coerceIn(1, 6)
                        ?: 3

                    val minutes = minutesText
                        .toIntOrNull()
                        ?.coerceIn(10, 45)
                        ?: 20

                    isGenerating = true
                    errorMessage = null

                    scope.launch {

                        try {

                            generatedPlan =
                                controller.generate(
                                    goal = goal,
                                    difficulty = difficulty,
                                    daysPerWeek = days,
                                    minutesPerWorkout = minutes
                                )

                        } catch (exception: Exception) {

                            errorMessage =
                                exception.message
                                    ?: "Could not generate plan"

                        } finally {

                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFD5DED9),
                    disabledContentColor = Color.White
                )
            ) {

                Text(
                    text = if (isGenerating) {
                        "CREATING YOUR PLAN..."
                    } else {
                        "GENERATE AI PLAN  ›"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )
            }
        }

        if (isGenerating) {
            item {
                GeneratingCard()
            }
        }

        if (errorMessage != null) {
            item {
                ErrorCard(
                    message = errorMessage ?: "Something went wrong"
                )
            }
        }

        generatedPlan?.let { plan ->

            item {
                GeneratedPlanHeader(
                    plan = plan
                )
            }

            item {
                PlanStats(
                    plan = plan
                )
            }

            item {
                Text(
                    text = "YOUR WEEK",
                    color = BrandNavy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )
            }

            items(
                count = plan.days.size,
                key = { index ->
                    plan.days[index].dayNumber
                }
            ) { index ->

                GeneratedDayCard(
                    day = plan.days[index]
                )
            }

            item {
                GenerationSource(
                    plan = plan
                )
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

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onBack),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(
                1.dp,
                BorderSoft
            ),
            shadowElevation = 2.dp
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "‹",
                    color = BrandNavy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(
            modifier = Modifier.width(13.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

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
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 10.sp,
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
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandNavy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {

        Box {

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandGreen.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.17f)
                ) {

                    Text(
                        text = "PERSONALIZED",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Build a plan\naround you.",
                    color = Color.White,
                    fontSize = 27.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Tell us your goal, level and schedule. AI will create a workout plan that fits.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    lineHeight = 17.sp
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
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {

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
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        options.forEach { option ->

            val active = option == selected

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSelected(option)
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (active) {
                    BrandNavy
                } else {
                    Color.White
                },
                border = if (active) {
                    null
                } else {
                    BorderStroke(
                        1.dp,
                        BorderSoft
                    )
                }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 13.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = option,
                        color = if (active) {
                            Color.White
                        } else {
                            BrandMuted
                        },
                        fontSize = 9.sp,
                        fontWeight = if (active) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.SemiBold
                        }
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
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandNavy
        ),
        shape = RoundedCornerShape(17.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
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
        shape = RoundedCornerShape(20.dp),
        color = BrandGreenSoft
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(27.dp),
                color = BrandGreenDark,
                strokeWidth = 3.dp
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = "BUILDING YOUR PLAN",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )

                Text(
                    text = "Personalizing your weekly routine...",
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
        shape = RoundedCornerShape(19.dp),
        color = Color(0xFFFFF1F1),
        border = BorderStroke(
            1.dp,
            Color(0xFFF2CACA)
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFFFFDCDC)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "!",
                        color = Color(0xFFC94646),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

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
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            BorderSoft
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "YOUR NEW PLAN",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = plan.title,
                        color = BrandNavy,
                        fontSize = 21.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {

                    Text(
                        text = "AI READY",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        ),
                        color = BrandGreenDark,
                        fontSize = 7.sp,
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
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        PlanStat(
            value = plan.goal,
            label = "GOAL",
            modifier = Modifier.weight(1f)
        )

        PlanStat(
            value = plan.difficulty,
            label = "LEVEL",
            modifier = Modifier.weight(1f)
        )

        PlanStat(
            value = "${plan.daysPerWeek}",
            label = "DAYS/WEEK",
            modifier = Modifier.weight(1f)
        )

        PlanStat(
            value = "${plan.minutesPerWorkout}",
            label = "MIN/SESSION",
            modifier = Modifier.weight(1f)
        )
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
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Column(
            modifier = Modifier.padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = value,
                color = BrandNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = label,
                color = BrandMuted,
                fontSize = 6.sp,
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
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            BorderSoft
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = BrandNavy
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = day.dayNumber.toString(),
                            color = BrandGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            day.exerciseIds.forEachIndexed { index, exerciseId ->

                val exercise =
                    OfflineRepository.getExercise(
                        exerciseId
                    )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AppBackground
                ) {

                    Row(
                        modifier = Modifier.padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "${index + 1}",
                            color = BrandGreenDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text(
                            text = exercise?.name ?: exerciseId,
                            color = BrandText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "›",
                            color = BrandMuted,
                            fontSize = 18.sp
                        )
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
        shape = RoundedCornerShape(19.dp),
        color = if (plan.generatedByAI) {
            BrandGreenSoft
        } else {
            Color.White
        },
        border = BorderStroke(
            1.dp,
            if (plan.generatedByAI) {
                Color.Transparent
            } else {
                BorderSoft
            }
        )
    ) {

        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = if (plan.generatedByAI) {
                    Color.White
                } else {
                    AppBackground
                }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "AI",
                        color = BrandGreenDark,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(9.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = if (plan.generatedByAI) {
                        "GENERATED BY GEMINI AI"
                    } else {
                        "LOCAL FALLBACK"
                    },
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )

                Text(
                    text = if (plan.generatedByAI) {
                        "Your plan was created from your selected preferences."
                    } else {
                        "Your plan was created using the local fallback."
                    },
                    color = BrandMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}
