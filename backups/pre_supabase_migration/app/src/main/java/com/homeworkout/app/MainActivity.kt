package com.homeworkout.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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
import com.homeworkout.app.ui.HwBottomNav
import com.homeworkout.app.ui.HwNavTab
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
    private var route by mutableStateOf(AppRoute.SPLASH)
    private var returnRoute by mutableStateOf(AppRoute.HOME)
    private var selectedExercise by mutableStateOf<ExerciseItem?>(null)
    private var authLoading by mutableStateOf(false)
    private var authError by mutableStateOf<String?>(null)

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("KEY_ROUTE", route.name)
        outState.putString("KEY_RETURN_ROUTE", returnRoute.name)
        outState.putBoolean("KEY_WORKOUT_STARTED", workoutStarted)
        outState.putBoolean("KEY_WORKOUT_COMPLETED", workoutCompleted)
        outState.putString("KEY_ACTIVE_WORKOUT_ID", activeWorkoutId)
        outState.putString("KEY_SELECTED_EXERCISE_ID", selectedExercise?.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            workoutStarted = savedInstanceState.getBoolean("KEY_WORKOUT_STARTED", false)
            workoutCompleted = savedInstanceState.getBoolean("KEY_WORKOUT_COMPLETED", false)
            activeWorkoutId = savedInstanceState.getString("KEY_ACTIVE_WORKOUT_ID") ?: "beginner_workout"
            val rName = savedInstanceState.getString("KEY_ROUTE")
            route = rName?.let { runCatching { AppRoute.valueOf(it) }.getOrNull() } ?: AppRoute.HOME
            val retName = savedInstanceState.getString("KEY_RETURN_ROUTE")
            returnRoute = retName?.let { runCatching { AppRoute.valueOf(it) }.getOrNull() } ?: AppRoute.HOME
            val exId = savedInstanceState.getString("KEY_SELECTED_EXERCISE_ID")
            selectedExercise = exId?.let { id ->
                ExerciseRepository.exercises.firstOrNull { it.id == id }?.let { e ->
                    ExerciseItem(
                        id = e.id,
                        name = e.name,
                        category = e.category,
                        difficulty = e.difficulty,
                        durationSeconds = e.durationSeconds,
                        calories = e.calories,
                        imageRes = e.imageRes,
                        description = e.description,
                        instructions = e.instructions
                    )
                }
            }
        }

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
                var isLoggedIn by remember {
                    mutableStateOf(
                        FirebaseAuth.getInstance().currentUser != null
                    )
                }

                LaunchedEffect(Unit) {
                    if (savedInstanceState == null && route == AppRoute.SPLASH) {
                        delay(2000)
                        route =
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                isLoggedIn = true
                                AppRoute.HOME
                            } else {
                                AppRoute.ONBOARDING
                            }
                    } else if (route == AppRoute.SPLASH) {
                        route =
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                isLoggedIn = true
                                AppRoute.HOME
                            } else {
                                AppRoute.ONBOARDING
                            }
                    }
                }

                val navTabs = setOf(
                    AppRoute.HOME,
                    AppRoute.WORKOUT_PLANS,
                    AppRoute.EXERCISE_LIBRARY,
                    AppRoute.PROGRESS,
                    AppRoute.AI_COACH,
                    AppRoute.PROFILE
                )
                @OptIn(ExperimentalLayoutApi::class)
                val isImeVisible = WindowInsets.isImeVisible
                val showBottomBar = isLoggedIn && !workoutStarted && !workoutCompleted && !isImeVisible && route in navTabs

                BackHandler(
                    enabled = route !in setOf(AppRoute.HOME, AppRoute.LOGIN, AppRoute.SPLASH, AppRoute.ONBOARDING)
                ) {
                    when (route) {
                        AppRoute.EXERCISE_DETAILS -> {
                            selectedExercise = null
                            route = AppRoute.EXERCISE_LIBRARY
                        }
                        AppRoute.NOTIFICATIONS -> {
                            route = returnRoute
                        }
                        else -> {
                            route = AppRoute.HOME
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            val currentTab = when (route) {
                                AppRoute.HOME -> HwNavTab.Home
                                AppRoute.WORKOUT_PLANS, AppRoute.EXERCISE_LIBRARY -> HwNavTab.Workouts
                                AppRoute.PROGRESS -> HwNavTab.Progress
                                AppRoute.AI_COACH -> HwNavTab.Coach
                                AppRoute.PROFILE -> HwNavTab.Profile
                                else -> HwNavTab.Home
                            }
                            HwBottomNav(
                                selected = currentTab,
                                onHome = { route = AppRoute.HOME },
                                onWorkouts = { route = AppRoute.WORKOUT_PLANS },
                                onProgress = { route = AppRoute.PROGRESS },
                                onCoach = { route = AppRoute.AI_COACH },
                                onProfile = { route = AppRoute.PROFILE }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {

                    workoutCompleted && isLoggedIn -> {
                        WorkoutCompletionScreen(
                            workoutId = activeWorkoutId,
                            onContinue = {
                                workoutCompleted = false
                                route = AppRoute.HOME
                            }
                        )
                    }

                    workoutStarted && isLoggedIn -> {
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
                        BackHandler {
                            finish()
                        }

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

                    route == AppRoute.WORKOUT_PLANS && isLoggedIn -> {
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

                    route == AppRoute.EXERCISE_LIBRARY && isLoggedIn -> {
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

                    route == AppRoute.EXERCISE_DETAILS && isLoggedIn -> {
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

                    route == AppRoute.PROGRESS && isLoggedIn -> {
                        ProgressScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.AI_COACH && isLoggedIn -> {
                        AICoachScreen(
                            onBack = {
                                route = AppRoute.HOME
                            },
                            bottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }

                    route == AppRoute.PROFILE && isLoggedIn -> {
                        ProfileScreen(
                            onBack = {
                                route = AppRoute.HOME
                            },
                            onOpenNotifications = {
                                returnRoute = AppRoute.PROFILE
                                route = AppRoute.NOTIFICATIONS
                            },
                            onLogout = {
                                authController.logout()
                                workoutStarted = false
                                workoutCompleted = false
                                selectedExercise = null
                                authLoading = false
                                authError = null
                                isLoggedIn = false
                                returnRoute = AppRoute.HOME
                                route = AppRoute.LOGIN
                            },
                            onOpenSubscription = {
                                route = AppRoute.SUBSCRIPTION
                            },
                            onOpenHome = {
                                route = AppRoute.HOME
                            },
                            onOpenPlans = {
                                route = AppRoute.WORKOUT_PLANS
                            },
                            onOpenProgress = {
                                route = AppRoute.PROGRESS
                            },
                            onOpenAICoach = {
                                route = AppRoute.AI_COACH
                            }
                        )
                    }

                    route == AppRoute.ADMIN && isLoggedIn -> {
                        AdminDashboardScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.SUBSCRIPTION && isLoggedIn -> {
                        SubscriptionScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.NOTIFICATIONS && isLoggedIn -> {
                        NotificationsScreen(
                            onBack = {
                                route = returnRoute
                            }
                        )
                    }

                    route == AppRoute.FORM_ANALYSIS && isLoggedIn -> {
                        FormAnalysisScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.AI_PLAN && isLoggedIn -> {
                        AIPlanGenerationScreen(
                            onBack = {
                                route = AppRoute.HOME
                            }
                        )
                    }

                    route == AppRoute.HOME && isLoggedIn &&
                        FirebaseAuth.getInstance().currentUser != null -> {
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
                        isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                        if (!isLoggedIn) {
                            workoutStarted = false
                            workoutCompleted = false
                            route = AppRoute.LOGIN
                        } else {
                            route = AppRoute.HOME
                        }
                    }
                }
            }
        }
    }
}

        workoutController.selectWorkout(
            "beginner_workout"
        )
    }
}
