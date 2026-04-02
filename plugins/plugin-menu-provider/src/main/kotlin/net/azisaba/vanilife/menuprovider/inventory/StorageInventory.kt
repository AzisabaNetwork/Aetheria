package net.azisaba.vanilife.menuprovider.inventory

import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.island.storage.StorageAccessor
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin

internal class StorageInventory(
    private val storage: StorageAccessor, val size: Int, private val plugin: Plugin,
) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(this, size, TITLE)

    override fun getInventory(): Inventory = inventory

    init {
        plugin.launch {
            storage.applyToInventory(inventory)
        }
    }

    fun update() {
        plugin.launch {
            storage.updateFromInventory(inventory)
        }
    }

    companion object {
        val TITLE: Component = Component.text()
            .append(
                Component.text(MenuProviderFonts.MenuIcons.STORAGE, NamedTextColor.WHITE)
                    .font(MenuProviderFonts.MENU_ICONS)
            )
            .appendSpace()
            .append(Component.translatable(MenuProviderTranslations.INVENTORY_VANILIFE_STORAGE))
            .build()
    }
}