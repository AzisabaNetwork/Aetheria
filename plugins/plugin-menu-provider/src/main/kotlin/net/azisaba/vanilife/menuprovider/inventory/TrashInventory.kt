package net.azisaba.vanilife.menuprovider.inventory

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.registry.keys.SoundEventKeys
import kotlinx.coroutines.delay
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

internal class TrashInventory(private val plugin: Plugin) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(this, 54, TITLE)

    override fun getInventory(): Inventory = inventory

    fun clearItemStacks(who: Audience) {
        plugin.launch {
            delay(1L.milliseconds)
            if (inventory.isEmpty) return@launch
            who.playDeleteSound()
            inventory.clear()
        }
    }

    private fun Audience.playDeleteSound() {
        val pitch = Random.nextDouble(0.0, 0.6).toFloat()
        playSound(Sound.sound(SoundEventKeys.BLOCK_CHISELED_BOOKSHELF_PICKUP, Sound.Source.UI, 0.75f, pitch))
    }

    companion object {
        val TITLE: Component = Component.text()
            .append(
                Component.text(MenuProviderFonts.MenuIcons.TRASH, NamedTextColor.WHITE)
                    .font(MenuProviderFonts.MENU_ICONS)
            )
            .appendSpace()
            .append(Component.translatable(MenuProviderTranslations.INVENTORY_VANILIFE_TRASH, NamedTextColor.RED))
            .build()
    }
}
