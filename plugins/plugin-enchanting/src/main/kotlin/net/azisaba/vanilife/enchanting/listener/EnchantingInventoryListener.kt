package net.azisaba.vanilife.enchanting.listener

import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent

internal object EnchantingInventoryListener : Listener {
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val inventoryHolder = event.inventory.holder as? EnchantingInventory ?: return
        val player = event.player as? Player ?: return
        inventoryHolder.hideRecipeBook(player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventoryHolder = event.inventory.holder as? EnchantingInventory ?: return
        val player = event.player as? Player ?: return
        inventoryHolder.restoreRecipeBook(player)
    }

    @EventHandler
    fun onPlayerRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        val inventoryHolder = event.player.openInventory.topInventory.holder
        if (inventoryHolder is EnchantingInventory) {
            event.shouldShowNotification(false)
        }
    }
}
