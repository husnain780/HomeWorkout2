package com.homeworkout.app

import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.SavedWorkoutPlan
import com.homeworkout.app.model.ThirtyDayProgramGenerator
import com.homeworkout.app.model.WorkoutAnalytics
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.view.ExercisePoseEngine
import com.homeworkout.app.view.WorkoutSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun pkceOAuthUrl_generatesValidVerifierAndChallenge() {
        val url = com.homeworkout.app.data.supabase.SupabaseClient.Auth.getOAuthUrl(
            provider = "google",
            redirectTo = "homeworkout://auth-callback"
        )
        assertTrue(url.contains("provider=google"))
        assertTrue(url.contains("redirect_to=homeworkout%3A%2F%2Fauth-callback"))
        assertTrue(url.contains("code_challenge_method=s256"))

        val verifier = com.homeworkout.app.data.supabase.SupabaseClient.peekPkceVerifier()
        assertNotNull("PKCE verifier should be saved", verifier)
        assertTrue("PKCE verifier must be non-empty", verifier!!.length >= 43)

        val expectedChallenge = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
            java.security.MessageDigest.getInstance("SHA-256")
                .digest(verifier.toByteArray(Charsets.US_ASCII))
        )
        assertTrue(
            "OAuth URL should contain matching SHA-256 code_challenge",
            url.contains("code_challenge=$expectedChallenge")
        )
    }

    @Test
    fun workoutAnalytics_computesPeriodStatsAndStreaksAccurately() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000
        val records = listOf(
            WorkoutHistory(
                id = "w1",
                workoutId = "full_body",
                workoutName = "Full Body Blast",
                durationMinutes = 15,
                caloriesBurned = 150,
                exercisesCompleted = 5,
                completedAt = now
            ),
            WorkoutHistory(
                id = "w2",
                workoutId = "core_crusher",
                workoutName = "Core Crusher",
                durationMinutes = 10,
                caloriesBurned = 110,
                exercisesCompleted = 4,
                completedAt = now - dayMs
            )
        )

        assertTrue("Streak should be at least 1", WorkoutAnalytics.currentStreak(records) >= 1)
        assertEquals(2, WorkoutAnalytics.workoutsLastSevenDays(records))

        val weeklyStats = WorkoutAnalytics.statsForPeriod(records, "Weekly")
        assertEquals(7, weeklyStats.size)
        assertEquals(2, weeklyStats.sumOf { it.workoutCount })
        assertEquals(260, weeklyStats.sumOf { it.calories })
        assertEquals(25, weeklyStats.sumOf { it.minutes })

        val monthlyStats = WorkoutAnalytics.statsForPeriod(records, "Monthly")
        assertEquals(4, monthlyStats.size)
        assertTrue(monthlyStats.sumOf { it.workoutCount } >= 1)

        val yearlyStats = WorkoutAnalytics.statsForPeriod(records, "Yearly")
        assertEquals(6, yearlyStats.size)
        assertTrue(yearlyStats.sumOf { it.workoutCount } >= 1)
    }

    @Test
    fun exerciseAndWorkoutPlanRepositories_haveCompleteValidCatalogs() {
        val exercises = ExerciseRepository.exercises
        assertEquals(30, exercises.size)
        val catalogIds = exercises.map { it.id }.toSet()

        exercises.forEach { ex ->
            assertTrue(ex.id.isNotBlank())
            assertTrue(ex.name.isNotBlank())
            assertTrue(ex.instructions.isNotEmpty())
            assertTrue(ex.calories > 0)
        }

        val plans = WorkoutPlanRepository.catalogPlans
        assertEquals(6, plans.size)
        plans.forEach { plan ->
            assertTrue(plan.id.isNotBlank())
            assertTrue(plan.title.isNotBlank())
            assertTrue(plan.exerciseIds.isNotEmpty())
            plan.exerciseIds.forEach { exId ->
                assertTrue(
                    "Plan ${plan.id} references unknown exercise ID '$exId'",
                    catalogIds.contains(exId)
                )
            }
        }
    }

    @Test
    fun exercisePoseEngine_supportsAllThirtyExercisesWithDistinctSmoothMovements() {
        val catalogIds = ExerciseRepository.exercises.map { it.id }
        assertEquals(30, catalogIds.size)

        catalogIds.forEach { id ->
            assertTrue(
                "ExercisePoseEngine must explicitly support '$id'",
                ExercisePoseEngine.supportedExerciseIds.contains(id)
            )
            assertTrue(ExercisePoseEngine.cycleDurationMs(id) in 1000..3000)

            val poseStart = ExercisePoseEngine.computePose(id, 0.0f)
            val poseQuarter = ExercisePoseEngine.computePose(id, 0.25f)
            val posePeak = ExercisePoseEngine.computePose(id, 0.50f)

            listOf(poseStart, poseQuarter, posePeak).forEach { pose ->
                pose.allJoints().forEach { joint ->
                    assertTrue("Joint x=${joint.x} out of bounds for $id", joint.x in 0.02f..0.98f)
                    assertTrue("Joint y=${joint.y} out of bounds for $id", joint.y in 0.02f..0.98f)
                }
            }

            // Verify movement occurs across the animation cycle
            val deltaMid = poseStart.allJoints().zip(posePeak.allJoints()).sumOf { (a, b) ->
                (abs(a.x - b.x) + abs(a.y - b.y)).toDouble()
            }
            val deltaQuarter = poseStart.allJoints().zip(poseQuarter.allJoints()).sumOf { (a, b) ->
                (abs(a.x - b.x) + abs(a.y - b.y)).toDouble()
            }
            assertTrue(
                "Exercise '$id' must animate joints across cycle phases",
                deltaMid > 0.01 || deltaQuarter > 0.01
            )
        }

        // Verify distinctness between core archetypes (squat vs jumping_jack vs push_up vs glute_bridge)
        val squatPeak = ExercisePoseEngine.computePose("squat", 0.5f)
        val pushUpPeak = ExercisePoseEngine.computePose("push_up", 0.5f)
        val jackPeak = ExercisePoseEngine.computePose("jumping_jack", 0.5f)
        val bridgePeak = ExercisePoseEngine.computePose("glute_bridge", 0.5f)

        assertNotEquals(squatPeak.head, pushUpPeak.head)
        assertNotEquals(squatPeak.handR, jackPeak.handR)
        assertNotEquals(pushUpPeak.hipR, bridgePeak.hipR)
    }

    @Test
    fun workoutSessionState_handlesTimerPausePreviousNextAndCompletionCleanly() {
        val durations = listOf(20, 30, 25)
        var state = WorkoutSessionState(
            totalSteps = durations.size,
            stepDurationSeconds = durations
        )

        assertEquals(0, state.currentIndex)
        assertEquals(20, state.remainingSeconds)
        assertEquals(0, state.activeSessionSeconds)

        // Tick 5 seconds
        repeat(5) { state = state.tickOneSecond() }
        assertEquals(15, state.remainingSeconds)
        assertEquals(5, state.activeSessionSeconds)

        // Pause and verify ticks do not advance countdown or active time
        state = state.togglePause()
        assertTrue(state.isPaused)
        repeat(3) { state = state.tickOneSecond() }
        assertEquals(15, state.remainingSeconds)
        assertEquals(5, state.activeSessionSeconds)

        // Resume and navigate Next
        state = state.togglePause()
        assertFalse(state.isPaused)
        state = state.nextExercise()
        assertEquals(1, state.currentIndex)
        assertEquals(30, state.remainingSeconds)
        assertEquals(5, state.activeSessionSeconds) // Active session time preserved!
        assertTrue(state.completedIndices.contains(0))

        // Tick 4 seconds on exercise 2, then navigate Previous
        repeat(4) { state = state.tickOneSecond() }
        assertEquals(26, state.remainingSeconds)
        assertEquals(9, state.activeSessionSeconds)

        state = state.previousExercise()
        assertEquals(0, state.currentIndex)
        assertEquals(20, state.remainingSeconds)
        assertEquals(9, state.activeSessionSeconds) // Active session time still preserved!

        // Advance through remaining exercises to completion
        state = state.nextExercise() // -> index 1
        state = state.nextExercise() // -> index 2
        assertEquals(2, state.currentIndex)
        assertFalse(state.isFinished)

        state = state.nextExercise() // -> finishes workout
        assertTrue(state.isFinished)
        assertEquals(setOf(0, 1, 2), state.completedIndices)
    }

    @Test
    fun workoutSessionTimer_advancesToNextExerciseAutomaticallyWithoutPrematureFinish() {
        val durations = listOf(5, 4) // Two exercises: 5s and 4s
        var state = WorkoutSessionState(
            totalSteps = durations.size,
            stepDurationSeconds = durations
        )

        // 1. Ticking down 5 seconds on Exercise 1 (from 5s down to 0)
        repeat(4) { state = state.tickOneSecond() }
        assertEquals(0, state.currentIndex)
        assertEquals(1, state.remainingSeconds)
        assertEquals(4, state.activeSessionSeconds)
        assertFalse("Workout must NOT finish after exercise 1", state.isFinished)

        // The final 5th second ticks: should automatically transition to Exercise 2
        state = state.tickOneSecond()
        assertEquals("Must advance to index 1", 1, state.currentIndex)
        assertEquals("Must set remaining seconds to exercise 2 duration", 4, state.remainingSeconds)
        assertEquals("Must preserve total active elapsed session seconds", 5, state.activeSessionSeconds)
        assertTrue("Completed indices must record exercise 0", state.completedIndices.contains(0))
        assertFalse("Workout must remain active on exercise 2", state.isFinished)

        // 2. Tick Exercise 2 down to the end (4 seconds)
        repeat(3) { state = state.tickOneSecond() }
        assertEquals(1, state.currentIndex)
        assertEquals(1, state.remainingSeconds)
        assertEquals(8, state.activeSessionSeconds)
        assertFalse(state.isFinished)

        // The final 4th second of exercise 2 ticks: workout is now complete
        state = state.tickOneSecond()
        assertEquals(1, state.currentIndex)
        assertEquals(0, state.remainingSeconds)
        assertEquals(9, state.activeSessionSeconds)
        assertTrue("Workout must finish only after the final exercise completes", state.isFinished)
        assertEquals(setOf(0, 1), state.completedIndices)
    }

    @Test
    fun thirtyDayProgramGenerator_buildsCompleteProgressiveScheduleWithRestDays() {
        val catalogIds = ExerciseRepository.exercises.map { it.id }.toSet()

        for (daysPerWeek in listOf(2, 3, 4, 5, 6)) {
            val schedule = ThirtyDayProgramGenerator.generateThirtyDaySchedule(
                goal = "Strength",
                difficulty = "Intermediate",
                daysPerWeek = daysPerWeek,
                minutesPerWorkout = 20
            )

            assertEquals("Schedule must have exactly 30 days", 30, schedule.size)
            assertEquals((1..30).toList(), schedule.map { it.dayNumber })

            // Verify first 7 days match exact weekly training frequency
            val week1ActiveCount = schedule.take(7).count { !it.isRestDay }
            assertEquals(daysPerWeek, week1ActiveCount)

            // Verify all active days have valid catalog exercises, warm-up, and cool-down
            schedule.forEach { day ->
                assertTrue(day.warmup.isNotBlank())
                assertTrue(day.cooldown.isNotBlank())
                if (day.isRestDay) {
                    assertTrue(day.exerciseIds.isEmpty())
                } else {
                    assertTrue(day.exerciseIds.size in 3..7)
                    day.exerciseIds.forEach { id ->
                        assertTrue("Invalid exercise ID '$id' in 30-day plan", catalogIds.contains(id))
                    }
                }
            }

            // Verify progressive overload across weeks (Week 4 duration >= Week 1 duration)
            val week1Active = schedule.first { it.weekNumber == 1 && !it.isRestDay }
            val week4Active = schedule.first { it.weekNumber == 4 && !it.isRestDay }
            assertTrue(week4Active.durationMinutes >= week1Active.durationMinutes)
            assertTrue(week4Active.exerciseIds.size >= week1Active.exerciseIds.size)
        }
    }

    @Test
    fun savedWorkoutPlan_mapsSpecificDayToWorkoutPlayerAndTracksProgress() {
        val schedule = ThirtyDayProgramGenerator.generateThirtyDaySchedule(
            goal = "Full Body",
            difficulty = "Beginner",
            daysPerWeek = 4,
            minutesPerWorkout = 15
        )
        val saved = SavedWorkoutPlan(
            id = "ai_plan_test_30",
            userId = "user_1",
            title = "30-Day Full Body Plan",
            description = "Test 30-day program",
            goal = "Full Body",
            difficulty = "Beginner",
            daysPerWeek = 4,
            minutesPerWorkout = 15,
            exerciseIds = schedule.flatMap { it.exerciseIds }.distinct(),
            days = schedule,
            generatedByAI = true,
            createdAt = 1000L,
            startDate = "2026-10-10",
            currentDay = 1,
            completedDays = emptyList()
        )

        val day1Plan = saved.toDayWorkoutPlan(1)
        assertEquals("ai_plan_test_30__day_1", day1Plan.id)
        assertEquals(schedule[0].exerciseIds, day1Plan.exerciseIds)

        val parsed = WorkoutPlanRepository.parsePlanDayWorkoutId("ai_plan_test_30__day_4")
        assertNotNull(parsed)
        assertEquals("ai_plan_test_30", parsed!!.first)
        assertEquals(4, parsed.second)
    }

    @Test
    fun exerciseImageMappings_areDistinctAndConsistentAcrossAllPlansAndDays() {
        val exercises = ExerciseRepository.exercises
        assertEquals(30, exercises.size)

        // 1. Every exercise must have its own unique drawable resource ID (no shared generic placeholder)
        val imageResSet = exercises.map { it.imageRes }.toSet()
        assertEquals(
            "All 30 exercises must have distinct exercise-specific drawable resources",
            30,
            imageResSet.size
        )

        exercises.forEach { ex ->
            assertNotEquals("Exercise ${ex.id} must have non-zero imageRes", 0, ex.imageRes)
            assertEquals(ex.imageRes, ExerciseRepository.imageRes(ex.id))

            val peak = ExercisePoseEngine.peakPhase(ex.id)
            assertTrue("Peak phase for ${ex.id} must be within (0, 1)", peak in 0.05f..0.95f)
            val peakPose = ExercisePoseEngine.computePose(ex.id, peak)
            assertEquals(ex.id, peakPose.exerciseId)
        }

        // 2. Verify switching days in a 30-day schedule updates the exercise image sequence
        val schedule = ThirtyDayProgramGenerator.generateThirtyDaySchedule(
            goal = "Full Body",
            difficulty = "Beginner",
            daysPerWeek = 4,
            minutesPerWorkout = 20
        )
        val activeDays = schedule.filter { !it.isRestDay }
        assertTrue(activeDays.size >= 16)

        val day1Images = activeDays[0].exerciseIds.map { ExerciseRepository.imageRes(it) }
        val day2Images = activeDays[1].exerciseIds.map { ExerciseRepository.imageRes(it) }
        assertTrue(day1Images.isNotEmpty())
        assertTrue(day2Images.isNotEmpty())
        assertEquals(activeDays[0].exerciseIds.size, day1Images.distinct().size)
        assertEquals(activeDays[1].exerciseIds.size, day2Images.distinct().size)
        assertNotEquals(
            "Different workout days should display different exercise image sequences",
            day1Images,
            day2Images
        )
    }
}