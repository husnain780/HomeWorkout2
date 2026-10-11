package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.AIWorkoutDay
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.SavedWorkoutPlan
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutPlan
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.HwSecondaryButton
import com.homeworkout.app.ui.HwSectionHeader
import com.homeworkout.app.ui.HwTopBar
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwError
import com.homeworkout.app.ui.theme.HwSuccess
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutPlansScreen(
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onOpenAIPlan: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val plansRev = WorkoutPlanRepository.revision
    val userId = SupabaseClient.currentUser?.id

    val savedPlans = remember(plansRev, userId) {
        WorkoutPlanRepository.getSavedWorkoutPlans(userId)
    }

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            WorkoutPlanRepository.syncWithCloud(userId)
        }
    }

    val activeProgram = savedPlans.firstOrNull()
    var selectedDayNumber by rememberSaveable(activeProgram?.id, activeProgram?.currentDay) {
        mutableIntStateOf(activeProgram?.currentDay ?: 1)
    }
    var showBuilder by rememberSaveable { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var errorBanner by remember { mutableStateOf<String?>(null) }
    var isBuildingProgram by remember { mutableStateOf(false) }

    var selectedGoal by rememberSaveable {
        mutableStateOf(UserPreferencesRepository.getFitnessGoal(userId).ifBlank { "Full Body" })
    }
    var selectedLevel by rememberSaveable {
        mutableStateOf(UserPreferencesRepository.getPreferredDifficulty(userId).ifBlank { "Beginner" })
    }
    var selectedDaysPerWeek by rememberSaveable {
        mutableIntStateOf(UserPreferencesRepository.getWeeklyGoal(userId).coerceIn(2, 6))
    }
    var selectedMinutes by rememberSaveable { mutableIntStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HwTopBar(
            title = "Workout Plans",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 10.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!statusBanner.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = HwSuccess.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, HwSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = HwSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusBanner!!,
                                color = HwSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (!errorBanner.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = HwError.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, HwError.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = errorBanner!!,
                            color = HwError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (activeProgram != null && !showBuilder) {
                item {
                    ThirtyDayDailyPlanSection(
                        program = activeProgram,
                        selectedDayNumber = selectedDayNumber.coerceIn(1, 30),
                        onSelectDay = { dayNum -> selectedDayNumber = dayNum },
                        onStartDayWorkout = { day ->
                            onStartWorkout("${activeProgram.id}__day_${day.dayNumber}")
                        },
                        onCompleteRestDay = { dayNum ->
                            val updated = WorkoutPlanRepository.markPlanDayCompleted(
                                "${activeProgram.id}__day_${dayNum}"
                            )
                            if (updated != null) {
                                selectedDayNumber = updated.currentDay
                                statusBanner = "Day $dayNum rest complete. Up next: Day ${updated.currentDay}."
                            }
                        },
                        onCustomizeClick = { showBuilder = true }
                    )
                }
            } else {
                item {
                    ThirtyDayProgramBuilderCard(
                        selectedGoal = selectedGoal,
                        selectedLevel = selectedLevel,
                        selectedDaysPerWeek = selectedDaysPerWeek,
                        selectedMinutes = selectedMinutes,
                        isBuilding = isBuildingProgram,
                        hasExistingProgram = activeProgram != null,
                        onSelectGoal = { selectedGoal = it },
                        onSelectLevel = { selectedLevel = it },
                        onSelectDays = { selectedDaysPerWeek = it },
                        onSelectMinutes = { selectedMinutes = it },
                        onBuildProgram = {
                            isBuildingProgram = true
                            errorBanner = null
                            statusBanner = null
                            scope.launch {
                                UserPreferencesRepository.saveProfilePreferences(
                                    fitnessGoal = selectedGoal,
                                    preferredDifficulty = selectedLevel,
                                    weeklyGoal = selectedDaysPerWeek
                                )
                                val res = WorkoutPlanRepository.createAndSaveThirtyDayProgram(
                                    goal = selectedGoal,
                                    difficulty = selectedLevel,
                                    daysPerWeek = selectedDaysPerWeek,
                                    minutesPerWorkout = selectedMinutes
                                )
                                isBuildingProgram = false
                                res.onSuccess { saved ->
                                    selectedDayNumber = saved.currentDay
                                    showBuilder = false
                                    statusBanner = "Plan saved"
                                }.onFailure { err ->
                                    errorBanner = err.message ?: "Could not save workout plan."
                                }
                            }
                        },
                        onCancel = { showBuilder = false },
                        onOpenAIPlan = onOpenAIPlan
                    )
                }
            }

            if (savedPlans.size > 1) {
                item {
                    HwSectionHeader(
                        title = "Saved Programs"
                    )
                }

                items(
                    items = savedPlans.drop(1),
                    key = { it.id }
                ) { savedPlan ->
                    SavedAIPlanCard(
                        plan = savedPlan,
                        onStart = { onStartWorkout(savedPlan.id) },
                        onDelete = { WorkoutPlanRepository.deleteSavedPlan(savedPlan.id) }
                    )
                }
            }

            item {
                HwSectionHeader(
                    title = "Quick Workouts"
                )
            }

            items(
                items = WorkoutPlanRepository.catalogPlans,
                key = { it.id }
            ) { plan ->
                WorkoutPlanCard(
                    plan = plan,
                    onStart = { onStartWorkout(plan.id) }
                )
            }
        }
    }
}

@Composable
private fun ThirtyDayDailyPlanSection(
    program: SavedWorkoutPlan,
    selectedDayNumber: Int,
    onSelectDay: (Int) -> Unit,
    onStartDayWorkout: (AIWorkoutDay) -> Unit,
    onCompleteRestDay: (Int) -> Unit,
    onCustomizeClick: () -> Unit
) {
    val selectedDay = remember(program, selectedDayNumber) {
        program.days.firstOrNull { it.dayNumber == selectedDayNumber }
            ?: program.days.first()
    }

    val startLocalDate = remember(program.startDate) {
        runCatching { LocalDate.parse(program.startDate) }.getOrDefault(LocalDate.now())
    }
    val selectedDateFormatted = remember(startLocalDate, selectedDay.dayNumber) {
        val date = startLocalDate.plusDays((selectedDay.dayNumber - 1).toLong())
        DateTimeFormatter.ofPattern("EEE, MMM d").format(date)
    }

    val completedSet = remember(program.completedDays) { program.completedDays.toSet() }
    val completedCount = completedSet.size
    val progressFraction = (completedCount.toFloat() / 30f).coerceIn(0f, 1f)
    val isSelectedCompleted = selectedDay.dayNumber in completedSet

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Uncluttered 30-Day Calendar & Progress Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = HwCard,
            border = BorderStroke(1.dp, HwBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = program.title,
                            color = HwTextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Day ${program.currentDay} of 30 • $completedCount completed",
                            color = HwTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onCustomizeClick),
                        shape = RoundedCornerShape(10.dp),
                        color = HwCardElevated,
                        border = BorderStroke(1.dp, HwBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Edit plan",
                                tint = HwTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Edit",
                                color = HwTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = HwAccent,
                    trackColor = HwCardElevated,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    program.days.forEach { dayItem ->
                        val isDone = dayItem.dayNumber in completedSet
                        val isCurrent = dayItem.dayNumber == program.currentDay
                        val isSelected = dayItem.dayNumber == selectedDay.dayNumber

                        val bgColor = when {
                            isSelected -> HwAccent
                            isDone -> HwSuccess.copy(alpha = 0.16f)
                            isCurrent -> HwAccentSoft
                            else -> HwCardElevated
                        }
                        val borderColor = when {
                            isSelected -> HwAccent
                            isCurrent -> HwAccent.copy(alpha = 0.7f)
                            isDone -> HwSuccess.copy(alpha = 0.45f)
                            else -> HwBorder
                        }
                        val textColor = when {
                            isSelected -> HwTextOnAccent
                            isDone -> HwSuccess
                            isCurrent -> HwAccent
                            else -> HwTextSecondary
                        }

                        Surface(
                            modifier = Modifier
                                .width(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectDay(dayItem.dayNumber) },
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Day",
                                    color = textColor.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${dayItem.dayNumber}",
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        isDone -> "✓"
                                        dayItem.isRestDay -> "Rest"
                                        else -> "${dayItem.exerciseIds.size}"
                                    },
                                    color = textColor.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Focused Card for Today's Workout / Rest Day
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = HwCard,
            border = BorderStroke(1.dp, HwBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day ${selectedDay.dayNumber} • $selectedDateFormatted",
                        color = HwTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (isSelectedCompleted) {
                        Text(
                            text = "Completed ✓",
                            color = HwSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (selectedDay.dayNumber == program.currentDay) {
                        Text(
                            text = "Today",
                            color = HwAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = selectedDay.focus,
                    color = HwTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (selectedDay.isRestDay) {
                        "Active recovery • ${selectedDay.durationMinutes} min light movement"
                    } else {
                        "${selectedDay.exerciseIds.size} exercises • About ${selectedDay.durationMinutes} min"
                    },
                    color = HwTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedDay.isRestDay) {
                    // Clean Rest-Day Layout with Recovery Illustration
                    RestDayRecoveryIllustration(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Give your muscles time to recover today. A short walk and a few minutes of easy stretching will keep you loose for your next workout.",
                        color = HwTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HwPrimaryButton(
                        text = if (isSelectedCompleted) "Rest Day Completed ✓" else "Complete Rest Day",
                        onClick = { onCompleteRestDay(selectedDay.dayNumber) },
                        icon = Icons.Outlined.Check
                    )
                } else {
                    // Exercise List with Exercise-Specific Image Thumbnails
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        selectedDay.exerciseIds.forEach { exId ->
                            val ex = ExerciseRepository.find(exId)
                            val imageRes = ex?.imageRes ?: ExerciseRepository.imageRes(exId)
                            val exName = ex?.name ?: exId.replace('_', ' ').replaceFirstChar { it.uppercase() }
                            val exCategory = ex?.category ?: "Full Body"
                            val exDuration = ex?.durationSeconds ?: 30

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = HwCardElevated,
                                border = BorderStroke(1.dp, HwBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Exercise-specific image thumbnail
                                    Surface(
                                        modifier = Modifier
                                            .size(width = 68.dp, height = 56.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF4F7F9)
                                    ) {
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = exName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exName,
                                            color = HwTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = exCategory,
                                            color = HwTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Text(
                                        text = "${exDuration}s",
                                        color = HwAccent,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HwPrimaryButton(
                        text = if (isSelectedCompleted) {
                            "Start Day ${selectedDay.dayNumber} Again"
                        } else {
                            "Start Workout"
                        },
                        onClick = { onStartDayWorkout(selectedDay) },
                        icon = Icons.Outlined.PlayArrow
                    )
                }

                if (selectedDay.dayNumber != program.currentDay) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HwSecondaryButton(
                        text = "Back to Today (Day ${program.currentDay})",
                        onClick = { onSelectDay(program.currentDay) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThirtyDayProgramBuilderCard(
    selectedGoal: String,
    selectedLevel: String,
    selectedDaysPerWeek: Int,
    selectedMinutes: Int,
    isBuilding: Boolean,
    hasExistingProgram: Boolean,
    onSelectGoal: (String) -> Unit,
    onSelectLevel: (String) -> Unit,
    onSelectDays: (Int) -> Unit,
    onSelectMinutes: (Int) -> Unit,
    onBuildProgram: () -> Unit,
    onCancel: () -> Unit,
    onOpenAIPlan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Set Up Your 30-Day Plan",
                color = HwTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Pick your main goal, experience level, and how often you want to train each week.",
                color = HwTextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Text(
                text = "Goal",
                color = HwTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Full Body", "Strength", "Cardio", "Core").forEach { goalOption ->
                    val active = selectedGoal.equals(goalOption, ignoreCase = true)
                    SelectorChip(
                        label = goalOption,
                        selected = active,
                        onClick = { onSelectGoal(goalOption) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Level",
                color = HwTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Beginner", "Intermediate", "Advanced").forEach { levelOption ->
                    val active = selectedLevel.equals(levelOption, ignoreCase = true)
                    SelectorChip(
                        label = levelOption,
                        selected = active,
                        onClick = { onSelectLevel(levelOption) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Days per week",
                color = HwTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4, 5, 6).forEach { days ->
                    SelectorChip(
                        label = "$days days",
                        selected = selectedDaysPerWeek == days,
                        onClick = { onSelectDays(days) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Session length",
                color = HwTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 15, 20, 25, 35).forEach { mins ->
                    SelectorChip(
                        label = "$mins min",
                        selected = selectedMinutes == mins,
                        onClick = { onSelectMinutes(mins) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            HwPrimaryButton(
                text = "Save 30-Day Plan",
                onClick = onBuildProgram,
                isLoading = isBuilding,
                icon = Icons.Outlined.Check
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasExistingProgram) {
                    HwSecondaryButton(
                        text = "Cancel",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                }
                HwSecondaryButton(
                    text = "AI Coach Plan",
                    onClick = onOpenAIPlan,
                    icon = Icons.Outlined.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) HwAccent else HwCardElevated,
        border = BorderStroke(1.dp, if (selected) HwAccent else HwBorder)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) HwTextOnAccent else HwTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SavedAIPlanCard(
    plan: SavedWorkoutPlan,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onStart),
        shape = RoundedCornerShape(20.dp),
        color = HwCardElevated,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    color = HwTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Day ${plan.currentDay} of 30 • ${plan.minutesPerWorkout} min",
                    color = HwTextSecondary,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete Plan",
                    tint = HwTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkoutPlanCard(
    plan: WorkoutPlan,
    onStart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onStart),
        shape = RoundedCornerShape(20.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        color = HwTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${plan.difficulty} • ${plan.durationMinutes} min • ${plan.exerciseIds.size} exercises",
                        color = HwTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = HwAccent,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Start ${plan.title}",
                            tint = HwTextOnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Exercise-specific image thumbnail strip for this routine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plan.exerciseIds.take(4).forEach { exId ->
                    val ex = ExerciseRepository.find(exId)
                    val imgRes = ex?.imageRes ?: ExerciseRepository.imageRes(exId)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF4F7F9)
                        ) {
                            Image(
                                painter = painterResource(id = imgRes),
                                contentDescription = ex?.name ?: exId,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ex?.name ?: exId,
                            color = HwTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

