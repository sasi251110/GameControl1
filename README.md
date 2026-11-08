# GameControl

Turn an Android phone into a customizable PlayStation-style game controller for a PC.

> Your Phone. Your Controller. Your Game.

## Status

| Phase | Part | State |
|---|---|---|
| 1 | Android controller UI | Done |
| 2 | Input engine, tester, unit tests | Done |
| 3 | Windows companion receives input | In progress |
| 4 | Wi-Fi connection and pairing | In progress |
| 5 | Virtual Xbox 360 controller (ViGEmBus) | In progress, driver is archived upstream |
| 6 | Connection hardening | Planned |
| 7 | Mapping and profiles | Planned |
| 8 | Linux companion | Planned |
| 9 | Bluetooth (experimental) | Planned |
| 10 | USB investigation | Planned |

## Repository layout

```
app/                     Android app (Kotlin, Jetpack Compose)
  src/main/java/...
    input/               Input engine and tuning
    model/               Controller buttons and UI state
    network/             Wire protocol and PC connection
    ui/controller/       Controller screen
    ui/connect/          PC connection screen
    ui/tester/           Controller tester screen
    ui/theme/            Colors, typography, theme
windows/                 Windows companion (C#, .NET 8)
  src/GameControl.Core/       Protocol, pairing, server (no driver dependency)
  src/GameControl.Companion/  WinForms app and ViGEm adapter
  tests/                      Core unit tests
docs/
  PROTOCOL.md            Wire protocol and security limits
  design/                DESIGN.md, Stitch exports, logo
  FONTS.md               Adding the design fonts
.github/workflows/
  android.yml            Android and Windows CI, tagged releases
```

## Build

Android: JDK 17, then open the folder in Android Studio, or run `./gradlew assembleDebug`.
Windows: see [windows/README.md](windows/README.md).

## Release

Push a tag such as `v0.2.0`. The workflow builds the Android APK and publishes it on the Releases page.
