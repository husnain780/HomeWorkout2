package com.homeworkout.app.view

import android.app.Activity
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.homeworkout.app.service.BillingManager
import com.homeworkout.app.service.SubscriptionState
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
fun SubscriptionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val billingManager = remember {
        BillingManager(context)
    }

    var state by remember {
        mutableStateOf(
            SubscriptionState()
        )
    }

    DisposableEffect(Unit) {

        billingManager.setListener {
            state = it
        }

        billingManager.start()

        onDispose {
            billingManager.close()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {

        item {
            PremiumHeader(
                onBack = onBack
            )
        }

        item {
            PremiumHero(
                isPremium = state.isPremium
            )
        }

        item {
            PricingCard(
                state = state,
                onSubscribe = {
                    activity?.let {
                        billingManager.launchPurchase(it)
                    }
                }
            )
        }

        item {
            FeaturesCard()
        }

        item {
            StatusCard(
                state = state
            )
        }

        item {
            RefreshButton(
                onClick = {
                    billingManager.start()
                }
            )
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
                text = "UPGRADE",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )

            Text(
                text = "Go Premium",
                color = BrandNavy,
                fontSize = 23.sp,
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
                    text = "✦",
                    color = BrandGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PremiumHero(
    isPremium: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(29.dp),
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
                                BrandGreen.copy(alpha = 0.42f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(21.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreen.copy(alpha = 0.17f)
                ) {

                    Text(
                        text = if (isPremium) {
                            "PREMIUM ACTIVE"
                        } else {
                            "PREMIUM EXPERIENCE"
                        },
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
                    text = if (isPremium) {
                        "You're already\npremium."
                    } else {
                        "Unlock your\nfull potential."
                    },
                    color = Color.White,
                    fontSize = 29.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (isPremium) {
                        "Your premium entitlement is active on this device."
                    } else {
                        "Train smarter with deeper coaching, planning and progress tools."
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 17.sp
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
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "HOMEWORKOUT PREMIUM",
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = if (state.productTitle.isNotBlank()) {
                            state.productTitle
                        } else {
                            "Premium subscription"
                        },
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
                        text = if (state.isPremium) {
                            "ACTIVE"
                        } else {
                            "MONTHLY"
                        },
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        ),
                        color = BrandGreenDark,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Text(
                text = if (state.price.isNotBlank()) {
                    state.price
                } else {
                    "Price shown by Google Play"
                },
                color = BrandNavy,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = state.message,
                color = BrandMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )

            if (state.isPremium) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = BrandGreenSoft
                ) {

                    Row(
                        modifier = Modifier.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(34.dp),
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
                            modifier = Modifier.width(9.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "PREMIUM ACTIVE",
                                color = BrandGreenDark,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )

                            Text(
                                text = "You're all set.",
                                color = BrandNavy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

            } else {

                Button(
                    onClick = onSubscribe,
                    enabled = state.isReady &&
                            state.price.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFD7DFDB),
                        disabledContentColor = Color.White
                    )
                ) {

                    Text(
                        text = "SUBSCRIBE NOW  $ARROW",
                        fontSize = 10.sp,
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Text(
                text = "WHAT YOU UNLOCK",
                color = BrandNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "More tools for a more focused journey",
                color = BrandMuted,
                fontSize = 10.sp
            )

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            PremiumFeature(
                title = "AI Coach",
                subtitle = "Personalized fitness guidance",
                icon = "AI"
            )

            PremiumDivider()

            PremiumFeature(
                title = "AI Workout Plans",
                subtitle = "Plans built around your goals",
                icon = "P"
            )

            PremiumDivider()

            PremiumFeature(
                title = "Advanced Form Analysis",
                subtitle = "More detailed training feedback",
                icon = "F"
            )

            PremiumDivider()

            PremiumFeature(
                title = "Detailed Progress",
                subtitle = "See more of your training journey",
                icon = "↗"
            )

            PremiumDivider()

            PremiumFeature(
                title = "Future Premium Features",
                subtitle = "More tools coming over time",
                icon = "✦"
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
            .padding(
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(12.dp),
            color = BrandGreenSoft
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = icon,
                    color = BrandGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = title,
                color = BrandText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                color = BrandMuted,
                fontSize = 9.sp
            )
        }

        Text(
            text = "✓",
            color = BrandGreenDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
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
        shape = RoundedCornerShape(21.dp),
        color = if (state.isPremium) {
            BrandGreenSoft
        } else {
            Color.White
        },
        border = BorderStroke(
            1.dp,
            if (state.isPremium) {
                Color.Transparent
            } else {
                BorderSoft
            }
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (state.isPremium) {
                    Color.White
                } else {
                    AppBackground
                }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = if (state.isPremium) {
                            "✓"
                        } else {
                            "i"
                        },
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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = if (state.isPremium) {
                        "ENTITLEMENT ACTIVE"
                    } else {
                        "SUBSCRIPTION STATUS"
                    },
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = if (state.isPremium) {
                        "Premium is active on this device."
                    } else {
                        "Purchases and entitlement are managed by Google Play."
                    },
                    color = BrandNavy,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
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
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            BrandNavy
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrandNavy
        )
    ) {

        Text(
            text = "REFRESH SUBSCRIPTION",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun FooterNote() {
    Text(
        text = "Subscription purchases are securely handled through Google Play.",
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 5.dp
            ),
        color = BrandMuted,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        textAlign = TextAlign.Center
    )
}
