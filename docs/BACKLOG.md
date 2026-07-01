# LiteWeight Backlog

Ideas and features **not yet scheduled** in `IMPLEMENTATION_PLAN.md`. Use this document to capture future work, rough scope, and open questions before promoting items into a formal phase.

**How to use**

- Add new ideas as sections below, with priority, problem statement, and acceptance hints.
- When an item is committed to a phase, move the detail into `IMPLEMENTATION_PLAN.md` and leave a short “Promoted to Phase N” note here or remove the entry.
- Prefer **offline-first** designs; network features must not block core logging.

**Priority key:** P0 critical · P1 high · P2 medium · P3 nice-to-have

---

## 1) Data backup, restore, and auto-sync — P0

### Problem

Users accumulate months of workout history, custom exercises, programs, and progression state locally. Today:

- Room uses `fallbackToDestructiveMigration()` during pre-release (schema bumps wipe data).
- There is no user-controlled export, import, or cloud backup.
- Device loss, uninstall, or a failed migration means **unrecoverable** training data.

Users need a trustworthy way to **back up**, **restore**, and optionally **keep data synced** across devices without sacrificing the offline-first core.

### Goals

| Goal | Description |
|------|-------------|
| **Manual backup** | User can export a portable snapshot on demand (file share, Downloads, SAF picker). |
| **Manual restore** | User can import a snapshot on a fresh install or after data loss, with clear preview and confirmation. |
| **Auto-sync (optional)** | Background sync to a user-chosen destination when enabled; no sync required for daily logging. |
| **Privacy** | User owns the data; explicit opt-in for cloud; document what leaves the device. |
| **Integrity** | Backup is versioned, checksum-validated, and tied to schema/catalog versions. |

### Non-goals (initial slice)

- Real-time multi-device collaborative editing.
- Social sharing of workouts.
- Server-side analytics or coaching dashboards.
- Replacing Room as the on-device source of truth.

### Proposed approach (layered)

#### Layer A — Local export/import (ship first)

- **Format:** versioned JSON (or SQLite export) bundle, e.g. `liteweight-backup-v1.json` + optional gzip.
- **Scope — include user-owned data:**
  - Custom `ExerciseKind` rows and user edits to built-ins (if any).
  - All `WorkoutSession` source rows (sessions, exercise entries, sets).
  - `Program`, `ProgramDay`, `ProgramExercise`, `ActiveProgram`.
  - `ProgressionScheme` / levels (user-created; merge built-in by id).
  - `SubstitutionGroup` / members, `RotationPlan` / slots.
  - `UserPreferences` (kg/lb, prefill defaults).
  - Analytics **projections** (optional — can recompute on restore instead).
- **Scope — exclude or re-seed:**
  - Built-in catalog payload (re-seed from assets by `catalogVersion`).
  - Built-in movement slots and default progression scheme (re-seed).
- **Metadata in bundle:** `schemaVersion`, `catalogVersion`, `exportedAt`, `deviceId` (anonymous), `checksum`.
- **UX:**
  - Settings → **Export backup** → system share sheet / save to file.
  - Settings → **Restore from backup** → file picker → summary (session count, date range, programs) → confirm destructive replace or merge strategy.
- **Merge vs replace:** v1 likely **replace-all** on restore (simpler); document risks. Merge/import-into-existing is a follow-up.

#### Layer B — Platform backup hooks

- Enable Android Auto Backup for eligible preferences (not full DB by default until migrations are stable).
- Document what Google Backup covers vs the explicit export bundle.

#### Layer C — Auto-sync (optional account)

- User signs in (Google, email magic link, or passkey — TBD).
- Encrypted blob stored in object storage (e.g. user-owned Google Drive app folder, S3-compatible, or LiteWeight-hosted — TBD).
- **WorkManager** periodic + on-change sync:
  - Push incremental changes since `lastSyncedAt` (change log or snapshot diff).
  - Pull remote if newer; surface conflict UI if both sides changed since last sync.
- **Offline:** queue outbound ops; sync when network available.
- Settings: **Auto-sync on/off**, **Wi‑Fi only**, **Last synced** timestamp, **Sync now**.

### Technical notes

- Introduce a `sync` or `backup` package: `BackupExporter`, `BackupImporter`, `BackupValidator`, optional `SyncRepository`.
- Consider a **`sync_metadata`** table: `lastExportAt`, `lastSyncAt`, `remoteRevision`, `pendingUpload`.
- Restoration should run in a transaction; re-run analytics projection after import.
- Align with production Room migrations before marketing restore to users (see `DATA_MODEL_AND_STORAGE.md` §5–§6).
- **Encryption at rest** for cloud blobs (client-side key derived from user secret or OS keystore).

### Open questions

- Cloud provider: bring-your-own (Drive) vs LiteWeight backend?
- Single-device vs multi-device concurrent use?
- Conflict policy: last-write-wins, or prompt user per entity type?
- Include photos/attachments in v1? (Likely no.)
- Play Data safety form wording for sync.

### Acceptance criteria (MVP — Layer A)

1. User exports backup file from Settings; file opens and contains expected schema version.
2. User on fresh install imports backup; sessions, programs, and custom exercises match export.
3. Invalid/corrupt file shows clear error; no partial corrupt DB state.
4. Export/import covered by round-trip integration test on fixture data.
5. Documented in app (short) and in `DATA_MODEL_AND_STORAGE.md` once shipped.

### Acceptance criteria (auto-sync — Layer C)

1. User enables auto-sync; completes workout offline; data uploads when online.
2. Second device sign-in offers restore from remote backup.
3. User can disable auto-sync and delete remote copy.
4. Sync errors are visible and non-blocking for logging.

### Related documents

- `DATA_MODEL_AND_STORAGE.md` — §6 Backup, export, retention
- `PROJECT_GUIDANCE.md` — §9 Privacy and safety
- `IMPLEMENTATION_PLAN.md` — Phase 7 (release checklist mentions backup/restore)

---

## 2) Production-safe Room migrations — P0

Replace `fallbackToDestructiveMigration()` with tested incremental migrations before Play release. Pair with backup (§1) so upgrades are safe even when migrations fail.

---

## 3) Rest timer and in-session shortcuts — P1

Rest timer between sets; duplicate last set; skip/reorder exercises during active workout. High value for gym UX; stays entirely on-device.

---

## 4) Free-session mode — P1

Start logging without an active program using the same `WorkoutSession` model. Entry point from Home when no program is active.

---

## 5) Onboarding flow — P1

First-run: units (kg/lb), days per week, equipment profile, optional goal. Seeds `RoutineGenerator` defaults and prefill preferences.

---

## 6) Program-level exercise override persistence — P2

Persist `ProgramExerciseOverride` (documented in `DATA_MODEL_AND_STORAGE.md` but not yet a table). UI to set a program-default substitute per movement slot.

---

## 7) Deload / fatigue user action — P2

User-facing deload week or intensity reduction tied to `ProgressionScheme`; optional tie-in to ML insights when Phase 6 ships.

---

## 8) Plate and bar math helpers — P2

Logging shortcuts for standard increments (2.5 / 5 kg or lb) and bar weight presets in Settings.

---

## 9) CSV export for sessions and PRs — P2

Lightweight export for spreadsheets, separate from full backup bundle (§1). Filter by date range and exercise.

---

## 10) Accessibility pass — P2

TalkBack labels, contrast, font scaling, and large touch targets audit for gym conditions.

---

## 11) ML-assisted insights — P1 (scheduled)

Tracked as **Phase 6** in `IMPLEMENTATION_PLAN.md`. See `ANALYTICS_AND_PROGRESSION.md` §9.

---

## 12) Import from other apps — P3

One-time import from Strong, Hevy, or CSV exports. High effort; depends on stable internal schema and backup format (§1).

---

## Adding new items

Use this template:

```markdown
## N) Title — Px

### Problem
…

### Goals
…

### Proposed approach
…

### Open questions
…

### Acceptance criteria
…
```
