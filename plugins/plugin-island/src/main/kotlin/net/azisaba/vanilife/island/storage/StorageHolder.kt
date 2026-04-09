package net.azisaba.vanilife.island.storage

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Range

interface StorageHolder {
    fun getStorageItem(index: @Range(from = 0, to = 53) Int): ItemStack?

    fun applyStorageToInventory(inventory: Inventory)

    suspend fun setStorageItem(index: @Range(from = 0, to = 53) Int, itemStack: ItemStack)

    suspend fun clearStorageItem(index: @Range(from = 0, to = 53) Int)

    suspend fun updateStorageFromInventory(inventory: Inventory)
}
