package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun WorkoutCompletionScreen(
    workoutId: String = "beginner_workout",
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BrandGreenSoft,
                            AppBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start = 20.dp,
                    top = 18.dp,
                    end = 20.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            SuccessIcon()

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "WORKOUT COMPLETE",
                color = BrandGreenDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "You showed up.\nThat matters.",
                color = BrandNavy,
                fontSize = 31.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Another session done. Keep building the habit.",
                color = BrandMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            SummaryCard()

            Spacer(
                modifier = Modifier.weight(1f)
            )

            MotivationCard()

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White
                )
            ) {

                Text(
                    text = "CONTINUE $ARROW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun SuccessIcon() {
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 8.dp
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            BrandGreen,
                            BrandGreenDark
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "✓",
                color = Color.White,
                fontSize = 37.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "SESSION SUMMARY",
                        color = BrandNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = "Great work today",
                        color = BrandMuted,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {

                    Text(
                        text = "DONE",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                SummaryMetric(
                    value = "20",
                    label = "MINUTES",
                    modifier = Modifier.weight(1f)
                )

                Divider()

                SummaryMetric(
                    value = "4",
                    label = "EXERCISES",
                    modifier = Modifier.weight(1f)
                )

                Divider()

                SummaryMetric(
                    value = "36",
                    label = "KCAL",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = value,
            color = BrandNavy,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = label,
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(38.dp)
            .background(BorderSoft)
    )
}

@Composable
private fun MotivationCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) {},
        shape = RoundedCornerShape(21.dp),
        color = BrandNavy
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = BrandGreen.copy(alpha = 0.18f)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "↑",
                        color = BrandGreen,
                        fontSize = 18.sp,
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
                    text = "KEEP THE MOMENTUM",
                    color = BrandGreenSoft,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Consistency beats perfection.",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


