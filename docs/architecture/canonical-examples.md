# Canonical examples

All Kotlin paths below are relative to `shared/src`. Production code is in
`commonMain/kotlin/me/kmpstarter`, common tests in `commonTest/kotlin/me/kmpstarter`.
The map describes the actual example, not a general application framework.

The [starter kit skills](../starter-kit.md) select relevant rows for base creation,
networking, Room or feature work. In particular, HTTP and Room are independent
capabilities; the combined details/editor flow is only one composition example.

| Responsibility and code | Matching behavior evidence | Use it when / do not generalize |
| --- | --- | --- |
| `ui/navigation/AppNavigation.kt`, `Destination.kt` and feature `*Route.kt`: typed ID routes, destination ViewModelStore, lifecycle collection, current-entry/RESUMED/singleTop tap guard | `ui/articledetails/ArticleDetailsLoadingTest.kt`: `WHEN details open directly THEN article and saved note are loaded by ID`; navigation gestures and restoration require device checks | Pass stable IDs and reload independently of the list. There is no navigation-event example here. |
| `ui/components/ScreenScaffold.kt`, `ScreenLoading.kt`, `ScreenError.kt`, `ui/theme/Theme.kt`: shared Material 3 app bar, standard accessible RTL back icon, consumed insets, centered loading and scrollable errors | Android/iOS compilation checks component APIs; placement, keyboard, RTL, large fonts and dark mode require device checks | Reuse official Material components and theme tokens. Screens pass presentation values/callbacks; Route retains navigation. Keep save progress beside Save, and apply keyboard insets only where needed. |
| `ui/articles/ArticlesScreen.kt`, `ArticlesViewModel.kt`: immutable state/callback UI and explicit retry | `ui/articles/ArticlesViewModelTest.kt`: `WHEN request fails THEN retry can load articles`, `WHEN retry is already running THEN no second request starts` | Keep asynchronous state in a ViewModel. A one-shot list without paging/cache is a V1 choice. |
| `ui/articledetails/ArticleDetailsViewModel.kt`, `domain/articledetails/LoadArticleDetailsUseCase.kt`: editor only after both reads | `ui/articledetails/ArticleDetailsLoadingTest.kt`: `WHEN note read fails THEN editor is not shown`, `WHEN retry is called with unsaved draft THEN draft is not reloaded` | Protect unread data and drafts. Requiring an online article before opening a local note is not an offline-app recommendation. |
| `domain/note/SaveNoteUseCase.kt` and detail save state: trim/validate, publish Saving before launch, success after persistence | `domain/note/SaveNoteUseCaseTest.kt`: `WHEN text is blank THEN note is not saved`; `ui/articledetails/ArticleNoteSavingTest.kt`: `WHEN save is repeated THEN only one write occurs`, `WHEN save fails THEN draft is retained`, `WHEN save completes THEN saved state contains normalized note` | Put a data operation through a use case, even a simple read. Local editing needs no use case. Saved is inline state, not an event. |
| `data/article/DefaultArticleRepository.kt`, `data/article/network/ArticleRemoteDataSource.kt`, `data/network/HttpClientFactory.kt`: DTO/domain mapping, specific SDK failures, named 15-second timeout | `data/article/DefaultArticleRepositoryTest.kt`: `WHEN details response has different ID THEN invalid response is reported`, `WHEN request exceeds 15 seconds THEN timeout failure is reported`, `WHEN programming failure occurs THEN failure propagates` | Translate expected boundary failures. Do not use broad catches, raw exception text in UI or automatic retry. |
| `data/note/RoomNoteRepository.kt`, `data/note/local/ArticleNoteDao.kt`, `data/local/StarterDatabase.kt`: entity mapping and atomic upsert by article ID | `data/note/RoomNoteRepositoryTest.kt`: `WHEN write fails with SQLite error THEN storage failure is reported`; `androidHostTest` and `iosTest` `data/note/NotePersistenceTest.kt`: `WHEN database reopens THEN latest note is restored` | Use production builders for real persistence evidence. A fake DAO or test compile does not prove disk persistence. iOS runtime execution remains blocked locally. |
| Boundary cancellation and ViewModel lifetime | `data/article/DefaultArticleRepositoryTest.kt`: `WHEN request is cancelled THEN cancellation propagates`; `data/note/RoomNoteRepositoryTest.kt`: `WHEN DAO is cancelled THEN cancellation propagates`; `ui/articledetails/ArticleNoteSavingTest.kt`: `WHEN ViewModel is cleared THEN no save success is published` | Cancellation propagates and produces no success/error UI. Cancellation cannot roll back an already completed transaction; reopening reads actual disk state. |
| `di/StarterGraph.kt`; `ui/articledetails/ArticleDetailsViewModel.kt`; Android `StarterApplication.kt`; iOS `MainViewController.kt`: host-owned database/client, unscoped VMs and Metro assisted factory for the article ID | Real-use-case ViewModel tests above; shared compilation and host builds verify wiring, no artificial DI-forwarding test | Create resources outside recomposition, scope VMs to destinations. Use `@AssistedInject`/`@AssistedFactory` for runtime inputs and let Metro supply use cases. Do not make VMs graph singletons or add unused factories/modules. |

A list click sends `article.id` through Route to typed navigation. The details Route
creates its ViewModel with that ID; `LoadArticleDetailsUseCase` reads HTTP through
ArticleRepository, then Room through NoteRepository. Editing changes only a draft.
Save runs SaveNoteUseCase → RoomNoteRepository → DAO upsert, then publishes Saved.
The API never receives notes: JSONPlaceholder write operations are simulations.

Details system back uses Compose's common back dispatcher on Android and iOS,
with the same live-state guard as toolbar back: only Saving blocks exit. The API
is deprecated in Compose 1.11.1 but matches Navigation Compose 2.9.2's iOS bridge.
No platform wrapper is needed. Manual gesture checks are blocked without a device.

Draft lifetime is the destination ViewModel lifetime: retained host configuration
changes can preserve it; destination removal, discarded host or process death do
not. Leaving normally discards a draft without a dialog. Room notes survive restart.
There is no refresh that overwrites an open editor, article cache or synchronization.
