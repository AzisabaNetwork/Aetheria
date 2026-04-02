package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.delay
import net.azisaba.vanilife.island.IslandFeature
import net.azisaba.vanilife.island.currentIsland
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class FlightListener(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        val island = player.currentIsland ?: return
        if (island.isEnabled(IslandFeature.FLIGHT)) {
            val isFlying = player.isFlying
            plugin.launch(plugin.entityDispatcher(player)) {
                delay(1L.milliseconds)
                player.allowFlight = true
                player.isFlying = isFlying
            }
        }
    }
}
