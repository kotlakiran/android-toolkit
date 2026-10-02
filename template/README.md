# app-template

Clonable Android app skeleton wired to the toolkit. Builds and runs as-is.

## New app in 6 steps

1. Copy this `template/` folder to your app's location.
2. In `template/settings.gradle.kts`: point `includeBuild("..")` at the toolkit
   checkout — or delete it, add Jitpack, and use
   `com.github.kotlakiran.android-toolkit:<module>:<tag>` coordinates in
   `app/build.gradle.kts`.
3. Rename the package: `namespace`/`applicationId` in `app/build.gradle.kts` and
   move the sources directory.
4. Change `android:label` in the manifest and the title in `MainActivity`.
5. Pick your theme: `ProvideAppTokens(DarkTokens)` or `LightTokens` (or your own
   `AppTokens`).
6. Replace the three demo screens with your tabs. Coach tab needs
   `google-services.json` + the google-services plugin for real AI answers.

## Build

From this directory: `../gradlew :app:assembleDebug` (uses the toolkit wrapper),
or add the standard Gradle wrapper files when copying to a standalone repo.
