package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
import com.homeworkout.app.service.AICoachResult
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

import com.homeworkout.app.model.AICoachRepository

@Composable
fun AICoachScreen(
    onBack: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val eventBus = remember { EventBus() }
    val controller = remember { AICoachController(eventBus) }
    val welcomeMessage = remember {
        AICoachMessage(
            id = "welcome",
            text = "Hello! I am your Personal AI Fitness Coach. Ask me about exercise form, workout routines, recovery, or fitness nutrition.",
            isUser = false
        )
    }

    val messages = remember {
        mutableStateListOf<AICoachMessage>().apply {
            add(welcomeMessage)
            addAll(AICoachRepository.getMessages())
        }
    }

    val repoRevision = AICoachRepository.revision
    LaunchedEffect(repoRevision) {
        val persisted = AICoachRepository.getMessages()
        val errorItems = messages.filter { it.isError }
        messages.clear()
        messages.add(welcomeMessage)
        messages.addAll(persisted)
        messages.addAll(errorItems)
    }

    LaunchedEffect(Unit) {
        val uid = com.homeworkout.app.data.supabase.SupabaseClient.currentUser?.id
        if (!uid.isNullOrBlank()) {
            AICoachRepository.syncWithCloud(uid)
        }
    }

    var input by rememberSaveable { mutableStateOf("") }
    var lastQuestion by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val onSendAction: (String) -> Unit = { questionText ->
        val question = questionText.trim()
        if (question.isNotEmpty() && !isLoading) {
            messages.removeAll { it.isError }
            val userMsg = controller.userMessage(question)
            messages.add(userMsg)
            AICoachRepository.saveMessage(userMsg)
            input = ""
            lastQuestion = question
            isLoading = true

            scope.launch {
                val history = WorkoutHistoryRepository.getAll()
                when (val result = controller.ask(message = question, history = history)) {
                    is AICoachResult.Success -> {
                        val coachMsg = controller.coachMessage(result.text)
                        messages.add(coachMsg)
                        AICoachRepository.saveMessage(coachMsg)
                    }
                    is AICoachResult.Error -> {
                        messages.add(
                            controller.coachErrorMessage(
                                text = result.message,
                                canRetry = result.canRetry
                            )
                        )
                    }
                }
                isLoading = false
            }
        }
    }

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
                .padding(bottom = bottomPadding)
                .imePadding()
        ) {
            CoachHeader(
                onBack = onBack,
                hasSavedHistory = messages.size > 1,
                onClearChat = {
                    AICoachRepository.clearHistory()
                    messages.clear()
                    messages.add(welcomeMessage)
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.size <= 1) {
                    item {
                        CoachIntro()
                    }

                    item {
                        GeneralPromptChips(
                            enabled = !isLoading,
                            onSelected = { prompt ->
                                input = prompt
                            }
                        )
                    }
                }

                items(items = messages, key = { it.id }) { message ->
                    CoachBubble(
                        message = message,
                        onRetry = {
                            if (lastQuestion.isNotBlank()) {
                                onSendAction(lastQuestion)
                            }
                        }
                    )
                }

                if (isLoading) {
                    item { ThinkingBubble() }
                }
            }

            @OptIn(ExperimentalLayoutApi::class)
            val isImeVisible = WindowInsets.isImeVisible

            Composer(
                modifier = if (!isImeVisible && bottomPadding == 0.dp) Modifier.navigationBarsPadding() else Modifier,
                input = input,
                isLoading = isLoading,
                onInputChanged = { input = it },
                onSend = {
                    onSendAction(input)
                }
            )
        }
    }
}

@Composable
private fun CoachHeader(
    onBack: () -> Unit,
    hasSavedHistory: Boolean = false,
    onClearChat: () -> Unit = {}
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
                text = "AI Fitness Coach",
                color = BrandNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Form & training guide",
                color = BrandMuted,
                fontSize = 12.sp
            )
        }

        if (hasSavedHistory) {
            Surface(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = onClearChat),
                shape = RoundedCornerShape(50.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSoft)
            ) {
                Text(
                    text = "Clear",
                    color = BrandMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
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
                        com.homeworkout.app.ui.SparkleIcon(
                            color = BrandGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "How can I help with your training today?",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Personalized coaching, movement technique, recovery, and workout advice.",
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
        Pair("Form Check", "How do I keep my lower back safe during push-ups and squats?"),
        Pair("Workout Plan", "What is an effective 4-day bodyweight progression routine?"),
        Pair("Recovery", "What are the best recovery habits between intense workout days?"),
        Pair("Nutrition Guide", "What are optimal pre and post-workout nutrition guidelines?"),
        Pair("Warm-Up", "What is a quick 5-minute dynamic warm-up before bodyweight training?"),
        Pair("Fat Loss", "How can I combine bodyweight circuits with intervals for fat loss?")
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
    message: AICoachMessage,
    onRetry: () -> Unit = {}
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
                color = if (message.isError) Color(0xFFC2410C) else BrandNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (message.isError) "!" else "AI",
                        color = if (message.isError) Color.White else BrandGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (message.isError) {
            Surface(
                modifier = Modifier.widthIn(max = 310.dp),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFDBA74)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEA580C).copy(alpha = 0.15f),
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "!",
                                    color = Color(0xFFEA580C),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = "STATUS NOTICE",
                            color = Color(0xFFEA580C),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.text,
                        color = Color(0xFF7C2D12),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (message.canRetry) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEA580C),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Retry Request",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 110.dp),
                placeholder = {
                    Text(
                        text = "Ask about exercises, form, nutrition, or recovery...",
                        color = BrandMuted,
                        fontSize = 12.sp
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = BrandGreen.copy(alpha = 0.35f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "Send",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
