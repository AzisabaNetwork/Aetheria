package net.azisaba.vanilife.menuprovider.listener

import net.azisaba.vanilife.menuprovider.inventory.TrashInventory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

internal object TrashInventoryListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventoryHolder = event.inventory.holder
        if (inventoryHolder is TrashInventory) {
            inventoryHolder.clearItemStacks(event.whoClicked)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val inventoryHolder = event.inventory.holder
        if (inventoryHolder is TrashInventory) {
            inventoryHolder.clearItemStacks(event.whoClicked)
        }
    }
}
