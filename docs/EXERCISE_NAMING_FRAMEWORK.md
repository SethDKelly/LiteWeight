# LiteWeight Exercise Naming Framework

Standard pattern for creating exercises missing from the built-in catalog, with **guided structured naming** and a **free-form text** escape hatch.

See also: `PROJECT_GUIDANCE.md` §4.1 (`ExerciseKind`), `DATA_MODEL_AND_STORAGE.md` (`ExerciseKind`).

---

## 1) Goals

- Make it **fast** to add a missing exercise during search or program edit.
- Produce **consistent, readable names** aligned with common lifting terminology.
- Allow **free-form** names when the structured pattern does not fit.
- Keep structured fields for search, substitution, and analytics grouping—even when display name is customized.

---

## 2) Descriptive name pattern

Most lifting names combine up to four parts (not all are required):

```text
[Equipment] + [Body/Implement Position] + [Primary Movement] + [Grip modifiers]
```

**Examples**

| Parts | Generated name |
|-------|----------------|
| Barbell · Flat Bench · Bench Press | Barbell Flat Bench Press |
| Dumbbell · Incline · Bench Press · Neutral | Dumbbell Incline Bench Press (Neutral Grip) |
| Cable · Seated · Row · Wide · Overhand | Cable Seated Row (Wide Grip, Overhand) |
| Barbell · — · Squat | Barbell Squat |

**Display rule:** omit empty segments; append grip modifiers in parentheses when present.

---

## 3) Structured fields

### 3.1 Equipment (tool)

Defines the implement used.

| Value | Label |
|-------|--------|
| `barbell` | Barbell |
| `dumbbell` | Dumbbell |
| `cable` | Cable |
| `smith_machine` | Smith Machine |
| `machine` | Machine (selectorized) |
| `kettlebell` | Kettlebell |
| `bodyweight` | Bodyweight |
| `band` | Band / resistance band |
| `other` | Other (pair with free-form note) |

### 3.2 Body / implement position (setup)

| Value | Label |
|-------|--------|
| `flat_bench` | Flat Bench |
| `incline` | Incline |
| `decline` | Decline |
| `seated` | Seated |
| `standing` | Standing |
| `lying` | Lying |
| `kneeling` | Kneeling |
| `unilateral` | Single-arm / single-leg (optional modifier) |
| `none` | — (not applicable) |

### 3.3 Primary movement (core action)

Canonical movement family—used for analytics grouping and substitution hints.

| Value | Label |
|-------|--------|
| `bench_press` | Bench Press |
| `overhead_press` | Overhead Press |
| `row` | Row |
| `pulldown` | Pulldown |
| `squat` | Squat |
| `deadlift` | Deadlift |
| `lunge` | Lunge |
| `curl` | Curl |
| `extension` | Extension |
| `raise` | Raise |
| `fly` | Fly |
| `carry` | Carry |
| `other` | Other (requires free-form movement label) |

Ship a **starter catalog** of common combinations; user-created rows use the same field vocabulary.

### 3.4 Grip / hand position (modifiers)

Optional. Omit when not meaningful for the movement.

**Grip width**

| Value | Label | Meaning |
|-------|--------|---------|
| `close` | Close / Narrow Grip | Hands closer than shoulder-width |
| `standard` | Standard / Medium Grip | Just outside shoulder-width (default when unspecified) |
| `wide` | Wide Grip | Wider than shoulder-width |
| `none` | — | Not applicable |

**Grip orientation (forearm rotation)**

| Value | Label | Meaning |
|-------|--------|---------|
| `pronated` | Overhand (Pronated) | Palms away from you; common default for presses |
| `supinated` | Supinated / Reverse Grip | Palms toward you |
| `neutral` | Neutral / Hammer Grip | Palms face each other |
| `none` | — | Not applicable |

---

## 4) Creation modes

## Mode A — Guided structured (default)

1. User searches built-in catalog.
2. If no match: open **“Create exercise”** with pickers for §3 fields.
3. App **previews** generated `displayName` live.
4. User confirms → `ExerciseKind` saved with structured fields + generated name.

Optional: **“Customize display name”** toggle reveals an editable name field; structured fields remain for search/substitution.

## Mode B — Free-form text (quick add)

1. User chooses **“Enter custom name”**.
2. Single text field (required) + optional notes.
3. `namingMode = freeform`; structured fields null unless user later converts to structured.

Use for niche variants (“Landmine press with fat grip”) without forcing bad fits into pickers.

## Mode C — Structured + free-form supplement

- Structured pickers for main facets.
- Optional **free-form suffix** appended or stored as `customQualifier` (e.g. “with chains”, “pause rep”).

---

## 5) UX requirements

- **Search-first:** creating an exercise is always reachable from exercise search (session, program editor, substitution).
- **Smart defaults:** pre-select last-used equipment; default grip width/orientation to `none` or `standard` / `pronated` where appropriate.
- **Duplicate detection:** warn if generated name closely matches an existing `ExerciseKind` (fuzzy match on normalized name + structured key).
- **Minimal taps:** structured path should be completable in ≤4 picker steps + confirm.
- **No dead ends:** every screen offers free-form escape (“Can’t find it? Type a name”).

---

## 6) Name generation (implementation sketch)

```kotlin
fun buildDisplayName(
    equipment: Equipment?,
    position: BodyPosition?,
    movement: PrimaryMovement?,
    movementFreeform: String?,
    gripWidth: GripWidth?,
    gripOrientation: GripOrientation?,
    customDisplayName: String?,
): String {
    if (!customDisplayName.isNullOrBlank()) return customDisplayName.trim()
    // concatenate non-none labels; append grip parenthetical
}
```

Store both `displayName` and structured components so renames and analytics do not lose semantics.

---

## 7) Catalog vs user-created

| Source | `isBuiltin` | Structured fields | Editable by user |
|--------|-------------|-------------------|------------------|
| Shipped catalog | `true` | Pre-filled structured fields + rich metadata | Rename/hide; fork to user copy if editing instructions |
| User structured | `false` | User-selected | Full edit |
| User free-form | `false` | Null (until converted) | Full edit |

Built-in exercises are loaded from the **exercise catalog bundle** (`EXERCISE_CATALOG.md`); the naming framework is the pattern for gaps not in the catalog.

---

## 8) Testing checklist

- Generated names match examples in §2.
- Free-form exercise logs and charts correctly under its `displayName`.
- Duplicate warning fires for near-identical structured exercises.
- Grip modifiers omitted when both width and orientation are `none`.
- Converting free-form → structured preserves history (`ExerciseKind` id stable on edit policy TBD: prefer same id with metadata update).
