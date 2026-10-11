package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.NetworkMonitor
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.model.WorkoutPlan
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwMetricCard
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.HwSectionHeader
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwOrange
import com.homeworkout.app.ui.theme.HwPurple
import com.homeworkout.app.ui.theme.HwSuccess
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary

@Composable
fun HomeScreen(
    onStartWorkout: (String) -> Unit,
    onOpenPlans: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenAICoach: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenFormAnalysis: () -> Unit,
    onOpenAIPlan: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenNotifications: () -> Unit = {}
) {
    val historyRev = WorkoutHistoryRepository.revision
    val prefsRev = UserPreferencesRepository.revision
    val plansRev = WorkoutPlanRepository.revision
    val syncRev = CloudSyncRepository.syncRevision

    val currentUserId = SupabaseClient.currentUser?.id
    val history = remember(historyRev, syncRev, currentUserId) {
        WorkoutHistoryRepository.getAll(currentUserId)
    }

    val totalWorkouts = remember(history) { WorkoutAnalytics.totalWorkouts(history) }
    val totalCalories = remember(history) { WorkoutAnalytics.totalCalories(history) }
    val totalMinutes = remember(history) { WorkoutAnalytics.totalMinutes(history) }
    val streakDays = remember(history) { WorkoutAnalytics.currentStreak(history) }

    val userName = remember(prefsRev, syncRev, currentUserId) {
        UserPreferencesRepository.getDisplayName(currentUserId).ifBlank { "Athlete" }
    }

    val preferredDifficulty = remember(prefsRev, syncRev, currentUserId) {
        UserPreferencesRepository.getPreferredDifficulty(currentUserId)
    }

    val savedPlans = remember(plansRev, syncRev, currentUserId) {
        WorkoutPlanRepository.getSavedWorkoutPlans(currentUserId)
    }

    val featuredPlan: WorkoutPlan = remember(savedPlans, preferredDifficulty) {
        savedPlans.firstOrNull()?.toWorkoutPlan()
            ?: WorkoutPlanRepository.catalogPlans.firstOrNull {
                it.difficulty.equals(preferredDifficulty, ignoreCase = true)
            }
            ?: WorkoutPlanRepository.catalogPlans.first()
    }

    val isOnline = NetworkMonitor.isOnline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HomeHeader(
                    userName = userName,
                    isOnline = isOnline,
                    onOpenProfile = onOpenProfile,
                    onOpenNotifications = onOpenNotifications
                )
            }

            item {
                TodayWorkoutCard(
                    plan = featuredPlan,
                    onStartWorkout = { onStartWorkout(featuredPlan.id) },
                    onOpenPlans = onOpenPlans
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HwSectionHeader(
                        title = "Progress",
                        actionText = "Details",
                        onAction = onOpenProgress
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HwMetricCard(
                            label = "Streak",
                            value = "$streakDays d",
                            icon = Icons.Outlined.LocalFireDepartment,
                            accent = HwOrange,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Minutes",
                            value = "$totalMinutes m",
                            icon = Icons.Outlined.Schedule,
                            accent = HwAccent,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Calories",
                            value = "$totalCalories kcal",
                            icon = Icons.Outlined.Bolt,
                            accent = HwCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HwSectionHeader(title = "Explore")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavCard(
                            title = "30-Day Plan",
                            subtitle = "Daily routine",
                            icon = Icons.Outlined.CalendarMonth,
                            accent = HwAccent,
                            onClick = onOpenPlans,
                            modifier = Modifier.weight(1f)
                        )
                        QuickNavCard(
                            title = "Exercise Library",
                            subtitle = "30 exercises",
                            icon = Icons.Outlined.FitnessCenter,
                            accent = HwCyan,
                            onClick = onOpenExerciseLibrary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavCard(
                            title = "AI Coach",
                            subtitle = "Form & guide",
                            icon = Icons.Outlined.Psychology,
                            accent = HwPurple,
                            onClick = onOpenAICoach,
                            modifier = Modifier.weight(1f)
                        )
                        QuickNavCard(
                            title = "Form Camera",
                            subtitle = "Posture check",
                            icon = Icons.Outlined.Videocam,
                            accent = HwOrange,
                            onClick = onOpenFormAnalysis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (CloudSyncRepository.isAdmin) {
                        QuickNavCard(
                            title = "Admin Console",
                            subtitle = "Platform metrics",
                            icon = Icons.Outlined.AdminPanelSettings,
                            accent = HwPurple,
                            onClick = onOpenAdmin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    isOnline: Boolean,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back,",
                color = HwTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = userName,
                color = HwTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HwCard)
                    .clickable(onClick = onOpenNotifications),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = HwTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(HwAccent, HwCyan)
                        )
                    )
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    color = HwTextOnAccent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun TodayWorkoutCard(
    plan: WorkoutPlan,
    onStartWorkout: () -> Unit,
    onOpenPlans: () -> Unit
) {
    val totalCalories = remember(plan) {
        plan.exerciseIds.sumOf { id ->
            ExerciseRepository.exercises.firstOrNull { it.id == id }?.calories ?: 8
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HwCardElevated,
        border = BorderStroke(1.5.dp, HwAccent.copy(alpha = 0.40f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HwAccent.copy(alpha = 0.12f),
                            HwCyan.copy(alpha = 0.04f),
                            HwCard
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HwBadge(
                        text = "TODAY'S WORKOUT",
                        color = HwAccent,
                        containerColor = HwAccentSoft
                    )
                    Text(
                        text = "View Plans →",
                        color = HwTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onOpenPlans)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = plan.title,
                    color = HwTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.4).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plan.durationMinutes} min • ${plan.exerciseCount} exercises • ~$totalCalories kcal",
                    color = HwTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                HwPrimaryButton(
                    text = "Start Workout",
                    onClick = onStartWorkout,
                    icon = Icons.Outlined.PlayArrow
                )
            }
        }
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = HwTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = HwTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
