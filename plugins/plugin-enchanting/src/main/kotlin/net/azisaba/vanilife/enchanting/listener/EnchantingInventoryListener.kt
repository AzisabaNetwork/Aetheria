package net.azisaba.vanilife.enchanting.listener

import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import net.azisaba.vanilife.enchanting.recipebook.RecipeBookToastSuppressor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent
import org.bukkit.plugin.Plugin

internal class EnchantingInventoryListener(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventoryHolder = event.view.topInventory.holder as? EnchantingInventory ?: return
        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory ?: return
        val clickedItem = event.currentItem
        var shouldSync = false

        if (event.rawSlot == EnchantingInventory.RESULT_SLOT) {
            event.isCancelled = true
            val result = inventoryHolder.confirmCraft(player) ?: return
            player.setItemOnCursor(result)
            player.updateInventory()
            inventoryHolder.sync(plugin, player)
            return
        }

        if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (clickedInventory == event.view.bottomInventory) {
                val itemStack = clickedItem ?: return
                val targetSlot = inventoryHolder.firstEmptyInputSlotFor(itemStack) ?: run {
                    event.isCancelled = true
                    return
                }
                event.isCancelled = true
                if (itemStack.amount <= 1) {
                    inventoryHolder.placeItemStack(targetSlot, itemStack)
                    clickedInventory.setItem(event.slot, null)
                } else {
                    inventoryHolder.placeItemStack(targetSlot, itemStack)
                    itemStack.amount -= 1
                    clickedInventory.setItem(event.slot, itemStack)
                }
                shouldSync = true
            }
        }

        if (event.action == InventoryAction.COLLECT_TO_CURSOR) {
            shouldSync = true
        }

        if (!event.isCancelled &&
            clickedInventory == event.view.topInventory &&
            (event.rawSlot == EnchantingInventory.TARGET_SLOT || event.rawSlot in EnchantingInventory.ALL_CRAFT_SLOTS)
        ) {
            shouldSync = true
        }

        if (shouldSync) {
            inventoryHolder.sync(plugin, player)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val inventoryHolder = event.view.topInventory.holder as? EnchantingInventory ?: return
        val player = event.whoClicked as? Player ?: return
        if (event.rawSlots.any { slot -> slot == EnchantingInventory.RESULT_SLOT }) {
            event.isCancelled = true
            inventoryHolder.sync(plugin, player)
            return
        }
        if (event.rawSlots.any { slot ->
                slot == EnchantingInventory.TARGET_SLOT || slot in EnchantingInventory.ALL_CRAFT_SLOTS
            }
        ) {
            inventoryHolder.sync(plugin, player)
        }
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val inventoryHolder = event.inventory.holder as? EnchantingInventory ?: return
        val player = event.player as? Player ?: return
        inventoryHolder.recipeBook.sendEmptyRecipeBook(player)
        inventoryHolder.sync(plugin, player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventoryHolder = event.inventory.holder as? EnchantingInventory ?: return
        val player = event.player as? Player ?: return
        inventoryHolder.rollbackCrafting(player)
        inventoryHolder.recipeBook.sendRestoredRecipeBook(player, plugin)
    }

    @EventHandler
    fun onPlayerRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        if (RecipeBookToastSuppressor.shouldSuppress(event.player)) {
            event.shouldShowNotification(false)
        }
    }
}
