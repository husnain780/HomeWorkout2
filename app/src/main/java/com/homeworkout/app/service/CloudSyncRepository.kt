package com.homeworkout.app.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.homeworkout.app.data.supabase.SupabaseClient
import com.homeworkout.app.data.supabase.SupabaseUser
import com.homeworkout.app.model.AICoachRepository
import com.homeworkout.app.model.NotificationRepository
import com.homeworkout.app.model.UserPreferencesRepository
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

data class CloudAdminStats(
    val totalUsers: Long = 0L,
    val totalCloudWorkouts: Long = 0L
)

object CloudSyncRepository {

    var isAdmin by mutableStateOf(false)
        private set

    var isCloudAvailable by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var cloudMessage by mutableStateOf("Supabase cloud sync ready.")
        private set

    var syncRevision by mutableIntStateOf(0)
        private set

    private var initialized = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize() {
        if (initialized) return
        initialized = true

        SupabaseClient.addAuthStateListener { user ->
            if (user == null) {
                isAdmin = false
                isCloudAvailable = false
                isSyncing = false
                cloudMessage = "Signed out."
                WorkoutHistoryRepository.notifyChanged()
                syncRevision++
            } else {
                handleUserLoggedIn(user)
            }
        }

        SupabaseClient.currentUser?.let { user ->
            handleUserLoggedIn(user)
        }
    }

    fun refreshNow() {
        val user = SupabaseClient.currentUser ?: return
        handleUserLoggedIn(user)
    }

    private fun handleUserLoggedIn(user: SupabaseUser) {
        scope.launch {
            isSyncing = true
            try {
                syncProfileAndRole(user)
                syncLocalHistory(user)
                downloadCloudHistory(user)
                WorkoutPlanRepository.syncWithCloud(user.id)
                NotificationRepository.syncWithCloud(user.id)
                AICoachRepository.syncWithCloud(user.id)
                WorkoutHistoryRepository.notifyChanged()
                syncRevision++
            } finally {
                isSyncing = false
            }
        }
    }

    suspend fun saveWorkoutToCloud(record: WorkoutHistory): Result<Unit> {
        val user = SupabaseClient.currentUser
            ?: return Result.failure(IllegalStateException("Not signed in to Supabase."))

        val isoTime = Instant.ofEpochMilli(record.completedAt).toString()
        val result = SupabaseClient.Database.upsertWorkoutHistory(
            userId = user.id,
            id = record.id,
            workoutId = record.workoutId,
            workoutName = record.workoutName,
            durationMinutes = record.durationMinutes,
            exercisesCompleted = record.exercisesCompleted,
            caloriesBurned = record.caloriesBurned,
            completedAtIso = isoTime
        )

        result.onSuccess {
            WorkoutHistoryRepository.markSynced(record.id, user.id)
            isCloudAvailable = true
            cloudMessage = "Workout saved to Supabase."
            syncRevision++
        }.onFailure { ex ->
            cloudMessage = ex.message ?: "Saved locally. Cloud sync pending."
        }

        return result
    }

    fun syncWorkout(record: WorkoutHistory) {
        scope.launch {
            saveWorkoutToCloud(record)
        }
    }

    suspend fun syncProfileAndRole(user: SupabaseUser) {
        val getResult = SupabaseClient.Database.getProfile(user.id)
        val existingProfile = getResult.getOrNull()

        if (existingProfile != null) {
            val role = existingProfile.optString("role", "user")
            isAdmin = (role == "admin")
            isCloudAvailable = true
            UserPreferencesRepository.applyRemoteProfile(user.id, existingProfile)
            // Touch last_seen_at without overwriting user's saved display_name
            SupabaseClient.Database.updateProfileDetails(userId = user.id)
        } else {
            val defaultName = UserPreferencesRepository.getDisplayName(user.id)
            val upsertResult = SupabaseClient.Database.upsertProfile(
                userId = user.id,
                displayName = defaultName,
                email = user.email ?: ""
            )
            if (upsertResult.isSuccess) {
                isCloudAvailable = true
                val fresh = SupabaseClient.Database.getProfile(user.id).getOrNull()
                if (fresh != null) {
                    isAdmin = (fresh.optString("role", "user") == "admin")
                    UserPreferencesRepository.applyRemoteProfile(user.id, fresh)
                }
            } else {
                isAdmin = false
                cloudMessage = "Supabase profile sync pending."
            }
        }
        syncRevision++
    }

    private suspend fun syncLocalHistory(user: SupabaseUser) {
        val unsyncedRecords = WorkoutHistoryRepository.getUnsynced(user.id)
        for (record in unsyncedRecords) {
            val isoTime = Instant.ofEpochMilli(record.completedAt).toString()
            val res = SupabaseClient.Database.upsertWorkoutHistory(
                userId = user.id,
                id = record.id,
                workoutId = record.workoutId,
                workoutName = record.workoutName,
                durationMinutes = record.durationMinutes,
                exercisesCompleted = record.exercisesCompleted,
                caloriesBurned = record.caloriesBurned,
                completedAtIso = isoTime
            )
            if (res.isSuccess) {
                WorkoutHistoryRepository.markSynced(record.id, user.id)
            }
        }
    }

    private suspend fun downloadCloudHistory(user: SupabaseUser) {
        val result = SupabaseClient.Database.getWorkoutHistory(user.id)
        result.onSuccess { records ->
            for (obj in records) {
                val id = obj.optString("id")
                val workoutId = obj.optString("workout_id")
                val workoutName = obj.optString("workout_name", "Workout")
                val duration = obj.optInt("duration_minutes", 0)
                val exercises = obj.optInt("exercises_completed", 0)
                val calories = obj.optInt("calories_burned", 0)
                val completedAtStr = obj.optString("completed_at")

                val completedAtMillis = runCatching {
                    Instant.parse(completedAtStr).toEpochMilli()
                }.getOrNull() ?: id.toLongOrNull() ?: System.currentTimeMillis()

                if (id.isNotBlank() && workoutId.isNotBlank()) {
                    WorkoutHistoryRepository.saveLocalIfMissing(
                        record = WorkoutHistory(
                            id = id,
                            workoutId = workoutId,
                            workoutName = workoutName,
                            durationMinutes = duration,
                            exercisesCompleted = exercises,
                            caloriesBurned = calories,
                            completedAt = completedAtMillis
                        ),
                        userId = user.id,
                        synced = true
                    )
                }
            }
            isCloudAvailable = true
            cloudMessage = "Supabase data synchronized."
            WorkoutHistoryRepository.notifyChanged()
        }.onFailure {
            cloudMessage = "Cloud history unavailable. Local data is active."
        }
    }

    fun loadAdminStats(callback: (Result<CloudAdminStats>) -> Unit) {
        scope.launch {
            val user = SupabaseClient.currentUser
            if (user == null) {
                callback(Result.failure(SecurityException("Please sign in to access administrator analytics.")))
                return@launch
            }

            syncProfileAndRole(user)
            if (!isAdmin) {
                callback(Result.failure(SecurityException("Access denied: Administrator privileges required.")))
                return@launch
            }

            val result = SupabaseClient.Database.getAdminStats()
            result.onSuccess { json ->
                val totalUsers = json.optLong("total_users", 0L)
                val totalWorkouts = json.optLong("total_cloud_workouts", 0L)
                callback(
                    Result.success(
                        CloudAdminStats(
                            totalUsers = totalUsers,
                            totalCloudWorkouts = totalWorkouts
                        )
                    )
                )
            }.onFailure { ex ->
                callback(Result.failure(ex))
            }
        }
    }
}
