# Olvex SDK

> Crash reporting and analytics for Kotlin Multiplatform apps — one SDK for Android and iOS.

[![Maven Central](https://img.shields.io/maven-central/v/dev.olvex/sdk)](https://central.sonatype.com/artifact/dev.olvex/sdk)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue)](https://kotlinlang.org/docs/multiplatform.html)

---

## Why Olvex?

Firebase Crashlytics and Sentry require separate SDKs for Android and iOS. In a KMP project that means two integrations, two dashboards, and stack traces that don't understand your `commonMain` code.

Olvex is built for KMP from the ground up:

- **One SDK** — add it once in `commonMain`, works on Android and iOS
- **KMP-aware stack traces** — crashes map back to your shared Kotlin code
- **Crashes + analytics together** — session context right next to the crash
- **Real-time dashboard** — no 24-48h delay like Firebase Analytics
- **Self-hostable** — your data stays yours

---

## Quick Start Steps

1. Clone this repo
2. Open in Android Studio
3. Paste your API key in `composeApp/src/commonMain/kotlin/dev/olvex/DemoConfig.kt`
4. Run on Android emulator or device
5. Open your [dashboard](https://olvex.dev/app) and watch events appear

## Installation

Add to your `shared` module `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation("dev.olvex:sdk:0.1.0-alpha8")
}
```

---

## Setup

### Android

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidOlvex.init(
            context = this,
            apiKey = "olvex_your_api_key"
        )
    }
}
```

### iOS

In your `MainViewController.kt` (iosMain):

```kotlin
fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit) {
        Olvex.init(apiKey = "olvex_your_api_key")
    }
    App()
}
```

Or call from Swift in `AppDelegate`:

```swift
import ComposeApp

func application(_ application: UIApplication,
                 didFinishLaunchingWithOptions ...) -> Bool {
    Olvex.shared.init(apiKey: "olvex_your_api_key")
    return true
}
```

---

## Usage

### Track custom events

```kotlin
// commonMain — works on both platforms
Olvex.track("button_clicked", mapOf(
    "screen" to "home",
    "variant" to "A"
))
```

### Session tracking

Sessions are tracked automatically on init. You can also control them manually:

```kotlin
Olvex.startSession()
Olvex.endSession()
```

### Crash reporting

Crashes are captured automatically. On next app launch they are sent to the dashboard with full stack trace and session context.

No extra setup needed — just call `init()`.

---

## Get your API key

1. Sign up at [olvex.dev](https://olvex.dev)
2. Create a project in the dashboard
3. Copy your API key

---

## Dashboard

After integration, open your project dashboard at [olvex.dev/app](https://olvex.dev/app) to see:

- Crash reports with stack traces
- Session count and duration
- Custom events with filters by type, device, OS version, app version
- Timeline of events

---

## Demo app

Clone this repo and run the test bench to see the SDK in action:

```bash
git clone https://github.com/Olvex-dev/olvex-demo
cd olvex-demo
```

Open in Android Studio, paste your API key in `composeApp/src/commonMain/kotlin/dev/olvex/DemoConfig.kt`, and run.

The test bench lets you:
- Start/end sessions
- Send custom events (including events with properties)
- Trigger a Kotlin crash and watch it appear in the dashboard after restart
- Trigger `SIGABRT` native crash on iOS to verify signal-based capture

---

## Roadmap

- [x] Crash reporting (Android)
- [x] Session tracking
- [x] Custom events
- [x] Persistent crash storage (survives app kill)
- [x] Dashboard with filters and charts
- [ ] Crash reporting (iOS)
- [ ] KMP stack trace symbolication
- [ ] dSYM upload for iOS
- [ ] Breadcrumbs
- [ ] User identification
- [ ] Alerts (email / Slack)
- [ ] Event caps / spend limits

---

## License

```
Copyright 2026 Olvex

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
