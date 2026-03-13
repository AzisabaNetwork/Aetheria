AGENTS

This plan will be executed in a loop by the build agent and supporting workers. The loop tasks:

1) prepare - move PortalFinder into plugin-islands and adapt package names; add DB migration for portals table if missing
2) implement - create PortalManager, PortalRepository, PortalEventListener, PortalHologram, and commands
3) test - run basic local tests (compile, unit-style checks, manual runtime checks)
4) commit - create a git commit capturing changes for the step and write status to AGENTS.md
5) deploy - not automatic; produce instructions for server deployment and migration

Loop behavior
- On each iteration:
  - If missing moved files: move `plugin-portal` finder files into `plugin-islands` and adapt package names.
  - If DB table missing: create migration SQL and Exposed table mapping; run tests and show SQL.
  - Implement next missing artifact from the implementation checklist.
  - Run `./gradlew :plugin-islands:build` (or the workspace build command), capture and report build errors.

Agent roles
- mover: relocate finder files and adapt imports
- storage: implement Exposed table and repository
- manager: implement PortalManager and runtime logic
- listener: implement event handlers for ignite, player portal entry, block break
- hologram: spawn/update TextDisplays
- test-runner: run builds and report compilation errors
 - committer: make a git commit at the end of each step and append a short status line to this AGENTS.md

Completed steps:
- mover: finder, detected and resource portals moved into plugin-islands and ignite listener registered (commit: "portal: move finder/detected/resourceportals into plugin-islands and register ignite listener")

Notes
- This AGENTS.md is a lightweight loop spec for local automated edits. Manual verification is required before production deployment.
