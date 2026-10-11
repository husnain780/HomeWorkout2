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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.HwBadge
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.HwSecondaryButton
import com.homeworkout.app.ui.HwTextField
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
import com.homeworkout.app.ui.theme.HwSuccess
import com.homeworkout.app.ui.theme.HwTextOnAccent
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLogout: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenAICoach: () -> Unit,
    onOpenAdmin: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val prefsRev = UserPreferencesRepository.revision
    val historyRev = WorkoutHistoryRepository.revision
    val syncRev = CloudSyncRepository.syncRevision

    val user = SupabaseClient.currentUser
    val userId = user?.id

    val name = remember(prefsRev, syncRev, userId) {
        UserPreferencesRepository.getDisplayName(userId)
    }
    val email = user?.email ?: "Signed-in account"
    val isAdmin = CloudSyncRepository.isAdmin

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editedName by rememberSaveable(name) { mutableStateOf(name) }
    var editedWeeklyGoal by rememberSaveable(prefsRev) {
        mutableIntStateOf(UserPreferencesRepository.getWeeklyGoal(userId))
    }
    var isSavingProfile by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }

    val notificationsEnabled = remember(prefsRev, syncRev, userId) {
        UserPreferencesRepository.isNotificationsEnabled(userId)
    }
    val remindersEnabled = remember(prefsRev, syncRev, userId) {
        UserPreferencesRepository.isRemindersEnabled(userId)
    }

    val history = remember(historyRev, syncRev, userId) {
        WorkoutHistoryRepository.getAll(userId)
    }
    val totalWorkouts = remember(history) { history.size }
    val totalMinutes = remember(history) { history.sumOf { it.durationMinutes } }
    val totalCalories = remember(history) { history.sumOf { it.caloriesBurned } }

    LaunchedEffect(userId) {
        if (user != null) {
            CloudSyncRepository.syncProfileAndRole(user)
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
            title = "Profile",
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(HwAccent, HwCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = HwTextOnAccent,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HwBadge(
                                text = if (isAdmin) "ADMIN" else "ATHLETE",
                                color = if (isAdmin) HwPurple else HwAccent,
                                containerColor = if (isAdmin) HwPurple.copy(alpha = 0.16f) else HwAccentSoft
                            )
                            HwBadge(
                                text = "${UserPreferencesRepository.getWeeklyGoal(userId)} DAYS / WEEK",
                                color = HwCyan,
                                containerColor = HwCyan.copy(alpha = 0.14f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = name,
                            color = HwTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = email,
                            color = HwTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatPill(value = "$totalWorkouts", label = "Workouts")
                            ProfileStatPill(value = "${totalMinutes}m", label = "Minutes")
                            ProfileStatPill(value = "~$totalCalories", label = "Kcal")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!isEditing) {
                            HwSecondaryButton(
                                text = "Edit Profile",
                                onClick = {
                                    editedName = name
                                    editedWeeklyGoal = UserPreferencesRepository.getWeeklyGoal(userId)
                                    saveFeedback = null
                                    saveError = null
                                    isEditing = true
                                },
                                icon = Icons.Outlined.Edit
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HwTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = "Display Name",
                                    placeholder = "Enter your full name"
                                )

                                Text(
                                    text = "WEEKLY WORKOUT GOAL (DAYS / WEEK)",
                                    color = HwTextTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(2, 3, 4, 5, 6).forEach { goalDays ->
                                        val selected = editedWeeklyGoal == goalDays
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { editedWeeklyGoal = goalDays },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) HwAccent else HwCardElevated,
                                            border = BorderStroke(1.dp, if (selected) HwAccent else HwBorder)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${goalDays}d",
                                                    color = if (selected) HwTextOnAccent else HwTextSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HwSecondaryButton(
                                        text = "Cancel",
                                        onClick = {
                                            isEditing = false
                                            saveError = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HwPrimaryButton(
                                        text = "Save Profile",
                                        onClick = {
                                            val cleanName = editedName.trim()
                                            if (cleanName.isEmpty()) {
                                                saveError = "Display name cannot be empty."
                                                return@HwPrimaryButton
                                            }
                                            isSavingProfile = true
                                            saveError = null
                                            saveFeedback = null
                                            scope.launch {
                                                val res = UserPreferencesRepository.saveProfilePreferences(
                                                    displayName = cleanName,
                                                    weeklyGoal = editedWeeklyGoal
                                                )
                                                isSavingProfile = false
                                                res.onSuccess {
                                                    isEditing = false
                                                    saveFeedback = "Profile updated"
                                                }.onFailure { err ->
                                                    saveError = err.message ?: "Failed to update profile."
                                                }
                                            }
                                        },
                                        isLoading = isSavingProfile,
                                        icon = Icons.Outlined.Save,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (!saveFeedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HwSuccess.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, HwSuccess.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = HwSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = saveFeedback!!,
                                        color = HwSuccess,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (!saveError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = saveError!!,
                                color = HwError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "PREFERENCES",
                            color = HwTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchRow(
                            title = "Workout Alerts",
                            subtitle = "Milestone & completion alerts",
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    val res = UserPreferencesRepository.saveProfilePreferences(
                                        notificationsEnabled = enabled
                                    )
                                    if (res.isSuccess) {
                                        saveFeedback = "Preferences saved"
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchRow(
                            title = "Daily Reminders",
                            subtitle = "Daily training reminder prompts",
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    val res = UserPreferencesRepository.saveProfilePreferences(
                                        remindersEnabled = enabled
                                    )
                                    if (res.isSuccess) {
                                        saveFeedback = "Preferences saved"
                                    }
                                }
                            }
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "FEATURES",
                        color = HwTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    ProfileMenuTile(
                        title = "Pro Membership",
                        subtitle = "Subscription & unlocks",
                        icon = Icons.Outlined.WorkspacePremium,
                        accent = HwOrange,
                        onClick = onOpenSubscription
                    )

                    ProfileMenuTile(
                        title = "Notifications",
                        subtitle = "Alerts and activity logs",
                        icon = Icons.Outlined.Notifications,
                        accent = HwCyan,
                        onClick = onOpenNotifications
                    )

                    ProfileMenuTile(
                        title = "Workout Plans",
                        subtitle = "30-day program & quick workouts",
                        icon = Icons.Outlined.FitnessCenter,
                        accent = HwAccent,
                        onClick = onOpenPlans
                    )

                    ProfileMenuTile(
                        title = "Progress & Analytics",
                        subtitle = "Streaks and calorie history",
                        icon = Icons.Outlined.Timeline,
                        accent = HwPurple,
                        onClick = onOpenProgress
                    )

                    ProfileMenuTile(
                        title = "AI Coach",
                        subtitle = "Form & recovery advice",
                        icon = Icons.Outlined.Psychology,
                        accent = HwAccent,
                        onClick = onOpenAICoach
                    )

                    if (isAdmin) {
                        ProfileMenuTile(
                            title = "Admin Console",
                            subtitle = "Platform metrics & controls",
                            icon = Icons.Outlined.AdminPanelSettings,
                            accent = HwPurple,
                            onClick = onOpenAdmin
                        )
                    }
                }
            }

            item {
                HwSecondaryButton(
                    text = "Sign Out",
                    onClick = onLogout,
                    icon = Icons.AutoMirrored.Outlined.Logout
                )
            }
        }
    }
}

@Composable
private fun ProfileStatPill(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = HwTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = HwTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = HwTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = HwTextSecondary,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = HwTextOnAccent,
                checkedTrackColor = HwAccent,
                uncheckedThumbColor = HwTextSecondary,
                uncheckedTrackColor = HwCardElevated
            )
        )
    }
}

@Composable
private fun ProfileMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = HwCard,
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = HwTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = HwTextSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = HwTextTertiary
            )
        }
    }
}
