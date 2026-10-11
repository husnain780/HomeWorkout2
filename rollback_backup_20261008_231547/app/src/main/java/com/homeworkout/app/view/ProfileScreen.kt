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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText

private const val ARROW = "›"

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var notificationsEnabled by remember {
        mutableStateOf(true)
    }

    var remindersEnabled by remember {
        mutableStateOf(true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            ProfileHeader(
                onBack = onBack
            )
        }

        item {
            ProfileHero()
        }

        item {
            SectionTitle(
                title = "FITNESS PROFILE",
                subtitle = "Your current training preferences"
            )
        }

        item {
            FitnessCard()
        }

        item {
            SectionTitle(
                title = "PREFERENCES",
                subtitle = "Control how HomeWorkout keeps you updated"
            )
        }

        item {
            PreferencesCard(
                notificationsEnabled = notificationsEnabled,
                remindersEnabled = remindersEnabled,
                onNotificationsChanged = {
                    notificationsEnabled = it
                },
                onRemindersChanged = {
                    remindersEnabled = it
                }
            )
        }

        item {
            NotificationsButton(
                onClick = onOpenNotifications
            )
        }

        item {
            EditProfileButton()
        }

        item {
            AccountFooter()
        }
    }
}

@Composable
private fun ProfileHeader(
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
                text = "ACCOUNT",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Profile",
                color = BrandNavy,
                fontSize = 24.sp,
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
                    text = "H",
                    color = BrandGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ProfileHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandNavy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandGreen.copy(alpha = 0.34f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(78.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.09f),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.14f)
                    )
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "H",
                            color = BrandGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Husnain",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "user@homeworkout.app",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 10.sp
                )

                Spacer(
                    modifier = Modifier.height(11.dp)
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.17f)
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    BrandGreen,
                                    CircleShape
                                )
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "PREMIUM MEMBER",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
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
}

@Composable
private fun FitnessCard() {
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
            modifier = Modifier.padding(14.dp)
        ) {

            ProfileValueRow(
                title = "Fitness Level",
                value = "Beginner",
                icon = "L"
            )

            ThinDivider()

            ProfileValueRow(
                title = "Primary Goal",
                value = "Build Strength",
                icon = "G"
            )

            ThinDivider()

            ProfileValueRow(
                title = "Workout Preference",
                value = "Home Workouts",
                icon = "H"
            )
        }
    }
}

@Composable
private fun ProfileValueRow(
    title: String,
    value: String,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(11.dp),
            color = BrandGreenSoft
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = icon,
                    color = BrandGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Text(
            text = title,
            color = BrandMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = BrandNavy,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSoft)
    )
}

@Composable
private fun PreferencesCard(
    notificationsEnabled: Boolean,
    remindersEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onRemindersChanged: (Boolean) -> Unit
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
            modifier = Modifier.padding(14.dp)
        ) {

            PreferenceItem(
                title = "Notifications",
                subtitle = "Workout and progress updates",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )

            ThinDivider()

            PreferenceItem(
                title = "Workout Reminders",
                subtitle = "Stay consistent with reminders",
                checked = remindersEnabled,
                onCheckedChange = onRemindersChanged
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = BrandText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = subtitle,
                color = BrandMuted,
                fontSize = 9.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD9E0DC),
                uncheckedBorderColor = Color(0xFFD0D8D3)
            )
        )
    }
}

@Composable
private fun NotificationsButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandGreen,
            contentColor = Color.White
        )
    ) {

        Text(
            text = "VIEW NOTIFICATIONS  $ARROW",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun EditProfileButton() {
    OutlinedButton(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(
            1.dp,
            BrandNavy
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrandNavy
        )
    ) {

        Text(
            text = "EDIT PROFILE",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun AccountFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        color = BrandGreenSoft
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color.White
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        color = BrandGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = "PROFILE STATUS",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Your fitness preferences are ready.",
                    color = BrandNavy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
