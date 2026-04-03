package net.azisaba.vanilife.enchanting.inventory

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import net.azisaba.vanilife.enchanting.EnchantingFonts
import net.azisaba.vanilife.enchanting.RecipeBookStates
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

internal class EnchantingInventory(player: Player) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(
        this,
        InventoryType.WORKBENCH,
        Component.text()
            .append(
                Component.text(EnchantingFonts.EnchantingIcons.ENCHANTING_TABLE, NamedTextColor.WHITE)
                    .font(EnchantingFonts.ENCHANTING_ICONS)
            )
            .appendSpace()
            .append(Component.text("Enchanting"))
            .build()
    )

    override fun getInventory(): Inventory = inventory

    fun hideRecipeBook(player: Player) {
        RecipeBookStates.cacheDiscovered(player)

        val user = PacketEvents.getAPI().playerManager.getUser(player)
        user.sendPacket(WrapperPlayServerRecipeBookAdd(emptyList(), true))
    }

    fun restoreRecipeBook(player: Player) {
        val snapshot = RecipeBookStates.lookup(player) ?: return
        val user = PacketEvents.getAPI().playerManager.getUser(player)

        player.undiscoverRecipes(snapshot.discovered)
        player.discoverRecipes(snapshot.discovered)

        snapshot.settings?.let {
            user.sendPacket(WrapperPlayServerRecipeBookSettings(snapshot.settings))
        }
    }
}
