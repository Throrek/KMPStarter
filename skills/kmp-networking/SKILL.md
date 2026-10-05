---
name: kmp-networking
description: Use when adding or changing Ktor HTTP networking, REST endpoints, DTO mapping or API failure handling in a KMPStarter Android/iOS app. Networking does not require Room or local persistence.
---

# Add or extend networking

Resolve this file's real path first (repository discovery may use a symlink), then
resolve `../..` from its directory as the **kit root**; all links below refer to
that version's code. Read the target app's AGENTS.md, catalog, graph, hosts and
`starter-provenance.json` if present. Extend its existing client/providers when
available. Do not regenerate or overwrite the app.

## Contract and references

Establish the real base URL, endpoint/response contract, authentication needs and
requested operation before implementing product data. Keep secrets out of source.
JSONPlaceholder and article IDs below are examples, not defaults for another API.

| Need | Canonical source |
| --- | --- |
| Client configuration | [HttpClientFactory](../../shared/src/commonMain/kotlin/me/kmpstarter/data/network/HttpClientFactory.kt) |
| Platform engines | [Android](../../shared/src/androidMain/kotlin/me/kmpstarter/data/network/HttpEngine.android.kt), [Darwin](../../shared/src/iosMain/kotlin/me/kmpstarter/data/network/HttpEngine.ios.kt) |
| DTO and endpoint | [ArticleDto](../../shared/src/commonMain/kotlin/me/kmpstarter/data/article/network/ArticleDto.kt), [remote source](../../shared/src/commonMain/kotlin/me/kmpstarter/data/article/network/ArticleRemoteDataSource.kt) |
| Mapping and failures | [repository](../../shared/src/commonMain/kotlin/me/kmpstarter/data/article/DefaultArticleRepository.kt), [domain error](../../shared/src/commonMain/kotlin/me/kmpstarter/domain/article/ArticleReadException.kt) |
| Behavioral evidence | [MockEngine tests](../../shared/src/commonTest/kotlin/me/kmpstarter/data/article/DefaultArticleRepositoryTest.kt) |

## Integration

1. Reuse compatible versions in an existing app. For a fresh base, copy only the
   `ktor` version and `ktor-*` aliases from the kit's
   [catalog](../../gradle/libs.versions.toml). In shared commonMain add
   `libs.ktor.client.core`, `libs.ktor.client.content.negotiation`,
   `libs.ktor.serialization.kotlinx.json`; retain existing Kotlin serialization
   plugin/JSON dependency. Add `libs.ktor.client.android` to androidMain,
   `libs.ktor.client.darwin` to iosMain and `libs.ktor.client.mock` to commonTest.
   Add client-core to androidApp when its host handles HttpClient types.
2. Adapt factory/engines, domain contract/use cases and feature-specific data files.
   URLs/timeouts/endpoints belong to their owners or constructor configuration.
   Map only expected SDK failures at data boundaries; preserve cancellation,
   unknown exceptions and original causes. Keep SDK types out of domain models.
3. Adapt only the HTTP portion of [StarterGraph](../../shared/src/commonMain/kotlin/me/kmpstarter/di/StarterGraph.kt).
   Use a graph factory with a provided HttpClient; retain existing database
   parameters/providers if present. Create one client outside composition in each
   host ([Android](../../androidApp/src/main/kotlin/me/kmpstarter/StarterApplication.kt),
   [iOS](../../shared/src/iosMain/kotlin/me/kmpstarter/MainViewController.kt)).
   Add Android INTERNET permission. Review actual iOS network/privacy requirements.
4. Use [develop-feature](../kmp-develop-feature/SKILL.md) when adding UI. Do not copy
   the combined article-details use case: its note read is a demo-specific dependency.
5. Adapt MockEngine cases for response mapping, expected failures, cancellation and
   programming errors; use virtual time for timeout tests. Close test clients.
   Run target AGENTS checks, including formatting, shared lint and both hosts.
   Inspect resolved dependencies when verifying HTTP-only output.
6. Record `networking` in provenance only after integration, and the actual API
   decisions in the app's handoff doc. Report passed and blocked checks.

HTTP alone adds no Room, SQLite, KSP, schema, cache, synchronization or automatic
retry. Missing API details are missing inputs, not permission to invent a backend.
