# KMP reference and skills

Final direction approved by the user: the runnable application is the single code
reference. Remove the parallel base template and generator introduced initially.

## Ownership

- Root app owns the Compose/Metro/Material, networking, Room, host and build examples
  with their tests. Its application sources remain unchanged by this correction.
- Four skills explain creation, networking, Room and feature development using
  direct references to that code. The creation skill has a file-selection and
  adaptation map; it does not embed copies of Kotlin or build implementations.
- An agent creates a new app by copying selected source files and adapting them
  in the destination. The static base includes Compose/Metro/Material/navigation,
  but neither HTTP nor Room. Optional capabilities are added independently.
- New apps own their AGENTS, product decisions and source/capability provenance.
  They build independently; the kit is an optional development reference only.
- `plugin.json` versions the complete reference/skills package. `.agents/skills`
  discovers the same skills locally. No global install or publication is implied.

## Acceptance

Remove the old template/generator and their CI steps; preserve reference CI and
existing app sources. Update active documentation without leaving stale commands.
Have a fresh consumer create and build a minimal app using only the revised skill
and runnable source. Validate metadata and reference links. Run Gradle serially,
format new Kotlin with Spotless and verify both platforms using installed tools.
Do not fabricate tests for a static screen, boot devices, install toolchains or
claim device runtime from compilation. Preserve staging; no commit/push/publication.
