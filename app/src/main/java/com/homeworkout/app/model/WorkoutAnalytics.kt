package com.homeworkout.app.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class DailyWorkoutStat(
    val date: LocalDate,
    val workoutCount: Int,
    val calories: Int,
    val minutes: Int
)

data class PeriodWorkoutStat(
    val label: String,
    val subtitle: String,
    val workoutCount: Int,
    val calories: Int,
    val minutes: Int
)

object WorkoutAnalytics {

    fun recordDate(
        record: WorkoutHistory
    ): LocalDate {
        val raw = if (record.completedAt > 0L) {
            record.completedAt
        } else {
            record.id.toLongOrNull() ?: System.currentTimeMillis()
        }
        val timestamp = if (raw > 0L) raw else System.currentTimeMillis()

        return runCatching {
            Instant
                .ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.getOrDefault(LocalDate.now())
    }

    fun currentStreak(
        history: List<WorkoutHistory>
    ): Int {
        if (history.isEmpty()) {
            return 0
        }

        val activeDates =
            history
                .map(::recordDate)
                .toSet()

        var currentDate = LocalDate.now()

        if (!activeDates.contains(currentDate)) {
            currentDate = currentDate.minusDays(1)
        }

        var streak = 0

        while (activeDates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    fun lastSevenDays(
        history: List<WorkoutHistory>
    ): List<DailyWorkoutStat> {
        val today = LocalDate.now()

        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dayRecords = history.filter { recordDate(it) == date }

            DailyWorkoutStat(
                date = date,
                workoutCount = dayRecords.size,
                calories = dayRecords.sumOf { it.caloriesBurned },
                minutes = dayRecords.sumOf { it.durationMinutes }
            )
        }
    }

    fun workoutsLastSevenDays(
        history: List<WorkoutHistory>
    ): Int {
        val cutoff = LocalDate.now().minusDays(6)
        return history.count { !recordDate(it).isBefore(cutoff) }
    }

    fun statsForPeriod(
        history: List<WorkoutHistory>,
        period: String
    ): List<PeriodWorkoutStat> {
        val today = LocalDate.now()
        return when (period) {
            "Monthly" -> {
                // Last 4 7-day windows
                (3 downTo 0).map { weekOffset ->
                    val endDate = today.minusDays((weekOffset * 7).toLong())
                    val startDate = endDate.minusDays(6)
                    val records = history.filter {
                        val d = recordDate(it)
                        !d.isBefore(startDate) && !d.isAfter(endDate)
                    }
                    val label = if (weekOffset == 0) "This Wk" else "W-${weekOffset}"
                    val sub = "${startDate.monthValue}/${startDate.dayOfMonth}"
                    PeriodWorkoutStat(
                        label = label,
                        subtitle = sub,
                        workoutCount = records.size,
                        calories = records.sumOf { it.caloriesBurned },
                        minutes = records.sumOf { it.durationMinutes }
                    )
                }
            }
            "Yearly" -> {
                // Last 6 calendar months
                val currentMonth = YearMonth.from(today)
                (5 downTo 0).map { monthOffset ->
                    val ym = currentMonth.minusMonths(monthOffset.toLong())
                    val records = history.filter {
                        YearMonth.from(recordDate(it)) == ym
                    }
                    val label = ym.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    PeriodWorkoutStat(
                        label = label,
                        subtitle = ym.year.toString().takeLast(2),
                        workoutCount = records.size,
                        calories = records.sumOf { it.caloriesBurned },
                        minutes = records.sumOf { it.durationMinutes }
                    )
                }
            }
            else -> {
                // Weekly (Last 7 days)
                lastSevenDays(history).map { stat ->
                    val dayLabel = stat.date.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .take(3)
                    PeriodWorkoutStat(
                        label = dayLabel,
                        subtitle = "${stat.date.monthValue}/${stat.date.dayOfMonth}",
                        workoutCount = stat.workoutCount,
                        calories = stat.calories,
                        minutes = stat.minutes
                    )
                }
            }
        }
    }

    fun totalWorkouts(
        history: List<WorkoutHistory>
    ): Int {
        return history.size
    }

    fun totalCalories(
        history: List<WorkoutHistory>
    ): Int {
        return history.sumOf {
            it.caloriesBurned
        }
    }

    fun totalMinutes(
        history: List<WorkoutHistory>
    ): Int {
        return history.sumOf {
            it.durationMinutes
        }
    }
}
