package com.homeworkout.app.model

data class AIWorkoutDay(
    val dayNumber: Int,
    val focus: String,
    val exerciseIds: List<String>,
    val isRestDay: Boolean = exerciseIds.isEmpty(),
    val warmup: String = if (exerciseIds.isEmpty()) {
        "5 min gentle full-body mobility & deep diaphragmatic breathing"
    } else {
        "3 min dynamic arm circles, hip openers & light marching in place"
    },
    val cooldown: String = if (exerciseIds.isEmpty()) {
        "Foam rolling or 5 min relaxed stretching for tight muscle groups"
    } else {
        "2 min chest opener, hamstring stretch & slow nasal recovery breaths"
    },
    val durationMinutes: Int = if (exerciseIds.isEmpty()) 10 else 20,
    val progressionNote: String = ""
) {
    val weekNumber: Int
        get() = ((dayNumber - 1) / 7) + 1
}

data class AIWorkoutPlan(
    val title: String,
    val description: String,
    val goal: String,
    val difficulty: String,
    val daysPerWeek: Int,
    val minutesPerWorkout: Int,
    val days: List<AIWorkoutDay>,
    val generatedByAI: Boolean
)

object ThirtyDayProgramGenerator {

    private val upperPushAndArms = listOf(
        "incline_push_up", "push_up", "shoulder_press", "biceps_curl", "triceps_dip", "lateral_raise", "diamond_push_up"
    )
    private val lowerBodyAndGlutes = listOf(
        "squat", "glute_bridge", "lunges", "reverse_lunge", "calf_raise", "step_up", "donkey_kick", "fire_hydrant", "wall_sit"
    )
    private val coreAndPosterior = listOf(
        "plank", "dead_bug", "bird_dog", "bicycle_crunch", "russian_twist", "side_plank", "superman", "hip_hinge", "flutter_kicks"
    )
    private val cardioAndConditioning = listOf(
        "jumping_jack", "standing_knee_raise", "mountain_climber", "high_knees", "burpee"
    )

    data class SplitTemplate(
        val focusName: String,
        val primaryPool: List<String>,
        val secondaryPool: List<String>,
        val warmup: String,
        val cooldown: String
    )

    private fun splitsForGoal(goal: String): List<SplitTemplate> {
        val normalized = goal.trim().lowercase()
        return when {
            normalized.contains("strength") -> listOf(
                SplitTemplate(
                    focusName = "Upper Body Push & Arms",
                    primaryPool = upperPushAndArms,
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min shoulder rolls, scapular push-ups & wrist mobility",
                    cooldown = "2 min doorway chest stretch, overhead triceps & shoulder stretch"
                ),
                SplitTemplate(
                    focusName = "Lower Body Strength & Glutes",
                    primaryPool = lowerBodyAndGlutes,
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min bodyweight good mornings, hip circles & ankle rocks",
                    cooldown = "2 min standing quad stretch, figure-4 glute & calf stretch"
                ),
                SplitTemplate(
                    focusName = "Core Pillar & Posterior Chain",
                    primaryPool = coreAndPosterior,
                    secondaryPool = cardioAndConditioning,
                    warmup = "3 min cat-cow spinal waves, thoracic rotations & glute bridges",
                    cooldown = "2 min child's pose, cobra abdominal stretch & slow breathing"
                ),
                SplitTemplate(
                    focusName = "Total Body Compound Strength",
                    primaryPool = listOf("squat", "push_up", "hip_hinge", "shoulder_press", "step_up", "plank"),
                    secondaryPool = upperPushAndArms,
                    warmup = "3 min inchworms, world's greatest stretch & light squats",
                    cooldown = "3 min full-body static stretching & deep recovery breaths"
                ),
                SplitTemplate(
                    focusName = "Sculpted Shoulders, Glutes & Core",
                    primaryPool = listOf("lateral_raise", "triceps_dip", "glute_bridge", "donkey_kick", "fire_hydrant", "bird_dog"),
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min band/arm pull-aparts, hip abductions & torso twists",
                    cooldown = "2 min cross-body shoulder stretch & seated spinal twist"
                )
            )
            normalized.contains("cardio") -> listOf(
                SplitTemplate(
                    focusName = "Metabolic Cardio Ignite",
                    primaryPool = cardioAndConditioning,
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min light marching, ankle bounces & arm swings",
                    cooldown = "3 min slow walk-in-place heart-rate drop & calf stretch"
                ),
                SplitTemplate(
                    focusName = "Lower Body Athletic Stamina",
                    primaryPool = lowerBodyAndGlutes,
                    secondaryPool = cardioAndConditioning,
                    warmup = "3 min leg swings, hip openers & slow bodyweight squats",
                    cooldown = "2 min hamstring, quad & hip-flexor recovery stretch"
                ),
                SplitTemplate(
                    focusName = "Core & Agile Conditioning",
                    primaryPool = coreAndPosterior,
                    secondaryPool = cardioAndConditioning,
                    warmup = "3 min torso rotations, knee hugs & plank walkouts",
                    cooldown = "2 min supine spinal twist & deep belly breathing"
                ),
                SplitTemplate(
                    focusName = "Upper Body & Cardio Circuit",
                    primaryPool = upperPushAndArms,
                    secondaryPool = cardioAndConditioning,
                    warmup = "3 min arm circles, jumping jacks & shoulder taps",
                    cooldown = "2 min chest opener, lat stretch & slow nasal breathing"
                )
            )
            normalized.contains("core") -> listOf(
                SplitTemplate(
                    focusName = "Anterior Core & Stability",
                    primaryPool = listOf("plank", "dead_bug", "bicycle_crunch", "flutter_kicks", "mountain_climber"),
                    secondaryPool = lowerBodyAndGlutes,
                    warmup = "3 min cat-cow, pelvic tilts & ribcage breathing",
                    cooldown = "2 min gentle cobra pose & child's pose relaxation"
                ),
                SplitTemplate(
                    focusName = "Lower Body & Glute Core Link",
                    primaryPool = listOf("glute_bridge", "squat", "bird_dog", "donkey_kick", "fire_hydrant", "hip_hinge"),
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min hip hinges, glute activation & knee lifts",
                    cooldown = "2 min figure-4 stretch & hamstring lengthener"
                ),
                SplitTemplate(
                    focusName = "Obliques, Anti-Rotation & Balance",
                    primaryPool = listOf("side_plank", "russian_twist", "standing_knee_raise", "reverse_lunge", "superman"),
                    secondaryPool = upperPushAndArms,
                    warmup = "3 min standing windmills, side bends & arm swings",
                    cooldown = "2 min seated side bend & supine knee-to-chest stretch"
                ),
                SplitTemplate(
                    focusName = "Total Body Core Conditioning",
                    primaryPool = listOf("mountain_climber", "push_up", "plank", "jumping_jack", "squat", "bicycle_crunch"),
                    secondaryPool = cardioAndConditioning,
                    warmup = "3 min dynamic marching, arm circles & torso twists",
                    cooldown = "3 min full-body floor stretch & box breathing"
                )
            )
            else -> listOf(
                SplitTemplate(
                    focusName = "Full Body Foundation",
                    primaryPool = listOf("squat", "incline_push_up", "push_up", "plank", "glute_bridge", "jumping_jack"),
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min arm circles, hip openers & light marching in place",
                    cooldown = "2 min chest stretch, quad stretch & slow deep breaths"
                ),
                SplitTemplate(
                    focusName = "Cardio & Core Conditioning",
                    primaryPool = cardioAndConditioning + listOf("bicycle_crunch", "dead_bug", "russian_twist"),
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min light high knees, torso twists & ankle rolls",
                    cooldown = "2 min walking heart-rate recovery & child's pose"
                ),
                SplitTemplate(
                    focusName = "Lower Body & Glute Sculpt",
                    primaryPool = lowerBodyAndGlutes,
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min leg swings, bodyweight squats & hip bridges",
                    cooldown = "2 min hamstring, glute & calf mobility stretch"
                ),
                SplitTemplate(
                    focusName = "Upper Body & Posture Strength",
                    primaryPool = upperPushAndArms + listOf("superman", "bird_dog"),
                    secondaryPool = coreAndPosterior,
                    warmup = "3 min shoulder rolls, wall slides & wrist warm-up",
                    cooldown = "2 min overhead triceps, chest & upper-back stretch"
                ),
                SplitTemplate(
                    focusName = "Total Body Athletic Flow",
                    primaryPool = listOf("step_up", "shoulder_press", "reverse_lunge", "mountain_climber", "side_plank", "biceps_curl"),
                    secondaryPool = lowerBodyAndGlutes,
                    warmup = "3 min dynamic lunges, arm swings & core activation",
                    cooldown = "3 min full-body stretch & parasympathetic breathing"
                )
            )
        }
    }

    /**
     * Returns which days of a 7-day week (1..7) are active workout days based on daysPerWeek (2..6).
     */
    fun activeDaysInWeekPattern(daysPerWeek: Int): Set<Int> = when (daysPerWeek.coerceIn(1, 7)) {
        1 -> setOf(1)
        2 -> setOf(1, 4)
        3 -> setOf(1, 3, 5)
        4 -> setOf(1, 2, 4, 6)
        5 -> setOf(1, 2, 3, 5, 6)
        6 -> setOf(1, 2, 3, 4, 5, 6)
        else -> setOf(1, 2, 3, 4, 5, 6, 7)
    }

    private fun filterByDifficulty(ids: List<String>, difficulty: String, week: Int): List<String> {
        val catalogMap = ExerciseRepository.exercises.associateBy { it.id }
        val normDiff = difficulty.trim().lowercase()
        val filtered = ids.filter { id ->
            val ex = catalogMap[id] ?: return@filter false
            when {
                normDiff == "beginner" && week <= 2 -> ex.difficulty != "Advanced"
                normDiff == "beginner" && week >= 3 -> true // Gradual progression in Weeks 3-4
                else -> true
            }
        }
        return filtered.ifEmpty { ids.filter { catalogMap.containsKey(it) } }
    }

    fun generateThirtyDaySchedule(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        seedDays: List<AIWorkoutDay> = emptyList()
    ): List<AIWorkoutDay> {
        val validFreq = daysPerWeek.coerceIn(2, 6)
        val validMinutes = minutesPerWorkout.coerceIn(10, 45)
        val activeWeekDays = activeDaysInWeekPattern(validFreq)
        val splits = splitsForGoal(goal)
        val validSeedDays = seedDays.filter { it.exerciseIds.isNotEmpty() }

        val baseExerciseCount = when {
            validMinutes <= 12 -> 3
            validMinutes <= 18 -> 4
            validMinutes <= 25 -> 5
            else -> 6
        }

        val result = mutableListOf<AIWorkoutDay>()
        var activeSessionCounter = 0

        for (dayNum in 1..30) {
            val weekNum = ((dayNum - 1) / 7) + 1
            val dayOfWeek = ((dayNum - 1) % 7) + 1
            val isActive = activeWeekDays.contains(dayOfWeek)

            val weekTheme = when (weekNum) {
                1 -> "Week 1 • Foundation & Form"
                2 -> "Week 2 • Progressive Volume"
                3 -> "Week 3 • Stamina & Density"
                else -> "Week 4 • Peak Mastery"
            }

            if (!isActive) {
                val restTitle = if (dayOfWeek == 7) {
                    "Rest & Full Recovery"
                } else {
                    "Active Recovery & Mobility"
                }
                result.add(
                    AIWorkoutDay(
                        dayNumber = dayNum,
                        focus = restTitle,
                        exerciseIds = emptyList(),
                        isRestDay = true,
                        warmup = "5 min gentle joint mobility, cat-cow spinal flow & deep nasal breathing",
                        cooldown = "10 min light walk or full-body foam rolling & hydration focus",
                        durationMinutes = 10,
                        progressionNote = "$weekTheme • Scheduled recovery allows muscle fibers and nervous system to rebuild stronger."
                    )
                )
            } else {
                val split = splits[activeSessionCounter % splits.size]
                val targetCount = (baseExerciseCount + (weekNum - 1) / 2).coerceIn(3, 7)

                val candidateIds = mutableListOf<String>()
                // Incorporate AI seed day exercises if available for this slot
                if (validSeedDays.isNotEmpty()) {
                    val seed = validSeedDays[activeSessionCounter % validSeedDays.size]
                    seed.exerciseIds.forEach { id ->
                        if (ExerciseRepository.find(id) != null && !candidateIds.contains(id)) {
                            candidateIds.add(id)
                        }
                    }
                }

                val primaryFiltered = filterByDifficulty(split.primaryPool, difficulty, weekNum)
                val secondaryFiltered = filterByDifficulty(split.secondaryPool, difficulty, weekNum)

                // Rotate selection offset across weeks so Weeks 1..4 evolve naturally
                val offset = (weekNum - 1) + (activeSessionCounter / splits.size)
                for (i in primaryFiltered.indices) {
                    val pick = primaryFiltered[(i + offset) % primaryFiltered.size]
                    if (!candidateIds.contains(pick)) {
                        candidateIds.add(pick)
                    }
                    if (candidateIds.size >= targetCount) break
                }

                for (i in secondaryFiltered.indices) {
                    if (candidateIds.size >= targetCount) break
                    val pick = secondaryFiltered[(i + offset) % secondaryFiltered.size]
                    if (!candidateIds.contains(pick)) {
                        candidateIds.add(pick)
                    }
                }

                val sessionDuration = (validMinutes + (weekNum - 1) * 2).coerceIn(10, 45)
                val focusLabel = if (validSeedDays.isNotEmpty() && activeSessionCounter < validSeedDays.size) {
                    validSeedDays[activeSessionCounter].focus.ifBlank { split.focusName }
                } else {
                    split.focusName
                }

                result.add(
                    AIWorkoutDay(
                        dayNumber = dayNum,
                        focus = focusLabel,
                        exerciseIds = candidateIds.take(targetCount),
                        isRestDay = false,
                        warmup = split.warmup,
                        cooldown = split.cooldown,
                        durationMinutes = sessionDuration,
                        progressionNote = "$weekTheme • ${candidateIds.take(targetCount).size} exercises (~$sessionDuration min)"
                    )
                )
                activeSessionCounter++
            }
        }

        return result
    }

    fun ensureThirtyDayPlan(plan: AIWorkoutPlan): AIWorkoutPlan {
        if (plan.days.size == 30) return plan
        val expandedDays = generateThirtyDaySchedule(
            goal = plan.goal,
            difficulty = plan.difficulty,
            daysPerWeek = plan.daysPerWeek,
            minutesPerWorkout = plan.minutesPerWorkout,
            seedDays = plan.days
        )
        return plan.copy(days = expandedDays)
    }
}
