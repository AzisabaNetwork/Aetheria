package net.azisaba.vanilife.enchanting.listener

import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

internal object EnchantingTableListener : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.clickedBlock
            ?.takeIf { it.type == Material.ENCHANTING_TABLE }
            ?: return

        event.isCancelled = true
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        if (!event.action.isRightClick) {
            return
        }

        EnchantingInventory().open(event.player)
    }
}
