# Reference skills verification — 2026-10-05

## Current creation workflow

The creation workflow has been revised to use only the runnable reference.
The previous template/generator and their CI steps were removed. Root application
sources and the original reference CI remain unchanged against the original HEAD.

A read-only consumer first confirmed that the old skill could not assemble an app
without the removed manifest/script/overlays. The missing decisions are now in
`skills/kmp-create-project/references/create-from-reference.md`: file selection,
minimal host/DI/navigation, dependency pruning, identity changes, policy and handoff.

A separate consumer created `ReferenceOnlyApp` (`org.example.referenceonly`) in
`/private/tmp/kmp-reference-only-evaluation`, using only the revised skill and
current reference sources. It did not read earlier evaluation apps or deleted
template contents. The resulting 38-file app has one static Home screen, Compose,
Material, Metro and typed navigation, plus adapted policy and provenance.

Verification of that fresh app passed:

- `spotlessApply` followed by `spotlessCheck`.
- Android debug assembly, shared production lint and app lint.
- Kotlin iOS simulator compilation and an unsigned Xcode simulator host build.
- Resolved Android runtime dependency inspection: no Ktor, Room or SQLite.
- XML/plist/scheme parsing, wrapper integrity and app identity consistency.

The Android-host test task reported `NO-SOURCE`, as expected for a static base;
this is not a claim of executed tests. The consumer also checked destination and
ID validation without mutating existing paths. No reusable generator was retained.

The reference checkout passed formatting, Android-host tests, Android assembly,
shared/app lint and Kotlin iOS simulator compilation. Existing outputs were mostly
up-to-date. Skill YAML metadata and Markdown links were checked locally.

## Earlier optional-skill evidence

These skills and their reference implementations were not changed by the creation
workflow correction. The following exercises were completed in the preceding
implementation phase, using explicitly supplied reference API/storage contracts.
They are not new runs of the revised creation skill.

| Exercise | Android-host tests | Android build/lint | iOS checks |
| --- | --- | --- | --- |
| Networking only | 17 passed | Pass | Kotlin and unsigned host compiled |
| Room only | 13 passed, including disk reopen | Pass | Production/tests compiled, tests linked, unsigned host built |
| Room added to networking | 30 passed | Pass | Production/tests compiled, tests linked, unsigned host built |
| Read-only list/detail feature | 31 passed | Pass | Production/tests compiled, tests linked, unsigned host built |

Adding Room preserved all 24 protected API/domain/test/UI files byte-for-byte,
including after formatting. HTTP-only had no Room/SQLite; Room-only had no Ktor.
Both Room fixtures exported real initial schemas. Their initial lint failures were
RestrictedApi in two generated implementations; the exact issue/file exemption
from the reference resolved them without disabling checks on authored code.

The feature consumer resolved the skill through `.agents/skills` and added
list/detail UI with real-use-case/fake-repository tests, independently of notes.
These disposable projects are not retained as another maintained code reference.

## Verification limits

No simulator was booted for these exercises. Compiling/linking iOS tests is not
runtime execution or iOS persistence evidence. Device UI, gestures, RTL, large fonts
and keyboard behavior were not verified. Existing Compose deprecation, Room
expect/actual and native-link warnings remain.

Skill metadata can be parsed with installed Ruby/Psych; the bundled Python
quick_validate helper was previously blocked by missing PyYAML. No tools were
installed as a workaround. The CI workflow has not been executed on GitHub here.
No global skill installation, publication, commit or push was performed.
