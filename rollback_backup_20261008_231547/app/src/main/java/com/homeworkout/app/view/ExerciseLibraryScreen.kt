package com.homeworkout.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.homeworkout.app.R
import com.homeworkout.app.ui.theme.AppBackground
import com.homeworkout.app.ui.theme.BorderSoft
import com.homeworkout.app.ui.theme.BrandGreen
import com.homeworkout.app.ui.theme.BrandGreenDark
import com.homeworkout.app.ui.theme.BrandGreenSoft
import com.homeworkout.app.ui.theme.BrandMuted
import com.homeworkout.app.ui.theme.BrandNavy
import com.homeworkout.app.ui.theme.BrandText

private const val ARROW = "›"

data class ExerciseItem(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val durationSeconds: Int,
    val calories: Int,
    val imageRes: Int
)

@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    onExerciseClick: (ExerciseItem) -> Unit
) {
    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    val exercises = remember {
        listOf(
            ExerciseItem(
                "push_up",
                "Push Up",
                "Chest",
                "Beginner",
                30,
                8,
                R.drawable.push_up
            ),
            ExerciseItem(
                "squat",
                "Bodyweight Squat",
                "Legs",
                "Beginner",
                40,
                10,
                R.drawable.bodyweight_squat
            ),
            ExerciseItem(
                "plank",
                "Plank",
                "Core",
                "Beginner",
                30,
                6,
                R.drawable.plank
            ),
            ExerciseItem(
                "jumping_jack",
                "Jumping Jacks",
                "Cardio",
                "Beginner",
                30,
                12,
                R.drawable.jumping_jacks
            ),
            ExerciseItem(
                "lunges",
                "Forward Lunges",
                "Legs",
                "Intermediate",
                40,
                11,
                R.drawable.forward_lunges
            ),
            ExerciseItem(
                "mountain_climber",
                "Mountain Climbers",
                "Cardio",
                "Intermediate",
                30,
                14,
                R.drawable.mountain_climbers
            )
        )
    }

    val categories = listOf(
        "All",
        "Chest",
        "Legs",
        "Core",
        "Cardio"
    )

    val filtered = exercises.filter { exercise ->

        val categoryMatch =
            selectedCategory == "All" ||
                    exercise.category.equals(
                        selectedCategory,
                        ignoreCase = true
                    )

        val searchMatch =
            searchText.isBlank() ||
                    exercise.name.contains(
                        searchText,
                        ignoreCase = true
                    ) ||
                    exercise.category.contains(
                        searchText,
                        ignoreCase = true
                    )

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
            LibraryHeader(
                onBack = onBack
            )
        }

        item {
            SearchBox(
                value = searchText,
                onValueChange = {
                    searchText = it
                }
            )
        }

        item {
            CategoryFilters(
                categories = categories,
                selected = selectedCategory,
                onSelected = {
                    selectedCategory = it
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

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

        items(
            items = filtered,
            key = {
                it.id
            }
        ) { exercise ->

            LibraryExerciseCard(
                exercise = exercise,
                onClick = {
                    onExerciseClick(exercise)
                }
            )
        }

        if (filtered.isEmpty()) {
            item {
                EmptyLibrary(
                    onClear = {
                        searchText = ""
                        selectedCategory = "All"
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryHeader(
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
            border = BorderStroke(1.dp, BorderSoft),
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

        Column(
            modifier = Modifier.weight(1f)
        ) {

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

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "06",
                    color = BrandGreenDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
        leadingIcon = {
            Text(
                text = "⌕",
                color = BrandMuted,
                fontSize = 22.sp
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Text(
                    text = "×",
                    color = BrandMuted,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable {
                            onValueChange("")
                        }
                        .padding(8.dp)
                )
            }
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

@Composable
private fun CategoryFilters(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(categories) { category ->

            val active = category == selected

            Surface(
                modifier = Modifier.clickable {
                    onSelected(category)
                },
                shape = RoundedCornerShape(50.dp),
                color = if (active) {
                    BrandNavy
                } else {
                    Color.White
                },
                border = if (active) {
                    null
                } else {
                    BorderStroke(1.dp, BorderSoft)
                }
            ) {

                Text(
                    text = category,
                    modifier = Modifier.padding(12.dp),
                    color = if (active) {
                        Color.White
                    } else {
                        BrandMuted
                    },
                    fontSize = 10.sp,
                    fontWeight = if (active) {
                        FontWeight.ExtraBold
                    } else {
                        FontWeight.SemiBold
                    }
                )
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(125.dp)
                    .height(145.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                                    Color.Black.copy(alpha = 0.28f)
                                )
                            )
                        )
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
                        modifier = Modifier.padding(8.dp),
                        color = BrandNavy,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(13.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = BrandGreenSoft
                ) {

                    Text(
                        text = exercise.difficulty.uppercase(),
                        modifier = Modifier.padding(7.dp),
                        color = BrandGreenDark,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = exercise.name,
                    color = BrandText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 20.sp
                )

                Text(
                    text = "Focus on control, breathing and clean technique.",
                    color = BrandMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {

                    MetaChip(
                        text = "${exercise.durationSeconds}s"
                    )

                    MetaChip(
                        text = "${exercise.calories} kcal"
                    )
                }

                Text(
                    text = "VIEW DETAILS $ARROW",
                    color = BrandGreenDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )
        }
    }
}

@Composable
private fun MetaChip(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = AppBackground
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(7.dp),
            color = BrandMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyLibrary(
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, BorderSoft)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = BrandGreenSoft
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "?",
                        color = BrandGreenDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "No exercises found",
                color = BrandNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Try another search or category.",
                color = BrandMuted,
                fontSize = 11.sp
            )

            TextButton(
                onClick = onClear
            ) {

                Text(
                    text = "CLEAR FILTERS",
                    color = BrandGreenDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}
