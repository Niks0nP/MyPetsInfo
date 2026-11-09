# Pet Profile as the Pet Data Root

* **Author:** Niks0nP
* **Date:** 2026-11-09

## Overview

Modernize the pet area from a two-field, single-record form into the home for one pet's identity, care history, weight, and health records. The first user experience remains focused on one current pet and works fully offline with persistent storage on one device. Internally, however, every pet-owned record and operation must have an explicit owner so a later multi-pet interface can be added without another ownership migration. Server synchronization may be added later, but it is not part of this release.

The root profile shows an app-owned avatar and cover image, name, species, breed, sex, birth date, color, description, latest weight and trend, and a neutral health summary. It is read-only by default. Editing profile facts and images is a separate save/cancel flow, while weight history and health records use their own screens. The supplied photo-heavy reference guides hierarchy only; it does not add a gallery, social features, or a requirement to copy the layout exactly.

This is primarily a data ownership and migration change, not a visual refresh. The current Room schema fixes the pet ID at `1`, retrieves only that row, stores tasks and completions without pet ownership, and has no registered migration. The work must therefore begin with data integrity and isolation before the richer profile UI.

## Analysis

### Core weaknesses in the current design

The existing implementation assumes a global pet at every layer:

* `PetEntity` defaults to ID `1`, and the DAO queries `WHERE id = 1`.
* `CareTaskEntity` has no `petId`; task and completion queries are global.
* `AppState` holds one pet, one task list, and one completion set with no active-pet context.
* The Room database is version 1 and registers no migration.
* Pet saving uses `INSERT OR REPLACE`. In SQLite, replacement can delete and reinsert the parent row, which would be dangerous after child foreign keys are added.
* Task completion cleanup is performed as two independent repository writes rather than by an enforced relationship and transaction.
* The current pet screen is always editable, has only name and species, and contains hard-coded Russian strings.

Adding columns to `PetEntity` without changing these assumptions would produce a modern-looking singleton while leaving schedules, completions, reminders, weight, and health unable to distinguish pets.

### Ownership and integrity model

The pet row becomes the aggregate root. Pet IDs should be generated global IDs, while legacy pet ID `1` remains unchanged during migration. Every pet-scoped DAO and repository operation must accept `petId`; no production query should silently assume pet `1` or return unscoped pet-owned data.

Recommended ownership is:

* `pets` owns profile facts and the current avatar/cover references.
* `care_tasks.petId` references `pets.id` and is indexed.
* `task_completions` references its task and normally derives pet ownership through that enforced relationship. A second independent `petId` should not be stored unless a composite foreign key prevents task/pet disagreement.
* `weight_measurements.petId` references `pets.id` and is indexed with its measurement date or timestamp.
* `health_records.petId` references `pets.id`; typed detail rows reference their health record.
* Media metadata, if placed in a separate table, references `pets.id` and permits only one avatar and one cover per pet in this release.

Foreign keys, indices, and transaction boundaries should replace repository conventions. Pet deletion remains out of the first UI until its consequences are designed. An archive policy is safer than immediate hard deletion because relational rows, alarms, and media files cannot be removed in one atomic transaction. If permanent deletion is later supported, database changes must be transactional, alarm cancellation deliberate, and file cleanup idempotent and recoverable.

The app still needs two different task query concepts: active-pet tasks for Today and Schedule, and all enabled tasks across pets for boot-time reminder rescheduling. Global task IDs can continue to identify alarms. Notification content and routing should retain pet context so identical tasks for different pets are not ambiguous later.

### Migration and rollback contract

A tested, non-destructive Room v1-to-v2 migration is a release blocker. It should:

1. Preserve the existing pet row or create legacy pet ID `1` when tasks exist but the pet row has not yet been created.
2. Add the new optional profile fields without changing the existing name and species.
3. Rebuild `care_tasks` with non-null `petId`, assigning every legacy task to pet `1` while preserving task IDs.
4. Rebuild completions with the chosen task foreign key and preserve their composite identity and values.
5. Create weight, health, and any media metadata tables with foreign keys and indices.
6. Verify row counts and `PRAGMA foreign_key_check` before removing old tables.
7. Export and commit the new Room schema and test migration from the checked-in version 1 schema with representative data.

Destructive fallback is not acceptable. Once the new schema is opened, an old binary cannot safely read it; rollback should therefore mean disabling or repairing UI inside the upgraded binary and shipping a forward migration when needed, not reinstalling schema version 1.

### Profile and navigation experience

The profile root should be scannable rather than a long form. It contains the cover, overlapping avatar, identity and summary facts, About card, latest weight and change, and factual health summaries that link to details. “No records yet” must not be presented as “healthy.” Full measurement and health histories stay off the root screen.

An explicit Edit action opens a dedicated editor. Name is required; species, breed, sex, birth date, color, and description may be unknown and should have documented optionality and length limits. Birth date is a date-only value, cannot be in the future, and age is derived. Sex uses a stable persisted code with an unknown/not-specified value; reproductive status is a separate concept and is outside the initial scope unless explicitly added. Species, breed, and color can begin as localized free text rather than requiring an offline taxonomy.

Save validates and commits a complete draft while preserving the pet ID and untouched fields. Cancel discards the draft. Leaving with unsaved changes needs a defined confirmation. Loading, saving, validation, low-storage, missing-image, and persistence failures must preserve user input and provide actionable messages rather than raw exception text.

### Offline image storage

Avatar and cover are managed assets, not external picker links. Each platform picker passes selected content to a common `PetMediaStore` contract with Android and iOS implementations. The store copies the image into app-private storage, normalizes orientation, strips unnecessary metadata, validates format and dimensions, downsamples to bounded sizes, and provides a thumbnail or display-sized variant. Room stores an opaque asset identifier or controlled relative path, not image bytes, absolute external paths, or permission-dependent URIs.

Replacement follows a failure-safe sequence: write a temporary file, validate it, atomically promote it, update the database, and only then remove the old asset. A database failure removes the new asset and leaves the old profile unchanged. Missing or corrupt files render deterministic placeholders. Cleanup must cover replaced assets, pet deletion, and crash-created orphans without deleting currently referenced files. Full camera capture, interactive cropping, retaining originals, a gallery, captions, and general attachments are not implicit requirements.

Local storage means the app persists its data and managed media on the device and does not provide an account or server synchronization in the first release. Future server synchronization is possible, so record identifiers and repository boundaries should remain stable, but the local schema should not add speculative remote IDs, tombstones, or conflict metadata yet. Android currently enables platform backup and iOS storage may also participate in device backup; operating-system backup remains a separate privacy and consistency decision. The database and managed media must always be included or excluded together so restored rows never point to absent images.

### Weight history

Weight is stored as a separate dated measurement, not a mutable field on the pet. Use a generated measurement ID, `petId`, a date or explicit date/time, an integer canonical unit such as grams, and an optional note. Convert and round only at the UI boundary. Index measurements by pet and time, provide bounded queries for the latest value and chart range, and keep full-history loading separate from the profile summary.

The same-day policy must be chosen before schema work. The smaller first version allows one reading per pet per date and edits that row, enforced by a unique index. If several readings per day are required, time must be captured and used for deterministic ordering instead of allowing ambiguous duplicates.

The chronological list is the primary, precise representation. A simple graph or sparkline is derived from pure common-code calculations and appears when there are at least two points. Empty, one-point, equal-value, edited, and deleted histories must be defined. “Change” should state its baseline, such as the previous measurement or the selected range.

### Health record model

The concept mixes point-in-time events with ongoing conditions. A veterinary visit naturally has a date, optional time, reason, examination result, medicines, and appointment notes. An illness and an allergy are ongoing conditions with a start date, an optional end date, and an active/resolved state. A medicine entry is a point-in-time event in the first version rather than a reusable treatment course. Forcing every category into one wide row would create invalid combinations, while isolated tables would duplicate ownership, dates, notes, and timeline behavior.

Use a common `health_records` envelope for ID, `petId`, stable category code, event/recorded date, optional time, title or summary, notes, and audit timestamps. Add typed detail rows or constrained domain models for category-specific fields. Illness and allergy details store their period and state. A veterinary visit has reason, examination result, appointment notes, and zero or more visit-only medicine entries. Standalone medicine records are dated one-time events; they are not reusable treatment courses in this version and must not be stored as comma-separated text.

The repository can merge typed records into one chronological timeline while each editor exposes only relevant fields. This resolves the contradiction between a coherent health history and category-specific validation. The profile summary remains factual: recent visit, latest vaccination record, recorded active items only when the model supports status, and record counts. It must not offer diagnosis or infer wellness from missing data.

### Accessibility, localization, privacy, and testability

The photographic layout must remain usable without vision, color perception, or chart interpretation. Essential text should not sit over arbitrary imagery unless a reliable scrim preserves contrast. Avatar, cover, edit, replace, remove, and navigation actions need meaningful labels and at least 48 dp targets. The overlapping header needs logical focus order and must survive large font sizes. Sex, health state, and weight change cannot be communicated by color or icon alone. The weight graph requires a text summary and accessible chronological list.

All new visible strings, units, enum labels, errors, and content descriptions must use the existing English/Russian resource system. Medical text, pet names, and raw file paths must not be written to logs or notification payloads. The first release should explicitly state whether normal platform sandboxing is sufficient or encryption at rest is a separate requirement.

Test seams should be designed before implementation. Inject clock/time-zone and media storage; keep graph calculations pure; split the current mixed DAO, repository, and app-wide state by capability. The minimum verification set includes:

* Migration fixtures for empty v1 data, pet-only data, tasks/completions without a pet row, and full non-ASCII legacy data.
* Two-pet isolation tests for every query and mutation, even while the production UI remains single-pet.
* Foreign-key, transaction, update-without-child-loss, and deletion-policy tests.
* Image import, cancel, replace, corrupt input, low storage, missing file, process restart, and orphan cleanup tests on Android and iOS.
* Weight unit, bounds, same-day policy, ordering, trend, and zero/one/many-point tests.
* Health category validation, visit/medicine atomicity, and chronological ordering tests.
* State tests for failure recovery and pet switching, plus Compose semantics and layout tests for compact/wide screens, dark theme, long content, and increased font scale.

### Inventive pattern resolution and Ideal Final Result

The central contradiction is a single-pet first release that must not preserve singleton architecture. Resolve it through segmentation and an intermediary: make `PetId` mandatory at persistence and state boundaries now, but bind the first UI to one active pet. A later switcher changes only selection and navigation, not ownership or storage.

The contradiction between one health timeline and category-specific correctness is resolved with a shared envelope plus typed details. The contradiction between durable images and transactional consistency is resolved with an app-owned media store, staged writes, atomic promotion, and compensating cleanup. The contradiction between a rich visual profile and accessibility is resolved through read/edit separation, progressive disclosure, and parallel textual representations.

The Ideal Final Result is that every pet-specific operation is structurally scoped to a pet, legacy data survives unchanged, image failures never destroy the previous profile, and the profile remains understandable without its photos or chart. Multi-pet UI, sync, and richer medical workflows can then be added as separate features instead of requiring another foundation rewrite.

## Goals vs. Non-Goals

* **In-Scope Goals:**
  * Make pet the enforced owner of profile facts, tasks, weights, health records, and managed media.
  * Preserve all existing pet, task, and completion data through a tested non-destructive migration.
  * Keep the first UI focused on one active pet while making internal APIs and tests support at least two pets.
  * Provide separate read and edit experiences for profile facts, avatar, and cover.
  * Store avatar and cover in app-private managed storage with bounded decoding and failure-safe replacement.
  * Provide dated weight CRUD, chronological history, deterministic change, a graph, and an accessible list.
  * Provide locally stored records for vaccinations, illnesses, allergies, medicines, and veterinary visits, with the visit fields named in the concept.
  * Keep profile summaries factual, localized, accessible, and useful offline on Android and iOS.
  * Preserve and clarify pet ownership in reminders and task completions.
* **Out-of-Scope Non-Goals:**
  * Accounts, server sync, sharing, export, or conflict resolution in the first release.
  * A pet creation/switching/deletion UI in the first delivery, even though the data layer supports several pets.
  * A pet photo gallery, arbitrary attachments, document scanning, camera workflow, or retained source originals.
  * Diagnosis, wellness scoring, treatment recommendations, weight prediction, or other medical judgment.
  * Medication reminders, provider directories, vaccine catalogs, or external health services unless separately specified.
  * Interactive chart zoom, tooltips, smoothing, targets, or forecasting.
  * Sync-oriented fields such as account IDs, remote IDs, tombstones, or conflict metadata.
  * A broad module or navigation-framework rewrite solely for this feature.

## Alternatives Weighed

### Add fields only to the existing singleton pet

Rejected because tasks, completions, state, reminders, and DAO queries would remain global. It would improve appearance without creating a real pet root and would force another migration before multi-pet UI.

### Build the complete multi-pet UI now

Rejected for the first delivery because creation, switching, deletion, empty-state onboarding, and per-pet notification routing add product scope without being necessary to prove the ownership model. The foundation will still be tested with two pets.

### Put `petId` directly on every row without relational constraints

Rejected because duplicated ownership can disagree, especially between a task and its completion. Ownership should be direct only for independent pet children and inherited through enforced relations where appropriate.

### Use one wide health table with many optional columns

Rejected because visit, vaccination, condition, allergy, and medicine fields have different rules, making malformed combinations easy. A common envelope with typed details keeps a unified timeline without losing validation.

### Use five unrelated health tables with no shared identity

Rejected because it duplicates common ownership, ordering, summary, notes, and timeline logic. Typed detail tables should share a common health-record identity.

### Store images as Room blobs

Rejected because large binary data would increase database size, migration cost, query memory, and restore complexity.

### Persist picker URIs or external absolute paths

Rejected because access may expire, source files may disappear, and the representation is not portable across Android and iOS.

### Infer a gallery from the visual reference

Rejected because the request names avatar and cover only. Gallery entities, ordering, captions, and lifecycle are speculative scope.

### Store weight as floating-point kilograms

Rejected because rounding and equality behavior would vary across storage, calculations, and display. A canonical integer unit yields deterministic history and graphs.

### Pre-build sync architecture

Rejected because possible future server synchronization does not yet justify account tables, remote IDs, tombstones, or conflict resolution. Stable record identity, explicit ownership, and repository boundaries are sufficient foundations until synchronization has concrete requirements.

### Use destructive migration or rely on binary rollback

Rejected because the app's value is its offline records. Destructive fallback loses existing data, and the old binary cannot safely open the upgraded schema.

## Key Decisions & Staging for Next Steps

### Decisions to carry into design drafting

* The first production UI has one active pet; data, repository, state, and tests are multi-pet-safe.
* Legacy pet ID `1` is preserved. New records receive stable generated identifiers; the detailed design must select an identifier format that will not force identity replacement if server synchronization is added later.
* Every pet-owned operation takes explicit pet context. Completions normally inherit their pet through an enforced task relationship.
* The upgrade uses a non-destructive Room v1-to-v2 migration with foreign keys, indices, exported schema, and migration fixtures.
* Profile is read-first with a separate editor. Name is required; other profile facts may be unknown and use documented nullable/stable encodings.
* Avatar and cover are copied to app-private managed storage; no image bytes or external picker references are stored in Room.
* Weight uses an integer canonical unit and a separate history table. The history list remains the accessible source of truth for the graph.
* Health uses a common record envelope and typed category details. Veterinary visits contain date, optional time, reason, examination result, visit-only medicine entries, and notes. Illnesses and allergies are conditions with start/end dates and active/resolved state. Standalone medicine records are one-time dated events.
* The first release persists data and managed media locally without accounts or server synchronization. Future sync is possible, but server-specific schema is deferred until its requirements exist.
* Health summaries are factual and never infer that missing records mean good health.
* Multi-pet switching, gallery, sync, diagnosis, and advanced charting remain separate future work.

### Product decisions still required before schema implementation

1. Should operating-system backup and device transfer include the local database and managed media, or should both be excluded?
2. Is one weight measurement per pet per date sufficient, and which display units must the UI support?
3. Does a medical event mean only a veterinary visit, and can it represent a future appointment as well as a completed visit?
4. Which fields must a one-time medicine event contain beyond its date and name, such as dose or note?
5. Is automatic crop/positioning sufficient for avatar and cover, or is interactive crop/reposition required?
6. What is the eventual pet deletion policy: archive, restricted deletion, or irreversible cascade?
7. Is simultaneous Android and iOS delivery required for every stage?

### Staged delivery

1. **Resolve invariants:** settle the open product decisions, field validation, privacy/backup meaning, deletion policy, and acceptance criteria.
2. **Ownership foundation:** implement the migration, generated pet IDs, pet-scoped APIs and state, foreign keys, indices, transactional writes, and two-pet isolation tests while keeping visible behavior close to the current app.
3. **Core profile:** add resource-backed profile fields, read-only overview, dedicated editor, validation, save/cancel behavior, and accessible empty/error states.
4. **Managed media:** add the cross-platform media store, avatar first and cover second, bounded processing, replacement rollback, missing-file fallback, and cleanup tests.
5. **Weight:** add measurement CRUD and history, then the pure trend mapper, textual summary, and accessible graph.
6. **Health:** establish the common envelope and veterinary-visit vertical slice, then add vaccination, illness, allergy, and medicine behaviors after their lifecycle decisions are fixed.
7. **Hardening gate:** verify migration from real v1 shapes, fresh-versus-migrated schema parity, cross-pet isolation, reminder ownership, failure injection, Android/iOS behavior, large text, screen readers, contrast, and privacy/backup configuration before calling the feature complete.
