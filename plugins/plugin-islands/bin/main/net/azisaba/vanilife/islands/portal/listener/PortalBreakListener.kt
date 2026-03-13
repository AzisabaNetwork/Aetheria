package net.azisaba.vanilife.islands.portal.listener

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import net.azisaba.vanilife.islands.portal.PortalManager
import net.azisaba.vanilife.islands.portal.PortalsTable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PortalBreakListener : Listener, KoinComponent {
    private val manager: PortalManager by inject()

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val player = event.player
        // Use chunk index for quick lookup
        val worldName = block.world.name
        val chunkX = block.x shr 4
        val chunkZ = block.z shr 4
        val candidateIds = manager.getPortalIdsForOriginChunk(worldName, chunkX, chunkZ)
        candidateIds.forEach { id ->
            val portal = manager.getPortalById(id) ?: return@forEach
            val min = portal.originMin
            val max = portal.originMax
            if (block.x in min.blockX()..max.blockX() && block.y in min.blockY()..max.blockY() && block.z in min.blockZ()..max.blockZ()) {
                // This block is within the origin portal bounds; check permissions
                if (player.uniqueId == portal.ownerUuid || player.hasPermission("vanilife.portal.unlink") || player.isOp) {
                    manager.removePortal(portal)
                    manager.unindexPortalOrigin(portal)
                } else {
                    // Prevent accidental/unwanted unlinking by non-owners
                    event.isCancelled = true
                    player.sendMessage("You are not allowed to unlink this portal. Only the island owner or admins may do this.")
                }
            }
        }
    }
}
