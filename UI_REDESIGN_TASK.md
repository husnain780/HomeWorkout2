# HomeWorkout Premium UI Redesign â€” Execute in this repository

Do the work in this workspace; do not just return code snippets. Inspect the actual source before editing. Make a timestamped backup under %TEMP% of every source file you will change. This folder may not be a Git repository.

## Goals
1. Redesign the whole app UI into one high-end, cohesive fitness product, taking the supplied reference screenshot as direction: clean off-white surfaces, deep navy text, emerald/mint accents, excellent spacing, consistent typography, crisp icons, well-composed exercise images, restrained borders/shadows, rounded cards and accessible touch targets. Avoid generic placeholder dashboards, overly decorative gradients, emoji-heavy icons, and fake analytics.
2. Fix Logout reliably.
3. Expand the exercise library to exactly 30 distinct exercises with local imagery.

## Preserve all functionality
- Keep MVC/event-driven structure and existing Firebase auth, AI Coach/AI Plan controllers, billing, cloud/admin authorization, offline storage, workout history, workout timer/rest/pause/skip/completion, CameraX and ML Kit pose detection intact.
- Keep existing composable signatures and callback contracts unless logout needs a new callback; update every call site when needed.
- Do not change package/application ID, Firebase configuration, Gradle versions, or introduce dependencies unless absolutely necessary.
- Never replace real service/repository behavior with mocks, never invent cloud metrics, and do not break existing exercise IDs referenced by workout plans or saved history.

## Screens to redesign consistently
Splash, Onboarding, Login, Register, Home, Workout Plans, Exercise Library, Exercise Details, Active Workout, Workout Completion, Progress/Analytics, AI Coach, Profile/Settings, Admin Dashboard, Subscription, Notifications, Form Analysis, and AI Plan Generation.

Home dashboard should match the reference direction: friendly greeting/profile, mint featured workout hero with exercise image and existing start callback, compact real-data stat cards, consistency/goals card, curated shortcuts, premium entry point, and five-item bottom navigation connected to working routes. Use actual WorkoutHistoryRepository data where available; show useful empty states instead of fabricated stats.

Keep every screen's functional internals. Do not remove camera analysis, timer states, AI controller calls, billing listeners, Firebase login/register, cloud role checks, or notification actions just to simplify UI.

## Logout must work
Trace the actual ProfileScreen and MainActivity navigation used in this repo. Add a prominent, styled Logout item and confirmation dialog. On confirmation call FirebaseAuth.getInstance().signOut(), clear logged-in/loading/error state, reset active/completed workout flags safely, and navigate to Login immediately without restarting the app. Make sure stale remembered route/state cannot send the user back Home after sign-out. Preserve email/password and Google sign-in flows.

## Exactly 30 exercises with local images
Expand the current ExerciseRepository/catalog to exactly 30 distinct entries while retaining the IDs of the current six exercises used by plans: push_up, squat, plank, jumping_jack, lunges, mountain_climber. Include these types of exercise: Push Up, Bodyweight Squat, Plank, Jumping Jacks, Forward Lunges, Mountain Climbers, Glute Bridge, Biceps Curl, Shoulder Press, Triceps Dip, Calf Raise, Burpee, High Knees, Bicycle Crunch, Dead Bug, Bird Dog, Russian Twist, Side Plank, Reverse Lunge, Wall Sit, Incline Push Up, Diamond Push Up, Superman, Hip Hinge, Donkey Kick, Fire Hydrant, Step Up, Lateral Raise, Flutter Kicks, Standing Knee Raise.
Give each a unique ID, category, difficulty, duration/calorie fields matching the existing model, actionable instructions, and a dedicated local image. Reuse existing images for the original six. For the rest, add local drawable/vector exercise illustrations consistent with the app; do not use runtime network URLs or repeat one identical picture for every entry. Ensure all 30 are searchable/filterable and open existing exercise details.

## Workflow
1. Inspect current APIs, data structures, resource names and routes before editing.
2. Back up every file before changing it.
3. Implement the shared design system and redesign screens while preserving behavior.
4. Fix the real logout route.
5. Add the 30 exercise records and local illustrations without breaking plan references.
6. Run ` .\gradlew.bat assembleDebug ` at project root. Fix all errors caused by changes and rerun.
7. Only after a successful build, if an ADB device is available, run `adb install -r .\app\build\outputs\apk\debug\app-debug.apk`.
8. Review changed files and make sure unrelated files were not edited. Do not claim tests/build/install passed unless output confirms it.

At the end, summarize changed files and the actual build/install result.
