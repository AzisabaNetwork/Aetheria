package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.registry.keys.EnchantmentKeys
import net.azisaba.vanilife.island.IslandCacheMap
import net.azisaba.vanilife.island.IslandPlayerMap
import net.azisaba.vanilife.island.leveling.LevelPredicate
import net.azisaba.vanilife.island.wrack.WrackType
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

internal class PlayerListener(private val cacheMap: IslandCacheMap, private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.launch {
            val island = cacheMap.lookupOrCreate(event.player.uniqueId)
            IslandPlayerMap.put(event.player, island)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.launch {
            IslandPlayerMap.remove(event.player)
        }
    }

    // @ TestCode
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.launch {
            val island = cacheMap.lookup(event.player.uniqueId)
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())
            repeat(message.length) {
                island?.spawnWrack(
                    WrackType.Enchantment(
                        EnchantmentKeys.AQUA_AFFINITY,
                        1,
                        LevelPredicate.Always,
                    )
                )
            }
        }
    }
}
