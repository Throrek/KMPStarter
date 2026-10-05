# Adapt the example

For a new application, prefer the [minimal starter kit](../starter-kit.md).
The steps below apply when intentionally adapting the complete demonstration.

1. Rename `rootProject.name` in `settings.gradle.kts`, the app label in
   `androidApp/src/main/AndroidManifest.xml`, and the display name in
   `iosApp/iosApp/Info.plist`. Keep resource copy in
   `shared/src/commonMain/composeResources/values/strings.xml`.
2. Change Kotlin package declarations/directories, Android namespace/applicationId
   in both module build files, Xcode `PRODUCT_BUNDLE_IDENTIFIER`, and the shared
   framework `bundleId`. If renaming `StarterShared`, also change Swift imports,
   Xcode framework search/link settings and the framework `baseName` together.
3. Replace the API URL/timeout constants in `HttpClientFactory`'s companion object,
   endpoint constants in `ArticleRemoteDataSource`'s companion object and DTOs in
   `data/article/network`, mapping/error handling in `DefaultArticleRepository`,
   and domain Article/repository/use cases. Update MockEngine behavior tests.
   Domain models stay independent of HTTP, Room, Compose and platform types.
4. Change ArticleNote/entity/DAO and `StarterDatabase` together. Keep the database
   file name/schema version in `StarterDatabase`'s companion object and table/query
   constants in their entity/DAO companions; both platform builders share the name.
   Retain schema exports in `shared/schemas`; released schema changes need an
   explicit migration
   and persistence tests. Do not add destructive migration to avoid that decision.
5. Replace or remove `ui/articles`, `ui/articledetails`, their domain/data features,
   destinations and graph providers together. Remove unused dependencies and tests;
   add behavior tests for the replacement. Keep Route/Screen separation and put
   every ViewModel data operation through a use case. Reuse `ui/components` for
   the Material scaffold, centered loading and screen errors. Use standard Material
   controls and `MaterialTheme` tokens; keep toolbar back as an accessible mirrored
   icon with the same guard as system back. Check Material 3 stability/compatibility
   separately from the Compose plugin version before updating the catalog.
6. Retain only necessary platform engine/database/host code. Recheck Android
   manifest, Xcode minimum OS/architectures, resources and both host builds. Update
   README/AGENTS/canonical examples to the actual remaining paths and behaviors.
   Re-audit `iosApp/iosApp/PrivacyInfo.xcprivacy` when dependencies or data use
   change. Its file-metadata reason `C617.1` covers the app's container files;
   review the linked app's API usage and current Apple approved reasons, then
   verify Xcode bundles the manifest. SQLite's `statfs`/`fstatfs` calls currently
   inspect filesystem types/flags for locking, not disk capacity; they do not
   justify `E174.1`. Apple lists both as required-reason APIs and documents no
   exemption for inspecting types/flags. Their missing declaration is a known
   privacy coverage gap and a release blocker: resolve it through dependency/API-use
   remediation or Apple clarification before distribution. A successful local build
   does not resolve the gap or establish App Store acceptance. SDK-only reason
   `0A2A.1` and boot-time reason `35F9.1` must not be copied without applicable usage.

Decide whether the new app needs offline access, persistent drafts, synchronization
or navigation effects; V1 intentionally demonstrates none. JSONPlaceholder is not
an authenticated backend, and its write responses do not persist notes. This
starter includes no generator and requires no access to LifeGenie.
