package net.azisaba.vanilife.enchanting

import net.azisaba.vanilife.Vanilife
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

object EnchantingRecipes {
    val ENCHANTING_TABLE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "enchanting_table")

    fun bootstrap(server: Server) {
        server.removeRecipe(NamespacedKey.minecraft("enchanting_table"))
        server.addRecipe(enchantingTable())
    }

    private fun enchantingTable(): ShapedRecipe = ShapedRecipe(
        ENCHANTING_TABLE, ItemStack.of(Material.ENCHANTING_TABLE),
    ).apply {
        shape(
            " B ",
            "DCD",
            "###",
        )

        setIngredient('#', Material.OBSIDIAN)
        setIngredient('B', Material.BOOK)
        setIngredient('C', Material.CRAFTING_TABLE)
        setIngredient('D', Material.DIAMOND)
    }
}
