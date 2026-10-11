# UI & UX Design Research — HomeWorkout2

This document records the official guidelines, mobile responsive layout patterns, and commercial fitness app design principles synthesized for the HomeWorkout2 application.

---

## 1. Official Android Guidelines & References

1. **Android Responsive & Adaptive Layouts**
   * *Source*: [Android Developers — Support different display sizes](https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes)
   * *Core Principle*: Move away from hardcoded device assumptions. Design for the available window space. Use flexible containers, dynamic column counts, and responsive spacers.
   * *Application in HomeWorkout2*:
     - Dynamic height cards that scale smoothly from compact 320dp screens to 430dp+ flagships.
     - Content-driven sizing instead of rigid fixed-height cards that cause text and artwork clipping.
     - Respecting system window insets (`statusBarsPadding()`, `navigationBarsPadding()`, `imePadding()`) to avoid overlap with gesture bars and camera cutouts.

2. **Android Window Size Classes**
   * *Source*: [Android Developers — Use window size classes](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes)
   * *Core Principle*: Breakpoints for `Compact` (<600dp width), `Medium` (600–840dp), and `Expanded` (>840dp).
   * *Application in HomeWorkout2*:
     - Compact phones (~320–393dp): 1-column layout, bottom navigation bar, vertical scrolling with `LazyColumn` or managed scroll containers.
     - Landscape / short viewport: Compress vertical padding, maintain accessible controls, scrollable sheet dialogs.

3. **Android Mobile Layout & Navigation Patterns**
   * *Source*: [Android Design — Mobile layout and navigation patterns](https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns)
   * *Core Principle*: Primary actions must be easily reachable (bottom third of screen / Thumb Zone). Touch targets must be at least 48x48dp.
   * *Application in HomeWorkout2*:
     - All primary CTA buttons (Start Workout, Complete, Send, Login) are at least 48–52dp in height with generous horizontal padding.
     - Bottom navigation bar elevated above system gesture insets with clear active indicators.

---

## 2. Professional Fitness App Benchmarking (Nike Training Club, Apple Fitness+, Strava)

1. **Visual Hierarchy & Rhythm**
   * *Industry Standard*: High-contrast typography where numbers (seconds remaining, streak count, reps, calories) are bold and readable from 1–2 meters away.
   * *Palette*: Pristine white/off-white canvas (`#F7FAF8`, `#FFFFFF`), deep navy typography (`#0F172A`), vibrant emerald green primary accents (`#10B981`, `#059669`), and refreshing mint containers (`#E8F5EE`).
   * *Avoidance*: No generic SaaS look, no random emojis for primary icons, no harsh neon gradients.

2. **Hero Workout Presentation**
   * *Industry Standard*: The "Today's Session" hero card combines a motivating title, difficulty/duration chips, and high-fidelity exercise artwork without cramped layout boundaries.
   * *Implementation*: Proportional image scaling (`ContentScale.Fit` with subtle alpha blending) and flexible copy container allowing titles to wrap naturally.

3. **Active Workout Player**
   * *Industry Standard*: Immersive, high-contrast, distraction-free player. Giant digital timer, clear distinction between WORK (active energy) and REST (calming recovery), with large pause/resume and skip buttons that cannot be mis-tapped.
   * *Implementation*: `WorkoutScreen.kt` with dynamic circular countdown ring, prominent exercise illustration, and fail-safe finish actions.

4. **AI Coaching Experience**
   * *Industry Standard*: Contextual conversational intelligence that acts as a knowledgeable personal coach and versatile problem solver, rather than a robotic keyword matcher.
   * *Implementation*: Multiline composer that lifts with IME keyboard, formatted code blocks and structured reasoning, general-purpose prompt chips (Coding, Math, Writing, Study, Fitness), and direct Gemini integration without canned refusal fallbacks.

5. **Iconography & Polish**
   * *Industry Standard*: Consistent vector glyphs and geometric Compose Canvas rendering for all navigation and action items.
   * *Implementation*: Clean vector iconography across Home, Library, Active Player, and Profile screens.
