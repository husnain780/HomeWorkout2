package com.homeworkout.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen() {

    val background = Color(0xFF071314)
    val navy = Color(0xFF172033)
    val green = Color(0xFF2FA36B)
    val greenLight = Color(0xFF9FE3D0)
    val white = Color(0xFFF7FAF8)
    val muted = Color(0xFF91A09C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        background,
                        Color(0xFF0B1919),
                        navy
                    )
                )
            )
    ) {

        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = 90.dp,
                    y = (-70).dp
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            green.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(
                    x = (-80).dp,
                    y = 80.dp
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            greenLight.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 28.dp,
                    vertical = 26.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.weight(1f)
            )

            SurfaceLogo(
                green = green,
                greenLight = greenLight
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Home",
                    color = white,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )

                Text(
                    text = "Workout",
                    color = greenLight,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Better today.",
                color = white.copy(alpha = 0.92f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Stronger tomorrow.",
                color = muted,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.055f)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 13.dp,
                        vertical = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                green,
                                CircleShape
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = "SMART FITNESS • REAL PROGRESS",
                        color = white.copy(alpha = 0.68f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "GET READY",
                    color = white.copy(alpha = 0.48f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                )

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(50.dp)
                        )
                        .background(
                            Color.White.copy(alpha = 0.10f)
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight()
                            .clip(
                                RoundedCornerShape(50.dp)
                            )
                            .background(green)
                    )
                }
            }
        }
    }
}

@Composable
private fun SurfaceLogo(
    green: Color,
    greenLight: Color
) {

    Box(
        modifier = Modifier
            .size(94.dp)
            .clip(
                RoundedCornerShape(28.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        green.copy(alpha = 0.22f),
                        greenLight.copy(alpha = 0.07f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(9.dp)
                    .height(32.dp)
                    .clip(
                        RoundedCornerShape(5.dp)
                    )
                    .background(green)
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(23.dp)
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
                    .background(greenLight)
            )

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Box(
                modifier = Modifier
                    .width(39.dp)
                    .height(6.dp)
                    .clip(
                        RoundedCornerShape(5.dp)
                    )
                    .background(green)
            )

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(23.dp)
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
                    .background(greenLight)
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Box(
                modifier = Modifier
                    .width(9.dp)
                    .height(32.dp)
                    .clip(
                        RoundedCornerShape(5.dp)
                    )
                    .background(green)
            )
        }
    }
}

