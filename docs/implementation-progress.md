# Postęp implementacji KMPStarter

Plan: `docs/superpowers/plans/2026-10-05-articles-notes-plan.md`.
Specyfikacja: `docs/superpowers/specs/2026-10-05-articles-notes-design.md`.
Koordynator prowadzi pięć kolejnych etapów; implementerzy nie pracują równolegle.
Każdy etap ma osobne review. Brak publikacji i zmian w LifeGenie.

## Stan

| Etap | Status | Weryfikacja |
| --- | --- | --- |
| 1. Fundamenty | Ukończony, review approved | Formatowanie, Android assemble/lint, Kotlin iOS i host Xcode passed |
| 2. HTTP | Ukończony, review approved | 14 testów passed + focused 2/2 po korekcie testów; Spotless passed |
| 3. Room i use case'y | Ukończony, review approved | 30/30 host, Android assemble/lint, iOS compile/link passed; iOS runtime blocked |
| 4. ViewModele i Metro | Ukończony, review approved | 50/50 host, Spotless, Android/iOS graph compile passed |
| 5. UI i integracja | Ukończony, oba review approved | Clean-copy: 50/50 host, Spotless, Android assemble/lint, iOS compile/link i Xcode passed |

## Kontrola planu przed implementacją

| Zakres | Producent → konsument / spójność | Wynik |
| --- | --- | --- |
| Etap 1 | Dwa hosty, dwa moduły, wspólny ekran; build zamiast pozornego testu | Spójny |
| Etap 2 | Repozytorium Article, data source i konfiguracja MockEngine | Spójny |
| Etap 3 | DAO jako data source, produkcyjne buildery w testach reopen | Spójny |
| Etap 4 | Prawdziwe use case'y w testach VM, Saving przed coroutine | Spójny |
| Etap 5 | Route posiada VM/lifecycle, Screen stan/callbacki; kopia projektu | Spójny |
| 1 → 2, 3, 4 | Konfiguracja shared i zależności → kod/testy poszczególnych warstw | Wersje i nazwy tasków wymagają potwierdzenia buildem |
| 1 → 5 | Hosty i App → integracja grafu poza rekompozycją | Spójny |
| 2 → 3, 4 | ArticleRepository → koordynacja szczegółów i VM | Sygnatury jednoznaczne |
| 2, 3 → 4 | HttpClient/StarterDatabase/repozytoria → graf Metro | Spójny |
| 3 → 4 | SaveNoteResult i ArticleDetails → stany ekranu | Spójny |
| 4 → 5 | Read-only StateFlow i callbacks → lifecycle Route/UI | Spójny |
| Wszystkie → 5 | Testy i konfiguracja → CI, dokumentacja, finalne review | Brak powtórnych buildów bez powodu |

## Decyzje i ograniczenia

- 2026-10-05: Katalog początkowy zawiera wyłącznie specyfikację i plan; nie ma `.git`. Etap 1 inicjalizuje samodzielne repozytorium i gałąź `codex/articles-notes` zgodnie z planem. Praca odbywa się w wskazanym katalogu; nowy worktree nie jest potrzebny do izolacji pustego projektu.
- Nie rozpoczynamy ponownie projektowania ani pisania planu. Nie instalujemy narzędzi i nie uruchamiamy lokalnych emulatorów/symulatorów. Rozwiązywanie deklarowanych zależności Gradle jest dozwolone.
- Read-only `adb devices -l`: brak podłączonych urządzeń Android. Ręczne scenariusze aplikacji będą not run, jeśli dostępność nie zmieni się przed etapem 5; żadnego emulatora nie uruchamiamy.
- Raporty etapów i briefy: `.superpowers/sdd/articles-notes/` (pliki robocze ignorowane przez Git); trwałe wyniki trafiają do tego dokumentu.

## Wyniki i review

Status „passed” wymaga rzeczywistego wyniku komendy; blokada platformy nie zastępuje testu.

### Etap 1 — ukończony

- Baseline dokumentów: `c667868d48188252feafe8e44dfc8bee229303dd`; gałąź `codex/articles-notes`.
- Inspekcja implementera: Git 2.33.0, JDK 17.0.14, Xcode 26.6 (17F113). Android SDK zawiera platformę `android-37.0`; dostępne build tools do 36.0.0. Zgodność zostanie oceniona przez build, bez instalacji brakujących narzędzi.
- `./gradlew --version`, `./gradlew spotlessApply` oraz `./gradlew spotlessCheck :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`: passed (ostatnie: 31 s, 64 taski); logi implementera w katalogu raportów. Zainstalowane Android build tools wystarczyły.
- Ruling: host Xcode ustawia `ARCHS=arm64` dla Debug/Release — domyślny generic simulator próbował także x86_64, którego zatwierdzony plan nie obejmuje, i `syncComposeResourcesForIos` zakończył się exit 65. Najmniejsza korekta ogranicza host do przewidzianych targetów; koszt: ten przykład wymaga Apple Silicon dla lokalnego iOS simulatora. Build hosta po zmianie jest w toku.
- Implementacja fundamentów: `6ae3676`; osobne review w toku. Host Xcode po korekcie: exit 0, `BUILD SUCCEEDED`. Lint po poprawce manifestu: exit 0, `No issues found`. Host tests: `NO-SOURCE` (nie traktujemy tego jako testów zachowania). Nie uruchomiono urządzeń ani symulatorów.
- Ostrzeżenia do końcowego raportu: deprecations helperów Compose DSL, stripping natywnej biblioteki Android, obiekt danych ICU/Skiko simulator 18.5 przy minimum iOS 16. Buildy przeszły; zgodność runtime iOS 16 pozostaje nieweryfikowana.
- Task 1: complete (`c667868` → `6ae3676`, review approved, brak Critical/Important). Review: `/private/tmp/kmpstarter-review1.md`.
- Drobne uwagi review odłożone: helpery Compose, stripping, ICU, AppIntents metadata. Korekta opisu dowodu: przy `CODE_SIGNING_ALLOWED=NO` task `embedAndSignAppleFrameworkForXcode` był `SKIPPED`; build frameworka, synchronizacja resources oraz linkowanie Swift przeszły. Nie deklarujemy wykonania podpisywania.

### Etap 2 — ukończony

- RED: `./gradlew :shared:testAndroidHostTest --tests '*DefaultArticleRepositoryTest'`, exit 1 w kompilacji testów z oczekiwanymi unresolved references brakującej implementacji. Zależności Ktor/testów zostały rozwiązane. Log: `.superpowers/sdd/articles-notes/logs/task-2-red.log`.
- Implementacja: `22f9ebd`, review w toku. Pierwszy run: 16 testów, 14 passed / 2 failed (asercje tożsamości wyjątków przy coroutine stack-trace recovery). Poprawione wyłącznie testy; focused rerun 2/2 passed. Nie deklarujemy jednego runu 16/16. Spotless passed, diff obejrzany.
- Wprowadzono publiczne kontrakty Article, data source Ktor, fabrykę klienta i silniki Android/Darwin. Bieżąca kompilacja iOS będzie sprawdzona przy integracji; testy używają MockEngine bez sieci. Ostrzeżenie SLF4J NOP odnotowane, bez dokładania loggera do aplikacji.
- Task 2: complete (`6ae3676` → `22f9ebd`, review approved, brak Critical/Important). Review: `/private/tmp/kmpstarter-review2.md`; recenzent potwierdził także konkretne wyjątki silnika Darwin w źródłach zainstalowanego Ktor. Drobne SLF4J warning odłożone do końcowego review.

### Etap 3 — ukończony

- Inspekcja `simctl` read-only: brak booted simulatorów. Rzeczywisty iOS reopen test nie będzie lokalnie wykonany; planujemy kompilację testu/frameworka bez uruchamiania symulatora. Android reopen będzie używał Robolectric i produkcyjnego buildera.
- RED domeny: 8/8 failed z TODO skeletons (testy wykonywalne). Następny run po implementacji domeny: 8 domenowych passed, 5/5 testów repozytorium failed zgodnie z brakiem implementacji. Logi `task-3-domain-red.log`, `task-3-repository-red.log`.
- Po implementacji repozytorium: 13/13 testów domeny/repozytorium passed. Reopen RED: oczekiwany błąd TODO buildera. Po dodaniu buildera run 29/30: Android persistence failed z `UnsatisfiedLinkError` (Android sqlite JNI ładowane w JVM macOS).
- Ruling: wyłącznie host-test runtime użyje wariantu `sqlite-bundled-jvm` — oficjalny sposób dla Robolectric, wymagany do JNI w JVM. Builder produkcyjny i API BundledSQLiteDriver pozostają wspólne z testem. Koszt: ten test potwierdza Room/builder i trwałość przez driver JVM, a nie natywny binarny wariant SQLite na urządzeniu Android; test ręczny na urządzeniu nadal potrzebny.
- GREEN po korekcie: 30 testów / 0 failures (16 HTTP + 8 domeny + 5 repozytorium + 1 rzeczywisty reopen). Końcowy serial run etapu passed: Spotless, 30 host testów, Android assemble/lint (`No issues found`), compileKotlinIosSimulatorArm64, compileTestKotlinIosSimulatorArm64, linkDebugFrameworkIosSimulatorArm64. Poprzedni native compile ujawnił brak importu extension IO i Foundation opt-ins; poprawiono i ponowiono ze zmianą kodu.
- Implementacja `31f5777`, review w toku. `linkDebugTestIosSimulatorArm64`: exit 0 (binarne testy zlinkowane, niewykonane). `dependencyInsight` potwierdza Android runtime `sqlite-bundled-android:2.6.2` i tylko test runtime `sqlite-bundled-jvm:2.6.2`. Xcode host pozostaje do końcowej weryfikacji w kopii.
- Task 3: complete (`22f9ebd` → `31f5777`, review approved, brak Critical/Important). Review: `/private/tmp/kmpstarter-review3.md`. Drobne warnings expect/actual Beta i stripping odłożone. Koordynator odczytał 5 bieżących XML: 16+1+5+4+4=30, wszystkie 0 failures/errors; reviewer potwierdził 14 nowych testów etapu. iOS reopen nadal blocked.

### Etap 4 — ukończony

- Kontrakty stanów zgodne ze spec: lista Loading/Content/Failed, szczegóły Loading/Ready/Failed, zapis Idle/Saving/Saved/Invalid/Failed. Błąd szczegółów rozróżnia Article(reason), NoteStorage i InvalidArticleId bez typów SDK. Graf udostępni createArticlesViewModel i createArticleDetailsViewModel(id).
- RED: focused 17 testów ViewModeli, kompilacja passed, 17/17 failed asercjami z no-op VM. Log `task-4-red.log`. Implementacja przejść stanów i Metro w toku.
- Rozszerzony RED: 20/20 failed (dodatkowo NotFound, ponowienie po failed load bez duplikatu, ponowienie failed save bez przeładowania draftu).
- Ruling: runtime Gradle wymaga teraz JDK 21+ przez zatwierdzony Metro 1.4.3 — plugin jawnie odrzucił JDK 17 przy pierwszej próbie GREEN (`task-4-green.log`). Użyjemy już zainstalowanego JDK 25.0.2, bez instalacji i zmian wersji zależności; Android bytecode pozostaje 17. README/CI zostaną dopasowane w etapie 5. Koszt: użytkownik startera musi mieć JDK 21+ mimo że sam szkielet działał na 17.
- Ruling: `createArticleDetailsViewModel(id)` jest rozszerzeniem StarterGraph w tym samym pliku — Metro traktuje metodę grafu z parametrem jako member injector. Rozszerzenie nadal tworzy niesingletonowy VM z use case'ami grafu, bez dodatkowej abstrakcji DI. Koszt: Route musi importować rozszerzenie. `createStarterGraph(database, client)` wywołuje wygenerowaną fabrykę Metro.
- GREEN focused: 20/20 nowych testów passed; Android graph kompiluje się. Następny serial run po zmianie wspólnego runtime: `spotlessCheck :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64 :shared:compileTestKotlinIosSimulatorArm64`, exit 0, 50/50 (30 poprzednich + 20 nowych), zero failures/errors/skips. Kompilacja głównego grafu i testów iOS passed. JDK 25/Robolectric ostrzega o future native-access requirement; bez zmian maskujących ostrzeżenie.
- Task 4: complete (`31f5777` → `432b60d`, review approved, brak Critical/Important). Review: `/private/tmp/kmpstarter-review4.md`; XML 50/50 potwierdzone przez review. Niesprawdzone host/back/UI pozostają do integracji. Wymóg JDK 21+ i import rozszerzenia grafu przekazane następnemu implementerowi.

### Etap 5 — ukończony

- Material 3 UI, typowana nawigacja po ID, hosty oraz wspólny Compose BackHandler napisane. Bez dodatkowych plików platformowych back. Callback back odczytuje bieżący VM state, aby zabezpieczyć także przerwę przed rekompozycją po Save.
- Wczesna kompilacja integracji: `spotlessApply :shared:compileAndroidMain :shared:compileKotlinIosSimulatorArm64`, exit 0 (17 s). Graf iOS inicjalizowany poza content/rekompozycją. Dokumentacja, CI i pełny clean-copy build w toku.
- Candidate `166f650` (21 plików integracji). Kopia przez `git archive`: `/private/tmp/kmpstarter-task5-166f650`, 76 plików / 0 symlinków; przed buildem brak `.git`, `.gradle`, build directories i `local.properties`. Weryfikacja w tej kopii jest w toku; cache i toolchainy te same, SDK/JDK przez zmienne środowiskowe.
- Pierwszy clean-copy Android run exit 1: host korzysta z publicznych fabryk zwracających typy Ktor, ale `implementation` w shared nie eksportuje ich do compile classpath hosta. Ruling: androidApp deklaruje używane `ktor-client-core` — najmniejsza jawna zależność hosta posiadającego klienta, bez przenoszenia logiki danych i bez nowych modułów. Koszt: katalog zależności zawiera tę samą bibliotekę także w hoście. Nowy candidate/archive i covering build po poprawce.
- Corrected candidate `505fc35`, kopia `/private/tmp/kmpstarter-task5-505fc35` (ponownie 76 plików, 0 symlinków, bez lokalnych build/cache inputs). Pełny Android clean-copy run: exit 0, 18 s, 97 tasków wykonanych; Spotless passed, 50 testów / 0 failures/errors/skips, assembleDebug passed, lintDebug `No issues found`. iOS/Xcode w toku.
- Końcowe clean-copy iOS: main/test compile i test binary link exit 0 (1m13s), unsigned generic Xcode host `BUILD SUCCEEDED` (Gradle osadzony 7 s). 76 source blobs kopii zgodne z `505fc35`, zero mismatches. Koordynator odczytał Android XML i lint kopii: 50/50, zero failures/errors/skips, `No issues found`.
- Actual iOS test runtime i ręczne scenariusze obu aplikacji blocked/not run z braku booted iOS simulatora/podłączonego Androida. CI napisane, ale workflow nie uruchomione na GitHub. Ostrzeżenia ICU minimum iOS 16, Room Beta, native access i stripping pozostają jawne. Etap czeka na osobne review, potem całościowe review.
- Task 5: complete (`432b60d` → `505fc35`, review approved, brak Critical/Important). Review: `/private/tmp/kmpstarter-review5.md`. Drobna uwaga odłożona do finalnego review: `ArticleDetailsScreen.kt:55`, consume Scaffold padding przed imePadding, aby uniknąć nadmiarowego bottom inset przy klawiaturze. Warnings i ograniczenia runtime pozostają jawne.

## Finalny przegląd

Pakiet obejmuje wszystkie zmiany `c667868` → `505fc35`. Osobny final reviewer potwierdził zgodność i brak Critical/Important. Raport: `/private/tmp/kmpstarter-final-review.md`. Ponownie odczytał 50/50 XML, lint i logi clean-copy, bez powtarzania buildów.

- Jedyna uwaga kodu: consume Scaffold padding przed imePadding; przekazana autorowi etapu 5 jako pojedyncza końcowa poprawka. Weryfikacja obejmie formatowanie i kompilację Android/iOS w nowej niezależnej kopii; bez ponawiania niezmienionych suite'ów.
- Zaakceptowane jako poza V1: offline/cache/sync, konta/usuwanie/paginacja/retry automatyczne, trwały draft/dialog/channel, generator/skill/framework, signing/dystrybucja/hardening/backup policy. To zakres zatwierdzonej specyfikacji, nie pominięte funkcje.
- Nierozstrzygnięte runtime/hosted behaviors pozostają ograniczeniami weryfikacji: native gesty/rendering, minimum iOS 16, rzeczywisty iOS Room persistence i wykonanie CI. Żaden lokalny build/test nie zastępuje tych wyników.
- Final fix: `045610f` — jeden plik, import i `.consumeWindowInsets(padding)` między padding a imePadding. Fresh archive `/private/tmp/kmpstarter-task5-045610f`: 76 source blobs zgodnych, 0 symlinków i bez lokalnych build/cache inputs. `./gradlew spotlessCheck :shared:compileAndroidMain :shared:compileKotlinIosSimulatorArm64`: exit 0, 5 s, 23 taski. Pokrycie pozostałych niezmienionych ścieżek: full clean-copy run `505fc35`; suite'ów nie powtarzano bez powodu. Scoped re-review w toku.
- Scoped re-review: ADDRESSED, brak nowych uwag w poprawce. Raport `/private/tmp/kmpstarter-final-fix-review.md`. Finalny review całości approved, brak Critical/Important i brak otwartych uwag kodu. Pozostałe ostrzeżenia są jawnie udokumentowane, bez tłumienia lub zbędnych zależności.

## Końcowy wynik

**Implementacja gotowa, baza nie w pełni zweryfikowana.** Pięć etapów ukończonych, każde z osobnym review; końcowy review i scoped review poprawki również ukończone. Kod aplikacji: `045610f` na `codex/articles-notes`. Brak publikacji, zmian LifeGenie i funkcji poza zakresem.

| Kontrola | Rzeczywisty wynik |
| --- | --- |
| Clean-copy `./gradlew spotlessCheck :shared:testAndroidHostTest :androidApp:assembleDebug :androidApp:lintDebug` (`505fc35`) | Exit 0; 50/50 testów, 0 failures/errors/skips; APK zbudowany; lint No issues found |
| Clean-copy `./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:compileTestKotlinIosSimulatorArm64 :shared:linkDebugTestIosSimulatorArm64` (`505fc35`) | Exit 0; kod i testy skompilowane, test executable zlinkowany; testy niewykonane |
| Clean-copy `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build` (`505fc35`) | Exit 0; BUILD SUCCEEDED; podpisywania nie deklarujemy |
| Clean-copy focused Spotless i Android/iOS shared compile po poprawce UI (`045610f`) | Exit 0; BUILD SUCCESSFUL; bez powtórki niezmienionych suite'ów |
| `:shared:iosSimulatorArm64Test`, rzeczywisty iOS persistence | Blocked / not run: brak booted simulatora, brak zgody na lokalny startup |
| Ręczne scenariusze Android/iOS, native SQLite Android i minimum iOS 16 | Not run: brak urządzeń; kompilacja i Robolectric nie zastępują tych kontroli |
| Workflow GitHub | Napisany, taski/property i YAML sprawdzone; hosted run nie wykonany |

49 testów wspólnych i 1 Android Room reopen. Android test używa produkcyjnego buildera i prawdziwego SQLite JVM; iOS reopen test używa produkcyjnego buildera, ale został tylko skompilowany i zlinkowany.

Pełne logi i raporty zachowano lokalnie w ignorowanym `.superpowers/sdd/articles-notes/`, zamiast usuwać dowody przed własnym przeglądem użytkownika. Trwałe wyniki, decyzje i ograniczenia są w tym dokumencie. Instrukcje uruchomienia: `README.md`; mapa kod/test: `docs/architecture/canonical-examples.md`; adaptacja: `docs/architecture/adapting-the-example.md`.


## Ujednolicenie UI Material 3 — 2026-10-05

Zakres zaakceptowany przez użytkownika: oficjalne komponenty Material 3, wspólny
scaffold i stany ekranów, standardowy przycisk wstecz, wyśrodkowane ładowanie,
postęp zapisu obok akcji oraz trwałe wymagania projektu.

- Dodano `ui/components/ScreenScaffold`, `ScreenLoading` i `ScreenError`; oba
  ekrany je współdzielą. Scaffold stosuje padding i konsumuje insety, a edytor
  osobno dodaje IME padding. Nawigacja i blokada system back pozostają w Route.
- Toolbar używa `IconButton` i `Icons.AutoMirrored.Filled.ArrowBack`, z opisem
  z zasobów. Lista używa klikalnych Material Card; postęp zapisu jest obok Save.
- Material 3 przypięto osobno do 1.9.0, najnowszej stabilnej wersji według
  publicznych metadanych Maven odczytanych 2026-10-05; nowsze wersje są alpha.
  Standardowa ikona pochodzi z `material-icons-core:1.7.3`.
- Zaktualizowano AGENTS, zatwierdzoną specyfikację, README, mapę canonical examples
  i instrukcję adaptacji. Nie dodano testów źródłowych ani automatyzacji UI.
- `./gradlew spotlessApply`: exit 0; `git diff --check`: exit 0; diff obejrzany,
  również nowe pliki. Osobny przegląd kodu: brak uwag wymagających poprawki.
- `./gradlew spotlessCheck :shared:testAndroidHostTest :androidApp:assembleDebug
  :androidApp:lintDebug :shared:compileKotlinIosSimulatorArm64`: exit 0.
  Raporty XML: 8 suites, 50 tests, 0 failures/errors/skipped. Lint: No issues found.
- Unsigned generic simulator `xcodebuild` hosta iOS: exit 0, BUILD SUCCEEDED.
  Log: `/private/tmp/kmpstarter-material-xcode.log`. Nie uruchomiono symulatora.
- `:shared:iosSimulatorArm64Test`: blocked / not run — odczyt `simctl list devices
  booted` potwierdził brak uruchomionych urządzeń. Wygląd, duże czcionki, RTL,
  klawiatura, dark mode i gesty na urządzeniu: not run. Nie deklarujemy wizualnej
  weryfikacji ani wykonania iOS persistence na podstawie kompilacji.

Weryfikacja korzystała z istniejącego JDK 25.0.2. Pozostają wcześniejsze
ostrzeżenia Room expect/actual Beta, native access, stripping bibliotek Androida
oraz minimum iOS obiektu ICU. Nie instalowano ani nie aktualizowano narzędzi;
nie wykonano commita, push, merge ani publikacji.
