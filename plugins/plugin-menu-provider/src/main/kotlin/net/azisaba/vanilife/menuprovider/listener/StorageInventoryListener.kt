package net.azisaba.vanilife.menuprovider.listener

import net.azisaba.vanilife.menuprovider.inventory.StorageInventory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

internal object StorageInventoryListener : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventoryHolder = event.inventory.holder
        val player = event.player as? Player ?: return
        if (inventoryHolder is StorageInventory) {
            inventoryHolder.close(player)
        }
    }
}
