# Assemble the base from existing files

All paths below are relative to the kit root resolved as described in SKILL.md.
Read each source before adapting it; the files themselves own code and
version values. Create only the existing `shared`, `androidApp` and `iosApp` layout.

## Copy these files

| Responsibility | Reference paths |
| --- | --- |
| Build shell and pins | `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml`, `gradle.properties`, `settings.gradle.kts`, `build.gradle.kts`, `.editorconfig`, `.gitignore` |
| Module configuration to simplify | `shared/build.gradle.kts`, `androidApp/build.gradle.kts` |
| Android host | `androidApp/src/main/AndroidManifest.xml`, `androidApp/src/main/kotlin/me/kmpstarter/MainActivity.kt`, `StarterApplication.kt` in that same directory |
| iOS host and Xcode integration | `iosApp/iosApp.xcodeproj/project.pbxproj`, `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme`, `iosApp/iosApp/ContentView.swift`, `iOSApp.swift`, `Info.plist`, `PrivacyInfo.xcprivacy` in that same source directory |
| Shared host and entry | `shared/src/iosMain/kotlin/me/kmpstarter/MainViewController.kt`, `shared/src/commonMain/kotlin/me/kmpstarter/App.kt` |
| DI and navigation to simplify | Under `shared/src/commonMain/kotlin/me/kmpstarter`: `di/StarterGraph.kt`, `ui/navigation/AppNavigation.kt`, `ui/navigation/Destination.kt` |
| Reusable UI and state | Under that same common package: `presentation/BaseViewModel.kt`, `ui/theme/Theme.kt`, `ui/components/ScreenScaffold.kt`, `ScreenLoading.kt`, `ScreenError.kt` in that same component directory |
| Policy to adapt | `AGENTS.md`; use the root README for toolchain/check instructions when writing the new app's short README |

Keep the wrapper executable. Copy named source files, not entire directories with
caches, build outputs, local.properties, signing settings, credentials or user data.
Do not copy demo `data`, `domain`, article/detail UI, ArticleFailureMessage, tests,
schemas or Room-generated lint exemptions into a base without those features.

## Simplify build configuration

In the new `shared/build.gradle.kts`, retain KMP, Android KMP, shared lint, Compose,
Kotlin serialization and Metro plugins. Retain targets, framework, SDK/JVM settings,
resources, lifecycle, navigation, Material and common test configuration.
Serialization plugin and JSON remain necessary for typed navigation.

Remove only optional capability configuration:

- Room and KSP plugins; Room/SQLite dependencies; all three Room compiler KSP
  configurations; schemaDirectory; the Robolectric SQLite JVM substitution.
- Ktor common dependencies, Android/Darwin engines and MockEngine test dependency.
- The Robolectric test dependency, which the Room skill adds back with reopen tests.

Remove client-core from `androidApp/build.gradle.kts` and INTERNET permission from
its manifest. Remove unused Ktor, Room, SQLite, KSP and Robolectric versions/aliases
from the copied catalog, preserving the other pinned versions. Root Gradle and
settings retain the reference's three-part layout; do not add optional modules.

## Simplify entry points

- In `StarterGraph`, remove feature providers, ViewModel factories and resource
  arguments. Keep a Metro dependency graph and its creation helper. The existing
  graph-factory pattern can use a parameterless Factory/create method; do not
  construct a client or database for an empty app.
- Both hosts create that graph once outside composition and continue passing it
  to App. Keep Android edge-to-edge and the existing Kotlin/Swift embedding.
- Retain the App → theme → navigation flow. Replace article destinations with one
  serializable Home destination and one HomeRoute. A static route needs no ViewModel.
- Use the existing ArticlesScreen as the composition reference when writing a small
  HomeScreen: shared ScreenScaffold and standard Material text, with no article list,
  data loading or note editor. Do not copy feature code just to leave it unused.
- Create `commonMain/composeResources/values/strings.xml` with English Home copy and
  the `back`, `loading`, `retry` keys required by the copied common components.
  Select those existing strings from the reference; optional translations use the
  same keys. Omit demo-specific strings. Review the retained privacy manifest
  against the new app's dependencies and actual data use before distribution.

## Rename and hand off

Apply the identity checklist in `docs/architecture/adapting-the-example.md` steps
1–2: package directories/declarations/imports, resource package, module namespaces,
Android application ID, Xcode bundle ID, framework bundleId, project/app labels.
Keep `StarterShared` and its Swift imports together. Change only app identity;
references to the source kit must continue to identify **kmp-starter**.

The new AGENTS.md owns local rules. Replace its kit-maintenance section with the
ongoing-development section below and update article/note-specific examples and
doc paths; storage rules apply only if storage is added. Record capabilities
initially as `base`; add `networking` or `room` after their integration. Provenance
is documentation, not a build switch or dependency. Do not claim that an
uncommitted kit matches HEAD.

### Ongoing development handoff

The target root `AGENTS.md` must retain the actual architecture and code-writing
rules locally: layer ownership and dependency direction, use cases for ViewModel
data operations, Route/Screen separation, focused functions and helper extraction,
constants, failure/cancellation handling, shared Material components, tests and
formatting/verification. A link to the kit is not a replacement for these rules.

Include a **Starter reference for ongoing development** section in that same file
with these instructions, adapted to the target's real documentation paths:

- For implementation, bug fixes and refactoring of application code, first read
  this AGENTS.md, `docs/starter-kit.md` and `starter-provenance.json`. These rules
  apply to ordinary edits as well as new features or infrastructure.
- Resolve the matching complete kit using the recorded reference access and
  version/revision. Read its `skills/kmp-develop-feature/SKILL.md` for feature code
  changes, including behavior-preserving refactors. Consult relevant rows of its
  `docs/architecture/canonical-examples.md` and the linked source/test files before
  editing. Read only examples relevant to the change; for build or host changes,
  consult the matching reference configuration instead.
- Read the kit's networking or Room skill when changing the corresponding
  capability. Follow this project's rules and product decisions when they differ
  from the demonstration; do not import its article/note business policy.
- If the required kit cannot be read or its recorded revision cannot be resolved,
  report the missing reference and request access. Do not silently use another
  version or claim to have checked unavailable examples. Independent work may
  continue under the local rules while access is resolved.

The target `docs/starter-kit.md` must explain how to locate that complete kit and
its skills/canonical map, consistent with `starter-provenance.json`. Use a portable
repository URL plus revision, package identity/version with its access method, or
a documented relative checkout location; do not depend on the starter author's
absolute filesystem path. Record actual source modifications separately. The
handoff documents provide reference access, not automatic skill installation or a
build dependency.

Before delivery, read the generated AGENTS.md as a fresh agent would: follow its
handoff links, resolve the recorded kit, open a relevant canonical example and
confirm that routine refactoring is covered and local function/layer rules remain.
Report missing access as a handoff limitation rather than claiming it is verified.

Validate with the skill's checks. An app without test sources needs no invented
tests. Keep the runnable reference and this file-selection recipe as the only
maintained sources; disposable evaluation apps stay outside the repository.
