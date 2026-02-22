# Magic Eight Ball 🎱

An Android app that brings the classic Magic Eight Ball to your phone — with a twist: the ball is **intelligent** and weighs its responses based on the sentiment of your question.

## Features

- **Intelligent responses** – detects positive, negative, and uncertain keywords in your question and biases the answer pool accordingly
- **All 20 classic answers** – the full set of positive, neutral, and negative Magic 8-Ball responses
- **Shake to ask** – shake your device to get a random answer (no question needed)
- **Tap to ask** – tap the ball for a response based on your typed question
- **Animated reveal** – smooth flip animation shows each answer
- **Colour-coded answers** – green for positive, amber for neutral, red for negative

## Screenshots

| Home screen | Answer revealed |
|---|---|
| *(launch the app to see)* | *(ask a question)* |

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── kotlin/com/example/magiceightball/
│   ├── EightBall.kt        # Core logic: 20 responses + keyword-based weighting
│   ├── ShakeDetector.kt    # Accelerometer shake gesture detection
│   └── MainActivity.kt     # UI controller with animations
└── res/
    ├── layout/activity_main.xml
    ├── values/strings.xml
    ├── values/colors.xml
    ├── values/themes.xml
    └── drawable/           # Ball and input background shapes
```

## How It Works

`EightBall.ask(question)` splits the question into words and counts hits against three keyword sets:

| Keyword set | Effect |
|---|---|
| `love`, `happy`, `good`, `succeed`, … | Boosts **positive** responses |
| `fail`, `bad`, `wrong`, `lose`, … | Boosts **negative** responses |
| `maybe`, `perhaps`, `possibly`, … | Boosts **neutral** responses |

Each matching keyword adds 2 extra copies of every response in that sentiment group to the selection pool, so the answer still surprises you while feeling contextually relevant.

## Requirements

| Tool | Version |
|---|---|
| Android Studio | Hedgehog or newer |
| Android SDK | API 35 (compile), API 21 (min) |
| Kotlin | 2.0.x |
| Gradle | 8.11.1+ |

## Getting Started

```bash
# Clone
git clone https://github.com/discover-Austin/magic-eight-ball.git
cd magic-eight-ball

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

Open the project in Android Studio and run it on an emulator or physical device.

## Running Tests

```bash
./gradlew :app:test
```

The JVM unit tests in `EightBallTest.kt` verify the response pool composition, keyword bias, and non-determinism without needing an Android device.