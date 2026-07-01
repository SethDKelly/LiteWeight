# LiteWeight Documentation Index

Use this index as the starting point for planning and implementation.

**Platform:** `minSdkVersion` **26** (Android 8.0 Oreo) · `targetSdkVersion` **35** (Android 15) · see `PROJECT_GUIDANCE.md` §2.

## Implementation status (as of Phase 5)

Phases **0–5** are shipped in the `main` branch. Room schema **v6**. Unit tests cover catalog models, progression evaluation, analytics metrics/PR detection, rotation resolution, and routine generation.

| Area | Document | Implemented |
|------|----------|-------------|
| Vision & concepts | `PROJECT_GUIDANCE.md` | Reference (living doc) |
| Phase roadmap | `IMPLEMENTATION_PLAN.md` | Phases 0–5 ✅ · 6–7 pending |
| Persistence | `DATA_MODEL_AND_STORAGE.md` | Room v6 tables (see §7) |
| Exercise naming | `EXERCISE_NAMING_FRAMEWORK.md` | Structured + free-form create |
| Exercise catalog | `EXERCISE_CATALOG.md` | v2 bundle (63 exercises) |
| Analytics & progression | `ANALYTICS_AND_PROGRESSION.md` | Rule-based projections + charts |

**Not yet implemented:** on-device ML insights (Phase 6), rest timer, free-session mode, export/backup, onboarding, and production-safe Room migrations (Phase 7).

## Core Documents

- `PROJECT_GUIDANCE.md`  
  Product direction, concept-oriented design, and capability requirements.

- `IMPLEMENTATION_PLAN.md`  
  High-level roadmap, phases, workstreams, and delivery checkpoints.

- `DATA_MODEL_AND_STORAGE.md`  
  Storage architecture, entity model, schema outline, and data lifecycle.

- `EXERCISE_NAMING_FRAMEWORK.md`  
  Standard descriptive pattern and free-form path for user-created exercises.

- `EXERCISE_CATALOG.md`  
  Rich built-in exercise database (classification, instructions, muscles); ExRx-informed seeding.

- `ANALYTICS_AND_PROGRESSION.md`  
  Progression metrics, PR rules, projection pipeline, and chart requirements.

- `BACKLOG.md`  
  Future features and ideas not yet in the phase plan (backup/sync, polish items, etc.).

## Suggested Reading Order

1. `PROJECT_GUIDANCE.md`
2. `IMPLEMENTATION_PLAN.md`
3. `DATA_MODEL_AND_STORAGE.md`
4. `EXERCISE_NAMING_FRAMEWORK.md`
5. `EXERCISE_CATALOG.md`
6. `ANALYTICS_AND_PROGRESSION.md`
7. `BACKLOG.md` (when planning post–Phase 5 work)

## Code map (packages)

```text
com.liteweight/
  core/          database, DI, navigation, startup
  exercise/      ExerciseKind + catalog
  session/       WorkoutSession logging
  program/       Program, presets, active program
  progression/   ProgressionScheme, levels
  analytics/     projections, PRs, charts
  movement/      MovementSlot roles
  substitution/  SubstitutionGroup members
  rotation/      RotationPlan cadence
  generator/     RoutineGenerator templates
```
