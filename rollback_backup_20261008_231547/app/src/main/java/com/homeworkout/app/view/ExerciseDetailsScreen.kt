package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private const val ARROW = "›"
private const val DOT = "•"

@Composable
fun ExerciseDetailsScreen(
    exercise: ExerciseItem,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
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
            ExerciseTitle(
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
                exercise = exercise
            )
        }

        item {
            InstructionsSection(
                exercise = exercise
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
                text = exercise.category.uppercase(),
                color = BrandGreenDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Exercise details",
                color = BrandNavy,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = BrandGreenSoft
        ) {

            Text(
                text = exercise.difficulty.uppercase(),
                modifier = Modifier.padding(9.dp),
                color = BrandGreenDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
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
            .height(290.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {

        Image(
            painter = painterResource(
                exercise.imageRes
            ),
            contentDescription = exercise.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            shape = RoundedCornerShape(50.dp),
            color = Color.White.copy(alpha = 0.94f)
        ) {

            Text(
                text = "TECHNIQUE",
                modifier = Modifier.padding(9.dp),
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
                color = BrandGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = exercise.name,
                color = Color.White,
                fontSize = 26.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Build control. Move with purpose.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ExerciseTitle(
    exercise: ExerciseItem
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text(
            text = "MASTER THE MOVEMENT",
            color = BrandGreenDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp
        )

        Text(
            text = when (exercise.id) {
                "push_up" ->
                    "A controlled upper-body push that targets your chest, shoulders and triceps."

                "squat" ->
                    "A foundational lower-body movement for strong and controlled legs."

                "plank" ->
                    "A core stability movement focused on keeping your body strong and aligned."

                "jumping_jack" ->
                    "A simple cardio movement that quickly raises your heart rate."

                "lunges" ->
                    "A unilateral leg movement that trains balance, strength and control."

                "mountain_climber" ->
                    "A dynamic full-body cardio movement combining core stability and speed."

                else ->
                    "Focus on controlled movement and consistent technique."
            },
            color = BrandMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsCard(
    exercise: ExerciseItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
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

            VerticalDivider()

            DetailStat(
                value = "${exercise.calories}",
                label = "KCAL",
                modifier = Modifier.weight(1f)
            )

            VerticalDivider()

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
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text(
            text = value,
            color = BrandNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(BorderSoft)
    )
}

@Composable
private fun DescriptionCard(
    exercise: ExerciseItem
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        SectionHeading(
            title = "WHY IT MATTERS"
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(21.dp),
            color = Color.White,
            border = BorderStroke(
                1.dp,
                BorderSoft
            )
        ) {

            Text(
                text = when (exercise.id) {
                    "push_up" ->
                        "Keep your body aligned as one unit while pressing through your hands. Quality reps matter more than speed."

                    "squat" ->
                        "Use your hips and legs together. Keep your chest controlled and maintain a stable foot position."

                    "plank" ->
                        "Brace your core and maintain one strong line from shoulders to heels. Avoid letting your hips collapse."

                    "jumping_jack" ->
                        "Stay light on your feet and keep a steady rhythm. Land softly and control every repetition."

                    "lunges" ->
                        "Take a stable step and lower under control. Focus on balance before increasing your pace."

                    "mountain_climber" ->
                        "Keep your shoulders stable while driving your knees forward. Maintain a strong core throughout."

                    else ->
                        "Use controlled movement and focus on proper technique."
                },
                modifier = Modifier.padding(16.dp),
                color = BrandText,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InstructionsSection(
    exercise: ExerciseItem
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {

        SectionHeading(
            title = "HOW TO DO IT"
        )

        val steps = when (exercise.id) {

            "push_up" -> listOf(
                "Start in a strong high-plank position.",
                "Lower your body with control.",
                "Press through your hands to return to the start."
            )

            "squat" -> listOf(
                "Stand with your feet about shoulder-width apart.",
                "Push your hips back and lower under control.",
                "Drive through your feet to stand tall."
            )

            "plank" -> listOf(
                "Place your forearms firmly on the floor.",
                "Keep your core tight and body aligned.",
                "Hold the position while breathing steadily."
            )

            "jumping_jack" -> listOf(
                "Stand tall with your arms by your sides.",
                "Jump while opening your legs and raising your arms.",
                "Return to the starting position softly."
            )

            "lunges" -> listOf(
                "Stand tall with your feet stable.",
                "Step forward and lower your body under control.",
                "Push through the front foot and return."
            )

            "mountain_climber" -> listOf(
                "Start in a strong high-plank position.",
                "Drive one knee toward your chest.",
                "Alternate legs while keeping your core stable."
            )

            else -> listOf(
                "Set up in the correct starting position.",
                "Perform the movement slowly and with control.",
                "Return to the starting position."
            )
        }

        steps.forEachIndexed { index, step ->

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
        fontSize = 13.sp,
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
        shape = RoundedCornerShape(19.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            BorderSoft
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = BrandGreenSoft
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = number.toString(),
                        color = BrandGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = BrandText,
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
        shape = RoundedCornerShape(22.dp),
        color = BrandGreenSoft
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        color = BrandGreenDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(11.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "FORM FIRST",
                    color = BrandGreenDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Slow, controlled repetitions are better than rushing through the movement.",
                    color = BrandNavy,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
