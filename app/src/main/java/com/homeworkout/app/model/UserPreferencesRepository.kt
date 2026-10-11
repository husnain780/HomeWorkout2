package com.homeworkout.app.model

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.homeworkout.app.data.supabase.SupabaseClient
import org.json.JSONObject

object UserPreferencesRepository {

    private const val PREFS_NAME = "homeworkout_user_preferences"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_DEFAULT_GOAL = "default_fitness_goal"
    private const val KEY_DEFAULT_DIFFICULTY = "default_preferred_difficulty"
    private const val KEY_DEFAULT_WEEKLY_GOAL = "default_weekly_goal"

    private var prefs: SharedPreferences? = null

    var revision by mutableIntStateOf(0)
        private set

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            revision++
        }
    }

    private fun userKey(field: String, userId: String? = SupabaseClient.currentUser?.id): String {
        val uid = userId?.takeIf { it.isNotBlank() } ?: "guest"
        return "${uid}_$field"
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs?.getBoolean(KEY_ONBOARDING_COMPLETED, false) ?: false
    }

    fun completeOnboarding(
        fitnessGoal: String = "Full Body",
        preferredDifficulty: String = "Beginner",
        weeklyGoal: Int = 4
    ) {
        val sp = prefs?.edit() ?: return
        sp.putBoolean(KEY_ONBOARDING_COMPLETED, true)
        sp.putString(KEY_DEFAULT_GOAL, fitnessGoal)
        sp.putString(KEY_DEFAULT_DIFFICULTY, preferredDifficulty)
        sp.putInt(KEY_DEFAULT_WEEKLY_GOAL, weeklyGoal.coerceIn(1, 14))
        val uid = SupabaseClient.currentUser?.id
        if (!uid.isNullOrBlank()) {
            sp.putString(userKey("fitness_goal", uid), fitnessGoal)
            sp.putString(userKey("preferred_difficulty", uid), preferredDifficulty)
            sp.putInt(userKey("weekly_goal", uid), weeklyGoal.coerceIn(1, 14))
        }
        sp.apply()
        revision++
    }

    fun getDisplayName(userId: String? = SupabaseClient.currentUser?.id): String {
        val user = SupabaseClient.currentUser
        val stored = prefs?.getString(userKey("display_name", userId), null)
        return when {
            !stored.isNullOrBlank() -> stored
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Athlete"
            !user?.email.isNullOrBlank() -> user?.email?.substringBefore('@') ?: "Athlete"
            else -> "Athlete"
        }
    }

    fun getFitnessGoal(userId: String? = SupabaseClient.currentUser?.id): String {
        val defaultGoal = prefs?.getString(KEY_DEFAULT_GOAL, "Full Body") ?: "Full Body"
        return prefs?.getString(userKey("fitness_goal", userId), defaultGoal) ?: defaultGoal
    }

    fun getPreferredDifficulty(userId: String? = SupabaseClient.currentUser?.id): String {
        val defaultDiff = prefs?.getString(KEY_DEFAULT_DIFFICULTY, "Beginner") ?: "Beginner"
        return prefs?.getString(userKey("preferred_difficulty", userId), defaultDiff) ?: defaultDiff
    }

    fun getWeeklyGoal(userId: String? = SupabaseClient.currentUser?.id): Int {
        val defaultWeekly = prefs?.getInt(KEY_DEFAULT_WEEKLY_GOAL, 4) ?: 4
        return (prefs?.getInt(userKey("weekly_goal", userId), defaultWeekly) ?: defaultWeekly)
            .coerceIn(1, 14)
    }

    fun isNotificationsEnabled(userId: String? = SupabaseClient.currentUser?.id): Boolean {
        return prefs?.getBoolean(userKey("notifications_enabled", userId), true) ?: true
    }

    fun isRemindersEnabled(userId: String? = SupabaseClient.currentUser?.id): Boolean {
        return prefs?.getBoolean(userKey("reminders_enabled", userId), true) ?: true
    }

    fun applyRemoteProfile(userId: String, profileJson: JSONObject) {
        val sp = prefs?.edit() ?: return
        val displayName = profileJson.optString("display_name").takeIf { it.isNotBlank() }
        if (displayName != null) {
            sp.putString(userKey("display_name", userId), displayName)
            if (SupabaseClient.currentUser?.id == userId) {
                SupabaseClient.updateCurrentUserDisplayName(displayName)
            }
        }
        val goal = profileJson.optString("fitness_goal").takeIf { it.isNotBlank() }
        if (goal != null) {
            sp.putString(userKey("fitness_goal", userId), goal)
        }
        val difficulty = profileJson.optString("preferred_difficulty").takeIf { it.isNotBlank() }
        if (difficulty != null) {
            sp.putString(userKey("preferred_difficulty", userId), difficulty)
        }
        if (profileJson.has("weekly_goal") && !profileJson.isNull("weekly_goal")) {
            sp.putInt(userKey("weekly_goal", userId), profileJson.optInt("weekly_goal", 4).coerceIn(1, 14))
        }
        if (profileJson.has("notifications_enabled") && !profileJson.isNull("notifications_enabled")) {
            sp.putBoolean(userKey("notifications_enabled", userId), profileJson.optBoolean("notifications_enabled", true))
        }
        if (profileJson.has("reminders_enabled") && !profileJson.isNull("reminders_enabled")) {
            sp.putBoolean(userKey("reminders_enabled", userId), profileJson.optBoolean("reminders_enabled", true))
        }
        sp.putBoolean(KEY_ONBOARDING_COMPLETED, true)
        sp.apply()
        revision++
    }

    suspend fun saveProfilePreferences(
        displayName: String? = null,
        fitnessGoal: String? = null,
        preferredDifficulty: String? = null,
        weeklyGoal: Int? = null,
        notificationsEnabled: Boolean? = null,
        remindersEnabled: Boolean? = null
    ): Result<Unit> {
        val user = SupabaseClient.currentUser
            ?: return Result.failure(IllegalStateException("Please sign in to save profile settings."))
        val userId = user.id

        val resolvedName = displayName?.trim()?.takeIf { it.isNotBlank() } ?: getDisplayName(userId)
        val resolvedGoal = fitnessGoal ?: getFitnessGoal(userId)
        val resolvedDiff = preferredDifficulty ?: getPreferredDifficulty(userId)
        val resolvedWeekly = (weeklyGoal ?: getWeeklyGoal(userId)).coerceIn(1, 14)
        val resolvedNotif = notificationsEnabled ?: isNotificationsEnabled(userId)
        val resolvedRemind = remindersEnabled ?: isRemindersEnabled(userId)

        // Persist locally immediately
        prefs?.edit()?.apply {
            putString(userKey("display_name", userId), resolvedName)
            putString(userKey("fitness_goal", userId), resolvedGoal)
            putString(userKey("preferred_difficulty", userId), resolvedDiff)
            putInt(userKey("weekly_goal", userId), resolvedWeekly)
            putBoolean(userKey("notifications_enabled", userId), resolvedNotif)
            putBoolean(userKey("reminders_enabled", userId), resolvedRemind)
            apply()
        }
        SupabaseClient.updateCurrentUserDisplayName(resolvedName)
        revision++

        // Persist to Supabase public.profiles
        val result = SupabaseClient.Database.updateProfileDetails(
            userId = userId,
            displayName = resolvedName,
            email = user.email,
            fitnessGoal = resolvedGoal,
            preferredDifficulty = resolvedDiff,
            weeklyGoal = resolvedWeekly,
            notificationsEnabled = resolvedNotif,
            remindersEnabled = resolvedRemind
        )
        return result.map { profileJson ->
            if (profileJson != null) {
                applyRemoteProfile(userId, profileJson)
            }
        }
    }
}
