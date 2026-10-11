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
import com.homeworkout.app.service.CloudAdminStats
import com.homeworkout.app.service.CloudSyncRepository
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
fun AdminDashboardScreen(
    onBack: () -> Unit
) {
    var stats by remember {
        mutableStateOf<CloudAdminStats?>(null)
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("Administrator dashboard")
    }

    if (!CloudSyncRepository.isAdmin) {

        AccessDeniedScreen(
            onBack = onBack
        )

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            AdminHeader(
                onBack = onBack
            )
        }

        item {
            AdminHero(
                message = message
            )
        }

        item {
            StatsSection(
                stats = stats
            )
        }

        item {
            RefreshCard(
                loading = loading,
                onRefresh = {

                    loading = true
                    message = "Loading cloud statistics..."

                    CloudSyncRepository.loadAdminStats { result ->

                        loading = false

                        result
                            .onSuccess { value ->

                                stats = value
                                message = "Cloud statistics loaded."
                            }
                            .onFailure { exception ->

                                message =
                                    exception.message
                                        ?: "Unable to load statistics."
                            }
                    }
                }
            )
        }

        item {
            CloudStatusCard(
                message = CloudSyncRepository.cloudMessage,
                onClick = {
                    message = CloudSyncRepository.cloudMessage
                }
            )
        }

        item {
            SecurityCard()
        }
    }
}

@Composable
private fun AdminHeader(
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
                text = "CONTROL CENTER",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Admin Dashboard",
                color = BrandNavy,
                fontSize = 22.sp,
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
                    text = "A",
                    color = BrandGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AdminHero(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandNavy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Box {

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandGreen.copy(alpha = 0.38f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.17f)
                ) {

                    Text(
                        text = "ADMIN ACCESS",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Cloud operations\nat a glance.",
                    color = Color.White,
                    fontSize = 27.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    stats: CloudAdminStats?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "CLOUD OVERVIEW",
                    color = BrandNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )

                Text(
                    text = "Live values from your cloud data",
                    color = BrandMuted,
                    fontSize = 10.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = if (stats != null) {
                    BrandGreenSoft
                } else {
                    Color.White
                },
                border = if (stats == null) {
                    BorderStroke(
                        1.dp,
                        BorderSoft
                    )
                } else {
                    null
                }
            ) {

                Text(
                    text = if (stats != null) {
                        "LIVE"
                    } else {
                        "NOT LOADED"
                    },
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),
                    color = if (stats != null) {
                        BrandGreenDark
                    } else {
                        BrandMuted
                    },
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            AdminMetricCard(
                title = "TOTAL USERS",
                value = stats?.totalUsers?.toString() ?: "--",
                icon = "U",
                modifier = Modifier.weight(1f)
            )

            AdminMetricCard(
                title = "CLOUD WORKOUTS",
                value = stats?.totalCloudWorkouts?.toString() ?: "--",
                icon = "W",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
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

        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            Surface(
                modifier = Modifier.size(37.dp),
                shape = RoundedCornerShape(12.dp),
                color = BrandGreenSoft
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = icon,
                        color = BrandGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = value,
                color = BrandNavy,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = title,
                color = BrandMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun RefreshCard(
    loading: Boolean,
    onRefresh: () -> Unit
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

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = BrandNavy
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "↻",
                        color = BrandGreen,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(11.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "CLOUD STATISTICS",
                    color = BrandNavy,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp
                )

                Text(
                    text = if (loading) {
                        "Refreshing your dashboard..."
                    } else {
                        "Pull the latest values from Firebase."
                    },
                    color = BrandMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = onRefresh,
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFD6DEDA),
                    disabledContentColor = Color.White
                ),
                contentPadding = PaddingValues(
                    horizontal = 13.dp,
                    vertical = 9.dp
                )
            ) {

                Text(
                    text = if (loading) {
                        "..."
                    } else {
                        "REFRESH"
                    },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun CloudStatusCard(
    message: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(23.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = BrandGreenSoft
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "☁",
                        color = BrandGreenDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(11.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "CLOUD SYNC STATUS",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = message,
                    color = BrandText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = ARROW,
                color = BrandNavy,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun SecurityCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        color = BrandGreenSoft
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = Color.White
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        color = BrandGreenDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "SECURITY",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )

                Text(
                    text = "Administrator privileges are controlled by the Firestore role.",
                    color = BrandNavy,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "The app UI does not grant admin access.",
                    color = BrandMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun AccessDeniedScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                )
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

            Text(
                text = "Admin Dashboard",
                color = BrandNavy,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(
            modifier = Modifier.height(70.dp)
        )

        Surface(
            modifier = Modifier.size(78.dp),
            shape = CircleShape,
            color = Color(0xFFFFEEEE)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "!",
                    color = Color(0xFFC94B4B),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "Access denied",
            color = BrandNavy,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = "This account does not have the administrator role.",
            color = BrandMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
