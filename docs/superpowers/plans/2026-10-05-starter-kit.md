# Reference-only Starter Skills Plan

**Goal:** Keep one maintained code reference and make the creation skill sufficient
for an agent to assemble a minimal independent app from it.

**Spec:** [design](../specs/2026-10-05-starter-kit-design.md)

**Scope:** Correct the initial duplicated template/generator design as requested.
No reference-app behavior changes, new app modules, commits or publishing.

## Tasks

- [x] Inspect staged/unstaged state and capture the old skill's dependency on
  the template/generator with a read-only consumer baseline.
- [x] Replace creation instructions with a source-selection/adaptation recipe.
  Preserve the other three skills and their canonical source references.
- [x] Remove the old generator, all template files and generator-specific CI steps.
  Align README, AGENTS and kit documentation with one source of code.
- [x] Exercise the revised creation skill in a disposable app, format and verify
  Android/iOS compilation, lint and host integration; validate links/metadata.
- [x] Record actual results and device limits, review the final diff and finish
  with spotlessCheck without changing the user's staging.

## Execution evidence

The baseline consumer confirmed the old skill could not work without its manifest,
script and overlays. Missing decisions included file selection, minimal hosts/DI,
dependency removal, coordinated identity changes and consumer policy/provenance.
The revised creation map owns those decisions as prose referring to current code.

Prior optional-skill exercises remain documented in
[verification](../../starter-kit-verification.md); only the changed creation path
needs a new full consumer exercise in this correction.
