package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.homeworkout.app.ui.theme.BrandMint
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import com.homeworkout.app.ui.theme.SurfaceWhite

@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Weekly") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProgressHeader(onBack = onBack)
        }

        item {
            HeroProgressCard()
        }

        item {
            PeriodTabs(
                selected = selectedTab,
                onSelected = { selectedTab = it }
            )
        }

        item {
            WeeklyChart(selectedTab = selectedTab)
        }

        item {
            SectionTitle(
                title = "TRAINING TOTALS",
                subtitle = "Lifetime metrics and accomplishments"
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
        HwBackButton(onClick = onBack)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "YOUR PERFORMANCE",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )
            Text(
                text = "Progress & Analytics",
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
                text = "Level 1",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = BrandGreenDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "THIS WEEK'S TARGET",
                        color = BrandGreenSoft,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Consistent Effort.",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "4 of 5 weekly workouts completed",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }

                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "80",
                                color = Color.White,
                                fontSize = 21.sp,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(7) { index ->
                    val active = index < 4
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (active) BrandGreen else Color.White.copy(alpha = 0.15f)
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
            .background(SurfaceWhite, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Weekly", "Monthly", "Yearly").forEach { tab ->
            val active = tab == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(tab) },
                shape = RoundedCornerShape(12.dp),
                color = if (active) BrandNavy else Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (active) Color.White else BrandMuted,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedTab.uppercase(),
                        color = BrandNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Activity distribution",
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
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val bars = listOf(0.35f, 0.65f, 0.50f, 0.85f, 0.60f, 0.95f, 0.45f)
                val labels = listOf("M", "T", "W", "T", "F", "S", "S")

                bars.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(value)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(if (index == 5) BrandNavy else BrandGreen)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = labels[index],
                            color = if (index == 5) BrandNavy else BrandMuted,
                            fontSize = 9.sp,
                            fontWeight = if (index == 5) FontWeight.ExtraBold else FontWeight.SemiBold
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
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
private fun OverviewGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                label = "ACTIVE MINUTES",
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = BrandGreenSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = value,
                color = BrandNavy,
                fontSize = 20.sp,
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "7",
                        color = BrandGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Keep going to reach your 10-day milestone.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun InsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = BrandMint,
        border = BorderStroke(1.dp, Color(0xFFC7EBD9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "COACH INSIGHT",
                color = BrandGreenDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "You are most consistent during morning sessions.",
                color = BrandNavy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sticking to your morning window increases your workout completion probability by 40%.",
                color = BrandMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
