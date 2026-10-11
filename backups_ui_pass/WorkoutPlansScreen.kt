package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.R
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.WorkoutPlan
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.HwBackButton
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMint
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.SurfaceWhite

@Composable
fun WorkoutPlansScreen(
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit
) {
    val allPlans = WorkoutPlanRepository.plans
    val filterOptions = listOf("All", "Beginner", "Intermediate")
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredPlans = remember(selectedFilter) {
        if (selectedFilter == "All") allPlans
        else allPlans.filter { it.difficulty.equals(selectedFilter, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HwBackButton(onClick = onBack)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TRAINING PROGRAMS",
                        color = BrandGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        text = "Workout Plans",
                        color = BrandNavy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = "${filteredPlans.size} plans",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = BorderSoft)

            // Category filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = filter },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) BrandGreen else SurfaceWhite,
                        border = BorderStroke(1.dp, if (isSelected) BrandGreen else BorderSoft)
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else BrandNavy,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Featured Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandNavy),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BrandNavy, Color(0xFF1E3557))
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(20.dp)
                                    .width(210.dp)
                            ) {
                                Text(
                                    text = "TARGETED ROUTINES",
                                    color = BrandGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Structured plans for every fitness goal.",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No equipment required. Build habits at home.",
                                    color = Color(0xFFC5D1E0),
                                    fontSize = 11.sp
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.bodyweight_squat),
                                contentDescription = "Workout Hero",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(150.dp)
                                    .padding(end = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                items(filteredPlans.size) { index ->
                    val plan = filteredPlans[index]
                    WorkoutPlanCard(
                        plan = plan,
                        imageRes = planImage(plan),
                        onStartWorkout = { onStartWorkout(plan.id) }
                    )
                }

                // AI Coach custom routine prompt
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandMint),
                        border = BorderStroke(1.dp, Color(0xFFC7EBD9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI CUSTOM PLAN",
                                    color = BrandGreenDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Want a personalized routine?",
                                    color = BrandNavy,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generate a custom schedule based on your specific schedule and target muscle groups.",
                                    color = BrandMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun planImage(plan: WorkoutPlan): Int {
    val firstId = plan.exerciseIds.firstOrNull() ?: ""
    return ExerciseRepository.imageRes(firstId)
}

@Composable
private fun WorkoutPlanCard(
    plan: WorkoutPlan,
    imageRes: Int,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF1F6F3), SurfaceWhite)
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = plan.title,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(155.dp),
                    contentScale = ContentScale.Fit
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    shape = RoundedCornerShape(50),
                    color = BrandNavy
                ) {
                    Text(
                        text = plan.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    shape = RoundedCornerShape(50),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = "${plan.exerciseCount} exercises",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = plan.title,
                    color = BrandNavy,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = plan.description,
                    color = BrandMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlanStat(
                        value = "${plan.durationMinutes} min",
                        label = "DURATION",
                        modifier = Modifier.weight(1f)
                    )
                    PlanStat(
                        value = "${plan.exerciseCount}",
                        label = "MOVEMENTS",
                        modifier = Modifier.weight(1f)
                    )
                    PlanStat(
                        value = "${plan.durationMinutes * 8} kcal",
                        label = "EST. BURN",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "START WORKOUT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = BrandNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
