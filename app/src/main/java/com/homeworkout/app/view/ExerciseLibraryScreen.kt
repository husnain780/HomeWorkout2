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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.homeworkout.app.ui.theme.HwAccent
import com.homeworkout.app.ui.theme.HwAccentSoft
import com.homeworkout.app.ui.theme.HwBackground
import com.homeworkout.app.ui.theme.HwBorder
import com.homeworkout.app.ui.theme.HwCard
import com.homeworkout.app.ui.theme.HwCardElevated
import com.homeworkout.app.ui.theme.HwTextPrimary
import com.homeworkout.app.ui.theme.HwTextSecondary

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
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

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
        val categoryMatch = selectedCategory == "All" || exercise.category.equals(selectedCategory, ignoreCase = true)
        val searchMatch = searchText.isBlank() ||
            exercise.name.contains(searchText, ignoreCase = true) ||
            exercise.category.contains(searchText, ignoreCase = true)
        categoryMatch && searchMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HwBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HwBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exercise Library",
                        color = HwTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = HwCardElevated,
                    border = BorderStroke(1.dp, HwBorder)
                ) {
                    Text(
                        text = "${filtered.size}",
                        color = HwAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search exercises...",
                        color = HwTextSecondary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = HwTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HwAccent,
                    unfocusedBorderColor = HwBorder,
                    focusedContainerColor = HwCard,
                    unfocusedContainerColor = HwCard
                ),
                singleLine = true
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val active = category == selectedCategory
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { selectedCategory = category },
                        shape = RoundedCornerShape(50.dp),
                        color = if (active) HwAccent else HwCard,
                        border = BorderStroke(1.dp, if (active) HwAccent else HwBorder)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = if (active) Color.White else HwTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No exercises found",
                        color = HwTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { searchText = ""; selectedCategory = "All" }) {
                        Text(text = "Reset Filter", color = HwAccent)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = filtered, key = { it.id }) { exercise ->
                    LibraryGridCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryGridCard(
    exercise: ExerciseItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HwCard),
        border = BorderStroke(1.dp, HwBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(Color(0xFFF4F7F9)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.name,
                    color = HwTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = HwAccentSoft
                    ) {
                        Text(
                            text = exercise.category,
                            color = HwAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${exercise.durationSeconds}s",
                        color = HwTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
