package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.model.ExerciseRepository
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
fun ExerciseDetailsScreen(
    exercise: ExerciseItem,
    onBack: () -> Unit
) {
    val fullExercise = remember(exercise.id) {
        ExerciseRepository.find(exercise.id)
    }

    val instructions = remember(exercise.id) {
        val repoInstructions = fullExercise?.instructions ?: emptyList()
        if (repoInstructions.isNotEmpty()) {
            repoInstructions
        } else if (exercise.instructions.isNotEmpty()) {
            exercise.instructions
        } else {
            listOf(
                "Set up with proper posture and stable stance.",
                "Perform the movement slowly through the full range of motion.",
                "Breathe out on effort and maintain strong core engagement.",
                "Return to starting position with full control."
            )
        }
    }

    val description = remember(exercise.id) {
        fullExercise?.description
            ?: exercise.description.ifBlank {
                "A foundational bodyweight movement designed to build strength, mobility, and stability."
            }
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
            TopHeader(
                exercise = exercise,
                onBack = onBack
            )
        }

        item {
            HeroImage(
                exercise = exercise
            )
        }

        item {
            StatsCard(
                exercise = exercise
            )
        }

        item {
            DescriptionCard(
                description = description
            )
        }

        item {
            InstructionsSection(
                instructions = instructions
            )
        }

        item {
            FormTipCard()
        }
    }
}

@Composable
private fun TopHeader(
    exercise: ExerciseItem,
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
                text = exercise.category.uppercase(),
                color = BrandGreenDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Movement Guide",
                color = BrandNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {
            Text(
                text = exercise.difficulty.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun HeroImage(
    exercise: ExerciseItem
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(26.dp))
    ) {
        Image(
            painter = painterResource(exercise.imageRes),
            contentDescription = exercise.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                    )
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            shape = RoundedCornerShape(50.dp),
            color = Color.White.copy(alpha = 0.95f)
        ) {
            Text(
                text = "TECHNIQUE",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = BrandNavy,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = exercise.category.uppercase(),
                color = BrandGreenSoft,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = exercise.name,
                color = Color.White,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Controlled execution • Optimal muscle recruitment",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatsCard(
    exercise: ExerciseItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DetailStat(
                value = "${exercise.durationSeconds}s",
                label = "DURATION",
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(BorderSoft)
            )

            DetailStat(
                value = "${exercise.calories}",
                label = "EST. KCAL",
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(BorderSoft)
            )

            DetailStat(
                value = exercise.difficulty,
                label = "LEVEL",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailStat(
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
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun DescriptionCard(
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeading(title = "ABOUT THIS EXERCISE")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderSoft)
        ) {
            Text(
                text = description,
                modifier = Modifier.padding(16.dp),
                color = BrandText,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun InstructionsSection(
    instructions: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(title = "STEP-BY-STEP TECHNIQUE")

        instructions.forEachIndexed { index, step ->
            InstructionCard(
                number = index + 1,
                text = step
            )
        }
    }
}

@Composable
private fun SectionHeading(
    title: String
) {
    Text(
        text = title,
        color = BrandNavy,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.9.sp
    )
}

@Composable
private fun InstructionCard(
    number: Int,
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = BrandGreenSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        color = BrandGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = BrandNavy,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FormTipCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = BrandMint,
        border = BorderStroke(1.dp, Color(0xFFC7EBD9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = SurfaceWhite
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        // Clean star / spark icon
                        val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round)
                        drawLine(
                            color = BrandGreen,
                            start = Offset(size.width * 0.5f, 0f),
                            end = Offset(size.width * 0.5f, size.height),
                            strokeWidth = stroke.width,
                            cap = stroke.cap
                        )
                        drawLine(
                            color = BrandGreen,
                            start = Offset(0f, size.height * 0.5f),
                            end = Offset(size.width, size.height * 0.5f),
                            strokeWidth = stroke.width,
                            cap = stroke.cap
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "COACH TIP",
                    color = BrandGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Controlled tempo and steady breathing provide significantly better muscle activation than rushing through repetitions.",
                    color = BrandNavy,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
