Portal Plan (Current)

Status
- Core implementation present in `plugins/plugin-islands`:
  - `PortalManager`, `PortalRepository` (Exposed `PortalsTable`), `Portal` data model
  - Random resource placement + resource portal animation (`ResourcePortals.createWithAnimation`)
  - Teleport handling and per-player cooldown (PortalEventListener)
  - Hologram: Adventure `Component` + TextDisplay spawn; `PortalHologram.kt` wrapper added
  - Break protection: `PortalBreakListener` now enforces owner/admin unlink checks (`vanilife.portal.unlink`)

One-by-one plan (next actions)
1) Hologram polish (EntityLib wrapper)
   - Convert PortalManager hologram spawn to use `PortalHologram` via EntityLib container/wrapper to control fonts, viewers, and metadata.
   - Persist hologram UUID and update in-memory portal record.

2) Commands & admin tools
   - Add `/island portal unlink` (owner), `/island giveigniter` (owner/admin), and `/portal list` (admin).
   - Hook commands into `Main.kt` during `onEnable` and add permission nodes.

3) DB migration
   - Provide SQL or migration steps to add `hologram_uuid` (nullable UUID) to existing `portals` table; ensure `Database.setupTables()` is safe.

4) Tests & verification
   - Run `./gradlew :plugins:plugin-islands:build`.
   - Manual test sequence: create island, build frame, ignite with igniter, verify portal spawn + hologram, teleport, break frame, restart.

5) Performance & robustness
   - Replace naive BlockBreak scan with spatial index/origin-block map for O(1) lookup.
   - Enforce `minPortalDistance` during placement and improve safe-placement checks (avoid liquids/lava/neighbour portals).

Notes
- After each next-step change, append a short status line to `plans/portal/AGENTS.md` describing the change and commit message.
- I will proceed with step 2 (commands) next unless you prefer a different order. Say "continue" to proceed, or pick a step number.
