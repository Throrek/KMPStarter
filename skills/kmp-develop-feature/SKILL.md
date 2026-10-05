---
name: kmp-develop-feature
description: Use when implementing or extending a KMPStarter feature across Compose screens, ViewModels, use cases, domain contracts and data, including loading, retry, detail navigation or editing. Not for adopting infrastructure unrelated to the requested feature.
---

# Develop a feature using the existing architecture

Resolve this file's real path first (repository discovery may use a symlink), then
resolve `../..` from its directory as the **kit root**. Read target AGENTS.md and
nearby feature code first. Consult only relevant rows in the kit's
[canonical map](../../docs/architecture/canonical-examples.md); product decisions
in the target override demonstration choices.

1. Define the behavior: input/identity, loading/content/empty/error states, retry,
   validation and any persistence result. Inspect existing capabilities. Use
   [networking](../kmp-networking/SKILL.md) or [Room](../kmp-room/SKILL.md) only when
   required. A local feature needs no server; a read-only API feature needs no
   database. A static screen does not need invented layers.
2. Put product models/contracts in `domain/<feature>`, SDK mapping/implementations
   in `data/<feature>`, UI in `ui/<feature>`. Every ViewModel data operation goes
   through a use case, including reads; local editing remains in the ViewModel.
   Domain models stay free of Compose/Room/Ktor/platform types. Reuse compatible
   existing abstractions; extract duplication after it exists.
3. Use immutable state and read-only StateFlow with viewModelScope. Follow
   [ArticlesViewModel](../../shared/src/commonMain/kotlin/me/kmpstarter/ui/articles/ArticlesViewModel.kt)
   for explicit retry and duplicate-request prevention. Publish operation state
   before launching, preserve drafts on persistence failure, and propagate
   cancellation without success/error UI. [SaveNoteUseCase](../../shared/src/commonMain/kotlin/me/kmpstarter/domain/note/SaveNoteUseCase.kt)
   illustrates normalize/validate/persist; its article ID and text rule are example
   domain policy, not requirements for other products.
4. Route owns ViewModel creation/lifecycle/navigation; Screen takes state/callbacks.
   Follow [ArticlesRoute](../../shared/src/commonMain/kotlin/me/kmpstarter/ui/articles/ArticlesRoute.kt)
   and [ArticlesScreen](../../shared/src/commonMain/kotlin/me/kmpstarter/ui/articles/ArticlesScreen.kt).
   Reuse the target ScreenScaffold/Loading/Error and Material tokens/components.
   Keep user text in English default resources with optional matching translations.
5. For details, pass a stable ID and reload independently of list memory.
   [Navigation](../../shared/src/commonMain/kotlin/me/kmpstarter/ui/navigation/AppNavigation.kt)
   shows current-entry/RESUMED and singleTop guards;
   [details ViewModel](../../shared/src/commonMain/kotlin/me/kmpstarter/ui/articledetails/ArticleDetailsViewModel.kt)
   shows Metro assisted inputs. Adapt the data flow to this feature: the example
   reads HTTP then a local note, which a read-only detail screen should not copy.
   Add graph providers/factories as needed, never singleton ViewModels.
6. Share the same live exit guard between system and toolbar back. Use an
   accessible mirrored icon. Apply keyboard insets only to editors. Review dark
   mode, RTL, large fonts, loading placement and consumed insets on a device.
7. Use real use cases with small fake repositories for ViewModel tests. Adapt
   [list behavior tests](../../shared/src/commonTest/kotlin/me/kmpstarter/ui/articles/ArticlesViewModelTest.kt),
   [direct detail loading](../../shared/src/commonTest/kotlin/me/kmpstarter/ui/articledetails/ArticleDetailsLoadingTest.kt)
   and, only for writes, [save tests](../../shared/src/commonTest/kotlin/me/kmpstarter/ui/articledetails/ArticleNoteSavingTest.kt).
   Test actual retry/failure/ordering/validation/cancellation with virtual time or
   gates. No forwarding, source-string, framework-internal or artificial UI tests.
8. Format with spotlessApply, inspect diff, then run target AGENTS checks, including
   shared lint and both hosts. Distinguish executed tests from compile/link-only
   checks. Update the feature's decisions and any changed capabilities in handoff
   docs. Report device verification as blocked when no device is available.
