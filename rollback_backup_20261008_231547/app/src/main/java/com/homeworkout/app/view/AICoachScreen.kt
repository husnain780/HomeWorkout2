package com.homeworkout.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.homeworkout.app.controller.AICoachController
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.AICoachMessage
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import kotlinx.coroutines.launch

@Composable
fun AICoachScreen(
    onBack: () -> Unit
) {
    val eventBus = remember {
        EventBus()
    }

    val controller = remember {
        AICoachController(eventBus)
    }

    val messages = remember {
        mutableStateListOf(
            AICoachMessage(
                id = "welcome",
                text = "Hi! I'm your HomeWorkout AI Coach. Ask me about workouts, technique, consistency, recovery or your progress.",
                isUser = false
            )
        )
    }

    var input by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 94.dp)
        ) {

            CoachHeader(
                onBack = onBack
            )

            CoachIntro()

            QuickQuestions(
                enabled = !isLoading,
                onSelected = {
                    input = it
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 18.dp,
                    top = 12.dp,
                    end = 18.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = messages,
                    key = {
                        it.id
                    }
                ) { message ->

                    CoachBubble(
                        message = message
                    )
                }

                if (isLoading) {

                    item {
                        ThinkingBubble()
                    }
                }
            }
        }

        Composer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            input = input,
            isLoading = isLoading,
            onInputChanged = {
                input = it
            },
            onSend = {

                val question = input.trim()

                if (
                    question.isEmpty() ||
                    isLoading
                ) {
                    return@Composer
                }

                messages.add(
                    controller.userMessage(
                        question
                    )
                )

                input = ""
                isLoading = true

                scope.launch {

                    val history =
                        WorkoutHistoryRepository.getAll()

                    val response =
                        controller.ask(
                            message = question,
                            history = history
                        )

                    messages.add(
                        controller.coachMessage(
                            response
                        )
                    )

                    isLoading = false
                }
            }
        )
    }
}

@Composable
private fun CoachHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                PaddingValues(
                    start = 18.dp,
                    top = 12.dp,
                    end = 18.dp,
                    bottom = 10.dp
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier
                .size(43.dp)
                .clickable(onClick = onBack),
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(
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
            modifier = Modifier.width(12.dp)
        )

        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "AI COACH",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            )

            Text(
                text = "Your personal training guide",
                color = BrandNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 6.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BrandGreen)
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text = "READY",
                    color = BrandGreenDark,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun CoachIntro() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 18.dp,
                end = 18.dp
            ),
        shape = RoundedCornerShape(25.dp),
        color = BrandNavy
    ) {

        Box {

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                BrandGreen.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.09f)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "AI",
                            color = BrandGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = "Hi there!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Tell me what you're working on and I'll help you move forward.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickQuestions(
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(
            top = 13.dp
        ),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        Text(
            text = "TRY ASKING",
            modifier = Modifier.padding(
                start = 18.dp,
                end = 18.dp
            ),
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    end = 18.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {

            QuickChip(
                text = "Workout advice",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelected(
                        "Give me advice for my next workout."
                    )
                }
            )

            QuickChip(
                text = "Recovery",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelected(
                        "How can I improve my recovery?"
                    )
                }
            )

            QuickChip(
                text = "Progress",
                enabled = enabled,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelected(
                        "How can I improve my workout progress?"
                    )
                }
            )
        }
    }
}

@Composable
private fun QuickChip(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(13.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Box(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 9.dp
            ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                color = BrandNavy,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CoachBubble(
    message: AICoachMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        verticalAlignment = Alignment.Bottom
    ) {

        if (!message.isUser) {

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = BrandNavy
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "AI",
                        color = BrandGreen,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(7.dp)
            )
        }

        Surface(
            modifier = Modifier.widthIn(
                max = 305.dp
            ),
            shape = if (message.isUser) {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 5.dp
                )
            } else {
                RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )
            },
            color = if (message.isUser) {
                BrandGreen
            } else {
                Color.White
            },
            border = if (message.isUser) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    BorderSoft
                )
            }
        ) {

            Text(
                text = message.text,
                modifier = Modifier.padding(
                    13.dp
                ),
                color = if (message.isUser) {
                    Color.White
                } else {
                    BrandText
                },
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ThinkingBubble() {
    Row(
        verticalAlignment = Alignment.Bottom
    ) {

        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.width(7.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 5.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                BorderSoft
            )
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(BrandGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun Composer(
    modifier: Modifier,
    input: String,
    isLoading: Boolean,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 14.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Row(
            modifier = Modifier.padding(
                start = 14.dp,
                top = 10.dp,
                end = 14.dp,
                bottom = 10.dp
            ),
            verticalAlignment = Alignment.Bottom
        ) {

            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                placeholder = {
                    Text(
                        text = "Ask your coach...",
                        color = BrandMuted,
                        fontSize = 12.sp
                    )
                },
                maxLines = 3,
                shape = RoundedCornerShape(17.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppBackground,
                    unfocusedContainerColor = AppBackground,
                    disabledContainerColor = AppBackground,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = BorderSoft,
                    focusedTextColor = BrandText,
                    unfocusedTextColor = BrandText,
                    cursorColor = BrandGreenDark
                )
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .clickable(
                        enabled = input.trim().isNotEmpty() &&
                                !isLoading,
                        onClick = onSend
                    ),
                shape = RoundedCornerShape(17.dp),
                color = if (
                    input.trim().isNotEmpty() &&
                    !isLoading
                ) {
                    BrandGreen
                } else {
                    Color(0xFFE5EAE7)
                }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "↑",
                        color = if (
                            input.trim().isNotEmpty() &&
                            !isLoading
                        ) {
                            Color.White
                        } else {
                            BrandMuted
                        },
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
