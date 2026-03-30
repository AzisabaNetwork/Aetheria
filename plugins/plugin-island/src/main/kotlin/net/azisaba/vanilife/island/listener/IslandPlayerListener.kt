package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import io.papermc.paper.registry.keys.EnchantmentKeys
import kotlinx.coroutines.runBlocking
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.IslandCacheMap
import net.azisaba.vanilife.island.IslandPlayerMap
import net.azisaba.vanilife.island.wrack.WrackType
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

internal class IslandPlayerListener(private val islands: IslandCacheMap, private val plugin: Plugin) : Listener {
    @EventHandler
    fun onAsyncPlayerSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        val playerUuid = event.connection.profile.id ?: return
        runBlocking {
            val island = islands.lookupOrCreate(playerUuid)
            event.spawnLocation = island.spawnPoint(island.position, Vanilife.getIslandsWorld())
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.launch {
            val island = islands.lookupOrCreate(player.uniqueId)
            IslandPlayerMap.put(player, island)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.launch {
            IslandPlayerMap.remove(player)
        }
    }

    // @ TestCode
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.launch {
            val island = islands.lookup(event.player.uniqueId)
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())
            repeat(message.length) {
                island?.spawnWrack(
                    WrackType.Enchantment(
                        EnchantmentKeys.AQUA_AFFINITY,
                        1,
                    )
                )
            }
        }
    }
}
