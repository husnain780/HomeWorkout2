package com.homeworkout.app.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.homeworkout.app.data.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate

data class SavedWorkoutPlan(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val goal: String,
    val difficulty: String,
    val daysPerWeek: Int,
    val minutesPerWorkout: Int,
    val exerciseIds: List<String>,
    val days: List<AIWorkoutDay>,
    val generatedByAI: Boolean,
    val createdAt: Long,
    val startDate: String = LocalDate.now().toString(),
    val currentDay: Int = 1,
    val completedDays: List<Int> = emptyList()
) {
    fun toWorkoutPlan(): WorkoutPlan {
        val activeDay = days.firstOrNull { it.dayNumber == currentDay && !it.isRestDay }
            ?: days.firstOrNull { it.dayNumber >= currentDay && !it.isRestDay }
            ?: days.firstOrNull { !it.isRestDay }

        val targetDayNum = activeDay?.dayNumber ?: currentDay.coerceIn(1, 30)
        val dayExercises = activeDay?.exerciseIds?.takeIf { it.isNotEmpty() } ?: exerciseIds

        return WorkoutPlan(
            id = "${id}__day_${targetDayNum}",
            title = "$title • Day $targetDayNum",
            description = activeDay?.focus ?: description.ifBlank { "$daysPerWeek days/wk • $goal" },
            durationMinutes = activeDay?.durationMinutes ?: minutesPerWorkout,
            exerciseCount = dayExercises.size,
            difficulty = difficulty,
            exerciseIds = dayExercises,
            category = goal,
            isAiGenerated = true
        )
    }

    fun toDayWorkoutPlan(dayNumber: Int): WorkoutPlan {
        val day = days.firstOrNull { it.dayNumber == dayNumber }
        val dayExercises = day?.exerciseIds?.takeIf { it.isNotEmpty() }
            ?: listOf("bird_dog", "glute_bridge", "hip_hinge")
        val focus = day?.focus ?: goal
        return WorkoutPlan(
            id = "${id}__day_${dayNumber.coerceIn(1, 30)}",
            title = "$title • Day $dayNumber ($focus)",
            description = day?.warmup ?: description,
            durationMinutes = day?.durationMinutes ?: minutesPerWorkout,
            exerciseCount = dayExercises.size,
            difficulty = difficulty,
            exerciseIds = dayExercises,
            category = focus,
            isAiGenerated = true
        )
    }

    fun toAIWorkoutPlan(): AIWorkoutPlan = AIWorkoutPlan(
        title = title,
        description = description,
        goal = goal,
        difficulty = difficulty,
        daysPerWeek = daysPerWeek,
        minutesPerWorkout = minutesPerWorkout,
        days = days,
        generatedByAI = generatedByAI
    )
}

object WorkoutPlanRepository {

    private var dbHelper: WorkoutDatabaseHelper? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var revision by mutableIntStateOf(0)
        private set

    fun initialize(context: Context) {
        if (dbHelper == null) {
            dbHelper = WorkoutDatabaseHelper.getInstance(context.applicationContext)
        }
    }

    fun notifyChanged() {
        revision++
    }

    private fun activeUserId(): String {
        return SupabaseClient.currentUser?.id ?: "local_anonymous_user"
    }

    val catalogPlans: List<WorkoutPlan> = listOf(
        WorkoutPlan(
            id = "full_body_blast",
            title = "Full Body Blast",
            description = "Balanced total-body conditioning & strength",
            durationMinutes = 18,
            exerciseCount = 6,
            difficulty = "Beginner",
            category = "Full Body",
            exerciseIds = listOf(
                "jumping_jack",
                "push_up",
                "squat",
                "plank",
                "mountain_climber",
                "lunges"
            )
        ),
        WorkoutPlan(
            id = "core_crusher",
            title = "Core Crusher",
            description = "Deep abdominal stability & oblique sculpting",
            durationMinutes = 14,
            exerciseCount = 6,
            difficulty = "Intermediate",
            category = "Core",
            exerciseIds = listOf(
                "plank",
                "bicycle_crunch",
                "dead_bug",
                "russian_twist",
                "side_plank",
                "flutter_kicks"
            )
        ),
        WorkoutPlan(
            id = "strength_builder",
            title = "Upper & Lower Strength",
            description = "Progressive bodyweight hypertrophy & control",
            durationMinutes = 24,
            exerciseCount = 7,
            difficulty = "Intermediate",
            category = "Strength",
            exerciseIds = listOf(
                "push_up",
                "diamond_push_up",
                "squat",
                "lunges",
                "shoulder_press",
                "triceps_dip",
                "glute_bridge"
            )
        ),
        WorkoutPlan(
            id = "hiit_cardio_burn",
            title = "HIIT Cardio Ignite",
            description = "High-tempo intervals for maximum calorie burn",
            durationMinutes = 16,
            exerciseCount = 6,
            difficulty = "Intermediate",
            category = "Cardio",
            exerciseIds = listOf(
                "jumping_jack",
                "high_knees",
                "mountain_climber",
                "burpee",
                "standing_knee_raise",
                "squat"
            )
        ),
        WorkoutPlan(
            id = "morning_mobility_flow",
            title = "Morning Mobility & Core",
            description = "Low-impact joint activation, glutes & posture",
            durationMinutes = 12,
            exerciseCount = 5,
            difficulty = "Beginner",
            category = "Mobility",
            exerciseIds = listOf(
                "bird_dog",
                "glute_bridge",
                "hip_hinge",
                "reverse_lunge",
                "calf_raise"
            )
        ),
        WorkoutPlan(
            id = "athletic_power_conditioning",
            title = "Athletic Power Circuit",
            description = "Explosive full-body endurance & plyometrics",
            durationMinutes = 28,
            exerciseCount = 7,
            difficulty = "Advanced",
            category = "Strength",
            exerciseIds = listOf(
                "burpee",
                "squat",
                "diamond_push_up",
                "mountain_climber",
                "shoulder_press",
                "side_plank",
                "high_knees"
            )
        )
    )

    val plans: List<WorkoutPlan>
        get() = getSavedWorkoutPlans().map { it.toWorkoutPlan() } + catalogPlans

    fun parsePlanDayWorkoutId(workoutId: String): Pair<String, Int>? {
        if (workoutId.contains("__day_")) {
            val parts = workoutId.split("__day_")
            val planId = parts.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
            val dayNum = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 30) ?: return null
            return planId to dayNum
        }
        val matchingSaved = getSavedWorkoutPlans().firstOrNull { it.id == workoutId }
        if (matchingSaved != null) {
            return matchingSaved.id to matchingSaved.currentDay.coerceIn(1, 30)
        }
        return null
    }

    fun findPlanById(id: String): WorkoutPlan? {
        if (id.contains("__day_")) {
            val (planId, dayNum) = parsePlanDayWorkoutId(id) ?: return null
            val saved = getSavedWorkoutPlans().firstOrNull { it.id == planId }
            if (saved != null) {
                return saved.toDayWorkoutPlan(dayNum)
            }
        }
        val savedMatch = getSavedWorkoutPlans().firstOrNull { it.id == id }
        if (savedMatch != null) {
            return savedMatch.toWorkoutPlan()
        }
        return catalogPlans.firstOrNull { it.id == id }
    }

    fun getSavedWorkoutPlans(userId: String? = null): List<SavedWorkoutPlan> {
        val helper = dbHelper ?: return emptyList()
        val targetUser = userId ?: activeUserId()
        val list = mutableListOf<SavedWorkoutPlan>()
        val db = helper.readableDatabase

        db.rawQuery(
            """
            SELECT id, user_id, title, description, goal, difficulty,
                   days_per_week, minutes_per_workout, exercise_ids, days_json,
                   generated_by_ai, created_at, start_date, current_day, completed_days_json
            FROM saved_workout_plans
            WHERE user_id = ?
            ORDER BY created_at DESC
            """.trimIndent(),
            arrayOf(targetUser)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val uId = cursor.getString(1)
                val title = cursor.getString(2)
                val description = cursor.getString(3)
                val goal = cursor.getString(4)
                val difficulty = cursor.getString(5)
                val daysPerWeek = cursor.getInt(6)
                val minutesPerWorkout = cursor.getInt(7)
                val exerciseIdsJson = cursor.getString(8)
                val daysJsonStr = cursor.getString(9)
                val generatedByAi = cursor.getInt(10) == 1
                val createdAt = cursor.getLong(11)
                val startDateRaw = cursor.getString(12) ?: ""
                val currentDayRaw = cursor.getInt(13).coerceIn(1, 30)
                val completedDaysRaw = cursor.getString(14) ?: "[]"

                val rawDays = parseDaysJson(daysJsonStr, minutesPerWorkout)
                val fullThirtyDays = if (rawDays.size == 30) {
                    rawDays
                } else {
                    ThirtyDayProgramGenerator.generateThirtyDaySchedule(
                        goal = goal,
                        difficulty = difficulty,
                        daysPerWeek = daysPerWeek,
                        minutesPerWorkout = minutesPerWorkout,
                        seedDays = rawDays
                    )
                }

                val exIds = parseStringArray(exerciseIdsJson).ifEmpty {
                    fullThirtyDays.flatMap { it.exerciseIds }.distinct()
                }

                list.add(
                    SavedWorkoutPlan(
                        id = id,
                        userId = uId,
                        title = title,
                        description = description,
                        goal = goal,
                        difficulty = difficulty,
                        daysPerWeek = daysPerWeek,
                        minutesPerWorkout = minutesPerWorkout,
                        exerciseIds = exIds,
                        days = fullThirtyDays,
                        generatedByAI = generatedByAi,
                        createdAt = createdAt,
                        startDate = startDateRaw.ifBlank { LocalDate.now().toString() },
                        currentDay = currentDayRaw,
                        completedDays = parseIntArray(completedDaysRaw)
                    )
                )
            }
        }
        return list
    }

    suspend fun createAndSaveThirtyDayProgram(
        goal: String,
        difficulty: String,
        daysPerWeek: Int,
        minutesPerWorkout: Int,
        customTitle: String? = null
    ): Result<SavedWorkoutPlan> {
        val schedule = ThirtyDayProgramGenerator.generateThirtyDaySchedule(
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek,
            minutesPerWorkout = minutesPerWorkout
        )
        val planTitle = customTitle?.takeIf { it.isNotBlank() }
            ?: "30-Day ${goal.replaceFirstChar { it.uppercase() }} Program"
        val planDesc = "Progressive 4-week ($daysPerWeek days/wk • ~${minutesPerWorkout}m) training plan tailored for $difficulty level."

        val aiPlan = AIWorkoutPlan(
            title = planTitle,
            description = planDesc,
            goal = goal,
            difficulty = difficulty,
            daysPerWeek = daysPerWeek.coerceIn(2, 6),
            minutesPerWorkout = minutesPerWorkout.coerceIn(10, 45),
            days = schedule,
            generatedByAI = true
        )
        return saveAIPlan(aiPlan)
    }

    suspend fun saveAIPlan(plan: AIWorkoutPlan): Result<SavedWorkoutPlan> {
        val userId = activeUserId()
        val now = System.currentTimeMillis()
        val planId = "ai_plan_${now}"
        val thirtyDayPlan = ThirtyDayProgramGenerator.ensureThirtyDayPlan(plan)

        val flattenedExercises = thirtyDayPlan.days
            .flatMap { it.exerciseIds }
            .distinct()
            .ifEmpty { listOf("jumping_jack", "push_up", "squat", "plank") }

        val savedPlan = SavedWorkoutPlan(
            id = planId,
            userId = userId,
            title = thirtyDayPlan.title,
            description = thirtyDayPlan.description,
            goal = thirtyDayPlan.goal,
            difficulty = thirtyDayPlan.difficulty,
            daysPerWeek = thirtyDayPlan.daysPerWeek,
            minutesPerWorkout = thirtyDayPlan.minutesPerWorkout,
            exerciseIds = flattenedExercises,
            days = thirtyDayPlan.days,
            generatedByAI = thirtyDayPlan.generatedByAI,
            createdAt = now,
            startDate = LocalDate.now().toString(),
            currentDay = 1,
            completedDays = emptyList()
        )

        saveLocalPlan(savedPlan)
        notifyChanged()

        val currentUser = SupabaseClient.currentUser
        if (currentUser != null) {
            val body = buildCloudPayload(savedPlan, currentUser.id)
            val cloudRes = SupabaseClient.Database.upsertSavedWorkoutPlan(body)
            if (cloudRes.isFailure) {
                return Result.failure(
                    cloudRes.exceptionOrNull() ?: Exception("Could not sync workout plan.")
                )
            }
        }

        NotificationRepository.recordAIPlanSavedNotification(
            planId = savedPlan.id,
            planTitle = savedPlan.title,
            daysPerWeek = savedPlan.daysPerWeek
        )
        return Result.success(savedPlan)
    }

    fun markPlanDayCompleted(workoutId: String): SavedWorkoutPlan? {
        val (planId, dayNumber) = parsePlanDayWorkoutId(workoutId) ?: return null
        val existing = getSavedWorkoutPlans().firstOrNull { it.id == planId } ?: return null

        val validDay = dayNumber.coerceIn(1, 30)
        val updatedCompleted = (existing.completedDays + validDay).distinct().sorted()
        val nextUncompleted = ((validDay + 1)..30).firstOrNull { it !in updatedCompleted }
            ?: (1..30).firstOrNull { it !in updatedCompleted }
            ?: 30

        val updatedPlan = existing.copy(
            currentDay = nextUncompleted,
            completedDays = updatedCompleted
        )

        saveLocalPlan(updatedPlan)
        notifyChanged()

        val currentUser = SupabaseClient.currentUser
        if (currentUser != null) {
            scope.launch {
                val body = buildCloudPayload(updatedPlan, currentUser.id)
                SupabaseClient.Database.upsertSavedWorkoutPlan(body)
            }
        }

        return updatedPlan
    }

    private fun buildCloudPayload(plan: SavedWorkoutPlan, userId: String): JSONObject {
        return JSONObject().apply {
            put("id", plan.id)
            put("user_id", userId)
            put("title", plan.title)
            put("description", plan.description)
            put("goal", plan.goal)
            put("difficulty", plan.difficulty)
            put("days_per_week", plan.daysPerWeek)
            put("minutes_per_workout", plan.minutesPerWorkout)
            put("exercise_ids", JSONArray(plan.exerciseIds))
            put("days_json", serializeDaysJsonArray(plan.days))
            put("generated_by_ai", plan.generatedByAI)
            put("created_at", Instant.ofEpochMilli(plan.createdAt).toString())
            put("start_date", plan.startDate)
            put("current_day", plan.currentDay.coerceIn(1, 30))
            put("completed_days", JSONArray(plan.completedDays))
        }
    }

    fun deleteSavedPlan(planId: String) {
        val helper = dbHelper ?: return
        val userId = activeUserId()
        helper.writableDatabase.delete(
            "saved_workout_plans",
            "user_id = ? AND id = ?",
            arrayOf(userId, planId)
        )
        notifyChanged()

        val currentUser = SupabaseClient.currentUser
        if (currentUser != null) {
            scope.launch {
                SupabaseClient.Database.deleteSavedWorkoutPlan(currentUser.id, planId)
            }
        }
    }

    suspend fun syncWithCloud(userId: String) {
        val res = SupabaseClient.Database.getSavedWorkoutPlans(userId)
        res.onSuccess { remoteList ->
            for (obj in remoteList) {
                val id = obj.optString("id")
                if (id.isBlank()) continue
                val title = obj.optString("title", "30-Day Workout Plan")
                val description = obj.optString("description", "")
                val goal = obj.optString("goal", "Full Body")
                val difficulty = obj.optString("difficulty", "Beginner")
                val daysPerWeek = obj.optInt("days_per_week", 3)
                val minutesPerWorkout = obj.optInt("minutes_per_workout", 20)
                val generatedByAi = obj.optBoolean("generated_by_ai", true)
                val createdAtIso = obj.optString("created_at")
                val createdAt = runCatching { Instant.parse(createdAtIso).toEpochMilli() }
                    .getOrDefault(System.currentTimeMillis())

                val exArr = obj.optJSONArray("exercise_ids") ?: JSONArray()
                val exIds = mutableListOf<String>()
                for (i in 0 until exArr.length()) {
                    val item = exArr.optString(i)
                    if (item.isNotBlank()) exIds.add(item)
                }

                val daysArr = obj.optJSONArray("days_json") ?: JSONArray()
                val rawDays = parseDaysJson(daysArr.toString(), minutesPerWorkout)
                val fullThirtyDays = if (rawDays.size == 30) {
                    rawDays
                } else {
                    ThirtyDayProgramGenerator.generateThirtyDaySchedule(
                        goal = goal,
                        difficulty = difficulty,
                        daysPerWeek = daysPerWeek,
                        minutesPerWorkout = minutesPerWorkout,
                        seedDays = rawDays
                    )
                }

                val startDate = obj.optString("start_date", "").ifBlank { LocalDate.now().toString() }
                val currentDay = obj.optInt("current_day", 1).coerceIn(1, 30)
                val completedArr = obj.optJSONArray("completed_days") ?: JSONArray()
                val completedDays = mutableListOf<Int>()
                for (i in 0 until completedArr.length()) {
                    val d = completedArr.optInt(i, -1)
                    if (d in 1..30) completedDays.add(d)
                }

                val savedPlan = SavedWorkoutPlan(
                    id = id,
                    userId = userId,
                    title = title,
                    description = description,
                    goal = goal,
                    difficulty = difficulty,
                    daysPerWeek = daysPerWeek,
                    minutesPerWorkout = minutesPerWorkout,
                    exerciseIds = exIds.ifEmpty { fullThirtyDays.flatMap { it.exerciseIds }.distinct() },
                    days = fullThirtyDays,
                    generatedByAI = generatedByAi,
                    createdAt = createdAt,
                    startDate = startDate,
                    currentDay = currentDay,
                    completedDays = completedDays.distinct().sorted()
                )
                saveLocalPlan(savedPlan)

                // If remote plan had fewer than 30 days, persist the upgraded 30-day schedule back to cloud
                if (rawDays.size != 30) {
                    SupabaseClient.Database.upsertSavedWorkoutPlan(buildCloudPayload(savedPlan, userId))
                }
            }
            notifyChanged()
        }
    }

    private fun saveLocalPlan(plan: SavedWorkoutPlan) {
        val helper = dbHelper ?: return
        val values = ContentValues().apply {
            put("id", plan.id)
            put("user_id", plan.userId)
            put("title", plan.title)
            put("description", plan.description)
            put("goal", plan.goal)
            put("difficulty", plan.difficulty)
            put("days_per_week", plan.daysPerWeek)
            put("minutes_per_workout", plan.minutesPerWorkout)
            put("exercise_ids", JSONArray(plan.exerciseIds).toString())
            put("days_json", serializeDaysJsonArray(plan.days).toString())
            put("generated_by_ai", if (plan.generatedByAI) 1 else 0)
            put("created_at", plan.createdAt)
            put("start_date", plan.startDate)
            put("current_day", plan.currentDay.coerceIn(1, 30))
            put("completed_days_json", JSONArray(plan.completedDays).toString())
        }
        helper.writableDatabase.insertWithOnConflict(
            "saved_workout_plans",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    private fun serializeDaysJsonArray(days: List<AIWorkoutDay>): JSONArray {
        val arr = JSONArray()
        days.forEach { d ->
            arr.put(
                JSONObject().apply {
                    put("day", d.dayNumber)
                    put("focus", d.focus)
                    put("exerciseIds", JSONArray(d.exerciseIds))
                    put("isRestDay", d.isRestDay)
                    put("warmup", d.warmup)
                    put("cooldown", d.cooldown)
                    put("durationMinutes", d.durationMinutes)
                    put("progressionNote", d.progressionNote)
                }
            )
        }
        return arr
    }

    private fun parseStringArray(jsonStr: String): List<String> {
        return runCatching {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) list.add(item)
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun parseIntArray(jsonStr: String): List<Int> {
        return runCatching {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Int>()
            for (i in 0 until arr.length()) {
                val item = arr.optInt(i, -1)
                if (item in 1..30) list.add(item)
            }
            list.distinct().sorted()
        }.getOrDefault(emptyList())
    }

    private fun parseDaysJson(jsonStr: String, defaultMinutes: Int = 20): List<AIWorkoutDay> {
        return runCatching {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<AIWorkoutDay>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dayNum = obj.optInt("day", i + 1)
                val focus = obj.optString("focus", "Full Body")
                val exArr = obj.optJSONArray("exerciseIds") ?: JSONArray()
                val exIds = mutableListOf<String>()
                for (j in 0 until exArr.length()) {
                    val id = exArr.optString(j)
                    if (id.isNotBlank()) exIds.add(id)
                }
                val isRest = obj.optBoolean("isRestDay", exIds.isEmpty())
                val warmup = obj.optString("warmup", "").ifBlank {
                    if (isRest) "5 min gentle full-body mobility & deep breathing"
                    else "3 min dynamic arm circles, hip openers & light marching"
                }
                val cooldown = obj.optString("cooldown", "").ifBlank {
                    if (isRest) "5 min relaxed stretching for tight muscle groups"
                    else "2 min chest opener, hamstring stretch & slow nasal recovery breaths"
                }
                val duration = obj.optInt("durationMinutes", if (isRest) 10 else defaultMinutes)
                val note = obj.optString("progressionNote", "")

                list.add(
                    AIWorkoutDay(
                        dayNumber = dayNum,
                        focus = focus,
                        exerciseIds = if (isRest) emptyList() else exIds,
                        isRestDay = isRest,
                        warmup = warmup,
                        cooldown = cooldown,
                        durationMinutes = duration,
                        progressionNote = note
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }
}
