package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.ui.HwBackButton
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import com.homeworkout.app.ui.theme.SurfaceWhite

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val isUnread: Boolean = false
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val notifications = listOf(
        NotificationItem(
            "Workout Reminder",
            "Your planned daily workout session is ready when you are.",
            "10 min ago",
            isUnread = true
        ),
        NotificationItem(
            "Streak Update",
            "Outstanding consistency! You are currently on a 7-day streak.",
            "1 hour ago",
            isUnread = true
        ),
        NotificationItem(
            "New Workout Routine",
            "A high-intensity bodyweight strength circuit has been unlocked.",
            "Yesterday",
            isUnread = false
        ),
        NotificationItem(
            "Monthly Progress",
            "Great dedication: You completed 16 workouts this calendar month.",
            "2 days ago",
            isUnread = false
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 32.dp),
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

        items(items = notifications, key = { it.title }) { notification ->
            NotificationCard(notification = notification)
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
        HwBackButton(onClick = onBack)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "UPDATES & ALERTS",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )
            Text(
                text = "Notifications",
                color = BrandNavy,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {
            Text(
                text = "$notificationCount new",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = BrandGreenDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NotificationHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(BrandGreen.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "!",
                            color = BrandGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "STAY CONSISTENT",
                        color = BrandGreenSoft,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Build habits that last.",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Timely reminders, streak updates, and AI coach recommendations.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RECENT ALERTS",
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )
            Text(
                text = "Chronological workout alerts",
                color = BrandMuted,
                fontSize = 10.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {
            Text(
                text = "LATEST",
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                color = BrandGreenDark,
                fontSize = 8.sp,
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, if (notification.isUnread) BrandGreen.copy(alpha = 0.5f) else BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationIcon(title = notification.title)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        color = BrandNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notification.time,
                        color = BrandMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = notification.message,
                    color = BrandText.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun NotificationIcon(
    title: String
) {
    val icon = when {
        title.contains("Workout") -> "W"
        title.contains("Streak") -> "S"
        title.contains("Progress") -> "P"
        else -> "N"
    }

    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = BrandGreenSoft
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = icon,
                color = BrandGreenDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AllCaughtUp() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You're all caught up with alerts",
            color = BrandMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
