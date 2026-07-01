# LiteWeight High-Level Implementation Plan

This plan converts `PROJECT_GUIDANCE.md` into executable work with clear phases.

## 1) Delivery Objectives

- Ship Android app with **`minSdkVersion` 26** (Oreo) and **`targetSdkVersion` 35** (Android 15), offline-first workout tracking.
- Support custom exercises, preset/custom routines and splits, and session prefill.
- Provide progression levels, routine rotation cadence, and basic visual analytics.
- Support exercise substitution, rule-based routine generation, and phased on-device ML insights.

## 2) Workstreams

- **Domain and storage**
  - Define concept-aligned entities and invariants.
  - Implement Room schema, migrations, repositories, and query layer.
- **Exercise catalog & naming**
  - **Rich built-in catalog** (classification, instructions, comments, muscles) — `EXERCISE_CATALOG.md`; ExRx-informed curation.
  - Structured builder and free-form create flow — `EXERCISE_NAMING_FRAMEWORK.md`.
- **Session UX**
  - Build fast logging flow, prepopulation behavior, and editing experience.
- **Program UX**
  - Build preset install path, custom authoring, progression levels, rotation setup, substitution, and routine generator.
- **Analytics**
  - Build projection pipeline and chart-ready read models for trends and PRs.
- **Insights (ML)**
  - Feature extraction, on-device model integration, insight cards with explainability.
- **Session UX extras**
  - Rest timer, duplicate set, skip/reorder exercises, kg/lb settings, free-session mode.
- **Quality and release**
  - Tests, performance checks, crash monitoring, and release hardening.

## 3) Phase Plan

## Phase 0: Project foundation

**Outcome:** Runnable app shell, toolchain pinned, concept package layout, empty navigation graph—no feature-complete logging yet.

- Set up app skeleton: Compose, navigation, DI, Room, coroutine dispatchers, test harness.
- Configure Gradle: `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`.
- Establish package layout by concept (`core`, `exercise`, `program`, `session`, `progression`).
- Wire Hilt, Room database module (schema v1 stub), and a single home destination.
- Add baseline CI workflow (assembleDebug, unit tests, lint).
- Document local run steps in root `README.md`.

## Phase 1: Core logging and exercises

**Outcome:** User can search the built-in catalog, create custom exercises, and log a basic session.

- Implement `ExerciseKind` with user-defined create/edit/archive.
- Implement **exercise creation flow**: catalog search → structured builder (equipment, position, movement, grip) + **free-form** quick add (`EXERCISE_NAMING_FRAMEWORK.md`).
- **Populate rich exercise catalog** (`EXERCISE_CATALOG.md`):
  - Define bundle schema + Room detail tables (classification, instructions, comments, muscle roles).
  - Build dev-time import/validation tooling (`tools/catalog/` → `assets/catalog/`).
  - Curate **MVP tier** (~80–120 lifts) using ExRx directory as taxonomy reference; app-owned instruction text.
  - Implement `CatalogSeeder` (first launch + version upgrades by `catalogId`).
  - Exercise detail UI: classification summary, steps, comments, muscles.
- Implement `WorkoutSession` draft/completed lifecycle and set logging.
- Persist session data and basic history list.
- Add **basic in-session exercise swap** (one-off replacement, no groups yet).
- Add unit preference (kg/lb) and user settings skeleton.
- Add unit tests for concept invariants, catalog seeding, and session immutability behavior.

## Phase 1b: Catalog expansion (parallel or follow-on)

- Expand toward **rich tier** (hundreds of entries) from ExRx directory coverage map.
- Search filters: muscle, equipment, mechanics (`compound` / `isolated`), force (`push` / `pull`).
- Wire catalog metadata into substitution suggestions (same target muscle).

## Phase 2: Programs, splits, and prefill

- Implement `Program` authoring and editing (custom routines/splits).
- Implement `PresetProgram` browsing and install-to-library flow.
- Implement `ActiveProgram` assignment.
- Implement `PrefillPolicy` v1 modes:
  - prescription only
  - carry last successful load
  - blank template

## Phase 3: Progression levels

- Implement `ProgressionScheme` (levels/tier rules and block assignment).
- Surface current level/phase in session and program views.
- Add rule-evaluation tests for level advancement.

## Phase 4: Analytics and progression visuals

- Build projection jobs/read models for progression metrics.
- Implement visual progression charts per exercise.
- Implement visual PR timeline and latest PR callouts.
- Validate performance on larger local datasets.

## Phase 5: Substitution, auto routines, and rotation

- Implement `MovementSlot`, `SubstitutionGroup`, and program-level overrides.
- Implement `RotationPlan` cadence resolver (N days/weeks/months).
- Implement `RoutineGenerator` v1 (template/rule-based; editable draft before save).
- Show current routine and next routine preview in planner UX.

## Phase 6: ML-assisted insights

- Build feature extraction pipeline from session projections.
- Integrate on-device model (TensorFlow Lite / LiteRT) for load/plateau/deload insights.
- Insight UI: explain, dismiss, optional accept-to-prefill.
- Rule-based fallback when data or confidence is insufficient.

## Phase 7: Polish and release readiness

- UX polish, onboarding copy, and empty states.
- Rest timer, duplicate-set shortcuts, free-session entry.
- Telemetry and crash reporting integration (privacy-safe).
- Final migration validation, backup/restore checks, and release checklist.

## 4) Definition of Done (per feature)

- Concept actions and invariants are documented and covered by tests.
- Storage model is migration-safe and backward compatible.
- UX supports offline behavior and recovery from interrupted sessions.
- Analytics values are reproducible from session source data.

## 5) Risks and Mitigations

- **Schema churn early on**
  - Mitigation: treat schema as versioned contract and add migration tests each phase.
- **Analytics inconsistency**
  - Mitigation: projection-only analytics; never manually edit computed metrics.
- **Complexity creep in routines**
  - Mitigation: keep v1 authoring constrained; add advanced options behind explicit toggles.
- **Backward compatibility across API 26–35**
  - Mitigation: AndroidX, avoid unnecessary platform APIs, test on Oreo and Android 15 emulators.
- **ML overfitting / bad suggestions**
  - Mitigation: confidence thresholds, rule fallbacks, user dismiss, minimum data gates.
- **Substitution breaks analytics continuity**
  - Mitigation: `MovementSlot` roles + audit fields; chart per exercise with optional “slot family” view later.
- **Auto-generated routines feel generic**
  - Mitigation: always show editable draft; start with proven templates, not open-ended generation.
- **Catalog content quality / licensing**
  - Mitigation: app-owned copy; ExRx as reference + link-out; legal review before verbatim import at scale.
- **Performance on history-heavy users**
  - Mitigation: indexed queries, incremental projections, and lightweight chart series.

## 6) Recommended Initial Backlog Slices

1. Fresh install seeds MVP catalog; search returns “Barbell Bench Press” with muscles shown.
2. Open exercise detail: classification, instructions, comments, target muscles.
3. Create custom exercise and use it in a session.
4. Create exercise via **structured pickers** (e.g. Barbell · Incline · Bench Press · Close Grip).
5. Create exercise via **free-form name** when pickers do not fit.
6. Create custom routine/split and activate it.
7. Install preset routine and duplicate/edit it.
8. Start session with selected prefill mode.
9. Swap an exercise in-session and verify prefill uses substitute history.
10. Generate a draft routine from days/week + equipment and edit before save.
11. View progression chart and PR history for one exercise.
12. Configure rotation cadence and observe active-routine switch.
13. Receive and dismiss an ML insight after completing several sessions.
