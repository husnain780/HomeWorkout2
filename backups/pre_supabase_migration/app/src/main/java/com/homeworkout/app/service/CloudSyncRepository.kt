package com.homeworkout.app.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutHistoryRepository

data class CloudAdminStats(
    val totalUsers: Long = 0L,
    val totalCloudWorkouts: Long = 0L
)

object CloudSyncRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    var isAdmin by mutableStateOf(false)
        private set

    var isCloudAvailable by mutableStateOf(false)
        private set

    var cloudMessage by mutableStateOf(
        "Cloud sync ready."
    )
        private set

    private var initialized = false

    fun initialize() {

        if (initialized) {
            return
        }

        initialized = true

        auth.addAuthStateListener { firebaseAuth ->

            val user =
                firebaseAuth.currentUser

            if (user == null) {

                isAdmin = false
                isCloudAvailable = false
                cloudMessage = "Signed out."

            } else {

                ensureUserProfile(user)

                loadRole(user)

                syncLocalHistory()

                downloadCloudHistory(user)
            }
        }

        auth.currentUser?.let { user ->

            ensureUserProfile(user)

            loadRole(user)

            syncLocalHistory()

            downloadCloudHistory(user)
        }
    }

    fun syncWorkout(
        record: WorkoutHistory
    ) {

        val user =
            auth.currentUser
                ?: return

        val data =
            hashMapOf<String, Any>(
                "id" to record.id,
                "workoutId" to record.workoutId,
                "workoutName" to record.workoutName,
                "durationMinutes" to
                    record.durationMinutes,
                "exercisesCompleted" to
                    record.exercisesCompleted,
                "caloriesBurned" to
                    record.caloriesBurned,
                "completedAt" to
                    Timestamp(
                        record.completedAt / 1000L,
                        0
                    )
            )

        firestore
            .collection("users")
            .document(user.uid)
            .collection("workoutHistory")
            .document(record.id)
            .set(data)
            .addOnSuccessListener {

                isCloudAvailable = true
                cloudMessage =
                    "Workout synced to cloud."

            }
            .addOnFailureListener { exception ->

                cloudMessage =
                    exception.message
                        ?: "Cloud sync unavailable."
            }
    }

    private fun ensureUserProfile(
        user: FirebaseUser
    ) {

        val reference =
            firestore
                .collection("users")
                .document(user.uid)

        reference
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    reference
                        .update(
                            mapOf(
                                "displayName" to
                                    (user.displayName
                                        ?: ""),
                                "email" to
                                    (user.email
                                        ?: ""),
                                "lastSeenAt" to
                                    FieldValue
                                        .serverTimestamp()
                            )
                        )

                } else {

                    val data =
                        hashMapOf<String, Any>(
                            "uid" to user.uid,
                            "displayName" to
                                (user.displayName
                                    ?: ""),
                            "email" to
                                (user.email
                                    ?: ""),
                            "role" to "user",
                            "createdAt" to
                                FieldValue
                                    .serverTimestamp(),
                            "lastSeenAt" to
                                FieldValue
                                    .serverTimestamp()
                        )

                    reference
                        .set(data)
                }

                isCloudAvailable = true

            }
            .addOnFailureListener {

                cloudMessage =
                    "Firestore is not configured yet."
            }
    }

    private fun loadRole(
        user: FirebaseUser
    ) {

        firestore
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val role =
                    snapshot
                        .getString("role")
                        ?: "user"

                isAdmin =
                    role == "admin"
            }
            .addOnFailureListener {

                isAdmin = false
            }
    }

    private fun syncLocalHistory() {

        val user =
            auth.currentUser
                ?: return

        WorkoutHistoryRepository
            .getAll()
            .forEach { record ->

                val data =
                    hashMapOf<String, Any>(
                        "id" to record.id,
                        "workoutId" to
                            record.workoutId,
                        "workoutName" to
                            record.workoutName,
                        "durationMinutes" to
                            record.durationMinutes,
                        "exercisesCompleted" to
                            record.exercisesCompleted,
                        "caloriesBurned" to
                            record.caloriesBurned,
                        "completedAt" to
                            Timestamp(
                                record.completedAt /
                                    1000L,
                                0
                            )
                    )

                firestore
                    .collection("users")
                    .document(user.uid)
                    .collection("workoutHistory")
                    .document(record.id)
                    .set(data)
            }
    }

    private fun downloadCloudHistory(
        user: FirebaseUser
    ) {

        firestore
            .collection("users")
            .document(user.uid)
            .collection("workoutHistory")
            .get()
            .addOnSuccessListener { result ->

                result.documents
                    .forEach { document ->

                        val id =
                            document
                                .getString("id")
                                ?: document.id

                        val workoutId =
                            document
                                .getString("workoutId")
                                ?: return@forEach

                        val workoutName =
                            document
                                .getString(
                                    "workoutName"
                                )
                                ?: "Workout"

                        val duration =
                            (
                                document
                                    .getLong(
                                        "durationMinutes"
                                    )
                                    ?: 0L
                            ).toInt()

                        val exercises =
                            (
                                document
                                    .getLong(
                                        "exercisesCompleted"
                                    )
                                    ?: 0L
                            ).toInt()

                        val calories =
                            (
                                document
                                    .getLong(
                                        "caloriesBurned"
                                    )
                                    ?: 0L
                            ).toInt()

                        val timestamp =
                            document
                                .getTimestamp(
                                    "completedAt"
                                )

                        val completedAt =
                            timestamp?.seconds
                                ?.times(1000L)
                                ?: id.toLongOrNull()
                                ?: 0L

                        WorkoutHistoryRepository
                            .saveLocalIfMissing(
                                WorkoutHistory(
                                    id = id,
                                    workoutId =
                                        workoutId,
                                    workoutName =
                                        workoutName,
                                    durationMinutes =
                                        duration,
                                    exercisesCompleted =
                                        exercises,
                                    caloriesBurned =
                                        calories,
                                    completedAt =
                                        completedAt
                                )
                            )
                    }

                isCloudAvailable = true
                cloudMessage =
                    "Cloud data synchronized."

            }
            .addOnFailureListener {

                cloudMessage =
                    "Cloud history unavailable. Local data is still available."
            }
    }

    fun loadAdminStats(
        callback:
            (Result<CloudAdminStats>) -> Unit
    ) {

        if (!isAdmin) {

            callback(
                Result.failure(
                    SecurityException(
                        "Administrator access required."
                    )
                )
            )

            return
        }

        val usersTask =
            firestore
                .collection("users")
                .count()
                .get(
                    AggregateSource.SERVER
                )

        val workoutsTask =
            firestore
                .collectionGroup(
                    "workoutHistory"
                )
                .count()
                .get(
                    AggregateSource.SERVER
                )

        usersTask
            .addOnSuccessListener {
                usersSnapshot ->

                workoutsTask
                    .addOnSuccessListener {
                        workoutsSnapshot ->

                        callback(
                            Result.success(
                                CloudAdminStats(
                                    totalUsers =
                                        usersSnapshot
                                            .count,
                                    totalCloudWorkouts =
                                        workoutsSnapshot
                                            .count
                                )
                            )
                        )
                    }
                    .addOnFailureListener {
                        exception ->

                        callback(
                            Result.failure(
                                exception
                            )
                        )
                    }
            }
            .addOnFailureListener { exception ->

                callback(
                    Result.failure(
                        exception
                    )
                )
            }
    }
}
