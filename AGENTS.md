# KMPStarter — agent instructions

## Starter kit workflows

- This checkout also packages reusable skills; see `docs/starter-kit.md`.
  For a new app, read `skills/kmp-create-project/SKILL.md`; for API work,
  `skills/kmp-networking/SKILL.md`; for persistence, `skills/kmp-room/SKILL.md`;
  for feature development, `skills/kmp-develop-feature/SKILL.md`.
- The root app is the single source of reference code and configuration. The
  creation skill selects and adapts those files in the destination app; do not
  maintain parallel templates, generators or embedded copies of app code in skills.
  Do not copy build outputs, local settings or credentials into a new app.
- Skills and examples travel as one versioned package. New apps follow
  their own AGENTS.md and record capabilities in `starter-provenance.json`.
  Do not regenerate over an existing app or impose article/note demo decisions.

## Scope and ownership

- Keep the app small: `shared` owns common Compose UI, ViewModels, use cases,
  domain contracts, data and Metro. `androidApp` and `iosApp` are thin hosts.
- Use `ui/<feature>`, `domain/<feature>`, `data/<feature>` and `di` inside `shared`.
  Room entities/DAOs belong to `data/<feature>/local`, the database to `data/local`.
- Domain models must not import Compose, Room, Ktor or platform types. Data maps
  DTOs/entities and expected SDK failures into domain models/errors.
- Every ViewModel data operation goes through a use case, including simple reads.
  This is an intentional exception to Caregiver's allowance of direct repository
  access for simple reads. Local UI state changes remain in the ViewModel.
- Route owns navigation, lifecycle and ViewModel creation; Screen takes immutable
  state and callbacks. ViewModels expose read-only StateFlow and use viewModelScope.
- Cancellation propagates. Map only specific expected I/O failures at their boundary;
  never hide programming failures with broad catches or show cancellation as success.
- For multiple exception types mapped into domain failures, prefer one thin
  `catch (failure: Exception)` and a private nullable mapping function in the owning
  data-layer class, using `when` with grouped types for the same domain failure.
  Return `null` for `CancellationException` and unknown exceptions, and rethrow
  the original exception immediately when the mapping is null. Preserve the
  original cause when wrapping a mapped failure. Put specific types before broader
  parents (for example, transport timeouts before `IOException`). Avoid long
  repeated catch chains and generic mapper frameworks. Keep a specific catch when
  only one type is handled, and separate short catches for distinct recovery actions.
- Keep native code to host, HTTP engine and database construction. New app/test code
  is Kotlin; Swift is only the iOS host. Do not add modules or abstractions speculatively.

## Material UI and shared screen components

- Use the official Material 3 Compose components and `MaterialTheme` tokens for
  colors, typography and shapes. Prefer the latest stable Material 3 release
  compatible with Compose Multiplatform; pin its version independently in the
  version catalog. Do not introduce alpha/Expressive APIs merely for visual novelty.
- Shared screen chrome belongs to `shared/ui/components/ScreenScaffold`: Material
  `Scaffold`, `TopAppBar`, content padding and consumed window insets. Feature
  Screens reuse it; Route still owns navigation, lifecycle and ViewModel creation.
- Toolbar back is a Material `IconButton` with `Icons.AutoMirrored.Filled.ArrowBack`
  and a localized content description, never a text button. Respect the same
  navigation guard as system back, including disabled exit while saving.
- Full-screen loading uses the shared `ScreenLoading`, centered horizontally and
  vertically in the available content area below the app bar. Use a standard
  Material progress indicator; operation progress stays beside its action.
- Reuse `ScreenError` for screen failures and optional explicit retry. Keep error
  content scrollable for small screens and large fonts. Use Material components
  such as clickable `Card`, `Button` and `OutlinedTextField` for feature content;
  do not recreate their visuals, touch targets or interaction semantics.
- Common components take presentation values/callbacks, never feature ViewModels,
  repositories or navigation controllers. Extract actual duplication without a
  generic screen framework. Keyboard insets remain opt-in for editor content.
- Review UI changes for shared component reuse, Material semantics, RTL back icon,
  loading placement, inset handling, dark mode and accessibility. Device checks
  are required to claim visual/runtime verification; compilation is insufficient.

## Constants and configuration

- In Kotlin app and test code, put fixed configuration and domain values in the
  owning class/interface's `companion object` as `const val`, or pass configurable
  values through constructor parameters. Do not use top-level constants or inline
  magic values for file names, URLs, endpoints, timeouts, schema/table names,
  schema versions or domain validation thresholds. Prefer existing SDK constants
  for values defined by an SDK. Keep constants private unless another owner needs them.
- Share each value through its owner: both platform database builders use
  `StarterDatabase.FILE_NAME`; domain ID validation uses `Article.MIN_ID`.
  Function-based builders and native hosts reuse the owning class's constants.
  Do not create a generic Constants object or duplicate a value across platforms.
- This rule excludes Compose layout values (padding, spacing, line counts),
  diagnostic messages and literal test inputs/expected results. Keep user-facing
  text in Compose resources. Build/resource metadata remains in its declarative
  configuration files. Test infrastructure configuration follows the constants rule.
- Room SQL queries are an exception to the constants rule: write the SQL directly
  in the DAO's `@Query` annotation, next to the method it describes. Do not extract
  query text into a `const val`. Reuse the entity's table-name constant in the SQL.
- English is always the default language. Keep the complete English strings in
  `shared/src/commonMain/composeResources/values/strings.xml`. Put translations in
  their language-qualified folders with the same resource keys and file name:
  for example, Polish in `composeResources/values-pl/strings.xml`, German in
  `composeResources/values-de/strings.xml`, never in `values` or `strings.pl`.
  Let Compose select translations from the system locale and fall back to English
  when no matching translation exists; do not hardcode the app locale.
- During review, check new literals and top-level declarations against this rule.

## Tests and change safety

- All test functions use English sentences enclosed in Kotlin backticks:
  `WHEN <condition> THEN <expected outcome>`. Keep `WHEN` and `THEN` uppercase
  and separate words with spaces, including common and platform-specific tests.
  Example: ``fun `WHEN the text is blank THEN the note is not saved`() = runTest { ... }``.
  New tests must follow the same convention. Nontrivial tests have `// Given`,
  `// When`, `// Then` and literal expectations.
- ViewModel tests use real use cases with small fake repositories. Test behavior,
  failed persistence, ordering, validation and cancellation; do not test forwarding,
  getters, framework internals, UI automation or source-string presence.
- Use coroutine virtual time or explicit gates, never real sleeps. Actual Room
  reopen tests must use production builders with a test file path, on both platforms.
- Preserve user-owned dirty/staged changes. Do not reset, commit unrelated changes,
  push, merge or publish without authorization. LifeGenie is a read-only historical
  reference, never a runtime dependency or a source of signing/Firebase/backup policy.
- Do not install/update system tools or start/reset local emulators/simulators.
  Resolve declared Gradle dependencies normally; report missing toolchains honestly.
- Run Gradle builds serially in this checkout. Inspect actual exit status and reports.
  A compiler/unit-test pass does not prove the app ran on a device or Room persisted.

## Function boundaries and formatting

- Keep functions focused on one responsibility. Extract a private named function
  when it makes a long or deeply nested block easier to understand, or gives a
  repeated policy one owner. Share helpers only when callers need the same behavior;
  do not create generic utilities just because a few lines look similar.
- Keep feature-specific composables beside their Screen. Move them to
  `ui/components` only when multiple features actually reuse them. In tests, share
  setup and resource cleanup where useful; keep actions and literal expectations
  visible in each test.
- Spotless with the pinned ktfmt version in `gradle/libs.versions.toml` is the
  formatting authority for Kotlin and Gradle Kotlin DSL. The root `build.gradle.kts`
  selects Kotlin style, four-space indentation, a 100-column wrapping target and
  LF line endings. `.editorconfig` provides matching editor defaults; ktfmt does
  not read it. Other file types are outside this formatter's scope.
- After editing Kotlin or Gradle Kotlin DSL, agents must invoke `./gradlew spotlessApply`
  through the shell tool before final verification. Let the formatter choose
  whitespace, wrapping and import order; do not manually reproduce or override
  its layout, use formatter suppression to preserve personal style, or introduce
  a second formatter. Function extraction and naming remain code-review decisions.
- Inspect the resulting diff and finish with `./gradlew spotlessCheck`. Run Gradle
  serially. If tooling is unavailable, report formatting as blocked; manual edits
  are not a substitute for a successful formatter run. CI runs `spotlessCheck`
  to detect drift without rewriting files.

## Verification

After Kotlin or Kotlin Gradle edits, run formatting and inspect the diff:

```sh
./gradlew spotlessApply
git diff --check
git diff
```

Before completion, run the checks relevant to the current stage:

```sh
./gradlew spotlessCheck :shared:testAndroidHostTest :androidApp:assembleDebug :shared:lintAndroidMain :androidApp:lintDebug :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:iosSimulatorArm64Test -PiosTestDevice=<already-booted-UUID>
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

Run `:shared:lintAndroidMain` explicitly for production common/Android API and
Compose checks; app lint alone misses shared production sources. The standard
`com.android.lint` plugin enables this task. Keep any generated-code suppressions
scoped to the specific issue and generated files; authored sources remain checked.

Canonical code/test map: `docs/architecture/canonical-examples.md`; adaptation:
`docs/architecture/adapting-the-example.md`. Gradle requires JDK 21+ (Metro);
Android bytecode remains JVM 17. No artificial template tests. Simulator tests require an
already available runtime/device; never start one without authorization. Record
unavailable checks as blocked, not passed. See README for toolchain setup.
