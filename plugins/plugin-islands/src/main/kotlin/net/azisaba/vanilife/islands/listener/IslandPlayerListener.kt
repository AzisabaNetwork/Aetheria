package net.azisaba.vanilife.islands.listener

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.ItemTypeKeys
import io.papermc.paper.registry.keys.SoundEventKeys
import kotlinx.coroutines.runBlocking
import net.azisaba.serialization.IntProvider
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.datadriven.ItemStackProvider
import net.azisaba.vanilife.islands.IslandManager
import net.azisaba.vanilife.islands.wrack.WrackType
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

internal class IslandPlayerListener(private val plugin: Plugin, private val service: IslandManager) : Listener {
    @EventHandler
    fun onAsyncPlayerSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        val playerUuid = event.connection.profile.id ?: return
        runBlocking {
            val island = service.lookupOrCreateByOwner(playerUuid)
            event.spawnLocation = island.primaryData.spawnPoint(island.pos, Vanilife.getIslandsWorld())
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.launch {
            val island = service.lookupByOwner(player.uniqueId)
            island?.addPlayer(player)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.launch {
            val island = service.lookupByOwner(player.uniqueId)
            island?.removePlayer(player)
        }
    }

    // @ TestCode
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        plugin.launch {
            val island = service.lookupByOwner(event.player.uniqueId)
            val message = PlainTextComponentSerializer.plainText().serialize(event.message())
            repeat(message.length) {
                island?.spawnWrack(
                    WrackType.Enchantment(
                        EnchantmentKeys.AQUA_AFFINITY
                    )
                )
            }
        }
    }
}
