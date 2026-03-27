# Dragon-Island (Dragon-Home) Specification — Vanilife

Last updated: 2026-03-22  
Author: sysnote8main (specification assistance)  
Repository: AzisabaNetwork/Vanilife

## Overview
The "Dragon-Island" feature marks an island as having "welcomed a dragon" after defeating the Ender Dragon on that island. It provides:
- Owner-targeted persistent effects (buff hooks)
- Island appearance customization (sky/water color from presets)
- A legacy ticket recorded at wipe time — colors are cleared on wipe but a legacy ticket is automatically granted

## Key Decisions (finalized)
- Persistence: Basic state is reset on world/island wipe. A "legacy ticket" remains across wipes (Option C), and server ops can enable/disable legacy behavior via config.
- Target audience: Initially only the island owner receives effects. The design allows extension to visitors later.
- Buffs: Exposed via an event/API so other modules can add arbitrary buffs. Default trial buffs are Damage Resistance and a Haste-like mining-speed boost.
- Colors: Preset-only color selection (like a wool color palette). Colors are cleared on wipe; the legacy ticket is automatically granted to the owner at wipe (auto-grant choice).

## Terminology
- island: game island identifier
- owner: island owner player name/UUID
- DragonInstalled: island marked as dragon-home
- LegacyTicket: a token retained at wipe that can be redeemed for color restoration or a boost on next island

## Acquisition Conditions
- Trigger: The Ender Dragon is defeated within the island bounds, and the kill credit is associated with the island (owner).
- Additional constraints (optional):
  - The kill must occur within the island's region (use existing island bounds)
  - Additional ritual items/altars may be added later, but are not required initially

## Effects (Design)
- Scope: Owner-only (can be extended to visitors via config later)
- Timing:
  - DragonBuffQueryEvent fires when the owner enters the island
  - Re-apply on a periodic tick (e.g., every 30s) or when needed
  - Persistent buffs should survive logouts and reapply after server restart during the restore step
- Buff model:
  - Event-based: external plugins listen for DragonBuffQueryEvent and add buffs to the provided accumulator
  - Default buffs provided: DAMAGE_RESISTANCE (amplifier=1) and a MINING_HASTE equivalent (small mining speed increase)
- Stacking: For same-type buffs, apply max-value/overwrite. Different buff types stack.

## Customization (Sky / Water color)
- Unlock: Owner can choose presets when island becomes DragonInstalled
- Storage: Saved per-island (but cleared on wipe); legacy ticket keeps last presets for later redemption
- Selection method: Preset-only; server defines presets in config (e.g., wool color keys mapped to color codes)
- Command/UI:
  - /island dragon setcolor <air|water> <presetKey>
  - GUI inventory selection is recommended for previewing presets
- Visual effect: Achievable via server-side visuals (particles, fog, holograms, time/weather adjustments). Full client-side sky color replacements require resource packs and are considered optional.

## Legacy (Wipe-Time Ticket)
- Behavior:
  - Colors are cleared on wipe.
  - A legacy ticket is automatically granted to the island owner at wipe (server-configurable).
  - Ticket use examples:
    - Restore the same preset color once on the next island
    - Consume for a one-time buff boost or credit
- Data example: island.dragon.legacy = { exists:true, boostCredit:1, lastPresetAir:"sky_blue" }

## Admin Features (Commands / Permissions)
- Example commands:
  - /island dragon grant <islandId> [player] — force-grant dragon state
  - /island dragon revoke <islandId>
  - /island dragon status <islandId>
  - /island dragon setcolor <islandId> <air|water> <presetKey>
  - /island dragon redeem-legacy <islandId>
- Permission nodes:
  - vanilife.dragon.admin.* — admin tasks
  - vanilife.dragon.use — owner operations
  - vanilife.dragon.redeem — redeem legacy ticket

## Data Model (Save Format)
We persist dragon metadata to PostgreSQL via Exposed. Example columns are described in the schema migration file.

## Events / API (Public Interface)
- Events:
  - DragonInstalledEvent(islandId: String, owner: UUID, time: Instant)
  - DragonRemovedEvent(islandId: String, reason: String, time: Instant)
  - DragonCustomizationChangedEvent(islandId: String, changedBy: UUID, field: String, presetKey: String)
  - DragonBuffQueryEvent(islandId: String, player: Player, buffAccumulator: MutableList<DragonBuff>)
- API (Kotlin interface example provided in code files)

## Implementation Notes (Paper / Folia)
- Folia: Use Folia scheduler for periodic tasks. Ensure thread-safety and follow Folia’s concurrency guidance.
- Data sync: Maintain metadata consistency in multi-world/distributed setups (use locks or serialised updates).
- Visuals: Do not rely on global client changes for sky color. Provide server-side visual alternatives; resource-pack-based visual changes are optional features that require distribution and opt-in.

## Logging / Auditing
- Log important events (installed/revoked/customization/legacy redeem) with timestamp and actor
- Provide admin view for event history if needed

## Localization (Example Messages)
- island.dragon.active = "This island is blessed by a dragon."
- island.dragon.granted = "%player% has welcomed a dragon to this island."
- island.dragon.legacy.received = "%player% received the dragon legacy token."

## Test Cases (high-level)
1. DragonInstalled trigger test
2. Legacy auto-grant test (on wipe)
3. Buff event test
4. Color set/save test
5. Permission test
6. Paper/Folia compatibility test

## Ops Notes
- Migration: Provide a migration to add a `dragon` section to existing island metadata (SQL migration included).
- Configuration examples:
  - dragon.legacy.enabled = true
  - dragon.buff.tickIntervalSeconds = 30
  - dragon.customPresets = { "sky_blue":"#87CEEB", ... }
  - dragon.applyToVisitors = false

---
(End of specification)