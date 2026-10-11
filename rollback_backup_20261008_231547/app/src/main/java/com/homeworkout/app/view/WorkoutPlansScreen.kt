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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.homeworkout.app.model.WorkoutPlan
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.SurfaceWhite

@Composable
fun WorkoutPlansScreen(
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit
) {
    val plans = WorkoutPlanRepository.plans

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = BrandNavy,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "WORKOUT PLANS",
                        color = BrandGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp
                    )

                    Text(
                        text = "Choose your session",
                        color = BrandNavy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BrandGreenSoft)
                ) {
                    Text(
                        text = "${plans.size} plans",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                color = BorderSoft
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 18.dp,
                    end = 20.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(27.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandNavy
                        )
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(184.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            BrandNavy,
                                            Color(0xFF263752)
                                        )
                                    )
                                )
                        ) {

                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(22.dp)
                                    .width(205.dp)
                            ) {

                                Text(
                                    text = "TRAIN WITH PURPOSE",
                                    color = BrandGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp
                                )

                                Spacer(modifier = Modifier.height(9.dp))

                                Text(
                                    text = "A plan for\nwhere you are now.",
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    lineHeight = 29.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Pick a session and start moving.",
                                    color = Color(0xFFD3DBE6),
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 10.dp)
                                    .size(165.dp)
                                    .clip(CircleShape)
                                    .background(
                                        BrandGreen.copy(alpha = 0.12f)
                                    )
                            )

                            Image(
                                painter = painterResource(
                                    id = R.drawable.bodyweight_squat
                                ),
                                contentDescription = "Workout plan",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(175.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "AVAILABLE PLANS",
                        color = BrandMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(plans.size) { index ->

                    val plan = plans[index]

                    WorkoutPlanCard(
                        plan = plan,
                        imageRes = planImage(plan),
                        onStartWorkout = {
                            onStartWorkout(plan.id)
                        }
                    )
                }

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandGreenSoft
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "NEED SOMETHING PERSONAL?",
                                    color = BrandGreenDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = "Create a routine around your goal with AI Plan.",
                                    color = BrandNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "AI",
                                color = BrandGreenDark,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun planImage(plan: WorkoutPlan): Int {
    return when (plan.exerciseIds.firstOrNull()) {
        "push_up" -> R.drawable.push_up
        "squat" -> R.drawable.bodyweight_squat
        "plank" -> R.drawable.plank
        "jumping_jack" -> R.drawable.jumping_jacks
        "lunges" -> R.drawable.forward_lunges
        "mountain_climber" -> R.drawable.mountain_climbers
        else -> R.drawable.bodyweight_squat
    }
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
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        border = BorderStroke(
            1.dp,
            BorderSoft
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFF1F6F3),
                                SurfaceWhite
                            )
                        )
                    )
            ) {

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = plan.title,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(170.dp),
                    contentScale = ContentScale.Fit
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BrandNavy)
                ) {
                    Text(
                        text = plan.difficulty.uppercase(),
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 6.dp
                        ),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = plan.title,
                    color = BrandNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = plan.description,
                    color = BrandMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(15.dp))

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
                        label = "EXERCISES",
                        modifier = Modifier.weight(1f)
                    )

                    PlanStat(
                        value = plan.difficulty,
                        label = "LEVEL",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = onStartWorkout,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15.dp),
                        border = BorderStroke(
                            1.dp,
                            BorderSoft
                        ),
                        contentPadding = PaddingValues(
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = "VIEW SESSION",
                            color = BrandNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen
                        ),
                        contentPadding = PaddingValues(
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = "START",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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
            .clip(RoundedCornerShape(14.dp))
            .background(AppBackground)
            .padding(
                horizontal = 9.dp,
                vertical = 10.dp
            )
    ) {

        Text(
            text = value,
            color = BrandNavy,
            fontSize = 12.sp,
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
