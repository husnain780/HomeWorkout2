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
import androidx.compose.ui.draw.clip
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

@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember {
        mutableStateOf("Weekly")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item {
            ProgressHeader(
                onBack = onBack
            )
        }

        item {
            HeroProgressCard()
        }

        item {
            PeriodTabs(
                selected = selectedTab,
                onSelected = {
                    selectedTab = it
                }
            )
        }

        item {
            WeeklyChart(
                selectedTab = selectedTab
            )
        }

        item {
            SectionTitle(
                title = "OVERVIEW",
                subtitle = "Your training at a glance"
            )
        }

        item {
            OverviewGrid()
        }

        item {
            ConsistencyCard()
        }

        item {
            InsightCard()
        }
    }
}

@Composable
private fun ProgressHeader(
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
                text = "YOUR PERFORMANCE",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Progress",
                color = BrandNavy,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = CircleShape,
            color = BrandGreenSoft
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↑",
                    color = BrandGreenDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun HeroProgressCard() {
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

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "THIS WEEK",
                        color = BrandGreenSoft,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "You're building\nmomentum.",
                        color = Color.White,
                        fontSize = 25.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "4 workouts completed",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp
                    )
                }

                Surface(
                    modifier = Modifier.size(86.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "80",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "MIN",
                                color = BrandGreenSoft,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                repeat(7) { index ->

                    val active = index < 4

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (active) {
                                    BrandGreen
                                } else {
                                    Color.White.copy(alpha = 0.11f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodTabs(
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(17.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        listOf(
            "Weekly",
            "Monthly",
            "Yearly"
        ).forEach { tab ->

            val active = tab == selected

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSelected(tab)
                    },
                shape = RoundedCornerShape(13.dp),
                color = if (active) {
                    BrandNavy
                } else {
                    Color.Transparent
                }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = tab,
                        color = if (active) {
                            Color.White
                        } else {
                            BrandMuted
                        },
                        fontSize = 10.sp,
                        fontWeight = if (active) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.SemiBold
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChart(
    selectedTab: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = selectedTab.uppercase(),
                        color = BrandNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = "Workout activity",
                        color = BrandMuted,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {

                    Text(
                        text = "+18% VS LAST PERIOD",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        color = BrandGreenDark,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {

                val bars = listOf(
                    0.35f,
                    0.62f,
                    0.48f,
                    0.82f,
                    0.57f,
                    0.91f,
                    0.43f
                )

                bars.forEachIndexed { index, value ->

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {

                        Box(
                            modifier = Modifier
                                .width(23.dp)
                                .fillMaxHeight(value)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp
                                    )
                                )
                                .background(
                                    if (index == 5) {
                                        BrandNavy
                                    } else {
                                        BrandGreen
                                    }
                                )
                        )

                        Spacer(
                            modifier = Modifier.height(7.dp)
                        )

                        Text(
                            text = listOf(
                                "M",
                                "T",
                                "W",
                                "T",
                                "F",
                                "S",
                                "S"
                            )[index],
                            color = if (index == 5) {
                                BrandNavy
                            } else {
                                BrandMuted
                            },
                            fontSize = 8.sp,
                            fontWeight = if (index == 5) {
                                FontWeight.ExtraBold
                            } else {
                                FontWeight.SemiBold
                            }
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
            fontSize = 13.sp,
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
private fun OverviewGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OverviewCard(
                value = "48",
                label = "TOTAL WORKOUTS",
                icon = "W",
                modifier = Modifier.weight(1f)
            )

            OverviewCard(
                value = "12,450",
                label = "TOTAL CALORIES",
                icon = "K",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OverviewCard(
                value = "720",
                label = "TOTAL MINUTES",
                icon = "M",
                modifier = Modifier.weight(1f)
            )

            OverviewCard(
                value = "7 days",
                label = "CURRENT STREAK",
                icon = "S",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OverviewCard(
    value: String,
    label: String,
    icon: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(21.dp),
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(11.dp),
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
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = label,
                color = BrandMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ConsistencyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandNavy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(47.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.16f)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "7",
                        color = BrandGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "CURRENT STREAK",
                    color = BrandGreenSoft,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "7 days in a row",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Keep going to reach 10 days.",
                    color = Color.White.copy(alpha = 0.66f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun InsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        color = BrandGreenSoft
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = "YOUR INSIGHT",
                color = BrandGreenDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "You're most consistent on weekends.",
                color = BrandNavy,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Keep that momentum and add one more weekday session.",
                color = BrandMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}
