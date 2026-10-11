package com.homeworkout.app.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 85.dp, y = (-70).dp)
                .background(
                    Brush.radialGradient(
                        listOf(BrandGreen.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = BrandGreenSoft
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "HW",
                            color = BrandGreenDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Home",
                            color = BrandNavy,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Workout",
                            color = BrandGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "YOUR DAILY TRAINING COMPANION",
                        color = BrandMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = "01 / 03",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large exercise visual card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(BrandMint, SurfaceWhite)
                        )
                    )
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "MOVE • TRAIN • GROW",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.bodyweight_squat),
                    contentDescription = "Fitness Training",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp, bottom = 12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Fitness Journey",
                color = BrandNavy,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Starts Right Here.",
                color = BrandGreen,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Get personalized workout plans, track real daily streaks, and master exercise technique with live computer vision form feedback.",
                color = BrandMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BrandGreen)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BorderSoft)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BorderSoft)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "GET STARTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account?",
                    color = BrandMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign In",
                    color = BrandGreenDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable(onClick = onLogin)
                )
            }
        }
    }
}
