package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText

data class ExerciseItem(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val durationSeconds: Int,
    val calories: Int,
    val imageRes: Int,
    val description: String = "",
    val instructions: List<String> = emptyList()
)

@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    onExerciseClick: (ExerciseItem) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val exercises = remember {
        ExerciseRepository.exercises.map { exercise ->
            ExerciseItem(
                id = exercise.id,
                name = exercise.name,
                category = exercise.category,
                difficulty = exercise.difficulty,
                durationSeconds = exercise.durationSeconds,
                calories = exercise.calories,
                imageRes = exercise.imageRes,
                description = exercise.description,
                instructions = exercise.instructions
            )
        }
    }

    val categories = remember { ExerciseRepository.categories() }

    val filtered = exercises.filter { exercise ->
        val categoryMatch =
            selectedCategory == "All" ||
                exercise.category.equals(selectedCategory, ignoreCase = true)
        val searchMatch =
            searchText.isBlank() ||
                exercise.name.contains(searchText, ignoreCase = true) ||
                exercise.category.contains(searchText, ignoreCase = true)
        categoryMatch && searchMatch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HwBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXERCISE LIBRARY",
                        color = BrandGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        text = "Master every movement",
                        color = BrandNavy,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = BrandGreenSoft
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = exercises.size.toString().padStart(2, '0'),
                            color = BrandGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text(
                        text = "Search exercises...",
                        color = BrandMuted,
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = BorderSoft,
                    focusedTextColor = BrandText,
                    unfocusedTextColor = BrandText,
                    cursorColor = BrandGreenDark
                )
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val active = category == selectedCategory
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = category },
                        shape = RoundedCornerShape(50.dp),
                        color = if (active) BrandNavy else Color.White,
                        border = if (active) null else BorderStroke(1.dp, BorderSoft)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (active) Color.White else BrandMuted,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MOVES FOR YOU",
                        color = BrandNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Tap any exercise to explore",
                        color = BrandMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "${filtered.size} MOVES",
                    color = BrandGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        items(items = filtered, key = { it.id }) { exercise ->
            LibraryExerciseCard(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) }
            )
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No exercises found",
                            color = BrandNavy,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try another search or category.",
                            color = BrandMuted,
                            fontSize = 11.sp
                        )
                        TextButton(
                            onClick = {
                                searchText = ""
                                selectedCategory = "All"
                            }
                        ) {
                            Text(
                                text = "CLEAR FILTERS",
                                color = BrandGreenDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryExerciseCard(
    exercise: ExerciseItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(118.dp)
                    .height(136.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandGreenSoft)
            ) {
                Image(
                    painter = painterResource(exercise.imageRes),
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = exercise.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        color = BrandNavy,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Surface(shape = RoundedCornerShape(50.dp), color = BrandGreenSoft) {
                    Text(
                        text = exercise.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        color = BrandGreenDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = exercise.name,
                    color = BrandText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.description.ifBlank {
                        "Focus on control, breathing and clean technique."
                    },
                    color = BrandMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Surface(shape = RoundedCornerShape(50.dp), color = AppBackground) {
                        Text(
                            text = "${exercise.durationSeconds}s",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            color = BrandMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(shape = RoundedCornerShape(50.dp), color = AppBackground) {
                        Text(
                            text = "${exercise.calories} kcal",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            color = BrandMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
