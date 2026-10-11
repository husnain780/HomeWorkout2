package com.homeworkout.app.view

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.R
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.SurfaceWhite

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenAICoach: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenFormAnalysis: () -> Unit,
    onOpenAIPlan: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 32.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandGreenDark,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Ready to move?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrandNavy,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Build momentum, one session at a time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandGreenSoft)
                        .clickable { onOpenProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        color = BrandGreenDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandNavy
                )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(235.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrandNavy,
                                        Color(0xFF22304A)
                                    )
                                )
                            )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.push_up),
                        contentDescription = "Workout",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(205.dp),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(22.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "TODAY'S SESSION",
                                modifier = Modifier.padding(
                                    horizontal = 11.dp,
                                    vertical = 7.dp
                                ),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Beginner\nFull Body",
                            color = Color.White,
                            fontSize = 27.sp,
                            lineHeight = 31.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "20 min  •  4 exercises",
                            color = Color(0xFFD2DBE7),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(22.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 22.dp
                        )
                    ) {
                        Text(
                            text = "START WORKOUT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.7.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick access",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandNavy,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Explore",
                    color = BrandGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            val primaryActions = listOf(
                Triple("WORKOUT PLANS", "Structured sessions", onOpenPlans),
                Triple("EXERCISES", "Browse the library", onOpenExerciseLibrary),
                Triple("PROGRESS", "Track your journey", onOpenProgress),
                Triple("AI COACH", "Personal guidance", onOpenAICoach),
                Triple("AI PLAN", "Build a routine", onOpenAIPlan),
                Triple("FORM CHECK", "Improve technique", onOpenFormAnalysis)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
                contentPadding = PaddingValues(0.dp)
            ) {

                items(primaryActions) { action ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(103.dp)
                            .clickable { action.third() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            BorderSoft
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(15.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(BrandGreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "•",
                                    color = BrandGreenDark,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = action.first,
                                color = BrandNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp
                            )

                            Text(
                                text = action.second,
                                color = BrandMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSubscription() },
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
                            text = "HOMEWORKOUT PREMIUM",
                            color = BrandGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Unlock your complete training experience.",
                            color = BrandNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "›",
                        color = BrandGreenDark,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            HorizontalDivider(
                color = BorderSoft
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "YOUR SPACE",
                color = BrandMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    onClick = onOpenProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        BorderSoft
                    )
                ) {
                    Text(
                        text = "Profile",
                        color = BrandNavy,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenAdmin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        BorderSoft
                    )
                ) {
                    Text(
                        text = "Admin",
                        color = BrandNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Train smarter. Stay consistent.",
                modifier = Modifier.fillMaxWidth(),
                color = BrandMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

