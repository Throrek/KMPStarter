---
name: kmp-room
description: Use when adding or changing Room local persistence, schemas, migrations or database-backed repositories in a KMPStarter Android/iOS app. Room can be used without networking or added later to an HTTP app.
---

# Add or extend Room

Resolve this file's real path first (repository discovery may use a symlink), then
resolve `../..` from its directory as the **kit root**. Read the target
AGENTS.md, catalog, existing schema/DAOs, graph, both hosts and provenance. Work
additively in an existing app; preserve its HTTP behavior, UI and regression tests.

## Contract and references

Define stored fields, identity, validation, required queries and whether a database
has already shipped. Use that product contract; the example stores one note per
article and does not specify favorites, deletion, caching or synchronization.

| Need | Canonical source |
| --- | --- |
| Database, driver and construction | [common](../../shared/src/commonMain/kotlin/me/kmpstarter/data/local/StarterDatabase.kt), [Android](../../shared/src/androidMain/kotlin/me/kmpstarter/data/local/StarterDatabase.android.kt), [iOS](../../shared/src/iosMain/kotlin/me/kmpstarter/data/local/StarterDatabase.ios.kt) |
| Entity and DAO | [entity](../../shared/src/commonMain/kotlin/me/kmpstarter/data/note/local/ArticleNoteEntity.kt), [DAO](../../shared/src/commonMain/kotlin/me/kmpstarter/data/note/local/ArticleNoteDao.kt) |
| Boundary mapping | [repository](../../shared/src/commonMain/kotlin/me/kmpstarter/data/note/RoomNoteRepository.kt), [domain](../../shared/src/commonMain/kotlin/me/kmpstarter/domain/note/NoteRepository.kt) |
| Behavior and disk evidence | [repository tests](../../shared/src/commonTest/kotlin/me/kmpstarter/data/note/RoomNoteRepositoryTest.kt), [Android reopen](../../shared/src/androidHostTest/kotlin/me/kmpstarter/data/note/NotePersistenceTest.kt), [iOS reopen](../../shared/src/iosTest/kotlin/me/kmpstarter/data/note/NotePersistenceTest.kt) |

## Integration

1. For a fresh base, copy only `room`, `sqlite`, `ksp`, `robolectric` versions and
   their matching library/plugin aliases from the kit [catalog](../../gradle/libs.versions.toml).
   In mature apps keep compatible pins. Apply Room/KSP to shared; add Room runtime
   and bundled SQLite to commonMain. Adapt the three processor configurations
   `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` and schemaDirectory from the
   [shared build](../../shared/build.gradle.kts).
2. Implement product entities/DAO under `data/<feature>/local`, database under
   `data/local`, and domain contracts/use cases separately. Share filename/version
   through the database companion; SQL stays in DAO annotations using entity table
   constants. Keep SDK types out of domain. Propagate cancellation and unknown
   failures; preserve causes for expected storage errors.
3. Adapt production builders with optional test file paths. Create one database
   outside composition in both hosts, provide it through the existing graph
   factory, and add repository bindings. Retain any client parameters/providers.
   The [reference graph](../../shared/src/commonMain/kotlin/me/kmpstarter/di/StarterGraph.kt)
   shows both resources; copy only what the target needs. Storage operations go
   through use cases. Local features need no HTTP read before opening.
4. Export the actual schema. New databases start at their initial version;
   released schemas need an explicit migration and a migration test. Never solve
   a migration by deleting user data. Review changed SDK/privacy requirements.
5. Adapt repository behavior tests and real reopen tests using production builders
   on both platforms. Add Robolectric to androidHostTest and the host-only bundled
   SQLite JVM substitution from the reference build. Run target AGENTS checks,
   including shared lint. Only if generated Room RestrictedApi diagnostics occur,
   adapt the exact generated-file exemption in [lint.xml](../../shared/lint.xml).
   Compile/link iOS tests when no booted device exists, and report runtime blocked.
6. Record `room` in provenance and schema/product decisions in the handoff doc.
   Re-run existing HTTP tests when integrating later. Do not replace their client,
   feature screens or domain flow with the example's combined details editor.

For new UI use [develop-feature](../kmp-develop-feature/SKILL.md). Room alone adds
no Ktor, INTERNET permission or remote domain feature.
