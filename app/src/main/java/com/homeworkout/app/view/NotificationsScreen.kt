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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.NotificationRepository
import com.homeworkout.app.model.UserNotification
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwSecondaryButton
import com.homeworkout.app.ui.HwTopBar
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwError
import com.homeworkout.app.ui.theme.HwOrange
import com.homeworkout.app.ui.theme.HwPurple
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val notifRev = NotificationRepository.revision
    val prefsRev = UserPreferencesRepository.revision
    val historyRev = WorkoutHistoryRepository.revision
    val userId = SupabaseClient.currentUser?.id

    val notifications = remember(notifRev, userId) {
        NotificationRepository.getAll(userId)
    }

    val notificationsEnabled = remember(prefsRev, userId) {
        UserPreferencesRepository.isNotificationsEnabled(userId)
    }

    val remindersEnabled = remember(prefsRev, userId) {
        UserPreferencesRepository.isRemindersEnabled(userId)
    }

    val history = remember(historyRev, userId) {
        WorkoutHistoryRepository.getAll(userId)
    }

    val workedOutToday = remember(history) {
        val today = LocalDate.now()
        history.any { WorkoutAnalytics.recordDate(it) == today }
    }

    val currentStreak = remember(history) {
        WorkoutAnalytics.currentStreak(history)
    }

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            NotificationRepository.syncWithCloud(userId)
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HwTopBar(
            title = "Notifications",
            subtitle = if (unreadCount > 0) {
                "$unreadCount unread alert(s)"
            } else {
                "${notifications.size} saved alert(s)"
            },
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Workout & Streak Alerts",
                                    color = HwTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (notificationsEnabled) {
                                        "Active • Recording real workout & AI plan events"
                                    } else {
                                        "Paused • Enable to log new workout notifications"
                                    },
                                    color = HwTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        UserPreferencesRepository.saveProfilePreferences(
                                            notificationsEnabled = enabled
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = HwTextOnAccent,
                                    checkedTrackColor = HwAccent,
                                    uncheckedThumbColor = HwTextSecondary,
                                    uncheckedTrackColor = HwCardElevated
                                )
                            )
                        }

                        if (remindersEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HwCardElevated,
                                border = BorderStroke(1.dp, HwBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (workedOutToday) Icons.Outlined.CheckCircle else Icons.Outlined.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (workedOutToday) HwAccent else HwOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (workedOutToday) {
                                            "Daily Reminder Status: You already completed a workout today! Current streak: $currentStreak day(s)."
                                        } else {
                                            "Daily Reminder Status: No workout logged today yet. Start a session to build your streak ($currentStreak day(s))."
                                        },
                                        color = HwTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (unreadCount > 0) {
                item {
                    HwSecondaryButton(
                        text = "Mark All as Read ($unreadCount)",
                        onClick = { NotificationRepository.markAllRead() },
                        icon = Icons.Outlined.DoneAll
                    )
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = HwCard,
                        border = BorderStroke(1.dp, HwBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = HwTextTertiary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Notifications Yet",
                                color = HwTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notifications are generated from your real activity—such as completing workouts, reaching streak milestones, or saving AI workout plans.",
                                color = HwTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = notifications,
                    key = { it.id }
                ) { item ->
                    RealNotificationCard(
                        item = item,
                        onDelete = { NotificationRepository.deleteNotification(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RealNotificationCard(
    item: UserNotification,
    onDelete: () -> Unit
) {
    val (icon, accent) = when (item.category) {
        "workout" -> Icons.Outlined.FitnessCenter to HwAccent
        "streak" -> Icons.Outlined.LocalFireDepartment to HwOrange
        "ai_plan" -> Icons.Outlined.AutoAwesome to HwCyan
        else -> Icons.Outlined.NotificationsActive to HwPurple
    }

    val timeLabel = remember(item.createdAt) {
        runCatching {
            val dt = Instant.ofEpochMilli(item.createdAt)
                .atZone(ZoneId.systemDefault())
            DateTimeFormatter.ofPattern("MMM d, h:mm a").format(dt)
        }.getOrDefault("Recent")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (!item.isRead) HwCardElevated else HwCard,
        border = BorderStroke(
            width = 1.dp,
            color = if (!item.isRead) accent.copy(alpha = 0.45f) else HwBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = HwTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HwAccent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    color = HwTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeLabel,
                    color = HwTextTertiary,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete Notification",
                    tint = HwTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
