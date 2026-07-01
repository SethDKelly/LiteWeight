# LiteWeight

Personal workout tracking Android app — offline-first, concept-oriented design.

## Requirements

- Android Studio Ladybug (2024.2.1) or newer
- **JDK 17 or 21** for Gradle (Android Studio’s bundled JBR works; JDK 26 is not supported by the current Kotlin toolchain)
- Android SDK 35

If `gradlew` fails with a Java version error, set `JAVA_HOME` to Android Studio’s JBR, for example:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

## SDK levels

| Setting | Value |
|---------|--------|
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 35 (Android 15) |
| `compileSdk` | 35 |

## Run locally

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Open the project in Android Studio and run the `app` configuration on an emulator or device.

## Project layout

```text
app/src/main/java/com/liteweight/
  core/         # DI, database, navigation, theme
  exercise/     # ExerciseKind domain + Room (Phase 1 expands here)
  session/      # Workout logging (Phase 1)
  program/      # Routines & splits (Phase 2)
  progression/  # Analytics projections (Phase 4+)
  home/         # Entry UI
```

## Documentation

See [`docs/README.md`](docs/README.md) for planning documents and implementation phases.

## Current status

**Phase 0 complete:** Gradle project, Compose shell, Hilt, Room, navigation.

**Phase 1 (in progress):** Rich exercise catalog seeding, exercise search/detail/create, workout logging, history, kg/lb settings.
