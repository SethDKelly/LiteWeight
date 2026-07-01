# LiteWeight — Project Guidance

This document defines vision, scope, platform targets, and a **concept-oriented design** foundation for LiteWeight: a lightweight Android app for basic fitness and weight-focused training, with program selection or design and clear progression tracking.

---

## 1. Product vision

**LiteWeight** prioritizes a small surface area and fast daily use: pick or build a program, log sets and loads, and see progression without social feeds, ads, or enterprise gym features.

**Primary outcomes**

- Users can **follow a structured program** (preset or custom) with minimal friction.
- Users can **log workouts** quickly (exercise → sets → weight/reps/time as applicable).
- Users can **see progression** over time (volume, load, consistency), not just a single session.

### Required capabilities (product backlog anchors)

| # | Capability | Notes |
|---|------------|--------|
| 1 | **User-defined exercises** | Users create movements via the **Exercise Naming Framework** (structured pickers + **free-form text**); **rich built-in catalog** fills common cases. See `EXERCISE_NAMING_FRAMEWORK.md`, `EXERCISE_CATALOG.md`. |
| 11 | **Rich exercise catalog** | Shipped database with **classification**, **instructions**, **comments**, and **muscle roles** (curated using [ExRx.net](https://exrx.net/Lists/Directory) as taxonomy reference) to power search, smart routines, and prepopulation. See `EXERCISE_CATALOG.md`. |
| 2 | **Premade routines / splits** | Users browse and **select** app-shipped or downloadable templates (e.g. PPL, upper/lower, full body). |
| 3 | **Custom routines / splits** | Users **author** routines: weekly split (which days / which focus), exercises, and targets—saved and reusable like presets. |
| 4 | **Progression levels** | Users configure **levels** (or equivalent tiers/rules) that govern how targets or phases advance—see `ProgressionScheme` (§4.7). |
| 5 | **Basic analytics** | **Visual** trends of load/volume/consistency over time; **visual** personal records **per exercise/lift** (with clear metric: weight × reps, 1RM estimate, etc.—pick one primary per exercise type). |
| 6 | **Rotating routines by cadence** | Users select one or more routines/splits and **cycle** among them on a **configurable cadence** (e.g. every *N* weeks, days, or months, with a defined anchor/start). |
| 7 | **Prepopulation rules** | Users define **how** a new session is filled from the active program and history (e.g. carry last successful loads, use prescription only, blank sets). See `PrefillPolicy` (§4.8). |
| 8 | **Exercise substitution** | Users **swap** a prescribed or in-session exercise for another (equipment unavailable, injury, preference) with sensible history/prefill carryover. See `ExerciseSubstitution` (§4.11). |
| 9 | **Auto routine creation** | Users generate or **auto-populate** routines from simple inputs (days/week, goal, equipment, experience) instead of authoring from scratch. See `RoutineGenerator` (§4.12). |
| 10 | **ML-assisted lift analytics** | On-device models augment rule-based analytics with automated insights (plateaus, suggested loads, deload hints). See `MLInsights` (§4.13) and `ANALYTICS_AND_PROGRESSION.md` §9. |

**Terminology:** In UX copy, **routine** usually means the full template; **split** means the weekly layout (which sessions fall on which days). The domain model may use a single `Program` concept that carries split metadata.

### Simplicity guardrails (easy-to-use lifting app)

The capability list is broad; **ship in layers** so daily logging stays simple:

- **Default path:** open app → see today’s work → log sets → done (≤3 meaningful taps to first logged set).
- **Progressive disclosure:** rotation, progression levels, prefill overrides, and ML insights live behind clear entry points—not the first-run experience.
- **Shipped (Phases 0–5):** custom exercises, active program with presets, fast logging, charts/PRs, in-session swap (muscle + substitution groups), rotation plans, routine generator, progression levels.
- **Next (Phases 6–7):** on-device ML insights, rest timer, free-session mode, export/backup, onboarding, production migrations.

**Still missing from docs (recommended additions):**

| Area | Gap | Recommendation |
|------|-----|----------------|
| **In-session UX** | Rest timer, duplicate last set, skip exercise, reorder exercises | Add to session UX backlog; high value for lifters |
| **Units & increments** | kg/lb preference done; plate/bar math, standard increment steps (2.5 / 5) | User settings + logging shortcuts |
| **Quick workout** | Log without an active program | “Free session” mode using same `WorkoutSession` model |
| **Program-level override table** | `ProgramExerciseOverride` entity documented but not persisted | Wire program default substitute UI to storage |
| **Onboarding** | First-run: units, days/week, equipment, optional goal | Drives `RoutineGenerator` inputs and defaults |
| **Deload / fatigue** | Mentioned in progression levels but not as user-facing action | Tie to `ProgressionScheme` + optional ML suggestion |
| **Import/export** | Export only lightly mentioned | Backup/restore story before users accumulate months of data |
| **Production migrations** | Destructive migration during dev | Replace before Play release |
| **Accessibility** | Large touch targets noted; no broader a11y | TalkBack labels, contrast, font scaling for gym use |

**Non-goals (initially)**

- Nutrition tracking, coaching marketplaces, social sharing, wearable-exclusive features, or medical claims.
- Parity with large “all-in-one” fitness platforms.

---

## 2. Platform and release policy

| Target | Policy |
|--------|--------|
| **Minimum SDK (`minSdkVersion`)** | **API 26 (Android 8.0 Oreo)** — supports **96%+** of active devices while keeping a practical compatibility floor. |
| **Target SDK (`targetSdkVersion`)** | **API 35 (Android 15)** — required baseline for **active development** and **Google Play** compliance at release. |
| **Compile SDK** | **API 35** — align with `targetSdkVersion` during development. |
| **Form factors** | Phone-first; tablet and foldable layouts as progressive enhancement. |
| **Distribution** | Google Play (or sideload); follow Play policies and data safety forms. |

**Rationale:** `targetSdkVersion` 35 satisfies current Play requirements for new and updated apps. `minSdkVersion` 26 maximizes reach without supporting pre–Oreo platforms. Use **AndroidX** and version-aware APIs only where platform behavior differs across 26–35; avoid raising `minSdk` unless a feature truly cannot be bridged.

**Compatibility checks:** Exercise critical paths (logging, persistence, charts) on at least one **API 26** and one **API 35** device or emulator before release.

---

## 3. Concept-oriented design (Daniel Jackson, MIT)

Design is driven by **concepts**: each concept has a **purpose**, **state** the user (or system) cares about, and **actions** that change that state. Features are **compositions** of concepts—not a flat list of screens.

**Rules of thumb for this project**

1. **Name concepts by user-meaning**, not by UI tab names (e.g. `WorkoutSession`, not `HomeFragment`).
2. **Keep concepts small**: if a concept does two unrelated jobs, split it.
3. **Specify invariants** (what must always be true) before implementation details.
4. **Compose**: e.g. `Program` + `Session` + `Progression` interact through well-defined operations, not shared “god” state.

This reduces rework: screens map to concepts; new features usually add a concept or an action, not scattered edits.

---

## 4. Core concepts (draft specification)

The following are **initial** concepts—refine names and fields as you validate with prototypes.

### 4.1 `ExerciseKind`

**Purpose:** Identify a type of movement users log (e.g. squat, row) independent of any program.

**State (examples):** `displayName`, `namingMode` (`structured` | `freeform`), structured facets (equipment, body position, primary movement, grip width, grip orientation—see `EXERCISE_NAMING_FRAMEWORK.md`), optional `customDisplayName` / `freeformName`, muscle tags, default unit (`weight` vs `time` vs `bodyweight`), provenance (`userDefined` vs `builtin`), optional **`catalogId`** / rich metadata (classification, instructions, comments, muscle roles—see `EXERCISE_CATALOG.md`).

**Actions:** `defineStructured`, `defineFreeform`, `edit`, `rename`, `archive`, `duplicate` (optional), `convertToStructured` (optional).

**Invariant:** Two `ExerciseKind` instances with the same identity refer to the same movement for analytics and PR history. **User-defined exercises are a first-class requirement**—the app ships a **standard exercise database**; when a movement is missing, users add one via the **descriptive naming pattern** or **free-form text** without leaving the search flow.

---

### 4.2 `Program` (routine template; carries **split**)

**Purpose:** Describe **what** should be done across weeks or sessions—a template, not a log. Supports both **premade** (installed) and **user-authored** routines with the same structure.

**State:** ordered structure: calendar or rotation slots → days/phases → blocks → prescribed work (e.g. exercise, target sets/reps/RPE, references to `ProgressionScheme`). **Split** is modeled here: which workout variants occur on which days (e.g. Mon Push / Wed Pull / Fri Legs).

**Actions:** `create` (custom), `edit`, `duplicate`, `assignToUser` (activate), `retire`, `importFromPreset` (see `PresetProgram`).

**Invariant:** Editing a `Program` does not retroactively rewrite past `WorkoutSession` records (sessions **snapshot** prescriptions or link version ids).

---

### 4.3 `ActiveProgram` (or `ProgramAssignment`)

**Purpose:** Bind the user’s **current** training context: which routine template is active, optional link to a **rotation** (§4.9), and scheduling anchors.

**State:** which `Program` is active (if not purely rotation-driven), start date, optional end or deload markers, pointer to active `RotationPlan` when cycling multiple routines.

**Actions:** `activate`, `pause`, `complete`, `switch`, `attachRotation`.

**Invariant:** If a `RotationPlan` is active, “current routine” is resolved by the rotation’s rules plus calendar; avoid ambiguous double activation without explicit product rules.

---

### 4.4 `WorkoutSession`

**Purpose:** Record **what was actually done** in one visit.

**State:** date/time, ordered list of performed exercises, each with performed sets (load, reps, time, flags), optional notes. Initial rows may be created by **`PrefillPolicy`** (§4.8).

**Actions:** `start`, `prefill` (delegates to policy), `addExercise`, `logSet`, `complete`, `discardDraft`.

**Invariant:** A completed session is immutable except for explicit `correct` or `annotate` actions (auditable if needed).

---

### 4.5 `Progression` (analytics projections)

**Purpose:** Turn raw logs into **comparisons** and **charts**: trends over time and **personal records per exercise**.

**State (derived):** aggregates keyed by `ExerciseKind` and time window; **PR events** (best weight × reps, best estimated 1RM, longest time—whichever the product fixes per exercise type); series suitable for **visual progression** (e.g. session max load, weekly volume).

**Actions:** `recompute` (on new data), `inspectWindow` (query), `prHistory` (per `ExerciseKind`), `seriesForChart` (load/volume over time).

**Invariant:** All analytics—including **visual progression** and **visual PRs**—are **projections** of `WorkoutSession` (and configured PR rules), not manually edited numbers.

---

### 4.6 `PresetProgram` (premade routines / splits)

**Purpose:** Ship curated **routines and splits** users can **select and install** without authoring—satisfies “premade” backlog item.

**State:** metadata, difficulty, equipment tags, revision, same structural shape as `Program` so install is a copy into the user’s library.

**Actions:** `publish` (app updates), `install` (copy into user’s `Program` library), `browse`.

**Invariant:** Installing never deletes user-authored programs unless user confirms destructive actions.

---

### 4.7 `ProgressionScheme` (progression levels)

**Purpose:** Capture **user-set progression levels** (or tiers/rules): how targets advance between sessions or phases—e.g. named levels (“Base / Build / Peak”), step loads, rep-range triggers, or deload flags.

**State:** ordered **levels** with associated rules (per exercise or program-wide defaults), optional linkage to `Program` blocks or calendar phases.

**Actions:** `defineLevel`, `reorder`, `assignToBlock`, `advance` (manual or rule-evaluated after sessions).

**Invariant:** Level semantics are **prescriptive** for future sessions; historical `WorkoutSession` rows remain unchanged when levels are edited later (same versioning philosophy as `Program`).

---

### 4.8 `PrefillPolicy` (how sessions prepopulate)

**Purpose:** Let users define **how** starting a session fills sets and loads from the active `Program` and past data.

**State:** scoped **rules** (global default, per-`Program`, or per-exercise override), e.g. “use program prescription only,” “carry **last successful** working weight,” “copy previous week same weekday,” “empty template.”

**Actions:** `setDefault`, `setProgramOverride`, `applyAtSessionStart` (produces draft lines for `WorkoutSession`).

**Invariant:** Prefill never **commits** a completed session automatically; it only initializes or suggests draft values until the user completes or edits.

---

### 4.9 `RotationPlan` (cadence across routines / splits)

**Purpose:** **Cycle** among **selected** routines/splits on a **set cadence**: e.g. rotate every *N* **weeks**, **days**, or **months**, with a calendar anchor.

**State:** ordered list of `Program` references (or slots), **cadence** type and *N*, start anchor, optional labels (mesocycle names).

**Actions:** `configure`, `advance` (time-driven or manual skip), `peekNext`, `pause`.

**Invariant:** The resolver that answers “which routine is active **today**” is deterministic given anchor + cadence + calendar; test edge cases (timezone, missed weeks).

---

### 4.10 Optional: `EquipmentProfile`

**Purpose:** Constrain program suggestions (home vs gym).

**State:** available equipment list.

**Actions:** `update`.

Use only if it stays simple; otherwise fold into `Program` metadata filters.

---

### 4.11 `MovementSlot` and `ExerciseSubstitution`

**Purpose:** Let users **replace** one exercise with another while preserving program intent and sensible logging continuity.

**State:**

- **`MovementSlot`:** abstract role in a `Program` block (e.g. `main_squat`, `horizontal_push`) with optional default `ExerciseKind`.
- **`SubstitutionGroup`:** equivalence set (user-defined or suggested) linking interchangeable exercises.
- **Session/program override:** which `ExerciseKind` fulfilled a slot for a given session or forward-looking program edit.

**Actions:** `substituteInSession`, `substituteInProgram` (this day forward), `suggestAlternates` (by muscle tag, equipment, history), `mapSlotToExercise`.

**Invariant:** Substitution updates **future** prescriptions and **current draft** sessions; completed `WorkoutSession` history keeps the exercise actually performed. Prefill for a substitute should prefer **history for the replacement exercise**, not the original.

---

### 4.12 `RoutineGenerator` (auto-create / populate routines)

**Purpose:** **Auto-create or populate** `Program` templates from minimal user input instead of manual authoring.

**State:** inputs—`daysPerWeek`, `goal` (strength/hypertrophy/general), `equipmentProfile`, `experienceLevel`, optional focus muscles; output—draft `Program` with split, `MovementSlot`s, default prescriptions, and linked `ProgressionScheme`.

**Actions:** `generateDraft`, `regenerateDay`, `acceptAsProgram`, `tune` (adjust one parameter and refresh).

**Invariant:** Generated routines are **drafts** until the user accepts; accepting creates a normal `Program` the user can edit like any custom routine. v1 may use **rule-based templates**; ML may rank or refine choices later (§4.13).

---

### 4.13 `MLInsights` (automated lift analytics)

**Purpose:** **Automate** analytics beyond fixed formulas—surface patterns users might miss (plateau, unusual volume spike, suggested next load, deload window).

**State (derived + model outputs):** per-`ExerciseKind` insight records with type, confidence, explanation text, optional suggested action, `modelVersion`, `computedAt`.

**Actions:** `runOnDeviceInference` (after session complete or on schedule), `dismissInsight`, `acceptSuggestion` (e.g. apply suggested weight to draft set).

**Invariant:** ML outputs are **advisory** and **explainable**; never silently overwrite user data or completed sessions. Prefer **on-device** inference (TensorFlow Lite / LiteRT) for privacy; rule-based fallbacks when data is sparse or model confidence is low.

---

## 5. Concept map (relationships)

```text
PresetProgram --install--> Program
RoutineGenerator --drafts--> Program
Program --activate--> ActiveProgram
RotationPlan --selects--> Program (ordered slots; cadence N weeks/days/months)
ActiveProgram --may-use--> RotationPlan
Program --may-reference--> ProgressionScheme (levels / rules)
Program --uses--> MovementSlot --maps-to--> ExerciseKind
ExerciseSubstitution --replaces--> MovementSlot / ExerciseKind (session or forward)
PrefillPolicy --initializes--> WorkoutSession (draft from Program + history)
ActiveProgram --instantiate--> WorkoutSession (prescription → performed)
WorkoutSession --feeds--> Progression (charts, PRs per ExerciseKind)
WorkoutSession --feeds--> MLInsights (on-device inference)
ExerciseKind --referenced-by--> Program, WorkoutSession, SubstitutionGroup
```

---

## 6. User experience principles

- **Exercises:** search built-in catalog first; **“Create exercise”** uses equipment → position → movement → grip pickers with live name preview, plus **free-form name** shortcut. See `EXERCISE_NAMING_FRAMEWORK.md`.
- **Substitution:** one obvious **“swap exercise”** action in-session and in program editor; show alternates by muscle/equipment, not a long list by default.
- **Auto routines:** offer **“build for me”** with 3–5 questions; show editable draft before save—never auto-activate without confirmation.
- **Routines / splits:** two paths—**(1)** pick **premade** templates, **(2)** build **custom** splits and save them alongside presets.
- **Rotation:** when multiple routines are in play, the UI should show **which routine is current**, **what’s next**, and how the **cadence** is configured (human-readable: “switches every 4 weeks”).

- **Logging first:** default flow is “today’s work” with large touch targets; editing deep history is secondary.
- **Prepopulation:** expose **clear defaults** (“start from last week’s weights”) and advanced overrides without forcing every user into settings.

- **Progression levels:** surface **what level or phase** the user is in when it affects targets; avoid jargon unless the user opts in.

- **Analytics:** ship **readable charts** for progression over time and **obvious PR callouts** per lift; keep v1 metrics bounded (don’t ship twelve chart types on day one).
- **ML insights:** short, plain-language cards (“volume trending down 3 weeks”) with **dismiss** and **why**; no black-box scores without explanation.
- **Offline-friendly:** core logging should work without network; sync only if you add cloud later. ML inference runs **on-device** where possible.

---

## 7. Suggested technical direction (non-binding)

Align implementation with concepts using clear **domain** types and **use cases** (Clean-style or similar), with UI as a thin layer.

| Area | Suggestion |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Async | Kotlin coroutines |
| Local persistence | Room (or SQLDelight) for sessions and programs |
| DI | Hilt or Koin |
| Navigation | Single-activity, type-safe routes where practical |
| Testing | Unit tests for concept invariants; UI tests for critical flows |
| On-device ML | TensorFlow Lite / LiteRT for lift insights; rule-based fallback when data sparse |

Map packages by concept or feature slice (e.g. `exercise`, `program`, `session`, `progression`, `analytics`, `movement`, `substitution`, `rotation`, `generator`) rather than only by layer, to keep boundaries visible.

---

## 8. Milestones (example)

1. **M1 — Core logging:** ✅ `ExerciseKind` (including **user-defined** exercises), `WorkoutSession`, local persistence, manual session logging, **in-session exercise swap** (basic).
2. **M2 — Programs & splits:** ✅ `Program` (custom **routines/splits**), `ActiveProgram`, `PresetProgram` (**premade** selection/install); `PrefillPolicy` v1; `MovementSlot` on program exercises.
3. **M3 — Progression & analytics:** ✅ `ProgressionScheme` (**progression levels**), rule-based projections, **visual progression** + **visual PRs** per exercise.
4. **M4 — Rotation & substitution:** ✅ `RotationPlan`; `SubstitutionGroup` + in-session swap; `RoutineGenerator` v1 (rule-based auto-populate).
5. **M5 — ML insights:** ⏳ `MLInsights` on-device (plateau/deload/load suggestions) with explainability and dismiss flow.
6. **M6 — Polish:** ⏳ export/backup, rest timer & logging shortcuts, production migrations, performance on long histories.

---

## 9. Privacy and safety

- Store health-adjacent data with **least privilege**; document what leaves the device if you add sync.
- Avoid claiming medical outcomes; keep copy aligned with general wellness.

---

## 10. How to evolve this document

- After each milestone, **revisit §4**: add/remove actions, tighten invariants, split concepts if responsibilities drift.
- Keep **one paragraph “purpose”** per concept in code review checklists so new features stay aligned with concept-oriented design.

---

## References

- Jackson, Daniel. *Conceptual modeling* and concept design (MIT). Use the project’s internal notes or course materials for full methodology; this guide applies the style at app scale.
- [Android API levels](https://developer.android.com/guide/topics/manifest/uses-sdk-element) — `minSdkVersion` **26**, `targetSdkVersion` **35**; re-verify against Play policy updates before each store submission.

## Related planning documents

- `README.md` (docs index)
- `IMPLEMENTATION_PLAN.md`
- `DATA_MODEL_AND_STORAGE.md`
- `EXERCISE_NAMING_FRAMEWORK.md`
- `EXERCISE_CATALOG.md`
- `ANALYTICS_AND_PROGRESSION.md`
