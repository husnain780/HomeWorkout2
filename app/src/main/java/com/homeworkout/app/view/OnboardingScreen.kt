package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.HwSecondaryButton
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwCyan
import com.homeworkout.app.ui.theme.HwOrange
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary
import com.homeworkout.app.ui.theme.HwTextTertiary

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    var selectedGoal by rememberSaveable { mutableStateOf(UserPreferencesRepository.getFitnessGoal()) }
    var selectedDifficulty by rememberSaveable { mutableStateOf(UserPreferencesRepository.getPreferredDifficulty()) }
    var selectedWeeklyGoal by rememberSaveable { mutableIntStateOf(UserPreferencesRepository.getWeeklyGoal()) }

    val goals = listOf("Full Body", "Strength", "Cardio", "Core")
    val difficulties = listOf("Beginner", "Intermediate", "Advanced")
    val weeklyTargets = listOf(3, 4, 5, 6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HwAccent.copy(alpha = 0.14f),
                            HwCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HwAccent)
                    )
                    Text(
                        text = "HOMEWORKOUT AI",
                        color = HwTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    )
                }

                TextButton(
                    onClick = {
                        UserPreferencesRepository.completeOnboarding(
                            fitnessGoal = selectedGoal,
                            preferredDifficulty = selectedDifficulty,
                            weeklyGoal = selectedWeeklyGoal
                        )
                        onLogin()
                    }
                ) {
                    Text(
                        text = "Sign In",
                        color = HwAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column {
                Text(
                    text = "PERSONALIZE YOUR SETUP",
                    color = HwAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Train Smarter.\nAnywhere.",
                    color = HwTextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 40.sp,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose your primary goal, level, and weekly target. These preferences are saved and used for your weekly goal and AI workout plans.",
                    color = HwTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Goal Selector
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. PRIMARY FITNESS GOAL",
                            color = HwTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goals.forEach { goal ->
                                val isSelected = selectedGoal == goal
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedGoal = goal },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) HwAccent.copy(alpha = 0.16f) else HwCardElevated,
                                    border = BorderStroke(1.dp, if (isSelected) HwAccent else HwBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = goal,
                                            color = if (isSelected) HwAccent else HwTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "2. FITNESS LEVEL",
                            color = HwTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            difficulties.forEach { level ->
                                val isSelected = selectedDifficulty == level
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDifficulty = level },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) HwCyan.copy(alpha = 0.16f) else HwCardElevated,
                                    border = BorderStroke(1.dp, if (isSelected) HwCyan else HwBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = level,
                                            color = if (isSelected) HwCyan else HwTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "3. WEEKLY WORKOUT TARGET",
                            color = HwTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            weeklyTargets.forEach { days ->
                                val isSelected = selectedWeeklyGoal == days
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedWeeklyGoal = days },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) HwOrange.copy(alpha = 0.16f) else HwCardElevated,
                                    border = BorderStroke(1.dp, if (isSelected) HwOrange else HwBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$days Days/Wk",
                                            color = if (isSelected) HwOrange else HwTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OnboardingFeatureCard(
                        icon = Icons.Outlined.AutoAwesome,
                        accent = HwAccent,
                        title = "AI Coach & Custom Routines",
                        subtitle = "Powered by Supabase Edge Functions & Gemini"
                    )
                    OnboardingFeatureCard(
                        icon = Icons.Outlined.Videocam,
                        accent = HwCyan,
                        title = "Real-Time Pose & Form Analysis",
                        subtitle = "Live CameraX + ML Kit joint angle tracking"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column {
                HwPrimaryButton(
                    text = "Save Preferences & Continue",
                    onClick = {
                        UserPreferencesRepository.completeOnboarding(
                            fitnessGoal = selectedGoal,
                            preferredDifficulty = selectedDifficulty,
                            weeklyGoal = selectedWeeklyGoal
                        )
                        onGetStarted()
                    },
                    icon = Icons.Outlined.CheckCircle
                )

                Spacer(modifier = Modifier.height(10.dp))

                HwSecondaryButton(
                    text = "I Already Have an Account",
                    onClick = {
                        UserPreferencesRepository.completeOnboarding(
                            fitnessGoal = selectedGoal,
                            preferredDifficulty = selectedDifficulty,
                            weeklyGoal = selectedWeeklyGoal
                        )
                        onLogin()
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String
) {
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

            Spacer(modifier = Modifier.width(12.dp))

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
                    fontSize = 12.sp
                )
            }
        }
    }
}
