# LiteWeight Exercise Catalog (Built-in Database)

Rich, **shipped** exercise database for search, substitution, intelligent routine generation, and workout prepopulation. User-created exercises (see `EXERCISE_NAMING_FRAMEWORK.md`) extend this catalog—they do not replace it.

**Primary curation reference:** [ExRx.net Exercise Directory](https://exrx.net/Lists/Directory) — taxonomy, muscle roles, classification labels, and coaching notes.

---

## 1) Goals

- Ship a **large, offline** built-in library so most users never author from scratch.
- Store **classification**, **instructions**, **comments**, and **muscles** per exercise where available.
- Align catalog entries with the **structured naming framework** (equipment, position, movement, grip).
- Feed **RoutineGenerator**, **MovementSlot** matching, **substitution**, and **session prefill** with muscle/equipment metadata.
- Version the catalog independently of app schema (`catalogVersion`).

---

## 2) ExRx as a curation source (not a runtime dependency)

ExRx organizes exercises in a directory by body region, equipment, and movement type. Typical exercise pages include:

| ExRx section | LiteWeight field | Use in app |
|--------------|------------------|------------|
| **Classification** | `classification` | Filter/sort; generator picks balanced patterns (compound vs isolation, push/pull) |
| **Instructions** | `instructions` | Exercise detail screen; optional in-session reminder |
| **Comments** | `comments` | Coaching tips, spotting notes, common mistakes |
| **Muscles** | `muscleRoles[]` | Target/synergist/stabilizer mapping for smart workouts and swaps |

**Legal / content policy**

- Do **not** scrape ExRx at runtime or bulk-redistribute copyrighted text without permission.
- Use ExRx as a **reference for taxonomy and curation** during development.
- Ship **app-owned** bundle content: original or licensed copy, with optional `referenceUrl` (link-out) per entry.
- Attribute ExRx in app **About / Data sources** when entries are derived from their directory structure.
- Revisit licensing with ExRx maintainers before shipping large verbatim instruction sets.

---

## 3) Catalog entry schema (bundle format)

Bundled as `app/src/main/assets/catalog/exercise_catalog_v{N}.json` (or split by region for size).

```json
{
  "catalogVersion": 1,
  "sourceAttribution": "Exercise taxonomy informed by ExRx.net (exrx.net). Instructions and comments are app-curated unless otherwise noted.",
  "entries": [
    {
      "catalogId": "exrx:barbell-bench-press",
      "displayName": "Barbell Bench Press",
      "namingMode": "structured",
      "equipment": "barbell",
      "bodyPosition": "flat_bench",
      "primaryMovement": "bench_press",
      "gripWidth": "standard",
      "gripOrientation": "pronated",
      "unitType": "weight",
      "classification": {
        "utility": "basic",
        "mechanics": "compound",
        "force": "push",
        "difficulty": "intermediate",
        "exrxCategoryPath": ["Chest", "Barbell"]
      },
      "instructions": [
        "Lie on bench with feet flat on floor.",
        "Grip bar slightly wider than shoulder width.",
        "Lower bar to mid-chest with control; press to lockout."
      ],
      "comments": [
        "Keep shoulder blades retracted.",
        "Use a spotter for heavy sets."
      ],
      "muscleRoles": [
        { "muscle": "pectoralis_major_sternal", "role": "target" },
        { "muscle": "anterior_deltoid", "role": "synergist" },
        { "muscle": "triceps_brachii", "role": "synergist" }
      ],
      "tags": ["chest", "horizontal_push", "barbell"],
      "referenceUrl": "https://exrx.net/WeightExercises/PectoralSternal/BBBenchPress"
    }
  ]
}
```

### Classification fields (align with ExRx-style labels)

| Field | Example values | Notes |
|-------|----------------|-------|
| `utility` | `basic`, `auxiliary`, `specific` | Movement priority in programming |
| `mechanics` | `compound`, `isolated` | Generator slot filling |
| `force` | `push`, `pull`, `static`, `bend` | Balance push/pull in routines |
| `difficulty` | `beginner`, `intermediate`, `advanced` | Onboarding / generator |
| `exrxCategoryPath` | `["Back", "Cable"]` | Traceability to source directory |

### Muscle roles

| Role | Meaning |
|------|---------|
| `target` | Primary muscle trained |
| `synergist` | Assists the movement |
| `stabilizer` | Stabilizes during execution |
| `antagonist` | Opposing muscle (optional) |
| `other` | Stretch, fixator, etc. |

Use a **normalized muscle vocabulary** (`muscle` slug table) so generator and analytics can query consistently.

---

## 4) Storage model (Room)

Extend `ExerciseKind` with detail tables (keeps list queries light):

| Table | Purpose |
|-------|---------|
| `exercise_kinds` | Existing row + `catalogId` (nullable), `catalogVersion` |
| `exercise_classifications` | 1:1 optional classification fields |
| `exercise_instructions` | 1:N ordered instruction steps |
| `exercise_comments` | 1:N ordered comment lines |
| `exercise_muscle_roles` | N:M muscle slug + role per exercise |
| `muscle_vocabulary` | Canonical muscle id, display name, body region |

**Rules**

- `isBuiltin = true` and non-null `catalogId` for shipped rows.
- User edits to built-ins: allow hide/archive and display rename; do not mutate shared catalog text (fork to user copy if they edit instructions).
- Catalog re-import updates built-ins by `catalogId`; never deletes user-authored rows.

---

## 5) Import and seed pipeline

**Dev-time (recommended)**

1. Maintain curated JSON (or CSV → JSON) in repo under `tools/catalog/`.
2. Run `CatalogImportTask` (Gradle or script) to validate schema, muscle slugs, and naming alignment.
3. Emit `exercise_catalog_v{N}.json` into `assets/`.

**Runtime (first launch / upgrade)**

1. `CatalogSeeder` reads bundle if `installedCatalogVersion < bundle.catalogVersion`.
2. Upsert by `catalogId` inside a transaction.
3. Record `installedCatalogVersion` in `app_metadata` table.
4. Full-text or tokenized search index optional (Phase 1+).

**Scope tiers**

| Tier | Count (target) | When |
|------|----------------|------|
| **MVP** | ~80–120 | Phase 1 — common barbell/dumbbell/machine lifts |
| **Rich** | ~400–800+ | Phase 1b / early Phase 2 — broad ExRx directory coverage |
| **Complete** | Ongoing | Incremental catalog versions post-release |

Prioritize entries that map cleanly to `MovementSlot` roles (squat, hinge, horizontal push, vertical pull, etc.).

---

## 6) Product integration

| Feature | How catalog helps |
|---------|-------------------|
| **Search** | Name, muscle, equipment, classification filters |
| **Exercise detail** | Instructions + comments offline |
| **RoutineGenerator** | Pick compounds by `force`/`mechanics`; fill slots by `target` muscle + equipment profile |
| **Substitution** | Suggest alternates with same `target` muscle + compatible equipment |
| **PrefillPolicy** | Default set/rep templates per `classification.difficulty` or movement family (optional) |
| **ML insights** | Muscle volume balance features across catalog metadata |

---

## 7) Acceptance criteria

- [ ] Bundle validates against schema; CI fails on invalid muscle slugs or duplicate `catalogId`.
- [ ] Fresh install seeds DB; search returns catalog exercises without network.
- [ ] Catalog upgrade preserves user data and updates built-ins by `catalogId`.
- [ ] At least MVP tier seeded before Phase 2 (programs depend on rich pick lists).
- [ ] About screen lists ExRx as taxonomy reference where applicable.

---

## 8) Open decisions

- **Verbatim vs paraphrased instructions** — legal review before scale import from ExRx.
- **Images / videos** — out of scope initially; link `referenceUrl` only.
- **Localization** — English first; catalog strings externalized for later translation.
