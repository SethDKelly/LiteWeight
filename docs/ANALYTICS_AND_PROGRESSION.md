# LiteWeight Analytics and Progression

This document outlines how to compute and present basic analytics for workout progression.

## 1) Analytics Scope (v1)

- Visual progression over time for a selected exercise.
- Visual personal records (PRs) for a selected exercise/lift.
- Lightweight consistency views (sessions per week or streak-like counters).

## 2) Source Data Contract

- Only use persisted workout logs as analytics input:
  - `WorkoutSession`
  - `WorkoutExerciseEntry`
  - `WorkoutSetEntry`
- Do not allow direct user edits of computed analytics values.

## 3) Core Metrics

For each `ExerciseKind`, define one primary progression metric by unit type:

- **Weight-based**: top set load, volume (`sum(load * reps)`), optional estimated 1RM.
- **Time-based**: best duration and time trend.
- **Bodyweight-only**: reps trend, set count, and consistency trend.

Keep formulas fixed and documented to avoid user confusion.

## 4) PR Rules

- PR event types:
  - max load
  - max reps at load
  - best estimated 1RM (if enabled)
  - longest time (time-based exercises)
- Tie-breaking:
  - prefer most recent completion timestamp for equal value
  - store both metric value and source set/session id

## 5) Projection Pipeline

- Trigger recompute on:
  - session completion
  - correction of a completed session
  - exercise/unit rule updates
- Use incremental updates where possible:
  - recompute only affected exercise/time windows
- Projection outputs:
  - chart series table/view
  - PR timeline table/view
  - summary snapshot table/view

## 6) Chart Requirements (v1)

- X-axis: date/time
- Y-axis: selected metric value
- Controls:
  - window (4 weeks, 12 weeks, 6 months, all)
  - metric selector (where multiple are valid)
- UX behavior:
  - show gaps as no-data, not zero
  - annotate PR points directly in chart

## 7) Validation and Testing

- Unit tests for metric formulas and PR tie-breaking.
- Golden-data tests:
  - fixed sample sessions with expected chart outputs.
- Regression tests:
  - corrections to historical sessions update projections correctly.
- Performance tests:
  - acceptable chart query latency on large local histories.

## 8) Known Trade-offs

- Estimated 1RM improves comparability but may be noisy for some users.
- Too many metrics reduce clarity; keep v1 small and expandable.
- Projection tables add storage cost but keep UI fast and deterministic.

## 9) ML-assisted analytics (phase 2+)

Layer **on-device ML** on top of rule-based projections—never replace the source-of-truth pipeline in v1.

### Candidate use cases (prioritized)

1. **Next-set load suggestion** — given recent sessions for an exercise, suggest weight/reps for the working set.
2. **Plateau / stagnation detection** — flag flat or declining trends over N weeks.
3. **Deload recommendation** — elevated fatigue signals (volume spike + performance drop).
4. **Routine refinement** — rank exercises for `MovementSlot` fill during `RoutineGenerator` (optional ML ranker).

### Architecture

```text
WorkoutSession complete
  → rule-based Progression recompute (required)
  → feature extraction (volume, e1RM trend, rep PRs, recency)
  → on-device model inference (TensorFlow Lite / LiteRT)
  → MLInsightRecord persistence
  → UI cards with explanation + dismiss/accept
```

### Guardrails

- **Explainability:** every insight includes human-readable “why.”
- **Confidence threshold:** hide or soften low-confidence outputs; fall back to rules.
- **Cold start:** new users see rule-based analytics only until minimum session count per exercise.
- **Privacy:** train/infer on-device; no upload of workout logs without explicit opt-in.
- **User control:** dismiss insights; disable ML features in settings.

### Testing

- Golden fixtures for feature extraction.
- Model output bounds tests (suggestions within sane % of recent loads).
- A/B or shadow mode during development to compare ML vs rule suggestions.

## 10) Implementation status

**Shipped (Phase 4):**

- `AnalyticsProjectionService` runs on session completion.
- Projection tables: `exercise_session_snapshots`, `exercise_pr_events`, `exercise_analytics_summaries`.
- `MetricsCalculator` and `PrDetector` with unit tests.
- Exercise detail UI: progression line chart (Compose Canvas), PR callouts, metric/window chips, PR timeline.
- Home: sessions completed this week.

**Not yet shipped:** ML-assisted insights (§9), golden-data regression fixtures, performance benchmarks on large histories.
