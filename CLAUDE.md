# CLAUDE.md

Guide for AI assistants working on the Magic Eight Ball Android app.

## Project Overview

Android application (Kotlin) implementing a Magic Eight Ball with intelligent keyword-based sentiment weighting. The ball analyzes questions for positive, negative, and uncertain keywords, then biases the response pool toward matching sentiments.

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
    EightBall.kt         # Core logic: 20 responses, keyword sets, weighted pool selection
    MainActivity.kt      # UI controller: view binding, animations, input handling
    ShakeDetector.kt     # Accelerometer shake gesture detection
  res/
    layout/              # activity_main.xml (ConstraintLayout)
    drawable/            # Ball and input background XML shapes
    values/              # colors.xml, strings.xml, themes.xml

app/src/test/
  kotlin/com/example/magiceightball/
    EightBallTest.kt     # JVM unit tests for EightBall (JUnit 4)

build.gradle             # Root: plugin versions (AGP 8.8.2, Kotlin 2.0.21)
app/build.gradle         # App module: SDK versions, dependencies, viewBinding
settings.gradle          # Gradle settings, repository config
```

## Tech Stack

- **Language**: Kotlin 2.0.21
- **Min SDK**: 21 (Android 5.0) / **Target & Compile SDK**: 35
- **Build**: Gradle 8.11.1 with Android Gradle Plugin 8.8.2
- **JVM target**: 17
- **UI**: ViewBinding, ConstraintLayout, Material Design Components
- **Tests**: JUnit 4.13.2 (unit), Espresso 3.6.1 (instrumentation)

## Architecture

The app follows a simple MVC-like pattern with three classes:

- **`EightBall`** -- Stateless logic class. `ask(question)` applies keyword weighting; `shake()` returns a uniformly random response. Contains `Response` data class and `Sentiment` enum.
- **`MainActivity`** -- View controller. Uses ViewBinding for type-safe view access. Handles button clicks, keyboard IME actions, ball taps, and shake callbacks. Runs flip/pulse animations on answer reveal.
- **`ShakeDetector`** -- Implements `SensorEventListener`. Monitors accelerometer for >2.7g force with 500ms debounce. Lifecycle-aware via `start()`/`stop()` called from `onResume()`/`onPause()`.

## Code Conventions

- **Classes**: PascalCase (`EightBall`, `ShakeDetector`)
- **Functions/variables**: camelCase (`revealAnswer`, `positiveHits`)
- **Constants**: UPPER_SNAKE_CASE in companion objects (`SHAKE_THRESHOLD_GRAVITY`)
- **Resources**: snake_case (`response_positive`, `bg_ball_outer`)
- **Static members**: Use Kotlin `companion object`
- **Data modeling**: Kotlin `data class` for value types, `enum class` for finite sets
- **No external linter** configured -- follow existing Kotlin style conventions

## Key Design Decisions

- **Weighted pool approach**: Rather than calculating probabilities, keyword matches add 2 extra copies of matching-sentiment responses to the selection pool per keyword hit. This keeps the algorithm simple while providing proportional biasing.
- **No network dependencies**: The app is fully offline with all responses hardcoded.
- **Graceful sensor degradation**: Devices without an accelerometer get a modified hint instead of a crash.
- **ViewBinding over findViewById**: All view references use generated binding classes for compile-time safety.

## Testing Notes

- Tests are JVM-only (no Android device required) and cover: response pool composition, blank question handling, keyword bias verification (statistical over 200 trials), non-determinism, and response text validity.
- Bias tests use statistical sampling (200 iterations) with conservative thresholds to avoid flakiness.
- No instrumentation tests are currently written despite Espresso being in dependencies.

## Dependencies

All dependencies are AndroidX / Google official libraries. No third-party libraries are used:

| Dependency | Purpose |
|---|---|
| `core-ktx` | Kotlin extensions for Android framework |
| `appcompat` | Backward-compatible Activity, themes |
| `material` | Material Design UI components |
| `constraintlayout` | Flexible layout system |
| `junit` | Unit test framework |
| `espresso-core` | UI test framework (available, unused) |
