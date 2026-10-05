# Using the KMP reference and skills

The runnable articles + local notes app is the single source of reference code,
configuration and tests. Skills explain how to select and adapt it for a new app.
There is no separate code template, generator or second set of build/host files.

## Create an application

Use `kmp-create-project` from this repository or the complete matching plugin.
For example, ask the agent:

> Use kmp-create-project to create WeatherApp at a new destination with application
> ID com.example.weather. Include Compose, Metro and Material, with no database.

Specify whether networking is needed; an empty base has neither HTTP nor storage.
The skill's [creation map](../skills/kmp-create-project/references/create-from-reference.md)
identifies the actual files to copy and the changes to make in the destination:
module dependencies, host/graph construction, navigation, resources and app identity.
It contains a procedure and references, not another implementation of those files.

The new app retains Compose, Metro, Material components, typed navigation,
lifecycle support, formatting and lint. Serialization remains for navigation.
A static Home screen needs no artificial data layer or tests. The framework can
keep `StarterShared` while application names and identifiers change together.
The app is independently buildable; it has no runtime/build dependency on this kit.

## Agent workflows

| Request | Read |
| --- | --- |
| New app from the reference | [kmp-create-project](../skills/kmp-create-project/SKILL.md) |
| Add/change REST/API data | [kmp-networking](../skills/kmp-networking/SKILL.md) |
| Add/change persistence | [kmp-room](../skills/kmp-room/SKILL.md) |
| Implement a feature | [kmp-develop-feature](../skills/kmp-develop-feature/SKILL.md) |

Networking and Room are independent additions. An HTTP app need not include Room,
SQLite or KSP; a local app need not include an HTTP client or INTERNET permission.
Adding both does not prescribe cache/sync or make local screens depend on HTTP.

The target app's AGENTS.md and product decisions govern later changes. The
[canonical map](architecture/canonical-examples.md) links the reference code to
behavioral tests. Article IDs, JSONPlaceholder and online-first note editing are
example decisions, not product requirements.

## Discovery, provenance and maintenance

`.agents/skills` points to `skills/` for discovery in this repository. The root
`plugin.json` packages skills together with the reference app, following
[OpenAI's package format](https://developers.openai.com/plugins/build/plugins).
Install/distribute the complete package through a supported plugin source, or
explicitly point an agent at a skill in a complete checkout. Copying only SKILL.md
files loses their references. Resolve symlinks before finding the kit root.

Record kit/version, selected capabilities and reference access in the new app's
`starter-provenance.json`. Record the actual source revision when available and
note local modifications; a dirty checkout does not equal its HEAD revision.
Keep a short `docs/starter-kit.md` in that app with product decisions and how to
find the matching kit. Provenance is a handoff record, not an installation switch.
If the reference is unavailable, the agent should request access rather than claim
it followed unseen examples. Do not use developer-specific absolute paths.

Maintain code and dependency versions in the runnable app. When its structure
changes, update the creation map and canonical links in the same change. Skills
retain workflow decisions instead of copied classes or build scripts. New apps
receive ordinary reviewed edits on upgrades; never recreate over an existing app.

Distribute reviewed tracked sources, without build outputs, caches, credentials or
local settings. Global installation, commits and publication require authorization.

## Verification

CI continues testing/building the runnable reference. After changes to creation
instructions or relevant reference wiring, have a fresh agent create a disposable
app outside this repository and run its own AGENTS checks serially. Do not retain
that disposable app as another template. Inspect actual resolved dependencies.

When an optional skill changes, exercise the affected combination: HTTP-only,
Room-only, both, or adding Room to an existing HTTP app. Preserve existing API/UI
behavior and regression tests during additive integration. Use actual requested
API/storage contracts; fixtures using the example contract must be labelled as such.

Run metadata/reference validation as well as behavior/build checks. Compilation
and test linking do not prove device UI or iOS persistence. See
[verification evidence and limits](starter-kit-verification.md) for recorded runs.
