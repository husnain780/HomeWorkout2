package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.service.CloudAdminStats
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwMetricCard
import com.homeworkout.app.ui.HwSecondaryButton
import com.homeworkout.app.ui.HwSectionHeader
import com.homeworkout.app.ui.HwTopBar
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwError
import com.homeworkout.app.ui.theme.HwOrange
import com.homeworkout.app.ui.theme.HwPurple
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit
) {
    val historyRev = WorkoutHistoryRepository.revision
    val totalWorkouts = remember(historyRev) {
        WorkoutHistoryRepository.getTotalWorkouts()
    }
    val totalCalories = remember(historyRev) {
        WorkoutHistoryRepository.getTotalCalories()
    }
    val totalMinutes = remember(historyRev) {
        WorkoutHistoryRepository.getTotalMinutes()
    }

    var stats by remember { mutableStateOf(CloudAdminStats()) }
    var isLoading by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf("Loading platform metrics from Supabase...") }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val isAdmin = CloudSyncRepository.isAdmin

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        statusMessage = "Verifying administrator privileges via Supabase..."
        CloudSyncRepository.loadAdminStats { result ->
            isLoading = false
            result.onSuccess {
                stats = it
                statusMessage = "Live analytics loaded via secured get_admin_stats() RPC."
            }.onFailure { error ->
                statusMessage = error.message ?: "Admin analytics unavailable."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HwTopBar(
            title = "Admin Dashboard",
            subtitle = "Protected by Supabase RLS & get_admin_stats() RPC",
            onBack = onBack
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = HwAccent)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = statusMessage,
                    color = HwTextSecondary,
                    fontSize = 14.sp
                )
            }
            return@Column
        }

        if (!isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwError.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Restricted",
                            tint = HwError,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Access Denied",
                            color = HwTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Administrator privileges are required to view platform-wide metrics. Your account currently has the standard 'user' role in public.profiles.",
                            color = HwTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HwBadge(
                            text = "ENFORCED BY SUPABASE RLS",
                            color = HwError,
                            containerColor = HwError.copy(alpha = 0.14f)
                        )
                    }
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            HwBadge(
                                text = "ADMIN VERIFIED",
                                color = HwAccent,
                                containerColor = HwAccentSoft
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = statusMessage,
                                color = HwTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            tint = HwAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HwSectionHeader(
                        title = "Cloud Platform Telemetry",
                        subtitle = "Aggregated across all Supabase accounts via get_admin_stats()"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HwMetricCard(
                            label = "Registered Users",
                            value = stats.totalUsers.toString(),
                            icon = Icons.Outlined.Group,
                            accent = HwCyan,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Cloud Workouts",
                            value = stats.totalCloudWorkouts.toString(),
                            icon = Icons.Outlined.CloudDone,
                            accent = HwAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HwSectionHeader(
                        title = "Your Admin Account Stats",
                        subtitle = "Workouts logged under your signed-in account"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HwMetricCard(
                            label = "My Workouts",
                            value = totalWorkouts.toString(),
                            icon = Icons.Outlined.FitnessCenter,
                            accent = HwAccent,
                            modifier = Modifier.weight(1f)
                        )
                        HwMetricCard(
                            label = "Est. Calories",
                            value = "$totalCalories kcal",
                            icon = Icons.Outlined.Bolt,
                            accent = HwOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HwMetricCard(
                        label = "Active Minutes",
                        value = "$totalMinutes min",
                        icon = Icons.Outlined.Schedule,
                        accent = HwPurple,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                HwSecondaryButton(
                    text = "Refresh Platform Analytics",
                    onClick = { refreshTrigger++ },
                    icon = Icons.Outlined.Refresh
                )
            }
        }
    }
}
