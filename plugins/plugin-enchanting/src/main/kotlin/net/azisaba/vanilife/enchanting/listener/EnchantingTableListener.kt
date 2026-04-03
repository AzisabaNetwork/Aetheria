package net.azisaba.vanilife.enchanting.listener

import net.azisaba.vanilife.enchanting.tables.EnchantingTableBehaviour
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

internal class EnchantingTableListener : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val enchantingTable = event.clickedBlock
            ?.takeIf { it.type == Material.ENCHANTING_TABLE }
            ?: return

        event.isCancelled = true
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        if (event.action.isRightClick) {
            EnchantingTableBehaviour.handleRightInteract(event.player, enchantingTable, event.item)
        } else if (event.action.isLeftClick) {
            EnchantingTableBehaviour.handleLeftInteract(event.player, enchantingTable)
        }
    }
}
