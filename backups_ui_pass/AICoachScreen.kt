package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.controller.AICoachController
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.AICoachMessage
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.ui.HwBackButton
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText
import com.homeworkout.app.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

@Composable
fun AICoachScreen(
    onBack: () -> Unit
) {
    val eventBus = remember { EventBus() }
    val controller = remember { AICoachController(eventBus) }
    val messages = remember {
        mutableStateListOf(
            AICoachMessage(
                id = "welcome",
                text = "Hello! I am your AI Assistant & Coach. I can help you with programming & code debugging, mathematics, learning & study assistance, writing, productivity, and comprehensive fitness guidance. What would you like to explore?",
                isUser = false
            )
        )
    }

    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
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
                .imePadding()
        ) {
            CoachHeader(onBack = onBack)

            CoachIntro()

            GeneralPromptChips(
                enabled = !isLoading,
                onSelected = { prompt ->
                    input = prompt
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = messages, key = { it.id }) { message ->
                    CoachBubble(message = message)
                }

                if (isLoading) {
                    item { ThinkingBubble() }
                }
            }

            Composer(
                modifier = Modifier.navigationBarsPadding(),
                input = input,
                isLoading = isLoading,
                onInputChanged = { input = it },
                onSend = {
                    val question = input.trim()
                    if (question.isEmpty() || isLoading) return@Composer

                    messages.add(controller.userMessage(question))
                    input = ""
                    isLoading = true

                    scope.launch {
                        val history = WorkoutHistoryRepository.getAll()
                        val response = controller.ask(message = question, history = history)
                        messages.add(controller.coachMessage(response))
                        isLoading = false
                    }
                }
            )
        }
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
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HwBackButton(onClick = onBack)

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "GENERAL-PURPOSE ASSISTANT",
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "AI Assistant & Coach",
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BrandGreen)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "GEMINI",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
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
            .padding(horizontal = 18.dp, vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = BrandNavy
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(BrandGreen.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "How can I help today?",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Ask anything: code, debugging, math, learning, writing, habits, or fitness guidance.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralPromptChips(
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    val chips = listOf(
        Pair("💻 Coding", "Explain binary search in Kotlin and provide a clean implementation"),
        Pair("📐 Math", "Solve step-by-step: what is the derivative of x^3 * ln(x)?"),
        Pair("✍️ Writing", "Summarize the 4 core habits for peak productivity and focus"),
        Pair("📚 Study", "Explain how memory consolidation works during sleep"),
        Pair("⚡ Fitness", "What is an effective progressive overload strategy for bodyweight exercises?"),
        Pair("🥗 Nutrition", "What are optimal pre and post-workout nutrition guidelines?")
    )

    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "EXPLORE TOPICS",
            modifier = Modifier.padding(horizontal = 18.dp),
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { (label, prompt) ->
                PromptChip(
                    text = label,
                    enabled = enabled,
                    onClick = { onSelected(prompt) }
                )
            }
        }
    }
}

@Composable
private fun PromptChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = BrandNavy,
                fontSize = 11.sp,
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
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = BrandNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "AI",
                        color = BrandGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = if (message.isUser) {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
            },
            color = if (message.isUser) BrandGreen else SurfaceWhite,
            border = if (message.isUser) null else BorderStroke(1.dp, BorderSoft),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.isUser) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    FormattedAIMessage(content = message.text)
                }
            }
        }
    }
}

@Composable
private fun FormattedAIMessage(content: String) {
    val codeBlockRegex = Regex("```([a-zA-Z0-9]*)\\n?([\\s\\S]*?)```")
    val matches = codeBlockRegex.findAll(content).toList()

    if (matches.isEmpty()) {
        Text(
            text = formatBoldText(content),
            color = BrandText,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal
        )
    } else {
        var lastIdx = 0
        matches.forEach { match ->
            val textBefore = content.substring(lastIdx, match.range.first)
            if (textBefore.isNotBlank()) {
                Text(
                    text = formatBoldText(textBefore.trim()),
                    color = BrandText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            val lang = match.groupValues[1]
            val code = match.groupValues[2].trim()

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                color = Color(0xFF1E242B),
                border = BorderStroke(1.dp, Color(0xFF2E3842))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (lang.isNotBlank()) {
                        Text(
                            text = lang.uppercase(),
                            color = BrandGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = code,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            lastIdx = match.range.last + 1
        }

        if (lastIdx < content.length) {
            val remaining = content.substring(lastIdx).trim()
            if (remaining.isNotBlank()) {
                Text(
                    text = formatBoldText(remaining),
                    color = BrandText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private fun formatBoldText(text: String): androidx.compose.ui.text.AnnotatedString {
    val parts = text.split("**")
    return buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = BrandNavy)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

@Composable
private fun ThinkingBubble() {
    Row(verticalAlignment = Alignment.Bottom) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = BrandNavy
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "AI",
                    color = BrandGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderSoft),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thinking...",
                    color = BrandMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
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
        color = SurfaceWhite,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp, max = 130.dp),
                placeholder = {
                    Text(
                        text = "Ask anything (code, math, writing, fitness)...",
                        color = BrandMuted,
                        fontSize = 12.sp
                    )
                },
                maxLines = 5,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppBackground,
                    unfocusedContainerColor = AppBackground,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = BorderSoft,
                    focusedTextColor = BrandNavy,
                    unfocusedTextColor = BrandNavy,
                    cursorColor = BrandGreenDark
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSend,
                enabled = input.isNotBlank() && !isLoading,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = BrandGreen.copy(alpha = 0.35f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "SEND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
