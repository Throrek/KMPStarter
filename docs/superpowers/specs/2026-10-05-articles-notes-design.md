# KMPStarter — projekt przykładu artykułów i notatek

Status: przykład zaimplementowany; wymagania UI Material 3 zaakceptowane i wdrożone. Data: 2026-10-05.

## Cel

Mała aplikacja Android/iOS, której kod można przeczytać i samodzielnie zmieniać.
Pokazuje HTTP, lokalną bazę, przepływ danych, postęp, błędy, ponowienie i sukces.
LifeGenie Caregiver jest źródłem doświadczeń architektonicznych, a nie szablonem
kopiowanym wraz z jego logiką produktu. Ten etap nie tworzy generatora ani skilla.

## Zakres pierwszej wersji

- Dwa ekrany: lista artykułów oraz szczegóły artykułu z edytorem jednej notatki.
- Demonstracyjne API: `https://jsonplaceholder.typicode.com`, wyłącznie
  `GET /posts` oraz `GET /posts/{id}`. Pola `id`, `title`, `body` tworzą `Article`.
- Notatka to `articleId` i jeden wymagany, niepusty tekst. Jedna notatka na artykuł.
- Notatki zapisuje Room. Nie wysyłamy ich do demonstracyjnego API.
- Wspólne Compose UI, nawigacja, ViewModele, use case'y, repozytoria, DTO,
  schemat Room, DAO, graf Metro oraz większość testów znajdują się w `shared`.
- Dwa moduły Gradle: `:shared`, `:androidApp`; osobny cienki host Xcode `iosApp`.
- Bez kont, Firebase, synchronizacji, cache artykułów, usuwania notatek,
  wyszukiwania, paginacji, analityki, automatycznych retry i własnego backendu.
- Spotless i testy wchodzą od pierwszych etapów. CI jest prostym sprawdzeniem PR,
  bez podpisywania, dystrybucji aplikacji i sekretów.

## Architektura

```text
Screen -> callback Route -> ViewModel -> UseCase -> Repository (domain)
                                                    |
                                            implementation (data)
                                                    |
                              ArticleRemoteDataSource / ArticleNoteDao
                                           Ktor HTTP / Room
```

Każdy odczyt/zapis danych z ViewModelu przechodzi przez use case — to świadoma
decyzja użytkownika dla tego startera, także dla prostego odczytu. Lokalna zmiana
tekstu i pozostałe przejścia stanu UI pozostają w ViewModelu bez osobnego use case’a. Nie dodajemy jednak
interfejsu do każdej klasy, bazowych repozytoriów ani uniwersalnego execute/retry.
DAO pełni rolę lokalnego data source'a; nie otaczamy go mechanicznym wrapperem.
Zdalny data source posiada endpointy i deserializację DTO. Repozytorium mapuje
DTO/entity na model domenowy oraz oczekiwane błędy SDK na błędy domenowe.

Modele domenowe nie importują Ktor, Room, Compose ani typów platformowych.
Use case'y można oznaczyć `@Inject`, jak w Caregiver; Metro nie przenosi do nich
odpowiedzialności za platformę. ViewModele mają read-only `StateFlow` i metody
obsługi działań. Mały `BaseViewModel<State>` zawiera tylko publikowanie stanu.
W tej wersji nie ma efektu wymagającego kanału: przejście do szczegółów wynika
z kliknięcia w Route, a sukces zapisu jest stanem. Nie kopiujemy nieużywanej
obsługi zdarzeń z Caregiver.

Route posiada lifecycle, tworzenie ViewModelu w `ViewModelStore` destination,
zbieranie stanu przez `collectAsStateWithLifecycle`, nawigację i system back.
Screen przyjmuje niemutowalny stan i callbacki. ViewModel szczegółów dostaje
stały `articleId`; nawigacja przenosi ID, a nie cały obiekt ani draft.
Graf Metro, baza i HttpClient powstają raz na host, poza rekompozycją.
ViewModele nie są singletonami grafu. Testy zamykają klienta i bazę.

## Wymagania UI — Material 3

Uzupełnienie zaakceptowane 2026-10-05: UI korzysta z oficjalnego systemu Material 3
w Compose Multiplatform, w najnowszej stabilnej kompatybilnej wersji, przypiętej
osobno w katalogu zależności. Kolory, typografia i kształty pochodzą z
`MaterialTheme`, z obsługą systemowego jasnego/ciemnego motywu. Nie tworzymy
własnych zamienników standardowych kontrolek ani nie wprowadzamy alpha/Expressive
wyłącznie dla efektów wizualnych.

Oba ekrany używają `ui/components/ScreenScaffold`: Material `Scaffold`, `TopAppBar`,
padding treści i konsumowanie insetów. Szczegóły przekazują callback wstecz i
blokadę podczas zapisu; wspólny komponent pokazuje `IconButton` ze standardową
strzałką `AutoMirrored.ArrowBack` oraz lokalizowanym opisem dostępności. Route
nadal posiada nawigację i system back; komponent nie zna ViewModelu ani kontrolera.

`ScreenLoading` umieszcza standardowy Material `CircularProgressIndicator` i
komunikat pośrodku dostępnej treści pod paskiem aplikacji, na obu osiach.
`ScreenError` zapewnia wspólny przewijalny komunikat błędu i opcjonalny przycisk
ponowienia. Lista używa klikalnych Material `Card`. Edytor używa
`OutlinedTextField` i `Button`, uwzględnia klawiaturę; postęp zapisu pojawia się
obok akcji zapisu, bez zastępowania edytora pełnoekranowym ładowaniem.

Przegląd obejmuje współdzielenie komponentów, insety, RTL, dostępność i motywy.
Build i testy zachowania nie potwierdzają wyglądu na urządzeniu; centrowanie,
duże czcionki, klawiatura i gesty wymagają kontroli na dostępnym urządzeniu.

## Zachowanie

### Lista

Start i jawne ponowienie: `Loading -> Content(articles)` albo `Failed(error)`.
`Content(emptyList())` renderuje pusty stan. Brak sztucznego opóźnienia spinnera.
Ponowne wywołanie podczas `Loading` nie rozpoczyna kolejnego żądania.
Kliknięcie w artykuł otwiera typowaną trasę z jego ID; Route chroni podwójne
kliknięcie przed utworzeniem dwóch destination.

### Szczegóły i notatka

`Loading -> Ready(article, draft, saveStatus)` albo `Failed(error)`.
Use case ładuje artykuł po ID, następnie notatkę z Room. Edytor staje się aktywny
dopiero po obu odczytach. Błąd odczytu notatki jest błędem ekranu, a nie pustą
notatką: użytkownik nie może przypadkiem nadpisać danych, których nie odczytano.
Ponowienie dotyczy nieudanego startowego odczytu; V1 nie ma odświeżania otwartego
edytora ani obserwacji nadpisującej draft. `onRetry` działa tylko w Failed;
w Loading/Ready niczego nie uruchamia, również gdy Ready zawiera błąd zapisu.
Wtedy ponowienie zapisu odbywa się przez `onSave`, z zachowanym draftem.

Zmiany pozostają lokalne do naciśnięcia Zapisz. Use case przycina brzegowe białe
znaki; pusty wynik daje walidację przy polu, bez zapisu. Zapis jednej notatki
jest atomowym upsert po `articleId`. `Saving` jest ustawiane przed startem
coroutine; blokuje ponowny zapis, zmianę tekstu i wyjście przez toolbar/system back.
Sukces jest publikowany dopiero po zakończeniu DAO: pozostajemy na szczegółach,
pokazujemy inline „Zapisano” i tekst zapisany po normalizacji. Kolejna zmiana
tekstu usuwa ten komunikat. Błąd zachowuje dokładny draft i pozwala powtórzyć zapis.

Stan zapisu ma warianty `Idle`, `Saving`, `Saved`, `Invalid`, `Failed`.
Nie synchronizujemy kilku niezależnych booleanów. Zwykłe wyjście z edytora
odrzuca niezapisany draft, bez dodatkowego dialogu w V1. Draft przeżywa
odtworzenie hosta przy zachowanym ViewModelu, ale nie śmierć procesu. Zapisane
notatki przeżywają restart. Po odtworzeniu trasy odczytujemy dane po ID.
Niepoprawne ID odrzucamy przed I/O; HTTP 404 daje stan „nie znaleziono”.
Repozytorium odrzuca odpowiedź szczegółów z ID innym niż żądane jako
InvalidResponse, aby nie połączyć artykułu z notatką innego artykułu.

### Błędy i anulowanie

HTTP: połączenie, timeout, odpowiedź HTTP, niepoprawny payload i brak artykułu.
W bazie: oczekiwany błąd SQLite jest tłumaczony na `NoteStorageException`.
UI nie otrzymuje surowych wyjątków SDK ani ich tekstów. Repozytoria łapią
konkretne błędy I/O/SDK; nie stosujemy `catch(Exception)` ukrywającego błędy kodu.
`CancellationException` propaguje się i nigdy nie oznacza błędu/sukcesu dla UI.
Limit czasu HTTP wynosi 15 s jako nazwana stała. Brak automatycznych retry.
Anulowanie zapisu nie jest obietnicą rollbacku już zakończonej transakcji:
następne otwarcie odczyta rzeczywisty stan bazy, upsert pozostaje idempotentny.

## Weryfikacja i prostota

- Konfiguracja i stałe domenowe w Kotlinie: `const val` w `companion object` właściciela albo parametry konstruktora klasy. Bez stałych top-level i magicznych wartości w logice. Dotyczy również konfiguracji infrastruktury testowej; literały danych i oczekiwań testów, wartości układu Compose, komunikaty diagnostyczne oraz deklaratywne pliki konfiguracji pozostają dopuszczalne.
- Nazwy wszystkich funkcji testowych zapisuj w backtickach jako zdania po angielsku: `WHEN <condition> THEN <expected outcome>`. Słowa `WHEN` i `THEN` są wielkimi literami, pozostała treść używa spacji zamiast camelCase.
- Każdy nietrywialny test: `// Given`, `// When`, `// Then`, literalne oczekiwania.
- `kotlin.test`, `kotlinx-coroutines-test`, małe fake repozytoria/DAO, Ktor MockEngine.
- Testy ViewModeli używają prawdziwych use case'ów z fake repozytoriami.
- Testy sieci nie wymagają Internetu; jeden test rzeczywistej bazy na platformę
  sprawdza zapis, zastąpienie notatki i odczyt po zamknięciu/ponownym otwarciu.
  Test wywołuje produkcyjny builder z podmienioną ścieżką pliku, aby sprawdzić
  także rzeczywistą konfigurację drivera i Room.
- Nie testujemy forwarding use case'a, getterów, Compose ani samego frameworka.
- Spotless: `ktfmt().kotlinlangStyle()`, `spotlessApply` przed sprawdzeniem diffu,
  `spotlessCheck` w weryfikacji i CI; formatowanie nigdy nie zastępuje review.
- Schemat Room startuje od v1, bez destructive fallback. Zmiana schematu po
  udostępnieniu aplikacji wymaga osobnej decyzji o migracji.
- Nowe pliki app/test: Kotlin; Swift tylko dla minimalnego hosta iOS.
- Około 250 linii produkcyjnych / 500 testowych to sygnał przeglądu, nie limit.
- Natywne minimum: engine HTTP, builder/ścieżka bazy, host, obsługa back.
- Framework iOS musi się kompilować; osobno trzeba sprawdzić host Xcode.
  Nie deklarujemy uruchomienia na urządzeniu z samego wyniku unit testów.

## Kryteria przydatności do kolejnych aplikacji

Po implementacji repozytorium musi być samodzielne: bez composite buildów,
symlinków, absolutnych ścieżek i odwołań wykonawczych do LifeGenie. Historyczne
odwołanie w dokumentacji jest dozwolone. Build/testy działają na podstawie
plików repo i udokumentowanych narzędzi, bez nieśledzonych plików autora.

README opisuje wymagane wersje narzędzi, uruchomienie obu platform, komendy
weryfikacji i kolejność czytania wzorca. Krótka instrukcja adaptacji wskazuje,
gdzie zmienić nazwę, package/bundle ID, URL API, modele, schemat Room i funkcję
przykładową. Nie powstaje skrypt masowej zmiany nazw ani dodatkowy framework.

Mapa canonical-examples dla każdej odpowiedzialności wskazuje kod i test,
wyjaśnia kiedy zastosować wzorzec oraz rozdziela ogólny mechanizm od decyzji
tej aplikacji. Wprost wymienia ograniczenia V1: brak cache/synchronizacji,
brak trwałości draftu po śmierci procesu, brak przykładu efektu nawigacyjnego.
Zależność otwarcia notatki od odczytu artykułu online to uproszczenie przykładu,
a nie zalecenie dla wszystkich aplikacji offline.

Przed uznaniem bazy za zweryfikowaną trzeba sprawdzić build z kopii zawierającej
tylko pliki projektu, poza oryginalnym katalogiem, z tymi samymi toolchainami
i cache zależności. Zastępuje to powtórny identyczny końcowy build; nie oznacza
instalowania narzędzi ani czyszczenia cache. Jeżeli środowisko blokuje kontrolę,
raport mówi „implementacja gotowa, baza nie w pełni zweryfikowana”.

## Źródła

- Lokalnie: `/Users/radoslaw.jablonski/LifeGeniePhone/AGENTS.md` oraz
  `docs/architecture/canonical-examples.md`, Contacts, Room builders i Metro graph.
- [API demonstracyjne](https://jsonplaceholder.typicode.com/): odczyt postów;
  operacje zapisu tego serwisu są symulowane, dlatego notatki zapisuje tylko Room.
- [Ktor MockEngine](https://ktor.io/docs/client-testing.html): testowanie wspólnej
  konfiguracji klienta przez podstawienie silnika HTTP.
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room): wspólne
  definicje Room oraz konstrukcja bazy specyficzna dla platformy.

Wersje zależności i nazwy tasków w planie są punktem wyjścia odczytanym z
LifeGenie, nie wynikiem kompilacji nowego projektu. Etap 1 je weryfikuje.
