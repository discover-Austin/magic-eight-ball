# CLAUDE.md

Guide for AI assistants working on the Magic Eight Ball Android app.

## Project Overview

Android application (Kotlin) implementing a Magic Eight Ball with intelligent keyword-based sentiment weighting. The ball analyzes questions for positive, negative, and uncertain keywords, then biases the response pool toward matching sentiments. Includes session history with share functionality and haptic feedback.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM, no device needed)
./gradlew :app:test

# Run Android instrumentation tests (requires emulator/device)
./gradlew :app:connectedAndroidTest

# Full build
./gradlew build
```

## Project Structure

```
app/src/main/
  kotlin/com/example/magiceightball/
    EightBall.kt         # Core logic: 20 responses, expanded keyword sets, group-normalized weighting
    MainActivity.kt      # UI controller: view binding, animations, history, share, haptic feedback
    ShakeDetector.kt     # Accelerometer shake gesture detection
    HistoryEntry.kt      # Data class for question–response history pairs
    HistoryAdapter.kt    # RecyclerView adapter for session history list
  res/
    layout/
      activity_main.xml  # Main screen: ball, input, history RecyclerView
      item_history.xml   # History list item: question, response, share button
    drawable/            # Ball shapes, input background, share/clear vector icons
    values/              # colors.xml, strings.xml, themes.xml

app/src/test/
  kotlin/com/example/magiceightball/
    EightBallTest.kt     # JVM unit tests (JUnit 4): pool, bias, normalization, keywords, history

build.gradle             # Root: plugin versions (AGP 8.8.2, Kotlin 2.0.21)
app/build.gradle         # App module: SDK versions, dependencies, viewBinding
settings.gradle          # Gradle settings, repository config
```

## Tech Stack

- **Language**: Kotlin 2.0.21
- **Min SDK**: 21 (Android 5.0) / **Target & Compile SDK**: 35
- **Build**: Gradle 8.11.1 with Android Gradle Plugin 8.8.2
- **JVM target**: 17
- **UI**: ViewBinding, ConstraintLayout, RecyclerView, Material Design Components
- **Tests**: JUnit 4.13.2 (unit), Espresso 3.6.1 (instrumentation)

## Architecture

The app follows a simple MVC-like pattern with five classes:

- **`EightBall`** -- Stateless logic class. `ask(question)` applies group-normalized keyword weighting; `shake()` returns a uniformly random response. Contains `Response` data class, `Sentiment` enum, and three expanded keyword sets (~60 positive, ~50 negative, ~30 uncertain keywords including morphological variants).
- **`MainActivity`** -- View controller. Uses ViewBinding for type-safe view access. Handles button clicks, keyboard IME actions, ball taps, and shake callbacks. Manages session history, share intents, haptic feedback, and flip/pulse animations.
- **`ShakeDetector`** -- Implements `SensorEventListener`. Monitors accelerometer for >2.7g force with 500ms debounce. Lifecycle-aware via `start()`/`stop()` called from `onResume()`/`onPause()`.
- **`HistoryEntry`** -- Data class holding a question string, `Response`, and timestamp. Used by `HistoryAdapter` to display session history.
- **`HistoryAdapter`** -- `RecyclerView.Adapter` with ViewBinding (`ItemHistoryBinding`). Shows question, colour-coded response, and per-item share button. Supports `addEntry()` (insert at top) and `clear()`.

## Code Conventions

- **Classes**: PascalCase (`EightBall`, `ShakeDetector`)
- **Functions/variables**: camelCase (`revealAnswer`, `positiveHits`)
- **Constants**: UPPER_SNAKE_CASE in companion objects (`SHAKE_THRESHOLD_GRAVITY`, `KEYWORD_BOOST`)
- **Resources**: snake_case (`response_positive`, `bg_ball_outer`, `ic_share`)
- **Static members**: Use Kotlin `companion object`
- **Data modeling**: Kotlin `data class` for value types, `enum class` for finite sets
- **No external linter** configured -- follow existing Kotlin style conventions

## Key Design Decisions

- **Group-normalized weighting**: Each sentiment group (positive/neutral/negative) gets an equal base weight of 1.0. Keyword hits boost the matching group's weight by `KEYWORD_BOOST` (2.0) per hit, then a random roll picks the sentiment group. This normalizes for the unequal response counts (10/5/5) so that negative and uncertain keywords are as effective as positive ones.
- **Expanded keyword vocabulary**: Each keyword set includes common morphological variants (e.g., "succeed", "success", "successful", "successfully") so the analysis recognises natural phrasing without needing a stemming library.
- **Session history**: In-memory `MutableList<HistoryEntry>` managed by `HistoryAdapter`. No persistence -- history clears on app restart. Newest entries appear at the top.
- **Share via intent**: Each history item has a share button that creates a `text/plain` `ACTION_SEND` intent with a formatted question + answer string.
- **Haptic feedback**: `performHapticFeedback(VIRTUAL_KEY)` on the ball container when an answer is revealed. No VIBRATE permission needed.
- **No network dependencies**: The app is fully offline with all responses hardcoded.
- **Graceful sensor degradation**: Devices without an accelerometer get a modified hint instead of a crash.
- **ViewBinding over findViewById**: All view references use generated binding classes for compile-time safety.

## Testing Notes

- Tests are JVM-only (no Android device required) and cover:
  - Response pool composition (20 responses, 10/5/5 split)
  - Blank question handling and uniform distribution
  - Keyword bias verification (statistical, 200+ trials per test)
  - Normalization verification (negative keywords as effective as positive)
  - Morphological variant recognition ("successful", "failure", "doubtful", etc.)
  - Keyword set sanity (no overlap between sets, all lowercase)
  - HistoryEntry data class construction
  - Non-determinism and response text validity
- Bias tests use statistical sampling with conservative thresholds to avoid flakiness.
- No instrumentation tests are currently written despite Espresso being in dependencies.

## Dependencies

All dependencies are AndroidX / Google official libraries. No third-party libraries are used:

| Dependency | Purpose |
|---|---|
| `core-ktx` | Kotlin extensions for Android framework |
| `appcompat` | Backward-compatible Activity, themes |
| `material` | Material Design UI components (includes RecyclerView) |
| `constraintlayout` | Flexible layout system |
| `junit` | Unit test framework |
| `espresso-core` | UI test framework (available, unused) |
