package com.homeworkout.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.homeworkout.app.controller.AuthController
import com.homeworkout.app.controller.WorkoutController
import com.homeworkout.app.events.AppEvent
import com.homeworkout.app.events.AuthEvent
import com.homeworkout.app.events.EventBus
import com.homeworkout.app.model.ExerciseRepository
import com.homeworkout.app.model.NetworkMonitor
import com.homeworkout.app.model.OfflineRepository
import com.homeworkout.app.model.WorkoutHistory
import com.homeworkout.app.model.WorkoutHistoryRepository
import com.homeworkout.app.model.WorkoutPlanRepository
import com.homeworkout.app.service.CloudSyncRepository
import com.homeworkout.app.ui.theme.HomeWorkoutTheme
import com.homeworkout.app.view.AICoachScreen
import com.homeworkout.app.view.AIPlanGenerationScreen
import com.homeworkout.app.view.AdminDashboardScreen
import com.homeworkout.app.view.ExerciseDetailsScreen
import com.homeworkout.app.view.ExerciseItem
import com.homeworkout.app.view.ExerciseLibraryScreen
import com.homeworkout.app.view.FormAnalysisScreen
import com.homeworkout.app.view.HomeScreen
import com.homeworkout.app.view.LoginScreen
import com.homeworkout.app.view.NotificationsScreen
import com.homeworkout.app.view.OnboardingScreen
import com.homeworkout.app.view.ProfileScreen
import com.homeworkout.app.view.ProgressScreen
import com.homeworkout.app.view.RegisterScreen
import com.homeworkout.app.view.SplashScreen
import com.homeworkout.app.view.SubscriptionScreen
import com.homeworkout.app.view.WorkoutCompletionScreen
import com.homeworkout.app.view.WorkoutPlansScreen
import com.homeworkout.app.view.WorkoutScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val eventBus = EventBus()
    private val authController = AuthController(eventBus)
    private val workoutController = WorkoutController(eventBus)

    private var workoutStarted by mutableStateOf(false)
    private var workoutCompleted by mutableStateOf(false)
    private var activeWorkoutId by mutableStateOf("beginner_workout")

    private enum class AppRoute {
        SPLASH,
        ONBOARDING,
        LOGIN,
        REGISTER,
        HOME,
        WORKOUT_PLANS,
        EXERCISE_LIBRARY,
        EXERCISE_DETAILS,
        PROGRESS,
        AI_COACH,
        PROFILE,
        ADMIN,
        SUBSCRIPTION,
        NOTIFICATIONS,
        FORM_ANALYSIS,
        AI_PLAN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WorkoutHistoryRepository.initialize(applicationContext)
        OfflineRepository.initialize(applicationContext)
        NetworkMonitor.start(applicationContext)
        CloudSyncRepository.initialize()

        eventBus.subscribe { event ->

            when (event) {

                is AuthEvent.LoginRequested -> {
                    Log.d(
                        "HomeWorkout",
                        "Login requested: ${event.email}"
                    )
                }

                is AuthEvent.RegisterRequested -> {
                    Log.d(
                        "HomeWorkout",
                        "Registration requested: ${event.email}"
                    )
                }

                AuthEvent.LoggedIn -> {
                    Log.d(
                        "HomeWorkout",
                        "Firebase user logged in"
                    )
                }

                AuthEvent.LoggedOut -> {
                    Log.d(
                        "HomeWorkout",
                        "Firebase user logged out"
                    )
                }

                is AppEvent.WorkoutSelected -> {
                    Log.d(
                        "HomeWorkout",
                        "Workout selected: ${event.workoutId}"
                    )
                }

                is AppEvent.WorkoutStarted -> {
                    activeWorkoutId = event.workoutId
                    workoutStarted = true
                    workoutCompleted = false

                    Log.d(
                        "HomeWorkout",
                        "Workout started: ${event.workoutId}"
                    )
                }

                is AppEvent.WorkoutCompleted -> {
                    workoutStarted = false
                    workoutCompleted = true
                    activeWorkoutId = event.workoutId

                    val completedPlan =
                        WorkoutPlanRepository.plans.firstOrNull {
                            it.id == event.workoutId
                        }

                    if (completedPlan != null) {

                        val calories =
                            completedPlan.exerciseIds.sumOf { exerciseId ->
                                ExerciseRepository.exercises
                                    .firstOrNull { it.id == exerciseId }
                                    ?.calories ?: 0
                            }

                        WorkoutHistoryRepository.add(
                            WorkoutHistory(
                                id = System.currentTimeMillis().toString(),
                                workoutId = completedPlan.id,
                                workoutName = completedPlan.title,
                                durationMinutes = completedPlan.durationMinutes,
                                exercisesCompleted = completedPlan.exerciseCount,
                                caloriesBurned = calories
                            )
                        )
                    }

                    Log.d(
                        "HomeWorkout",
                        "Workout completed: ${event.workoutId}"
                    )
                }

                else -> {
                    Log.d(
                        "HomeWorkout",
                        "Other event received"
                    )
                }
            }
        }

        setContent {

            HomeWorkoutTheme {

                var route by remember {
                    mutableStateOf(AppRoute.SPLASH)
                }

                var returnRoute by remember {
                    mutableStateOf(AppRoute.HOME)
                }

                var isLoggedIn by remember {
                    mutableStateOf(
                        FirebaseAuth.getInstance().currentUser != null
                    )
                }

                var authLoading by remember {
                    mutableStateOf(false)
                }

                var authError by remember {
                    mutableStateOf<String?>(null)
                }

                var selectedExercise by remember {
                    mutableStateOf<ExerciseItem?>(null)
                }

                LaunchedEffect(Unit) {

                    delay(2000)

                    route =
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            isLoggedIn = true
                            AppRoute.HOME
                        } else {
                            AppRoute.ONBOARDING
                        }
                }

                when {

                    workoutCompleted -> {
                        WorkoutCompletionScreen(
                            workoutId = activeWorkoutId,
                            onContinue = {
                                workoutCompleted = false
                                route = AppRoute.HOME
                            }
                        )
                    }

                    workoutStarted -> {
                        WorkoutScreen(
                            workoutId = activeWorkoutId,
                            onCompleteWorkout = {
                                workoutController.completeWorkout(
                                    activeWorkoutId
                                )
                            }
                        )
                    }

                    route == AppRoute.SPLASH -> {
                        SplashScreen()
                    }

                    route == AppRoute.ONBOARDING -> {
                        OnboardingScreen(
                            onGetStarted = {
                                route = AppRoute.LOGIN
                                authError = null
                            },
                            onLogin = {
                                route = AppRoute.LOGIN
                                authError = null
                            }
                        )
                    }

                    route == AppRoute.LOGIN -> {
                        LoginScreen(
                            onLogin = { email, password ->

                                authLoading = true
                                authError = null

                                authController.login(
                                    email = email,
                                    password = password
                                ) { success, message ->

                                    authLoading = false

                                    if (success) {
                                        isLoggedIn = true
                                        route = AppRoute.HOME
                                        authError = null
                                    } else {
                                        authError = message
                                    }
                                }
                            },
                            onGoogleLogin = {

                                authLoading = true
                                authError = null

                                authController.loginWithGoogle(
                                    activity = this@MainActivity,
                                    scope = lifecycleScope
                                ) { success, message ->

                                    authLoading = false

                                    if (success) {
                                        isLoggedIn = true
                                        route = AppRoute.HOME
                                        authError = null
                                    } else {
                                        authError = message
                                    }
                                }
                            },
                            onOpenRegister = {
                                route = AppRoute.REGISTER
                                authError = null
                            },
                            isLoading = authLoading,
                            errorMessage = authError
                        )
                    }

                    route == AppRoute.REGISTER -> {
                        RegisterScreen(
                            onRegister = { name, email, password ->

                                authLoading = true
                                authError = null

                                authController.register(
                                    name = name,
                                    email = email,
                                    password = password
                                ) { success, message ->

                                    authLoading = false

                                    if (success) {
                                        isLoggedIn = true
                                        route = AppRoute.HOME
                                        authError = null
                                    } else {
                                        authError = message
                                    }
                                }
                            },
                            onBackToLogin = {
                                route = AppRoute.LOGIN
                                authError = null
                            },
                            isLoading = authLoading,
                            errorMessage = authError
                        )
                    }

                    route == AppRoute.WORKOUT_PLANS -> {
                        WorkoutPlansScreen(
                            onBack = {
                                route = AppRoute.HOME
                            },
                            onStartWorkout = { workoutId ->

                                activeWorkoutId = workoutId
                                route = AppRoute.HOME

                                workoutController.startWorkout(
                                    workoutId
                                )
                            }
                        )
                    }

                    route == AppRoute.EXERCISE_LIBRARY -> {
                        ExerciseLibraryScreen(
                            onBack = {
                                route = AppRoute.HOME
                            },
                            onExerciseClick = {
                                selectedExercise = it
                                route = AppRoute.EXERCISE_DETAILS
                            }
                        )
                    }

                    route == AppRoute.EXERCISE_DETAILS -> {
                        selectedExercise?.let { exercise ->

                            ExerciseDetailsScreen(
                                exercise = exercise,
                                onBack = {
                                    selectedExercise = null
                                    route = AppRoute.EXERCISE_LIBRARY
                                }
                            )
                        }
                    }

                    route == AppRoute.PROGRESS -> {
                        ProgressScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.AI_COACH -> {
                        AICoachScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.PROFILE -> {
                        ProfileScreen(
                            onBack = {
                                route = AppRoute.HOME
                            },
                            onOpenNotifications = {
                                returnRoute = AppRoute.PROFILE
                                route = AppRoute.NOTIFICATIONS
                            }
                        )
                    }

                    route == AppRoute.ADMIN -> {
                        AdminDashboardScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.SUBSCRIPTION -> {
                        SubscriptionScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.NOTIFICATIONS -> {
                        NotificationsScreen(
                            onBack = {
                                route = returnRoute
                            }
                        )
                    }

                    route == AppRoute.FORM_ANALYSIS -> {
                        FormAnalysisScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.AI_PLAN -> {
                        AIPlanGenerationScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.HOME && isLoggedIn -> {
                        HomeScreen(
                            onStartWorkout = {
                                activeWorkoutId = "beginner_workout"

                                workoutController.startWorkout(
                                    "beginner_workout"
                                )
                            },
                            onOpenPlans = {
                                route = AppRoute.WORKOUT_PLANS
                            },
                            onOpenExerciseLibrary = {
                                route = AppRoute.EXERCISE_LIBRARY
                            },
                            onOpenProgress = {
                                route = AppRoute.PROGRESS
                            },
                            onOpenAICoach = {
                                route = AppRoute.AI_COACH
                            },
                            onOpenProfile = {
                                route = AppRoute.PROFILE
                            },
                            onOpenFormAnalysis = {
                                route = AppRoute.FORM_ANALYSIS
                            },
                            onOpenAIPlan = {
                                route = AppRoute.AI_PLAN
                            },
                            onOpenSubscription = {
                                route = AppRoute.SUBSCRIPTION
                            },
                            onOpenAdmin = {
                                route = AppRoute.ADMIN
                            }
                        )
                    }

                    else -> {
                        route = AppRoute.LOGIN
                    }
                }
            }
        }

        workoutController.selectWorkout(
            "beginner_workout"
        )
    }
}
