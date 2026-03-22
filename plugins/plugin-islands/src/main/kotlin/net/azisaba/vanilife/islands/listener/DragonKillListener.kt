package net.azisaba.vanilife.islands.listener

import kotlinx.coroutines.launch
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.islands.IslandManager
import net.azisaba.vanilife.islands.DragonMetadata
import net.azisaba.vanilife.event.DragonInstalledEvent
import net.azisaba.vanilife.world.IslandPos
import org.bukkit.entity.EnderDragon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.Plugin
import org.koin.core.context.GlobalContext
import java.time.Instant

internal class DragonKillListener(private val plugin: Plugin, private val service: IslandManager) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity !is EnderDragon) return

        // Compute island pos from dragon location
        val loc = entity.location
        val islandPos = IslandPos.fromBlockPos(loc.blockX, loc.blockZ)

        // Use coroutine to perform repository lookups/updates
        plugin.launch {
            val island = service.lookupByPos(islandPos) ?: return@launch

            // Only mark if not already installed
            if (!island.dragonData.installed) {
                if (island.dragonData is DragonMetadata.Writable) {
                    island.dragonData.setInstalled(true)
                }

                // Fire event
                val islandId = island.pos.toLong().toString()
                server.pluginManager.callEvent(DragonInstalledEvent(islandId, island.ownerUuid, Instant.now()))

                // Notify owner if online
                val owner = plugin.server.getPlayer(island.ownerUuid)
                owner?.sendMessage(Vanilife.translations().getTranslation("island.dragon.granted").replace("%player%", owner.name))
            }
        }
    }
}
