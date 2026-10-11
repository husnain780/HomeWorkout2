package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.homeworkout.app.R
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.BookIcon
import com.homeworkout.app.ui.CameraIcon
import com.homeworkout.app.ui.CheckmarkIcon
import com.homeworkout.app.ui.ClockIcon
import com.homeworkout.app.ui.CrownIcon
import com.homeworkout.app.ui.DumbbellIcon
import com.homeworkout.app.ui.FlameIcon
import com.homeworkout.app.ui.HwBottomNav
import com.homeworkout.app.ui.HwNavTab
import com.homeworkout.app.ui.SparkleIcon
import com.homeworkout.app.ui.TrophyIcon
import com.homeworkout.app.ui.ZapIcon
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMint
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.SurfaceWhite
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenAICoach: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenFormAnalysis: () -> Unit,
    onOpenAIPlan: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val history = remember { WorkoutHistoryRepository.getAll() }
    val streak = WorkoutAnalytics.currentStreak(history)
    val workoutCount = WorkoutAnalytics.totalWorkouts(history)
    val totalMinutes = WorkoutAnalytics.totalMinutes(history)
    val totalCalories = WorkoutAnalytics.totalCalories(history)
    val weekly = remember(history) { WorkoutAnalytics.lastSevenDays(history) }
    val weeklyWorkouts = weekly.sumOf { it.workoutCount }
    val weeklyGoal = 4
    val goalProgress = (weeklyWorkouts.toFloat() / weeklyGoal).coerceIn(0f, 1f)

    val featured = WorkoutPlanRepository.plans.firstOrNull()
    val user = FirebaseAuth.getInstance().currentUser
    val firstName = user?.displayName
        ?.trim()
        ?.substringBefore(" ")
        ?.takeIf { it.isNotBlank() }
        ?: "Athlete"
    val initial = firstName.first().uppercaseChar().toString()
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")).uppercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 100.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayFormatted,
                        color = BrandGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$greeting,",
                        color = BrandNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = firstName,
                        color = BrandNavy,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile),
                    shape = CircleShape,
                    color = BrandGreenSoft,
                    border = BorderStroke(2.dp, BrandGreen.copy(alpha = 0.35f)),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            color = BrandGreenDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Featured Workout Card (Mint Hero)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandMint),
                border = BorderStroke(1.dp, Color(0xFFD0EADB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(204.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth(0.58f)
                            .padding(start = 18.dp, top = 18.dp, bottom = 18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = BrandGreenDark.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "FEATURED SESSION",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = BrandGreenDark,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = featured?.title ?: "Beginner Full Body",
                            color = BrandNavy,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${featured?.durationMinutes ?: 20}m",
                                color = BrandNavy.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = BrandMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = featured?.difficulty ?: "Beginner",
                                color = BrandNavy.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onStartWorkout,
                            modifier = Modifier.height(42.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            contentPadding = PaddingValues(horizontal = 18.dp)
                        ) {
                            Text(
                                text = "START WORKOUT",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = R.drawable.push_up),
                        contentDescription = featured?.title ?: "Today's workout",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(175.dp)
                            .padding(end = 4.dp, bottom = 4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Statistics (4-metric grid with Vector Icons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStat(
                    icon = { FlameIcon(color = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                    value = "$streak",
                    unit = if (streak == 1) "day" else "days",
                    label = "STREAK",
                    modifier = Modifier.weight(1f)
                )
                DashboardStat(
                    icon = { TrophyIcon(color = Color(0xFFF59E0B), modifier = Modifier.size(16.dp)) },
                    value = "$workoutCount",
                    unit = "done",
                    label = "WORKOUTS",
                    modifier = Modifier.weight(1f)
                )
                DashboardStat(
                    icon = { ClockIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    value = formatMinutes(totalMinutes),
                    unit = "",
                    label = "TIME",
                    modifier = Modifier.weight(1f)
                )
                DashboardStat(
                    icon = { ZapIcon(color = Color(0xFF3B82F6), modifier = Modifier.size(16.dp)) },
                    value = if (totalCalories > 0) "$totalCalories" else "—",
                    unit = "kcal",
                    label = "CALORIES",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Target / Habit Card
            Text(
                text = "Weekly Target",
                color = BrandNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (weeklyWorkouts >= weeklyGoal) BrandGreen else BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        if (weeklyWorkouts >= weeklyGoal) {
                            CheckmarkIcon(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = "$weeklyWorkouts",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Consistency",
                                color = BrandNavy,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (weeklyWorkouts >= weeklyGoal) {
                                    "$weeklyWorkouts / $weeklyGoal Goal Met"
                                } else {
                                    "$weeklyWorkouts of $weeklyGoal workouts"
                                },
                                color = if (weeklyWorkouts >= weeklyGoal) BrandGreenDark else BrandMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE9EEEB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (weeklyWorkouts == 0) 0.03f else goalProgress)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(BrandGreen)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Explore Grid with Vector Icons
            Text(
                text = "Explore & Features",
                color = BrandNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExploreCard(
                    title = "Library",
                    subtitle = "30 movements",
                    icon = { BookIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenExerciseLibrary,
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    title = "AI Coach",
                    subtitle = "General assist",
                    icon = { SparkleIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenAICoach,
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    title = "Plans",
                    subtitle = "Programs",
                    icon = { DumbbellIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenPlans,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExploreCard(
                    title = "Form AI",
                    subtitle = "Camera check",
                    icon = { CameraIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenFormAnalysis,
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    title = "AI Routine",
                    subtitle = "Smart split",
                    icon = { ZapIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenAIPlan,
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    title = "Premium",
                    subtitle = "Pro access",
                    icon = { CrownIcon(color = BrandGreenDark, modifier = Modifier.size(16.dp)) },
                    onClick = onOpenSubscription,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0m"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
private fun DashboardStat(
    icon: @Composable () -> Unit,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = BrandNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    color = BrandMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = BrandGreenDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ExploreCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(106.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(10.dp),
                color = BrandGreenSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Column {
                Text(
                    text = title,
                    color = BrandNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = BrandMuted,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}
