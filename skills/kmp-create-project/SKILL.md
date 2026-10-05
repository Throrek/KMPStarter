---
name: kmp-create-project
description: Use when creating a new Android/iOS Kotlin Multiplatform app from KMPStarter's runnable reference, with Compose, Metro and Material, optionally followed by networking or Room. Not for replacing an existing app.
---

# Create an app from the runnable reference

Resolve this file's real path first (repository discovery may use a symlink), then
resolve `../..` from its directory as the **kit root**. The runnable app is the
single source of code and pinned configuration. Use the
[creation map](references/create-from-reference.md) for file selection and exact
adaptations; do not create another maintained template, generator or copy of the
reference inside this repository.

1. Establish a new destination, project/display name and lowercase dotted
   application ID. Ask only for missing identity choices. Refuse existing paths
   (including symlinks) or output inside the kit; create the destination exclusively
   before copying. Validate identifiers against Kotlin/Android/iOS requirements;
   reject Kotlin keywords and the reserved `kotlin` root namespace. Do not alter
   existing files or initialize Git/install tools/start devices without authorization.
2. Follow the creation map: copy selected current reference files, simplify the
   two module build files, graph, hosts and navigation, then add one static Home
   screen. Adapt only in the new app. Keep shared components and version pins from
   the reference. The empty screen needs no artificial ViewModel, repository,
   use case or tests.
3. Apply the coordinated identity changes in
   [adaptation steps 1–2](../../docs/architecture/adapting-the-example.md).
   Keep the framework name `StarterShared` unless a rename was requested; its Swift
   imports and Xcode integration already agree. Keep English default resources.
4. Adapt the source AGENTS.md to the new app: preserve general architecture,
   function boundaries, constants, UI, testing, formatting and verification rules;
   replace reference-only paths and demo-specific instructions. Include the
   mandatory ongoing-development section described in the creation map's
   [handoff contract](references/create-from-reference.md#ongoing-development-handoff).
   Record kit/version, chosen capabilities and
   reference location in `starter-provenance.json`, plus product decisions and
   how to access the matching complete kit in `docs/starter-kit.md`. Record the
   actual source revision when known and disclose local modifications. Do not
   invent a source hash or copy this kit's implementation reports as app docs.
5. Add only requested capabilities using [networking](../kmp-networking/SKILL.md)
   and/or [Room](../kmp-room/SKILL.md); use
   [develop-feature](../kmp-develop-feature/SKILL.md) for product behavior. Missing
   API/schema decisions do not justify copying the demo business feature.
6. Invoke spotlessApply in the new app, inspect the diff, then run its AGENTS
   checks serially: spotlessCheck, Android build/tests, shared and app lint, iOS
   compilation and unsigned host build. Inspect resolved dependencies for the
   chosen capabilities. NO-SOURCE means no executed tests; device UI and runtime
   persistence require a device. Report unavailable checks as blocked.

Deliver the independent app path, capabilities, source provenance and actual
verification. Skills supply the procedure; the runnable reference supplies code.
