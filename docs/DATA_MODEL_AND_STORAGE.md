# LiteWeight Data Model and Storage

This document defines the high-level persistence strategy for exercises, routines/programs, sessions, and analytics source data.

## 1) Storage Approach

- Primary store: local relational database (`Room` over SQLite).
- Source-of-truth model:
  - write entities for domain concepts
  - read models for list/detail screens
  - projection tables for analytics
- Offline-first by default; optional sync can be layered later.

## 2) Core Entities (Concept-Aligned)

## `ExerciseKind`

- `id`
- `displayName` (generated or user override)
- `namingMode` (`structured`, `freeform`)
- **Structured facets** (nullable when `freeform`):
  - `equipment` (`barbell`, `dumbbell`, `cable`, `smith_machine`, `machine`, `kettlebell`, `bodyweight`, `band`, `other`)
  - `bodyPosition` (`flat_bench`, `incline`, `decline`, `seated`, `standing`, `lying`, `kneeling`, `unilateral`, `none`)
  - `primaryMovement` (canonical enum + optional `primaryMovementFreeform` when `other`)
  - `gripWidth` (`close`, `standard`, `wide`, `none`)
  - `gripOrientation` (`pronated`, `supinated`, `neutral`, `none`)
- `freeformName` (required when `namingMode = freeform`)
- `customQualifier` (optional suffix, e.g. “with chains”)
- `unitType` (`weight`, `time`, `bodyweight`, `distance`)
- `muscleTags` (normalized table or serialized tags)
- `isBuiltin`
- `isArchived`
- `createdAt`, `updatedAt`

**Name generation:** `displayName` is derived from structured facets per `EXERCISE_NAMING_FRAMEWORK.md` unless `customDisplayName` / `freeformName` is set.

**Indexes:** normalized `displayName` for search; composite index on structured facets for duplicate detection.

### `ExerciseKind` rich metadata (built-in catalog)

See `EXERCISE_CATALOG.md`.

- `catalogId` (stable id, e.g. `exrx:barbell-bench-press`; null for user-created)
- `catalogVersion` (bundle version seeded from)
- `referenceUrl` (optional link-out, e.g. ExRx exercise page)

### `ExerciseClassification` (1:1, built-in / optional user)

- `exerciseKindId`
- `utility`, `mechanics`, `force`, `difficulty`
- `exrxCategoryPath` (JSON array for traceability)

### `ExerciseInstruction` / `ExerciseComment` (1:N)

- `exerciseKindId`, `sortOrder`, `text`

### `MuscleVocabulary`

- `muscleSlug`, `displayName`, `bodyRegion`

### `ExerciseMuscleRole` (N:M)

- `exerciseKindId`, `muscleSlug`, `role` (`target`, `synergist`, `stabilizer`, `antagonist`, `other`)

### `AppMetadata`

- `installedCatalogVersion` (int; drives upgrade seeding)

## `Program`

- `id`
- `name`
- `sourceType` (`preset_installed`, `custom`)
- `splitType` (optional label, e.g. `PPL`, `UpperLower`, `Custom`)
- `revision`
- `isArchived`
- `createdAt`, `updatedAt`

## `ProgramDay` / `ProgramBlock`

- `programId`
- ordered session day/slot metadata
- block-level prescriptions (exercise refs, set/rep targets, RPE, notes)
- optional `movementSlotId` (abstract role) instead of fixed `exerciseKindId`

## `MovementSlot`

- `id`
- `roleKey` (e.g. `main_squat`, `accessory_push`)
- default `exerciseKindId` (nullable)
- muscle/equipment tags for substitution suggestions

## `SubstitutionGroup` / `SubstitutionMember`

- group id, label
- member rows: `exerciseKindId`, optional priority

## `ProgramExerciseOverride`

- `programId`, `movementSlotId` or block ref
- `exerciseKindId` (user’s chosen substitute)
- effective scope (`session`, `from_date`, `permanent`)

## `RoutineGeneratorProfile`

- persisted user defaults: `daysPerWeek`, `goal`, `equipmentProfileId`, `experienceLevel`
- last-used inputs for “regenerate” flows

## `PresetProgram`

- `presetId`
- `name`, `difficulty`, `equipmentTags`
- `payloadVersion`
- `installedProgramId` (nullable mapping once installed)

## `ActiveProgram`

- `id`
- `programId` (nullable if rotation-only mode is allowed)
- `rotationPlanId` (nullable)
- `startDate`, `endDate`
- `status` (`active`, `paused`, `completed`)

## `ProgressionScheme` / `ProgressionLevel`

- scheme metadata and linked level rows
- assignment scope (`global`, `program`, `block`, `exercise`)
- advancement rule metadata

## `PrefillPolicy`

- scope (`global`, `program`, `exercise`)
- mode (`prescription`, `carry_last_success`, `blank`, `copy_prior_window`)
- options payload (typed config)

## `RotationPlan` / `RotationSlot`

- cadence type (`days`, `weeks`, `months`)
- cadence interval (`N`)
- anchor datetime
- ordered routine slots referencing `Program`

## `WorkoutSession`

- `id`
- `status` (`draft`, `completed`, `discarded`)
- `scheduledFromProgramId` (nullable)
- `startedAt`, `completedAt`
- `notes`

## `WorkoutExerciseEntry`

- `id`
- `sessionId`
- `exerciseKindId`
- optional `movementSlotId` (if substituted from program slot)
- optional `substitutedFromExerciseKindId` (audit trail)
- ordering index

## `WorkoutSetEntry`

- `id`
- `workoutExerciseEntryId`
- set index
- `loadValue`, `repCount`, `timeSeconds`, `distanceValue`
- `isWarmup`, `isCompleted`
- optional flags (`amrap`, `failed`, etc.)

## `MLInsightRecord` (projection)

- `id`
- `exerciseKindId` (nullable for global insights)
- `insightType` (`plateau`, `deload_suggestion`, `load_recommendation`, `volume_anomaly`, etc.)
- `summaryText`, `explanationText`
- `confidence`, `modelVersion`
- `suggestedActionPayload` (typed JSON)
- `status` (`active`, `dismissed`, `accepted`)
- `computedAt`, `sourceSessionId` (nullable)

## 3) Data Integrity Rules

- Completed `WorkoutSession` rows are immutable except explicit correction actions.
- `Program` revisions do not rewrite historical session records.
- `RotationPlan` resolves current program deterministically from anchor + cadence + clock.
- Analytics are derived from `WorkoutSession` and related source tables only.
- Substitutions on completed sessions are immutable; program-level overrides affect future sessions only unless editing a draft.
- `MLInsightRecord` rows are derived/advisory; accepting a suggestion updates draft session or user settings—not historical logs.

## 4) Indexing and Query Priorities

- Index by:
  - `WorkoutSession.completedAt`
  - `WorkoutExerciseEntry.exerciseKindId`
  - foreign keys across session and set tables
  - active assignment lookups (`ActiveProgram.status`, `startDate`)
- Prioritize query paths:
  - catalog search by name, muscle, equipment, mechanics
  - today session load
  - session history by date
  - progression series by exercise and date range
  - PR retrieval per exercise

## 5) Migration Strategy

- Version schema from day one and track each migration in tests.
- Never drop user training data in destructive migrations **in production releases**.
- Use additive changes first, then background backfill when needed.
- Keep old-to-new mapping docs for concept refactors.

**Current pre-release behavior:** the app uses `fallbackToDestructiveMigration()` while the schema is still evolving (v6). Installing an APK with a higher schema version **wipes local data**. Replace this with tested incremental migrations before Play Store release.

## 6) Backup, Export, and Retention

- Allow platform backup where policy permits.
- Plan CSV/JSON export for session history and PRs.
- Keep soft-delete/archive semantics for exercises and programs to preserve history links.

See `BACKLOG.md` §1 for the proposed backup, restore, and auto-sync design.

## 7) Current implementation (Room v6)

The following tables exist in `LiteWeightDatabase` (version **6**). Names match Room `@Entity` table names.

| Table | Purpose |
|-------|---------|
| `exercise_kinds` | User and built-in exercises |
| `exercise_classifications` | Mechanics, force, utility (catalog) |
| `exercise_instructions` / `exercise_comments` | Rich catalog text |
| `muscle_vocabulary` / `exercise_muscle_roles` | Muscle tagging |
| `app_metadata` | Installed catalog version |
| `workout_sessions` / `workout_exercise_entries` / `workout_set_entries` | Session source of truth |
| `programs` / `program_days` / `program_exercises` | Routine templates (`movementSlotId` on exercises) |
| `active_programs` | Active assignment (`rotationPlanId` optional) |
| `installed_presets` | Preset install tracking |
| `progression_schemes` / `progression_levels` | Level definitions and rules |
| `exercise_session_snapshots` / `exercise_pr_events` / `exercise_analytics_summaries` | Analytics projections |
| `movement_slots` | Seeded abstract roles (9 defaults) |
| `substitution_groups` / `substitution_members` | User-defined swap pools |
| `rotation_plans` / `rotation_slots` | Cadence-based program rotation |

**Not yet persisted:** `ProgramExerciseOverride`, `RoutineGeneratorProfile`, `MLInsightRecord`, and standalone `PrefillPolicy` rows (prefill modes are handled in application logic today).

**Seeded on first launch:** exercise catalog v2 (63 entries), movement slots, built-in “Linear 3-phase” progression scheme, preset program definitions (install on demand).
