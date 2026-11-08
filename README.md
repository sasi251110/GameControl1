# GameControl

Turn an Android phone into a customizable PlayStation-style game controller for a PC.

> Your Phone. Your Controller. Your Game.

## Status

Phase 1: virtual controller UI (Android, Jetpack Compose, landscape).
No networking yet. The PC companion and Wi-Fi connection come in later phases.

## Repository layout

```
app/                     Android app (Kotlin, Jetpack Compose)
  src/main/java/...
    model/               Controller button enum and UI state
    ui/controller/       Controller screen, D-pad, sticks, face buttons
    ui/theme/            Colors, typography, theme
docs/
  design/
    DESIGN.md            Design system (colors, type, spacing, components)
    stitch/              Stitch screen exports (HTML + PNG reference)
    logo/                Logo artwork
  FONTS.md               How to add the design fonts
.github/workflows/
  android.yml            Lint, unit tests, debug APK, tagged releases
```

## Build

Requirements: JDK 17, Android Studio (or Android SDK), Android 8.0+ device.

- Android Studio: open the folder, sync Gradle, press Run.
- Command line:
  ```bash
  gradle wrapper --gradle-version 8.9   # once, creates gradlew
  ./gradlew assembleDebug
  ```

The GitHub Actions workflow creates the Gradle wrapper automatically if it is missing.

## Release

Push a tag such as `v0.1.0`. The workflow builds the APK and publishes it on the Releases page.

## Roadmap

1. Android controller UI (done in this repo)
2. Input engine and controller tester
3. Custom mapping and profiles
4. Windows companion and Wi-Fi connection
5. Bluetooth (experimental)
6. Linux companion
