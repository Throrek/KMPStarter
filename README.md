# KMPStarter

For a **new application**, use the [starter skills](docs/starter-kit.md): agents
assemble a minimal Compose/Metro/Material app directly from this runnable example,
then add networking, Room or product features as needed. This example is the single
source of code; there is no parallel template or generator to maintain.

Small Android/iOS Kotlin Multiplatform example: an HTTP article list and details
with one local Room note per article. Shared Compose UI, ViewModels, use cases,
domain contracts, data and Metro live in `shared`; native hosts only embed it.
Articles use JSONPlaceholder GET `/posts` and `/posts/{id}`. Notes stay on-device:
that demonstration API simulates writes and is not a notes backend.

## Toolchains

Pinned: Gradle **9.5.0**, Kotlin **2.4.10**, AGP **9.3.2**, Compose **1.11.1**,
Material 3 **1.9.0** (pinned separately), Material icons core **1.7.3**,
Metro **1.4.3**, Navigation Compose **2.9.2**, lifecycle **2.10.0**, Ktor **3.4.3**,
Room **2.8.4** and bundled SQLite **2.6.2**. See `gradle/libs.versions.toml`.
Gradle requires **JDK 21+** because of Metro; Android bytecode remains JVM **17**.
Local verification uses installed OpenJDK **25.0.2**, Xcode **26.6** (17F113),
iPhone Simulator SDK **26.5**, and Android SDK platform **37**. No local tool
installation or update is part of this project.

Set `JAVA_HOME` to an installed JDK 21 or newer (Android Studio's Gradle JDK must
match). On macOS `/usr/libexec/java_home -F -v '21+'` selects a registered installation;
if yours is unregistered, set its actual installation path explicitly:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -F -v '21+')"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$HOME/Library/Android/sdk"
```

Use your SDK's actual path; alternatively use your own ignored `local.properties`.
Select an installed Xcode in Locations or set `DEVELOPER_DIR`. SDK/JDK automatic
installation is disabled; Gradle resolves declared dependencies normally.
Android minSdk is **24**, compileSdk/targetSdk **37**. iOS minimum is **16.0**, with
`iosArm64` and `iosSimulatorArm64` only. Xcode `ARCHS=arm64` matches these targets:
simulator builds require Apple silicon. No CocoaPods or SwiftPM is required.

## Run

Open in Android Studio, choose `androidApp` and an already configured device, then
Run. For a debug APK:

```sh
./gradlew :androidApp:assembleDebug
```

The APK is `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Android needs
only INTERNET permission. `StarterApplication` creates the database, client and
graph once per process; an Activity recreation retains destination ViewModels.

Open `iosApp/iosApp.xcodeproj`, select the shared `iosApp` scheme and an available
arm64 simulator or device, then Run. The Kotlin build phase embeds `StarterShared`
before Swift compilation. Set device signing locally; the project stores no team
or secrets. The Kotlin host creates one graph/database/client outside composition.
To build the host without starting a simulator:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

## Formatting

Formatting is configured per repository in `build.gradle.kts`: Spotless runs
ktfmt **0.63**, pinned separately in `gradle/libs.versions.toml`, with Kotlin style,
four-space indentation, a **100-column wrapping target** and LF line endings.
Long indivisible strings and identifiers can exceed the wrapping target.
The same profile covers Kotlin sources/tests and Gradle Kotlin DSL; generated
build outputs and Gradle caches are excluded. Swift, XML and Markdown are not
formatted by these tasks.

From the repository root, humans and agents use the same commands:

```sh
./gradlew spotlessApply  # Rewrite formatting with the project's formatter.
./gradlew spotlessCheck # Verify formatting without changing files (also in CI).
```

Agents must run the tool after Kotlin edits instead of manually adjusting code
layout; this workflow is recorded in [AGENTS.md](AGENTS.md). Those instructions
require the command to be run, but are not an on-save hook. The PR workflow checks
the result independently. No global formatter or IDE plugin is required.

[.editorconfig](.editorconfig) supplies matching basic editor settings, but ktfmt
does not read it and an IDE's built-in formatter may wrap code differently. Always
use Spotless for the final layout. To change the profile, edit the Gradle settings,
align the editor defaults and run `spotlessApply` followed by `spotlessCheck`.
Extracting functions, choosing names and deciding what to share remain review
decisions; a formatter does not make those structural changes.

References: [Kotlin conventions](https://kotlinlang.org/docs/coding-conventions.html),
[ktfmt](https://kotlin.github.io/ktfmt/),
[Spotless Gradle integration](https://github.com/diffplug/spotless/tree/main/plugin-gradle#ktfmt),
[Codex project instructions](https://developers.openai.com/codex/guides/agents-md).

## Verify

Name every test function as an English sentence enclosed in Kotlin backticks,
with uppercase `WHEN` and `THEN` and spaces between words. This applies to new
and existing common, Android and iOS tests:

```kotlin
@Test
fun `WHEN the text is blank THEN the note is not saved`() = runTest {
    // Given
    // When
    // Then
}
```

Run Gradle serially. After Kotlin edits, format and inspect the diff:

```sh
./gradlew spotlessApply
git diff --check
git diff
./gradlew spotlessCheck :shared:testAndroidHostTest :androidApp:assembleDebug :shared:lintAndroidMain :androidApp:lintDebug
./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:iosSimulatorArm64Test -PiosTestDevice=<already-booted-simulator-UUID>
```

`:shared:testAndroidHostTest` runs common behavior tests plus the actual Android
Room reopen test with Robolectric. iOS tests use the production Room builder too.

`:shared:lintAndroidMain` analyzes production `commonMain` and `androidMain`
using Android API and Compose checks. The Android-KMP plugin requires the explicit
`com.android.lint` opt-in; app lint alone does not analyze shared production code.
The report is `shared/build/reports/lint-results-androidMain.html`. `shared/lint.xml`
exempts only `RestrictedApi` calls in the two Room-generated implementations;
authored sources keep that check. iOS-specific sources are outside this analysis.
See the [AGP lint plugin](https://developer.android.com/reference/tools/gradle-api/9.3/com/android/build/gradle/LintPlugin)
and [lint configuration](https://developer.android.com/studio/write/lint#configure-the-lint-file).

Without a booted simulator, compilation/linking can be checked separately with
`:shared:compileTestKotlinIosSimulatorArm64 :shared:linkDebugTestIosSimulatorArm64`;
this does not execute tests or prove iOS persistence. Do not start/reset local
devices or install runtimes as a verification workaround.

Final integration checks use a temporary `git archive HEAD` checkout, containing
only tracked files, without build outputs, `.gradle`, `local.properties` or symlinks.
Use the same installed `JAVA_HOME`, `ANDROID_HOME` and existing dependency caches.
This verifies independence from the author's checkout; it does not prove device UI.

The PR workflow runs formatting/common tests/Android assemble/lint on Linux and
common/iOS tests plus the unsigned host build on an arm64 macOS runner. Only CI
may prepare its declared dependencies and boot a simulator from a preinstalled
runtime. It logs that runtime/UUID and passes the UUID through `iosTestDevice`.
The workflow has not been executed on GitHub during local implementation.

Local limitations: no attached Android device or booted iOS simulator, so manual
UI checks, keyboard/large-font/back gestures/host restoration and iOS runtime Room
tests are blocked. Existing warnings remain: Room expect/actual Beta, JDK native
access, Android native-library stripping and the ICU data object built for iOS
simulator 18.5 while targeting 16.0. Minimum-iOS runtime support is untested.

## Material UI

Screens reuse `shared/ui/components/ScreenScaffold`, `ScreenLoading` and
`ScreenError`. The scaffold owns the Material app bar, mirrored accessible back
icon and content insets; Route owns navigation. Loading is centered in the content
area, and note save progress stays beside Save. Article items use Material Cards.
Use official Material 3 components and `MaterialTheme` colors, typography and
shapes. Keep English resources as the default and localized accessibility labels.

Material 3 has an independent release cycle: pin the latest compatible stable
version rather than assuming the Compose plugin alias selects a stable library.
The current stable multiplatform line is 1.9.0; newer Expressive releases are
prereleases. See [Material 3 versioning](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html#decoupled-material3-versioning)
and [Material app bars](https://developer.android.com/develop/ui/compose/components/app-bars).
Device checks are needed for actual layout, large fonts, RTL, keyboard and gestures.

## Constants and configuration

Kotlin configuration and domain constants belong to their owner's `companion
object` as `const val`, or are constructor parameters when configurable. Avoid
inline magic values and top-level constants. Database builders on both platforms
share `StarterDatabase.FILE_NAME` and `SCHEMA_VERSION`; HTTP configuration lives
in `HttpClientFactory`, endpoints in `ArticleRemoteDataSource`, and the minimum
article ID in `Article.MIN_ID`. Room table/query names belong to the entity/DAO.

This includes test infrastructure configuration. Keep test inputs and expected
results literal. Compose layout values, diagnostic messages and declarative build
metadata can remain inline; user-facing copy stays in Compose resources.

## Read and adapt

Read `App.kt` → `ui/navigation` → feature Route/Screen/ViewModel → use case →
domain repository → data repository → HTTP/Room. Start at
[canonical examples](docs/architecture/canonical-examples.md), then
[adaptation](docs/architecture/adapting-the-example.md). [AGENTS.md](AGENTS.md)
defines ownership and test rules; the approved
[design](docs/superpowers/specs/2026-10-05-articles-notes-design.md) records scope.

V1 has no cache, sync, deletion, accounts or automatic retry. Opening a note first
requires its article online; this is an example simplification. Unsaved drafts
survive while their ViewModel survives, including Android configuration changes,
but not process death or a discarded destination/host. Saved notes survive restart.
There is no navigation event channel or rename generator. This repository is
standalone, with no runtime dependency on LifeGenie.

Sources: [Metro compatibility](https://zacsweers.github.io/metro/latest/compatibility/),
[Compose navigation](https://kotlinlang.org/docs/multiplatform/compose-navigation.html),
[Kotlin direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html),
[Room KMP](https://developer.android.com/kotlin/multiplatform/room),
[JSONPlaceholder writes](https://jsonplaceholder.typicode.com/guide/),
[GitHub runner architectures](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).
