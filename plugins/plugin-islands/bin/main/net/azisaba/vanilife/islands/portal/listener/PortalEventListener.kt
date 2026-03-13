package net.azisaba.vanilife.islands.portal.listener

import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.islands.Config
import net.azisaba.vanilife.islands.storage.resolveSpawnPoint
import net.azisaba.vanilife.islands.portal.PortalManager
import net.azisaba.vanilife.islands.portal.ResourcePortals
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.bukkit.entity.Player
import java.util.WeakHashMap
import kotlin.math.max

class PortalEventListener(private val plugin: Plugin) : Listener, KoinComponent {
    private val portalManager: PortalManager by inject()
    private val config: Config by inject()
    // per-player cooldowns (player -> last teleport epoch millis)
    private val lastTeleport: MutableMap<Player, Long> = WeakHashMap()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to ?: return
        val world = to.world

        // check portals in this world
        portalManager.getPortalsInWorld(world.name).forEach { portal ->
            val min = portal.resourceMin
            val max = portal.resourceMax
            if (to.blockX in min.blockX()..max.blockX() && to.blockY in min.blockY()..max.blockY() && to.blockZ in min.blockZ()..max.blockZ()) {
                // inside portal region
                val now = System.currentTimeMillis()
                val last = lastTeleport[player] ?: 0L
                if (now - last < (config.portal.teleportCooldownSeconds * 1000L)) return@forEach

                // teleport to owner's island spawn (async) using plugin coroutine scope
                plugin.launch {
                    val islandInfo = portalManager.lookupIslandForPortal(portal)
                    val dest = islandInfo?.primaryData?.let { data -> data.resolveSpawnPoint(islandInfo.pos) }
                    if (dest is Location) {
                        player.teleportAsync(dest)
                        lastTeleport[player] = System.currentTimeMillis()
                    }
                }
            }
        }
    }
}
