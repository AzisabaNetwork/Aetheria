package net.azisaba.vanilife.enchanting.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import net.azisaba.vanilife.enchanting.EnchantingTranslations
import net.azisaba.vanilife.island.getIslandAt
import net.azisaba.vanilife.world.IslandsWorld
import io.papermc.paper.math.Position
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.koin.core.context.GlobalContext

internal class EnchantingTableListener(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.clickedBlock
            ?.takeIf { it.type == Material.ENCHANTING_TABLE }
            ?: return

        if (!event.action.isRightClick || event.player.isSneaking) {
            return
        }

        event.isCancelled = true
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)
        plugin.launch(plugin.entityDispatcher(event.player)) {
            val player = event.player
            val island = (player.world as? IslandsWorld)?.getIslandAt(Position.fine(player.location))
            if (island == null) {
                event.clickedBlock?.location?.add(0.5, 1.0, 0.5)?.let { location ->
                    player.world.spawnParticle(Particle.ANGRY_VILLAGER, location, 16, 0.4, 0.25, 0.4, 0.0)
                }
                player.sendMessage(
                    Component.translatable(
                        EnchantingTranslations.ENCHANTING_NO_ISLAND,
                        NamedTextColor.RED,
                    )
                )
                return@launch
            }
            EnchantingInventory().open(player)
        }
    }
}
