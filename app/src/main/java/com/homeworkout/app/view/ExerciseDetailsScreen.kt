package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.ui.HwBackButton
import com.homeworkout.app.ui.HwPrimaryButton
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary

@Composable
fun ExerciseDetailsScreen(
    exercise: ExerciseItem,
    onBack: () -> Unit,
    onStart: () -> Unit = {}
) {
    val fullExercise = remember(exercise.id) {
        ExerciseRepository.find(exercise.id)
    }

    val instruction = remember(exercise.id) {
        fullExercise?.instructions?.firstOrNull()
            ?: fullExercise?.description?.takeIf { it.isNotBlank() }
            ?: "Maintain smooth controlled form throughout the movement. Breathe steadily."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HwBackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Exercise Details",
                color = HwTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Continuous Human Demonstration Area
        AnimatedExerciseCharacter(
            exerciseId = exercise.id,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise Name & Short Instruction
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = HwAccentSoft,
                    border = BorderStroke(1.dp, HwAccent.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = exercise.category,
                        color = HwAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = HwCard,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Text(
                        text = "${exercise.durationSeconds}s",
                        color = HwTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exercise.name,
                color = HwTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = HwCard,
                border = BorderStroke(1.dp, HwBorder)
            ) {
                Text(
                    text = instruction,
                    color = HwTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent Single Action Button
        HwPrimaryButton(
            text = "Start Exercise",
            onClick = onStart,
            icon = Icons.Outlined.PlayArrow,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
