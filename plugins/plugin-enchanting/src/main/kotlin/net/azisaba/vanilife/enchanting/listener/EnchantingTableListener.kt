package net.azisaba.vanilife.enchanting.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.enchanting.EnchantingTranslations
import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import net.azisaba.vanilife.island.getIslandAt
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin

internal class EnchantingTableListener(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val enchantingTable = event.clickedBlock?.takeIf {
            it.type == Material.ENCHANTING_TABLE
        } ?: return

        val player = event.player

        if (!event.action.isRightClick || player.isSneaking) {
            return
        }

        event.isCancelled = true

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        plugin.launch(plugin.entityDispatcher(player)) {
            val island = (player.world as? IslandsWorld)?.getIslandAt(player.location)

            if (island == null) {
                player.sendMessage(
                    Component.translatable(
                        EnchantingTranslations.ENCHANTING_NO_ISLAND,
                        NamedTextColor.RED,
                    )
                )

                val particleLocation = enchantingTable.location.toCenterLocation().add(0.5, 1.0, 0.5)
                player.world.spawnParticle(Particle.POOF, particleLocation, 16, 0.4, 0.25, 0.4, 0.0)
                return@launch
            } else {
                player.openInventory(EnchantingInventory().inventory)
            }
        }
    }
}
