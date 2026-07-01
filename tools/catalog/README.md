# Exercise catalog tooling

Curated source files and import scripts for the built-in exercise database.

**Spec:** `docs/EXERCISE_CATALOG.md`  
**Output:** `app/src/main/assets/catalog/exercise_catalog_v{N}.json`

## Workflow (planned)

1. Author or transform entries under `source/` (JSON; ExRx directory as curation reference).
2. Run validation/import task (Gradle) — checks schema, muscle slugs, duplicate `catalogId`.
3. Emit versioned bundle into `app/src/main/assets/catalog/`.
4. App `CatalogSeeder` imports on first launch / catalog version bump.

Do not commit scraped third-party text without licensing review.
