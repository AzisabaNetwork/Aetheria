Portal Plan - Island ⇄ Resource World (one-to-one)

Overview
- When an island owner ignites a valid portal frame on their island using the special igniter item, the system automatically spawns a matching portal at a random safe coordinate in the configured resource world.
- Any player who enters the resource-world portal is teleported into the owner's island spawn. Each island has at most one linked resource portal (one link per origin portal).

Goals
- Provide an easy, one-click way for players to create a resource-world access portal from their island.
- Encourage community by making resource portals discoverable in the resource world and safe to use.

Design Summary
- Detection: reuse existing PortalFinder to detect a valid frame on the island side.
- Creation: when ignited with the special igniter, create a resource-world portal at a random safe coordinate and persist the mapping (origin ↔ resource).
- Teleportation: players entering the resource-world portal are teleported to the owner's island spawn (via IslandManager + PrimaryIslandData.resolveSpawnPoint).
- Hologram: spawn a TextDisplay inside the resource portal showing island name, owner, and online-player count (owner face when supported).
- Persistence: store portal metadata in DB so portals survive restarts and can be cleaned up or re-spawned.

Primary flows

1) Creation (owner ignites with special igniter)
  - Listen to BlockIgniteEvent and check the ignite item for a persistent data flag (NamespacedKey `vanilife:portal_igniter`).
  - Run `PortalFinder.findPortal(plugin, location)` to detect the island portal frame.
  - Permission check: only island owner (or op/admin) may create.
  - PortalManager.createPortal(ownerUuid, detectedIslandPortal):
    - Pick random sample coordinates in the configured resource world, find safe Y and test area for validity.
    - Construct a DetectedPortal for the resource-world position and call existing ResourcePortals.createWithAnimation() to create the visual portal blocks.
    - Persist mapping into `portals` table with both sides' bounding data.
    - Spawn PortalHologram in the resource world and register the portal in memory.

2) Teleportation (player enters the resource-world portal)
  - Intercept PlayerPortalEvent (or match player location in player move events if needed). If the entry location matches a resource portal bounding box, cancel default behavior.
  - Teleport player asynchronously to island spawn: lookup Island via IslandManager (by ownerUuid or islandPos), then call `primaryData.resolveSpawnPoint(islandPos)` and `player.teleportAsync(...)`.
  - Apply a teleport cooldown per player (configurable) to avoid teleport loops.

3) Cleanup / removal
  - On island frame destruction (BlockBreakEvent): debounce and re-run PortalFinder; if frame no longer valid, call PortalManager.removePortal(portalId) to remove resource portal blocks/hologram and mark DB inactive.
  - Admin unlink command for manual removal.

Hologram
- Use TextDisplay (or EntityLib wrapper) to spawn non-persistent displays inside the resource portal center.
- Content lines (top→bottom):
  1) Island display name (PrimaryIslandData.displayName or fallback to owner name)
  2) Owner info (name; attempt owner-face embedding if runtime supports it, otherwise plain name)
  3) Online players count (e.g., "3 online") and possibly short player list.
- Update frequency: configurable (default 3s). Only re-render for nearby viewers to save CPU.

Persistence: DB schema (conceptual)
- Table: `portals`
  - id: Long PK (auto-increment)
  - owner_uuid: UUID
  - origin_pos_long: Long (serialize IslandPos with same scheme as Islands table)
  - origin_min_x/min_y/min_z, origin_max_x/..: Int (origin bounds stored for frame match)
  - orientation: Int (0/1 for XY/ZY)
  - inner_width: Int, inner_height: Int
  - resource_world: VARCHAR
  - resource_x, resource_y, resource_z: Int (center or min bound depending on stored format)
  - created_at: Timestamp
  - active: Boolean

Files to move/add (suggested)
- Move into `plugins/plugin-islands` (package: `net.azisaba.vanilife.islands.portal`):
  - `PortalFinder.kt` (from plugin-portal)
  - `DetectedPortal.kt` (from plugin-portal)
  - `ResourcePortals.kt` (from plugin-portal; adapt package and dependencies)

- Add under `plugins/plugin-islands/src/main/kotlin/net/azisaba/vanilife/islands/portal/`:
  - `PortalManager.kt` — lifecycle, in-memory index, create/remove, startup load
  - `PortalRepository.kt` — Exposed table and CRUD
  - `Portal.kt` — data model
  - `PortalEventListener.kt` — ignite handler, portal entry handler, block-break cleanup
  - `PortalHologram.kt` — spawn/update/cleanup TextDisplays
  - `PortalCommands.kt` — `/island portal list|unlink|giveigniter` (owner/admin)

Integration notes
- Register `PortalManager` as a Koin `single` in `plugins/plugin-islands` Main.kt and register listeners during onEnable.
- Use `plugin.launch(plugin.regionDispatcher(location))` and `withContext(plugin.regionDispatcher(location))` for region-safe block reads/writes (as done by existing finder code).
- On plugin enable, load all active `portals` rows and ensure resource-side portals exist (re-create if missing).

Config (defaults)
- resource-world: "resources"
- portal.spawnRadius: 20000
- portal.spawnAttempts: 50
- portal.frameMaterial: PRISMARINE
- portal.minPortalDistance: 200
- portal.teleportCooldownSeconds: 3

Performance & concurrency
- Keep an in-memory index keyed by resource world to bounding boxes for quick lookup.
- Use event-driven teleport lookup rather than per-tick scanning.
- Use regionDispatcher for all block access to be Folia-safe.

Testing checklist
- Ignite frame with special igniter → resource portal spawns (frame + portal blocks) and hologram appears.
- Player enters resource portal → teleports to owner's island spawn.
- Owner breaks island frame → resource portal removed and DB updated.
- Server restart → portals reloaded and resource portals re-spawned/validated.
- Hologram shows island name, owner name/face fallback, and online count updates.

Estimated effort (MVP)
1) Move finder + basic wiring: 0.5 day
2) PortalRepository + DB table: 0.5 day
3) PortalManager create logic + random placement: 1.0–1.5 days
4) Player portal entry handler + teleport cooldown: 0.5 day
5) Hologram implementation and updates: 0.5 day
6) Cleanup, commands, config, testing, polish: 1.0 day
Total: ~3.5–4.5 days for MVP

Next steps
1) Confirm this plan and config defaults.
2) I will move the portal finder code into `plugins/plugin-islands` and create the PortalManager/DB skeleton.
3) Implement create/teleport/hologram flows and manual test.

Files created for this plan:
- `plans/portal/PLAN.md` (this file)
