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

## Open in Android Studio

1. **File → Open** and select this repository root.
2. Wait for Gradle sync (Android Studio creates `local.properties` with your SDK path automatically).
3. Run the **`app`** configuration on an emulator or device (API 26+).

## Run locally

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew testDebugUnitTest
```

On Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug testDebugUnitTest
```

CI runs `assembleDebug`, unit tests, and `lintDebug` on push to `main` (see `.github/workflows/android.yml`).

## Project layout

```text
app/src/main/java/com/liteweight/
  core/         # DI, Room database, navigation, theme, app startup
  exercise/     # ExerciseKind catalog, search, create, detail
  session/      # Workout logging, history, active workout
  program/      # Programs, presets, active program, prefill
  progression/  # Progression schemes, levels, auto-advance rules
  analytics/    # Session projections, PR detection, charts
  movement/     # Movement slots (abstract exercise roles)
  substitution/ # Substitution groups and swap suggestions
  rotation/     # Rotation plans and cadence resolver
  generator/    # Rule-based routine generator
  home/         # Entry screen
  settings/     # User preferences (e.g. kg/lb)

app/src/main/assets/catalog/
  exercise_catalog_v2.json   # 63 built-in exercises
  preset_programs_v1.json    # 3 installable preset programs

tools/catalog/                # Dev-time catalog build scripts
docs/                         # Planning and architecture documents
```

## Documentation

See [`docs/README.md`](docs/README.md) for the full documentation index, reading order, and implementation status.

## Current status

**Phases 0–5 are implemented.** Room schema **v6**. Next planned work: Phase 6 (ML-assisted insights) and Phase 7 (polish and release readiness). See [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md).

| Phase | Status | Highlights |
|-------|--------|------------|
| 0 — Foundation | ✅ | Compose shell, Hilt, Room, navigation, CI |
| 1 — Core logging | ✅ | Catalog seeding, exercise CRUD, session logging, history, kg/lb |
| 1b — Catalog expansion | ✅ | 63-exercise v2 catalog, filter chips, muscle-based swap hints |
| 2 — Programs | ✅ | Custom programs, preset install, active program, prefill modes |
| 3 — Progression | ✅ | Linear 3-phase scheme, level display, auto-advance on session complete |
| 4 — Analytics | ✅ | Projections on session complete, progression chart, PR timeline, weekly count |
| 5 — Substitution & rotation | ✅ | Movement slots, substitution groups, rotation plans, routine generator |
| 6 — ML insights | ⏳ | Not started |
| 7 — Polish | ⏳ | Rest timer, onboarding, export/backup, release hardening |

**Note:** Schema bumps currently use `fallbackToDestructiveMigration()` during pre-release development. User data is wiped on upgrade until proper migrations are added before a store release.
