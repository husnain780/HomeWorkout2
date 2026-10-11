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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.PeriodWorkoutStat
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwMetricCard
import com.homeworkout.app.ui.HwSectionHeader
import com.homeworkout.app.ui.HwTopBar
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {
    val historyRev = WorkoutHistoryRepository.revision
    val syncRev = CloudSyncRepository.syncRevision
    val currentUserId = SupabaseClient.currentUser?.id

    val history = remember(historyRev, syncRev, currentUserId) {
        WorkoutHistoryRepository.getAll(currentUserId)
    }

    val totalWorkouts = remember(history) {
        WorkoutAnalytics.totalWorkouts(history)
    }

    val totalCalories = remember(history) {
        WorkoutAnalytics.totalCalories(history)
    }

    val totalMinutes = remember(history) {
        WorkoutAnalytics.totalMinutes(history)
    }

    val streakDays = remember(history) {
        WorkoutAnalytics.currentStreak(history)
    }

    var selectedTab by rememberSaveable { mutableStateOf("Weekly") }
    val periodStats = remember(history, selectedTab) {
        WorkoutAnalytics.statsForPeriod(history, selectedTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HwTopBar(
            title = "Progress",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                PeriodTabs(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )
            }

            if (history.isEmpty()) {
                item {
                    EmptyProgressCard()
                }
            }

            item {
                PeriodChart(
                    period = selectedTab,
                    stats = periodStats
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HwSectionHeader(
                        title = "All-Time Stats"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HwMetricCard(
                            label = "Streak",
                            value = "$streakDays days",
                            icon = Icons.Outlined.LocalFireDepartment,
                            accent = HwOrange,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Sessions",
                            value = totalWorkouts.toString(),
                            icon = Icons.Outlined.FitnessCenter,
                            accent = HwAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HwMetricCard(
                            label = "Est. Burned",
                            value = "$totalCalories kcal",
                            icon = Icons.Outlined.Bolt,
                            accent = HwCyan,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Active Time",
                            value = "$totalMinutes min",
                            icon = Icons.Outlined.Schedule,
                            accent = HwPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HwSectionHeader(
                        title = "Recent Activity"
                    )
                }

                items(
                    items = history.take(15),
                    key = { it.id }
                ) { record ->
                    WorkoutHistoryRow(record = record)
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HwCardElevated,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = HwAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Workouts Yet",
                color = HwTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Complete your first workout to track your streaks, active time, and calories burned.",
                color = HwTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PeriodTabs(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    val tabs = listOf("Weekly", "Monthly", "Yearly")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) HwAccent else HwCard
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) HwTextOnAccent else HwTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodChart(
    period: String,
    stats: List<PeriodWorkoutStat>
) {
    val maxCalories = (stats.maxOfOrNull { it.calories } ?: 0).coerceAtLeast(50)
    val periodWorkouts = stats.sumOf { it.workoutCount }
    val periodMinutes = stats.sumOf { it.minutes }
    val periodCalories = stats.sumOf { it.calories }

    val chartTitle = when (period) {
        "Monthly" -> "Last 4 Weeks Activity"
        "Yearly" -> "Last 6 Months Activity"
        else -> "Last 7 Days Activity"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chartTitle,
                        color = HwTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$periodWorkouts sessions • $periodMinutes min • ~$periodCalories kcal",
                        color = HwTextSecondary,
                        fontSize = 12.sp
                    )
                }

                HwBadge(
                    text = period.uppercase(),
                    color = HwAccent,
                    containerColor = HwAccentSoft
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { stat ->
                    val fraction = if (stat.calories == 0) {
                        0.08f
                    } else {
                        (stat.calories.toFloat() / maxCalories.toFloat()).coerceIn(0.16f, 1f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (stat.calories > 0) "${stat.calories}" else "0",
                            color = if (stat.calories > 0) HwAccent else HwTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(HwCardElevated),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (stat.calories > 0) {
                                            Brush.verticalGradient(
                                                colors = listOf(HwAccent, HwCyan)
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(HwBorder, HwBorder)
                                            )
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stat.label,
                            color = HwTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryRow(
    record: WorkoutHistory
) {
    val formattedDate = remember(record.completedAt) {
        runCatching {
            val dt = Instant.ofEpochMilli(record.completedAt)
                .atZone(ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a").format(dt)
        }.getOrDefault("Completed session")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HwSuccess.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = HwSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.workoutName,
                    color = HwTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    color = HwTextSecondary,
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.durationMinutes} min",
                    color = HwAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "~${record.caloriesBurned} kcal",
                    color = HwTextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
