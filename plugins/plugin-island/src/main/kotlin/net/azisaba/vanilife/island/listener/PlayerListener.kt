package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.event.player.AsyncChatEvent
import net.azisaba.vanilife.island.currentIsland
import net.azisaba.vanilife.island.leveling.LevelPredicate
import net.azisaba.vanilife.island.ownedIsland
import net.azisaba.vanilife.island.wrack.WrackType
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

internal class PlayerListener(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.ownedIsland.addPlayer(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        player.currentIsland?.removePlayer(player)
    }

    // @ TestCode
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.launch {
            val island = event.player.ownedIsland
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())
            repeat(message.length) {
                island.spawnWrack(
                    WrackType.Enchantment(
                        Enchantment.AQUA_AFFINITY,
                        1,
                        LevelPredicate.Always,
                    )
                )
            }
        }
    }
}
