package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.event.player.AsyncChatEvent
import net.azisaba.vanilife.island.IslandsAccessor
import net.azisaba.vanilife.island.IslandsPlayerAccessor
import net.azisaba.vanilife.island.leveling.IslandLevelPredicate
import net.azisaba.vanilife.island.wrack.WrackType
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

internal class PlayerListener(private val islands: IslandsAccessor, private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.launch {
            val island = islands.byOwner(event.player.uniqueId) ?: return@launch
            IslandsPlayerAccessor.assign(event.player, island)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.launch {
            IslandsPlayerAccessor.unassign(event.player)
        }
    }

    // @ TestCode
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.launch {
            val island = islands.byOwner(event.player.uniqueId) ?: return@launch
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())
            repeat(message.length) {
                island.spawnWrack(
                    WrackType.Enchantment(
                        Enchantment.AQUA_AFFINITY,
                        1,
                        IslandLevelPredicate.Always,
                    )
                )
            }
        }
    }
}
