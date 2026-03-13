package net.azisaba.vanilife.islands.portal.listener

import kotlinx.coroutines.launch
import net.azisaba.vanilife.islands.Config
import net.azisaba.vanilife.islands.portal.PortalManager
import net.azisaba.vanilife.islands.portal.ResourcePortals
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PortalEventListener(private val plugin: Plugin) : Listener, KoinComponent {
    private val portalManager: PortalManager by inject()
    private val config: Config by inject()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // TODO: match movement into resource-world portal and teleport player to owner's island
    }
}
