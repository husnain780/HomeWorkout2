package com.homeworkout.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.activity.compose.BackHandler
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onOpenRegister: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    // Prevent back navigation to protected screens after logout
    BackHandler {
        // consume back press
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = 90.dp,
                    y = (-75).dp
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            BrandGreen.copy(alpha = 0.15f),
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
                    horizontal = 22.dp,
                    vertical = 18.dp
                )
        ) {

            LoginBrandHeader()

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(66.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = BrandGreenSoft
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "H",
                            color = BrandGreenDark,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(17.dp)
                )

                Text(
                    text = "Welcome Back",
                    color = BrandNavy,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Continue your fitness journey.",
                    color = BrandMuted,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        BorderSoft
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(17.dp)
                    ) {

                        Text(
                            text = "ACCOUNT ACCESS",
                            color = BrandGreenDark,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp
                        )

                        Spacer(
                            modifier = Modifier.height(13.dp)
                        )

                        PremiumTextField(
                            value = email,
                            onValueChange = {
                                email = it
                            },
                            label = "Email",
                            enabled = !isLoading
                        )

                        Spacer(
                            modifier = Modifier.height(11.dp)
                        )

                        PremiumTextField(
                            value = password,
                            onValueChange = {
                                password = it
                            },
                            label = "Password",
                            enabled = !isLoading
                        )

                        if (errorMessage != null) {

                            Spacer(
                                modifier = Modifier.height(11.dp)
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFF0F0)
                            ) {

                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    ),
                                    color = Color(0xFFB3261E),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(15.dp)
                        )

                        Button(
                            onClick = {
                                onLogin(
                                    email,
                                    password
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(53.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(17.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen,
                                contentColor = Color.White
                            )
                        ) {

                            if (isLoading) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(21.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )

                            } else {

                                Text(
                                    text = "Login",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        OrDivider()

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        OutlinedButton(
                            onClick = onGoogleLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(51.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(17.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                BorderSoft
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BrandNavy
                            )
                        ) {

                            Surface(
                                modifier = Modifier.size(23.dp),
                                shape = CircleShape,
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    BorderSoft
                                )
                            ) {

                                Box(
                                    contentAlignment = Alignment.Center
                                ) {

                                    Text(
                                        text = "G",
                                        color = BrandNavy,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.width(9.dp)
                            )

                            Text(
                                text = "Continue with Google",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(11.dp)
                        )

                        OutlinedButton(
                            onClick = onOpenRegister,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                BrandGreen.copy(alpha = 0.35f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BrandGreenDark
                            )
                        ) {

                            Text(
                                text = "Create Account",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                BrandGreen,
                                CircleShape
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = "Secure account access",
                        color = BrandMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "HOMEWORKOUT • TRAIN SMART • STAY CONSISTENT",
                modifier = Modifier.fillMaxWidth(),
                color = BrandMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun LoginBrandHeader() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        LogoMark()

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Home",
                    color = BrandNavy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Workout",
                    color = BrandGreenDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = "YOUR DAILY TRAINING COMPANION",
                color = BrandMuted,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {

            Text(
                text = "SIGN IN",
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),
                color = BrandGreenDark,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun LogoMark() {

    Row(
        modifier = Modifier
            .width(42.dp)
            .height(42.dp)
            .clip(
                RoundedCornerShape(13.dp)
            )
            .background(BrandGreenSoft),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        LogoBar(
            width = 5.dp,
            height = 19.dp
        )

        Spacer(
            modifier = Modifier.width(2.dp)
        )

        LogoBar(
            width = 4.dp,
            height = 14.dp,
            light = true
        )

        Spacer(
            modifier = Modifier.width(3.dp)
        )

        LogoBar(
            width = 17.dp,
            height = 4.dp
        )

        Spacer(
            modifier = Modifier.width(3.dp)
        )

        LogoBar(
            width = 4.dp,
            height = 14.dp,
            light = true
        )

        Spacer(
            modifier = Modifier.width(2.dp)
        )

        LogoBar(
            width = 5.dp,
            height = 19.dp
        )
    }
}

@Composable
private fun LogoBar(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    light: Boolean = false
) {

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(
                RoundedCornerShape(50.dp)
            )
            .background(
                if (light) {
                    Color(0xFF9FE3D0)
                } else {
                    BrandGreen
                }
            )
    )
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                fontSize = 11.sp
            )
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandGreen,
            unfocusedBorderColor = BorderSoft,
            focusedLabelColor = BrandGreenDark,
            unfocusedLabelColor = BrandMuted,
            cursorColor = BrandGreen,
            focusedTextColor = BrandText,
            unfocusedTextColor = BrandText
        )
    )
}

@Composable
private fun OrDivider() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = BorderSoft
        )

        Text(
            text = "OR",
            modifier = Modifier.padding(
                horizontal = 11.dp
            ),
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = BorderSoft
        )
    }
}
