package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val notifications = listOf(
        NotificationItem(
            "Workout Reminder",
            "Your workout is waiting for you today.",
            "10 min ago"
        ),
        NotificationItem(
            "Streak Update",
            "You are on a 4 day workout streak.",
            "1 hour ago"
        ),
        NotificationItem(
            "New Workout",
            "A new full body workout is available.",
            "Yesterday"
        ),
        NotificationItem(
            "Progress",
            "You completed 12 workouts this month.",
            "Yesterday"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            NotificationHeader(
                onBack = onBack,
                notificationCount = notifications.size
            )
        }

        item {
            NotificationHero()
        }

        item {
            SectionHeading()
        }

        items(
            items = notifications,
            key = {
                it.title
            }
        ) { notification ->

            NotificationCard(
                notification = notification
            )
        }

        item {
            AllCaughtUp()
        }
    }
}

@Composable
private fun NotificationHeader(
    onBack: () -> Unit,
    notificationCount: Int
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
                text = "UPDATES",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Notifications",
                color = BrandNavy,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = CircleShape,
            color = BrandNavy
        ) {

            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = notificationCount.toString(),
                    color = BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun NotificationHero() {
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
                    .size(190.dp)
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

            Row(
                modifier = Modifier.padding(19.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.09f)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "!",
                            color = BrandGreen,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = "STAY IN THE LOOP",
                        color = BrandGreenSoft,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Keep your momentum.",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Workout reminders, progress updates and new training ideas.",
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "LATEST UPDATES",
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )

            Text(
                text = "Your latest HomeWorkout activity",
                color = BrandMuted,
                fontSize = 10.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {

            Text(
                text = "RECENT",
                modifier = Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 5.dp
                ),
                color = BrandGreenDark,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) {},
        shape = RoundedCornerShape(23.dp),
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

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            NotificationIcon(
                title = notification.title
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = notification.title,
                        color = BrandText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = notification.time,
                        color = BrandMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = notification.message,
                    color = BrandMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Text(
                text = "›",
                color = BrandNavy,
                fontSize = 23.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun NotificationIcon(
    title: String
) {
    val icon = when {
        title.contains("Workout Reminder") -> "W"
        title.contains("Streak") -> "S"
        title.contains("New Workout") -> "+"
        title.contains("Progress") -> "↗"
        else -> "!"
    }

    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = when {
            title.contains("Workout") -> BrandGreenSoft
            title.contains("Streak") -> Color(0xFFEFF3F9)
            title.contains("Progress") -> Color(0xFFEFF3F9)
            else -> BrandGreenSoft
        }
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = icon,
                color = if (
                    title.contains("Streak") ||
                    title.contains("Progress")
                ) {
                    BrandNavy
                } else {
                    BrandGreenDark
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AllCaughtUp() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = BrandGreenSoft
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(35.dp),
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
                    text = "ALL CAUGHT UP",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )

                Text(
                    text = "You're up to date with HomeWorkout.",
                    color = BrandNavy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
