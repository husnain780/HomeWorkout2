package com.homeworkout.app.view

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.service.BillingManager
import com.homeworkout.app.service.SubscriptionState
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
fun SubscriptionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val billingManager = remember { BillingManager(context) }
    var state by remember { mutableStateOf(SubscriptionState()) }

    DisposableEffect(Unit) {
        billingManager.setListener { state = it }
        billingManager.start()
        onDispose { billingManager.close() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumHeader(onBack = onBack)
        }

        item {
            PremiumHero(isPremium = state.isPremium)
        }

        item {
            PricingCard(
                state = state,
                onSubscribe = {
                    activity?.let { billingManager.launchPurchase(it) }
                }
            )
        }

        item {
            FeaturesCard()
        }

        item {
            StatusCard(state = state)
        }

        item {
            RefreshButton(onClick = { billingManager.start() })
        }

        item {
            FooterNote()
        }
    }
}

@Composable
private fun PremiumHeader(
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
                text = "MEMBERSHIP",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Premium Access",
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
                text = "PRO",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = BrandGreenDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun PremiumHero(
    isPremium: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BrandNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(BrandGreen.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (isPremium) "PRO PLAN ACTIVE" else "ALL FEATURES UNLOCKED",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isPremium) "You're already\na Pro member." else "Elevate your\ntraining.",
                    color = Color.White,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (isPremium) {
                        "Your subscription entitlement is active on this device. Enjoy unrestricted access."
                    } else {
                        "Get intelligent AI coaching, customized workout planning, and real-time form correction."
                    },
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PricingCard(
    state: SubscriptionState,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SUBSCRIPTION TIER",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.productTitle.isNotBlank()) state.productTitle else "HomeWorkout Pro",
                        color = BrandNavy,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {
                    Text(
                        text = if (state.isPremium) "ACTIVE" else "MONTHLY",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Text(
                text = if (state.price.isNotBlank()) state.price else "$4.99 / mo",
                color = BrandNavy,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = if (state.message.isNotBlank()) state.message else "Cancel anytime directly through Google Play Store.",
                color = BrandMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )

            if (state.isPremium) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = BrandGreenSoft
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = SurfaceWhite
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(14.dp)) {
                                    val stroke = Stroke(width = 2.5f, cap = StrokeCap.Round)
                                    drawLine(
                                        color = BrandGreen,
                                        start = Offset(size.width * 0.2f, size.height * 0.5f),
                                        end = Offset(size.width * 0.45f, size.height * 0.8f),
                                        strokeWidth = stroke.width,
                                        cap = stroke.cap
                                    )
                                    drawLine(
                                        color = BrandGreen,
                                        start = Offset(size.width * 0.45f, size.height * 0.8f),
                                        end = Offset(size.width * 0.85f, size.height * 0.2f),
                                        strokeWidth = stroke.width,
                                        cap = stroke.cap
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SUBSCRIPTION ACTIVE",
                                color = BrandGreenDark,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "All premium privileges enabled.",
                                color = BrandNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onSubscribe,
                    enabled = state.isReady && state.price.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFD7DFDB),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = "SUBSCRIBE NOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "INCLUDED BENEFITS",
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Everything you need to stay on track",
                color = BrandMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            PremiumFeature(
                title = "AI Fitness Coach",
                subtitle = "Unlimited conversational fitness guidance",
                icon = "AI"
            )
            PremiumDivider()

            PremiumFeature(
                title = "Adaptive AI Workout Plans",
                subtitle = "Schedules tailored to your equipment and targets",
                icon = "PL"
            )
            PremiumDivider()

            PremiumFeature(
                title = "Real-Time Form Analysis",
                subtitle = "Camera-based pose evaluation and angle feedback",
                icon = "CV"
            )
            PremiumDivider()

            PremiumFeature(
                title = "In-Depth Analytics",
                subtitle = "Full volume, consistency and caloric metrics",
                icon = "AN"
            )
        }
    }
}

@Composable
private fun PremiumFeature(
    title: String,
    subtitle: String,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = BrandGreenSoft
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = icon,
                    color = BrandGreenDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = BrandText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = BrandMuted,
                fontSize = 9.sp
            )
        }

        Canvas(modifier = Modifier.size(14.dp)) {
            val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round)
            drawLine(
                color = BrandGreen,
                start = Offset(size.width * 0.2f, size.height * 0.5f),
                end = Offset(size.width * 0.45f, size.height * 0.8f),
                strokeWidth = stroke.width,
                cap = stroke.cap
            )
            drawLine(
                color = BrandGreen,
                start = Offset(size.width * 0.45f, size.height * 0.8f),
                end = Offset(size.width * 0.85f, size.height * 0.2f),
                strokeWidth = stroke.width,
                cap = stroke.cap
            )
        }
    }
}

@Composable
private fun PremiumDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSoft)
    )
}

@Composable
private fun StatusCard(
    state: SubscriptionState
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (state.isPremium) BrandGreenSoft else SurfaceWhite,
        border = BorderStroke(1.dp, if (state.isPremium) Color.Transparent else BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (state.isPremium) SurfaceWhite else AppBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.isPremium) "✓" else "i",
                        color = BrandGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (state.isPremium) "GOOGLE PLAY CONFIRMED" else "PURCHASE SYSTEM",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = if (state.isPremium) {
                        "Entitlement verified by Google Play Billing."
                    } else {
                        "Purchases and renewals are managed via your Google account."
                    },
                    color = BrandNavy,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RefreshButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSoft),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy)
    ) {
        Text(
            text = "RESTORE / REFRESH PURCHASES",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun FooterNote() {
    Text(
        text = "Recurring subscriptions automatically renew unless canceled in Google Play Store settings.",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        color = BrandMuted,
        fontSize = 9.sp,
        lineHeight = 13.sp,
        textAlign = TextAlign.Center
    )
}
