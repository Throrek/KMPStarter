# KMPStarter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Zbudować minimalny przykład KMP: lista artykułów HTTP i szczegóły z lokalną notatką Room, łatwy do ręcznego przeglądu i modyfikacji.

**Architecture:** Wspólny kod Compose, ViewModeli, use case'ów, repozytoriów, danych i Metro znajduje się w jednym module `shared`. Każdy ViewModel komunikuje się wyłącznie z use case'ami, a repozytoria z data source'ami: klasą HTTP lub Room DAO. Android/iOS dostarczają tylko host i konieczne elementy platformowe.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Metro, Ktor, kotlinx.serialization, Room + bundled SQLite, typed Navigation Compose, coroutines, kotlin.test, Spotless/ktfmt.

**Spec:** [Projekt i zachowania](../specs/2026-10-05-articles-notes-design.md).

Status: plan po przeglądzie zakresu i jakości; implementacja nie rozpoczęta. Katalog docelowy:
`/Users/radoslaw.jablonski/KMPStarter`. LifeGenie pozostaje niezmienione.

## Global Constraints

- Dwa ekrany: lista artykułów oraz szczegóły artykułu z edytorem jednej notatki.
- Każdy odczyt/zapis danych z ViewModelu przechodzi przez use case, także prosty odczyt. Lokalne zmiany stanu UI pozostają w ViewModelu.
- Moduły `:shared`, `:androidApp`; projekt Xcode `iosApp/iosApp.xcodeproj`.
- Package/application ID: `me.kmpstarter`; framework: `StarterShared`; baza: `starter.db`.
- Android minSdk 24, compileSdk/targetSdk 37; iOS minimum 16.0; iosArm64 + iosSimulatorArm64.
- API `https://jsonplaceholder.typicode.com`, GET `/posts` oraz `/posts/{id}`.
- Timeout HTTP 15 s, nazwana stała; brak automatycznych retry i cache artykułów.
- Notatka to `articleId` i jeden wymagany, niepusty tekst; jedna notatka na artykuł.
- Wynik zapisu: inline „Zapisano”, bez nawigacji i bez kanału zdarzeń w V1.
- Testy: `when<Condition>Then<Outcome>`; sekcje `// Given`, `// When`, `// Then`.
- Testy ViewModeli używają prawdziwych use case'ów z fake repozytoriami.
- Bez testów trivial forwarding, UI automation, procentowych celów coverage i real sleeps.
- Bez Firebase, kont, synchronizacji, usuwania, generatora, skilla tworzenia projektu i publikacji.
- Lokalnie nie instalować ani aktualizować narzędzi/systemowych runtime'ów i nie uruchamiać/resetować emulatorów bez zgody. Rozwiązywanie deklarowanych zależności Gradle jest częścią builda; brak toolchainu zgłosić. W jednorazowym CI wolno przygotować deklarowane zależności oraz uruchomić symulator z preinstalowanym runtime; to nie uprawnia do zmian na komputerze użytkownika.
- Nie kopiować konfiguracji Firebase, signing, sekretów, reguł backupu i polityk kiosku z LifeGenie.
- Projekt jest samodzielny: brak lokalnych zależności od LifeGenie, build z plików repo i udokumentowanych narzędzi.

## Review Focus

- Błąd początkowego odczytu notatki nie może udawać braku notatki i umożliwiać nadpisania — etap 3/4.
- Powtórne Save i zmiana tekstu podczas zapisu nie mogą tworzyć drugiego zapisu ani utraty draftu — etap 4.
- Anulowanie nie może zostać pokazane jako sukces lub zwykły błąd — etap 2/3/4.
- Odtworzona trasa szczegółów musi działać bez wcześniej załadowanej listy; niepoprawne ID nie uruchamia I/O — etap 3/4/5.
- Fake DAO nie potwierdza trwałości Room na urządzeniu — rzeczywisty test reopen Android/iOS w etapie 3 i jawny raport ograniczeń w etapie 5.

## Wynik przeglądu planu

Przegląd 2026-10-05 obejmował zakres, możliwość ponownego użycia, weryfikację i
niezależną ocenę subagenta. Zachowano dwa ekrany i dwa moduły Gradle. Dopisano
kryteria adaptacji i samodzielności, wczesny build hosta iOS, użycie produkcyjnych
builderów w testach Room, ochronę draftu przed Retry i zgodność ID odpowiedzi.
Usunięto dwa testy samego przekazywania wyjątków przez use case. Nie dodano
cache, synchronizacji, kanałów zdarzeń, nowych modułów ani generatora.

To ocena planu. Nie potwierdza kompilacji ani jakości nieistniejącej implementacji.

## Organizacja pracy subagentów

Koordynator prowadzi cały proces w czacie implementacyjnym: ustala kontrakty, przydziela
zakres, rozstrzyga niejasności i sprawdza wyniki. Użytkownik wybrał pracę
subagentów; nie pytamy ponownie o metodę wykonania.

- Jeden subagent implementujący na etap; pracują kolejno, bo etapy zależą od siebie.
- Po etapie osobny subagent robi jedno review obejmujące wymagania i jakość.
- Poprawki wracają do autora etapu; kolejne review obejmuje zmieniony zakres.
- Na końcu jeden przegląd integracyjny całej aplikacji.
- Subagenci dostają spec, własne zadanie i potrzebne kontrakty, bez pełnej historii rozmowy; nie delegują dalej.
- Koordynator prowadzi `docs/implementation-progress.md`: stan etapów, dowody testów, decyzje, blokady. Nie zleca ponownie ukończonej pracy po utracie kontekstu.
- Nie uruchamiamy kilku Gradle buildów naraz w tym samym katalogu. Recenzent korzysta z raportu testów; nie powtarza ich bez nowej zmiany lub konkretnego podejrzenia.
- Małe, przeglądalne etapy; brak automatycznego push/merge/publikacji. Ten plan nie wymaga commitów dokumentacji. Przy implementacji ustalić Git repo i gałąź `codex/articles-notes`, bez dotykania repo LifeGenie.
- Po zatwierdzeniu planu koordynator realizuje etapy bez pytań „czy kontynuować” po każdym z nich. Stan zmian pozostaje widoczny w raporcie.

## Układ plików i kontrakty

`P` oznacza `shared/src/commonMain/kotlin/me/kmpstarter`, a `T` oznacza
`shared/src/commonTest/kotlin/me/kmpstarter`. Poniżej ścieżki względem repo.

```text
androidApp/                         cienki host Android
iosApp/                             cienki host SwiftUI/Xcode
shared/src/commonMain/kotlin/me/kmpstarter/
  App.kt
  di/StarterGraph.kt
  presentation/BaseViewModel.kt
  domain/article/                   Article, kontrakt, błędy, LoadArticlesUseCase
  domain/note/                      ArticleNote, kontrakt, błędy, SaveNoteUseCase
  domain/articledetails/            ArticleDetails, LoadArticleDetailsUseCase
  data/article/                     DefaultArticleRepository
  data/article/network/             ArticleDto, ArticleRemoteDataSource
  data/network/                     HttpClientFactory
  data/note/                        RoomNoteRepository
  data/note/local/                  ArticleNoteEntity, ArticleNoteDao
  data/local/                       StarterDatabase
  ui/navigation/                    AppNavigation, Destination
  ui/articles/                      State, ViewModel, Route, Screen
  ui/articledetails/                 State, ViewModel, Route, Screen
shared/src/androidMain/              engine HTTP, builder bazy
shared/src/iosMain/                  engine HTTP, builder bazy, MainViewController
shared/src/commonTest/               testy zachowania i HTTP
shared/src/androidHostTest/          rzeczywisty test Room (Robolectric)
shared/src/iosTest/                  rzeczywisty test Room
shared/schemas/                     eksport aktualnego schematu
```

Nie tworzyć pustych katalogów ani plików „na przyszłość”. Typy wyniku/błędu
używane w jednym miejscu trzymać przy właścicielu. Brak modułu `core` i `build-logic`.

Kontrakty wspólne (definicje powstają w etapach podanych niżej):

```kotlin
data class Article(val id: Int, val title: String, val body: String)
interface ArticleRepository {
    suspend fun loadArticles(): List<Article>
    suspend fun loadArticle(id: Int): Article
}
// ArticleReadException(reason: ArticleReadFailure, cause: Throwable? = null)
// reason: Connection, Timeout, Http, InvalidResponse, NotFound
class LoadArticlesUseCase(repository: ArticleRepository) {
    suspend operator fun invoke(): List<Article>
}

data class ArticleNote(val articleId: Int, val text: String)
interface NoteRepository {
    suspend fun loadNote(articleId: Int): ArticleNote?
    suspend fun saveNote(note: ArticleNote)
}
// NoteStorageException(cause: Throwable)
// SaveNoteResult = Saved(note: ArticleNote) | InvalidText
class SaveNoteUseCase(repository: NoteRepository) {
    suspend operator fun invoke(articleId: Int, text: String): SaveNoteResult
}
data class ArticleDetails(val article: Article, val note: ArticleNote?)
class LoadArticleDetailsUseCase(articles: ArticleRepository, notes: NoteRepository) {
    suspend operator fun invoke(articleId: Int): ArticleDetails
}
```

Powyższy blok określa sygnatury, nie jest plikiem Kotlin do skopiowania.
ID musi być dodatnie. Use case szczegółów/zapisu odrzuca niepoprawne ID przed
repozytorium (`require`); ViewModel mapuje niepoprawną trasę na Failed bez ich
wywołania. ArticleReadException / NoteStorageException są wyjątkami domenowymi;
walidacja tekstu jest zwykłym wynikiem. Nie dokładamy uniwersalnego `Result<T>`.

## Task 1: Kompilowalny szkielet i zasady projektu

**Właściciel:** subagent fundamentów. **Wynik:** dwa hosty wspólnego prostego ekranu.

**Pliki:** `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`,
`gradle/libs.versions.toml`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`,
`.gitignore`, `AGENTS.md`, `README.md`, `shared/build.gradle.kts`,
`androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`,
`androidApp/src/main/kotlin/me/kmpstarter/MainActivity.kt`, `P/App.kt`,
`shared/src/iosMain/kotlin/me/kmpstarter/MainViewController.kt`,
`iosApp/iosApp.xcodeproj/project.pbxproj`, współdzielony scheme `iosApp`,
`iosApp/iosApp/{iOSApp.swift,ContentView.swift,Info.plist}`.

**Interfejsy:** produkuje taski `:shared:testAndroidHostTest`,
`:shared:compileKotlinIosSimulatorArm64`, `:shared:iosSimulatorArm64Test`,
`:androidApp:assembleDebug`, `:androidApp:lintDebug`, `spotlessApply/Check`.

- [ ] Sprawdzić lokalne JDK, SDK, Xcode i Git; zapisać wynik bez zmiany środowiska. Nie kopiować `local.properties` z cudzymi ścieżkami do repo.
- [ ] Skonfigurować minimalne moduły. Punkt wyjścia z LifeGenie: Gradle 9.5.0 (z sumą wrappera), Kotlin 2.4.10, AGP 9.3.2, Compose 1.11.1, Metro 1.4.3, KSP 2.3.10, Room 2.8.4, SQLite 2.6.2, Ktor 3.4.3, Spotless 8.10.2. Pozostałe użyte wersje z katalogu Caregiver: serialization 1.11.0, coroutines 1.10.1, KMP lifecycle 2.10.0, KMP navigation 2.9.2, activity-compose 1.13.0, Robolectric 4.16.1. Nie kopiować całego katalogu ani samoczynnie aktualizować stosu.
- [ ] Włączyć Compose resources, `withHostTestBuilder`, framework statyczny `StarterShared` oraz integrację `embedAndSignAppleFrameworkForXcode`. Bez zależności SwiftPM/CocoaPods, bo przykład nie potrzebuje natywnych SDK.
- [ ] Dodać Spotless dla `.kt` i `.gradle.kts`, styl ktfmt Kotlin; wykluczyć `build`/`.gradle`. W `AGENTS.md` zapisać granice warstw, nazwy/układ testów, ochronę zmian użytkownika i komendy weryfikacji. Zaznaczyć wyjątek use case'ów wszystkich operacji względem Caregiver.
- [ ] Uruchomić `./gradlew spotlessApply`, obejrzeć diff, następnie `./gradlew spotlessCheck :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`. Brak przykładowego testu `2+2`; ten etap sprawdza build. Już w tym etapie zbudować host: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build`. Zgodność wersji uznać dopiero po udanym buildzie; blokady zapisać. README zapisuje faktycznie użyte wersje JDK/Xcode/SDK; konfiguracja nie ma zależeć od nieudokumentowanego globalnego ustawienia.
- [ ] Review zakresu: czy hosty są cienkie, projekt niezależny od LifeGenie i nie ma nieużywanych zależności.

## Task 2: Odczyt artykułów przez HTTP

**Właściciel:** subagent sieci. **Wynik:** przetestowany odczyt listy/szczegółów.

**Pliki:** `P/domain/article/{Article,ArticleRepository,ArticleReadException,LoadArticlesUseCase}.kt`,
`P/data/article/DefaultArticleRepository.kt`,
`P/data/article/network/{ArticleDto,ArticleRemoteDataSource}.kt`,
`P/data/network/HttpClientFactory.kt`, natywne `data/network/HttpEngine.android.kt`
i `HttpEngine.ios.kt`, `T/data/article/DefaultArticleRepositoryTest.kt`.

**Interfejsy:** produkuje kontrakty Article z sekcji wyżej;
`HttpClientFactory(engine: HttpClientEngine).create(): HttpClient`;
`ArticleRemoteDataSource(client: HttpClient)` z suspend `loadArticles(): List<ArticleDto>`
i `loadArticle(id: Int): ArticleDto`. Wspólna konfiguracja działa też z MockEngine.

- [ ] Napisać testy publicznego repozytorium z prawdziwym data source'em i MockEngine: `WHEN response is valid THEN articles are loaded` (literalny artykuł 7, „Kotlin”, „Shared code”), `WHEN list is empty THEN empty list is returned`, `WHEN details are requested THEN article is loaded by ID` (ścieżka `/posts/7`).
- [ ] Dodać zachowania błędów: `WHEN server returns 500 THEN HTTP failure is reported`, `WHEN article returns 404 THEN not found is reported`, `WHEN connection fails THEN connection failure is reported`, `WHEN request times out THEN timeout failure is reported`, `WHEN payload is malformed THEN invalid response is reported`, `WHEN request is cancelled THEN cancellation propagates`. ID <= 0, pusty tytuł i duplikaty ID w liście traktować jako InvalidResponse; sprawdzić jednym testem wariantowym bez odtwarzania parsera. Dodać `WHEN details response has different ID THEN invalid response is reported`: żądanie `/posts/7`, odpowiedź z ID 8, brak udostępnienia błędnego Article. MockEngine nie używa sieci ani real sleeps.
- [ ] Uruchomić focused `./gradlew :shared:testAndroidHostTest --tests '*DefaultArticleRepositoryTest'` i potwierdzić spodziewany brak implementacji/błędne zachowanie.
- [ ] Dodać minimalną implementację, ContentNegotiation JSON (`ignoreUnknownKeys`), kontrolę statusów i wspólny timeout 15 s. Repozytorium tłumaczy konkretne błędy, cancellation propaguje. Użyć Ktor engine Android i Darwin. Nie tworzyć interfejsu dla klasy HTTP tylko w celu mockowania.
- [ ] Dodać `LoadArticlesUseCase` i ponownie uruchomić focused testy po `spotlessApply`/inspekcji diffu. Nie pisać testu samego forwarding use case'a. Review interfejsów i błędów.

## Task 3: Room, notatki i logika use case'ów

**Właściciel:** subagent danych. **Wynik:** walidacja/zapis notatki i odczyt szczegółów.

**Pliki:** `P/domain/note/{ArticleNote,NoteRepository,NoteStorageException,SaveNoteUseCase}.kt`,
`P/domain/articledetails/{ArticleDetails,LoadArticleDetailsUseCase}.kt`,
`P/data/note/RoomNoteRepository.kt`, `P/data/note/local/{ArticleNoteEntity,ArticleNoteDao}.kt`,
`P/data/local/StarterDatabase.kt`, natywne `data/local/StarterDatabase.android.kt`
i `.ios.kt`, `shared/schemas/me.kmpstarter.data.local.StarterDatabase/1.json`,
`T/domain/note/SaveNoteUseCaseTest.kt`, `T/domain/articledetails/LoadArticleDetailsUseCaseTest.kt`,
`T/data/note/RoomNoteRepositoryTest.kt`, platformowe
`shared/src/{androidHostTest,iosTest}/kotlin/me/kmpstarter/data/note/NotePersistenceTest.kt`.

**Interfejsy:** produkuje kontrakty Note/Details wyżej;
`ArticleNoteDao.load(articleId: Int): ArticleNoteEntity?` i
`ArticleNoteDao.upsert(note: ArticleNoteEntity)` są suspend.
`ArticleNoteEntity(articleId: Int, text: String)` ma klucz główny `articleId`.
`StarterDatabase.articleNoteDao()`; platformowe `createStarterDatabase` z opcjonalną ścieżką pliku bazy (produkcyjny katalog jako default; test przekazuje plik tymczasowy). Builder i konfiguracja drivera są wspólne dla produkcji i testu danej platformy.

- [ ] Testy Given/When/Then: `WHEN text is blank THEN note is not saved` (" \n "), `WHEN text has outer whitespace THEN trimmed note is saved` ("  My note  " -> "My note"), `WHEN article ID is invalid THEN no repository is called`.
- [ ] Testy koordynacji: `WHEN details are loaded THEN note belongs to requested article` (ID 7), `WHEN no note exists THEN details have no note`, `WHEN note read fails THEN details load fails`. Test repozytorium z małym fake DAO: oczekiwany błąd SQLite -> NoteStorageException, brak wiersza -> null; `WHEN DAO is cancelled THEN cancellation propagates` sprawdza granicę tłumaczenia błędów. Nie powtarzać testów propagacji na każdym forwarding use case. Bez osobnych testów mechanicznego mapowania pól.
- [ ] Uruchomić focused testy i potwierdzić spodziewane niepowodzenie; wdrożyć minimalne use case'y/repozytorium/DAO. Artykuł i notatkę ładować sekwencyjnie w jednym use case; bez rozbudowanego agregatora równoległych stanów.
- [ ] Dodać Room @Database v1, eksport schematu, @ConstructedBy oraz wygenerowane przez KSP actual konstruktora. Natywne buildery: applicationContext lub Application Support, BundledSQLiteDriver, IO. Brak allowMainThreadQueries/destructive migration.
- [ ] Dodać `WHEN database reopens THEN latest note is restored` na obu platformach: plik tymczasowy, upsert ID 7 "First", ponowny upsert ID 7 "Updated", ID 8 "Other", close/reopen, literalne odczyty obu notatek, cleanup. Oba testy używają produkcyjnego `createStarterDatabase` z tymczasową ścieżką, nie oddzielnej testowej konfiguracji Room. AndroidHostTest używa Robolectric (włączyć zasoby Android dla tego taska); iOS rzeczywistego drivera. Nie ukrywać braku platformy fake testem.
- [ ] Uruchomić focused testy domeny/danych i platformowy test Room, `spotlessApply`, obejrzeć diff oraz schemat. Review granic i zachowania błędnego odczytu.

## Task 4: ViewModele, stany i graf Metro

**Właściciel:** subagent prezentacji. **Wynik:** przetestowane zachowanie obu ekranów.

**Pliki:** `P/presentation/BaseViewModel.kt`, `P/di/StarterGraph.kt`,
`P/ui/articles/{ArticlesState,ArticlesViewModel}.kt`,
`P/ui/articledetails/{ArticleDetailsState,ArticleDetailsViewModel}.kt`,
`T/ui/articles/ArticlesViewModelTest.kt`,
`T/ui/articledetails/{ArticleDetailsLoadingTest,ArticleNoteSavingTest}.kt`,
`T/fixtures/{FakeArticleRepository,FakeNoteRepository}.kt`.

**Interfejsy:** `ArticlesViewModel(loadArticles: LoadArticlesUseCase)` i
`ArticleDetailsViewModel(articleId: Int, loadDetails: LoadArticleDetailsUseCase, saveNote: SaveNoteUseCase)`.
Oba startują odczyt w init, mają read-only `state` i `onRetry()` działające tylko w Failed;
szczegóły dodatkowo `onNoteChanged(text: String)` i `onSave()`.
Stany odpowiadają dokładnie specyfikacji. Graph factory przyjmuje bazę i HttpClient,
wiąże repozytoria, dostarcza use case'y i unscoped ViewModele. Dla ID użyć
małej fabryki/metody grafu `createArticleDetailsViewModel(articleId: Int)`
konstruującej VM z dostarczonymi przez Metro use case'ami; bez nowego DI frameworka.

- [ ] Napisać testy listy: `WHEN request is pending THEN loading is visible`, `WHEN articles are loaded THEN content is visible`, `WHEN request fails THEN retry can load articles`, `WHEN retry is already running THEN no second request starts`.
- [ ] Testy szczegółów: `WHEN note is loading THEN editor is not enabled`, `WHEN note read fails THEN editor is not shown`, `WHEN article ID is invalid THEN failure appears without IO`, `WHEN details open directly THEN article and saved note are loaded by ID`, `WHEN retry is called with unsaved draft THEN draft is not reloaded` (Ready z lokalnym tekstem, wywołanie onRetry, bez nowych odczytów i bez zmiany draftu). Fake repozytorium i prawdziwe use case'y, bez Mockito dla use case'ów.
- [ ] Testy zapisu: `WHEN save is pending THEN saving is visible`, `WHEN save is repeated THEN only one write occurs`, `WHEN note changes during save THEN draft is unchanged`, `WHEN save fails THEN draft is retained`, `WHEN save completes THEN saved state contains normalized note`, `WHEN saved note is edited THEN success message clears`, `WHEN text is blank THEN validation appears without write`, `WHEN ViewModel is cleared THEN no save success is published`. Używać StandardTestDispatcher i CompletableDeferred, obserwować stan przed zwolnieniem gate.
- [ ] Potwierdzić czerwone focused testy. Zaimplementować stan, operacje i Metro. Chronić save przed launch; nie dokładać nullable Job/flag, gdy wystarcza stan Loading/Saving. Catch tylko wokół use case I/O, aktualizacja stanu poza try; nie ukrywać błędów reducerów.
- [ ] Testy uruchomić focused, formatować/obejrzeć diff. Review: wszystkie zależności VM są use case'ami, brak SDK w prezentacji, zachowany draft i brak fałszywego sukcesu.

## Task 5: Compose, hosty, dokumentacja i końcowa weryfikacja

**Właściciel:** subagent integracji. **Wynik:** uruchamialna aplikacja i czytelne przykłady.

**Pliki:** `P/App.kt`, `P/ui/navigation/{AppNavigation,Destination}.kt`,
`P/ui/articles/{ArticlesRoute,ArticlesScreen}.kt`,
`P/ui/articledetails/{ArticleDetailsRoute,ArticleDetailsScreen}.kt`,
`P/ui/theme/Theme.kt`, `P/platform/navigation/BackHandler.kt` i jego
natywne implementacje (tylko jeśli wspólne API nie wystarcza),
`shared/src/commonMain/composeResources/values/strings.xml`, hosty z etapu 1,
`androidApp/src/main/kotlin/me/kmpstarter/StarterApplication.kt`,
`README.md`, `AGENTS.md`, `docs/architecture/canonical-examples.md`,
`docs/architecture/adapting-the-example.md`, `.github/workflows/verify.yml`.

**Interfejsy:** destination `Articles` i `ArticleDetails(articleId: Int)`;
Screen dostaje odpowiedni state i callbacki; Route tworzy VM z grafu w scope
destination, zbiera state i posiada nawigację. `App(graph: StarterGraph)`.

- [ ] Podłączyć hosty i graf; Android wymaga INTERNET, bez dodatkowych uprawnień. Baza i klient poza rekompozycją; iOS host tylko osadza ComposeUIViewController.
- [ ] Zbudować Material 3 UI: lista z loading/empty/error/retry, przewijane szczegóły i pole notatki, błąd pola, save progress/error/success. Uwzględnić IME/insets i duże fonty, bez rozbudowanego design systemu. UI copy w resources; nie pisać testów dokładnej treści komunikatów.
- [ ] Podłączyć typowaną nawigację po ID, ochronę szybkiego podwójnego kliknięcia (sprawdzenie aktualnego destination/lifecycle i singleTop), back blokowany tylko podczas Saving na Android/iOS. Nie stosować indeksu listy jako ID ani zależności szczegółów od pamięci listy.
- [ ] Zaktualizować krótkie AGENTS i canonical-examples, wskazując rzeczywiste pliki i testy. Opisać drogę od kliknięcia do DB/HTTP, semantykę cancellation, trwałość draftu i dlaczego API nie przechowuje notatek. Przy każdym przykładzie wskazać pasujący test, kiedy używać i czego nie uogólniać. README: wymagane toolchainy, start Android/iOS i kolejność czytania kodu. Krótka instrukcja adaptacji: nazwa, package/bundle ID, URL, modele, schema i usunięcie/zastąpienie funkcji przykładowej. Bez skryptu generatora i bez wymogu czytania LifeGenie do zrozumienia startera.
- [ ] Dodać małe CI na pull_request: Linux — spotlessCheck, wspólne unit testy, Android assemble/lint; macOS — wspólne iOS testy oraz simulator host build. Wybrać simulator z preinstalowanego runtime runnera i w CI w razie potrzeby go uruchomić. Jego UUID przekazać do Gradle przez `-PiosTestDevice=<UUID>` i podłączyć tę właściwość do `device` taska KotlinNativeSimulatorTest; zanotować wybrany runtime/UUID. Lokalnie nadal nie uruchamiać symulatorów bez zgody. Bez prywatnych sekretów i podpisywania. Komendy tasków sprawdzić lokalnie przed zapisaniem workflow; lokalny wynik nie oznacza wykonania workflow na GitHub.
- [ ] Wykonać `./gradlew spotlessApply` i sprawdzić diff. Końcową pełną weryfikację wykonać z tymczasowej kopii zawierającej tylko pliki repo (bez build/.gradle/local.properties i bez symlinków do oryginału), z tymi samymi toolchainami/cache zależności. SDK wskazać udokumentowaną zmienną środowiskową. Tam uruchomić raz `./gradlew spotlessCheck :shared:testAndroidHostTest :androidApp:assembleDebug :androidApp:lintDebug`; nie powtarzać identycznego builda w oryginalnym katalogu bez potrzeby.
- [ ] W tej samej tymczasowej kopii wykonać `./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:iosSimulatorArm64Test -PiosTestDevice=<dostępny-UUID>`. Nie kopiować konfiguracji CoreImage/QR z Caregiver. Dla hosta: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build`.
- [ ] Na dostępnych, już uruchomionych urządzeniach ręcznie sprawdzić listę, retry po braku sieci, otwarcie szczegółów, walidację pustego tekstu, zapis i odczyt po restarcie, klawiaturę/duży font, back podczas zapisu i odtworzenie hosta. Nie dodawać sztucznych opóźnień ani debug przełączników; trudne błędy zapisu/pending są deterministycznie sprawdzone w unit testach. Brak urządzenia to blocked/not run, nie passed.
- [ ] Reviewer etapu, następnie końcowy przegląd całości przez osobnego subagenta. Raport koordynatora: wykonane komendy, wyniki, ograniczenia i ścieżki do kodu. Poprawki wymagają focused testów, bez ponownego uruchamiania wszystkich niezmienionych suite'ów.

## Warunek zakończenia

Oba ekrany działają zgodnie ze spec, warstwy są czytelne, testy zachowania i
formatowanie przechodzą, Android oraz host/framework iOS są zweryfikowane w
dostępnym środowisku. Build z niezależnej kopii i instrukcja adaptacji potwierdzają przydatność do nowego projektu. Każda niewykonana kontrola ma jawny powód; przy braku istotnego builda/testu raport mówi „implementacja gotowa, baza nie w pełni zweryfikowana”, a nie „wszystko gotowe”. Użytkownik
otrzymuje kod do własnego przeglądu; ekstrakcja template/generatora/skilla jest
następnym, osobnym etapem po jego uwagach.
