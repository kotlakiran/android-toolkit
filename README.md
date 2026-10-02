# android-toolkit

Shared Android library modules for fast app builds. Built from the parts that
Tally and Crop Season had to solve once and every future app needs again.

| Module | What it gives an app |
|---|---|
| `core-ui` | Theme tokens (dark/light), SectionCard, StatTile, MeterBar, SegmentedControl, AppListRow, FloatingBottomBar |
| `money-core` | Pure-Kotlin INR money (paise, Indian grouping, ₹L/₹Cr compact) + EMI math: fixed/floating/hybrid, amortization, prepay (extra-monthly / lump-sum), rate shock, refinance break-even, safe-EMI band |
| `ai-coach` | Firebase AI Logic wrapper (key stays server-side, App Check installer), safety settings, rule-engine fallback, CoachChat composable |
| `data-import` | PDF bank statement import (on-device text extraction), generic statement parser (salary/EMI/UPI tagging), CSV import, SAF pickers |
| `notif-capture` | NotificationListener with user-picked app allowlist, OTP/sensitive filter, consent-sheet composable, Play-safe: optional, revocable, PDF fallback |
| `template/` | Clonable app skeleton wired to the toolkit (not published) |

## Use from an app (Jitpack)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// app build.gradle.kts
implementation("com.github.kotlakiran.android-toolkit:core-ui:0.1.0")
implementation("com.github.kotlakiran.android-toolkit:money-core:0.1.0")
```

Versions are git tags. Tag a release, Jitpack builds it:
`git tag v0.1.0 && git push origin v0.1.0` → dependency version `0.1.0`.

## Local development (no publish needed)

```kotlin
// in the app's settings.gradle.kts
includeBuild("path/to/android-toolkit")
// then depend on the published coordinates:
implementation("com.kotlakiran.toolkit:core-ui:0.1.0")
```

Gradle substitutes the included build automatically. The `template/` app shows this working.

## ai-coach setup in the consuming app

1. Apply `com.google.gms.google-services` and add `google-services.json`.
2. Add `debugImplementation("com.google.firebase:firebase-appcheck-debug")` for debug builds.
3. `AppCheckInstaller.install(context, debug = BuildConfig.DEBUG)` in `Application.onCreate` before first `AiCoach` use.
4. App Check enforcement lives in Firebase Console (service `firebaseml.googleapis.com`);
   debug builds print a debug token to logcat on first run — register it in the console
   when enforcement is on.

## Design rules encoded here

- No SMS permission anywhere. Play only grants SMS to the default SMS handler.
- Notification capture is opt-in, app-allowlisted, revocable, and the app must
  stay fully usable via PDF import when declined.
- All money is `Long` paise. Never float currency.
- Money math is pure Kotlin (no Android deps) so it unit-tests on the JVM.
