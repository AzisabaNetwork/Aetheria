package net.azisaba.vanilife.cooking

import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.*
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.ItemStack

object CookingRecipes {
    val STEAMED_RICE_FROM_RICE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "steamed_rice_from_rice")
    val BAMBOO_SHOOT_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "bamboo_shoot_rice_from_ingredients")
    val CURRY_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "curry_rice_from_ingredients")
    val MISO_SOUP_FROM_MISO: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "miso_soup_from_miso")
    val PARFAIT_FROM_ICECREAM_AND_FRUIT: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "parfait_from_icecream_and_fruit")
    val GRILLED_SQUID_FROM_FIREFLY_SQUID: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_squid_from_firefly_squid")
    val GRILLED_MACKEREL_FROM_SARDINE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_mackerel_from_sardine")
    val SALMON_ROE_SUSHI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "salmon_roe_sushi_from_ingredients")

    fun bootstrap(server: Server) {
        server.addRecipe(steamedRiceFromRice())
        server.addRecipe(bambooShootRiceFromIngredients())
        server.addRecipe(curryRiceFromIngredients())
        server.addRecipe(misoSoupFromMiso())
        server.addRecipe(parfaitFromIcecreamAndFruit())
        server.addRecipe(grilledSquidFromFireflySquid())
        server.addRecipe(grilledMackerelFromSardine())
        server.addRecipe(salmonRoeSushiFromIngredients())
    }

    private fun steamedRiceFromRice(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.STEAMED_RICE)
        return ShapelessRecipe(STEAMED_RICE_FROM_RICE, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.RICE)))
        }
    }

    private fun bambooShootRiceFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.BAMBOO_SHOOT_RICE)
        return ShapelessRecipe(BAMBOO_SHOOT_RICE_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BAMBOO_SHOOT)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.RICE)))
            // optional butter omitted
        }
    }

    private fun curryRiceFromIngredients(): ShapedRecipe {
        val result = ItemStack.of(CookingItems.CURRY_RICE)
        val recipe = ShapedRecipe(CURRY_RICE_FROM_INGREDIENTS, result)
        recipe.shape(" M ", "OSR", "   ")
        recipe.setIngredient('M', ExactChoice(ItemStack.of(CookingItems.MISO)))
        recipe.setIngredient('O', ExactChoice(ItemStack.of(CookingItems.ONION)))
        recipe.setIngredient('S', ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
        recipe.setIngredient('R', ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
        return recipe
    }

    private fun misoSoupFromMiso(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.MISO_SOUP)
        return ShapelessRecipe(MISO_SOUP_FROM_MISO, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.MISO)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.GREEN_ONION)))
        }
    }

    private fun parfaitFromIcecreamAndFruit(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.PARFAIT)
        return ShapelessRecipe(PARFAIT_FROM_ICECREAM_AND_FRUIT, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SOFT_SERVE_ICE_CREAM)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STRAWBERRY)))
        }
    }

    private fun grilledSquidFromFireflySquid(): CampfireRecipe = CampfireRecipe(
        GRILLED_SQUID_FROM_FIREFLY_SQUID,
        ItemStack.of(CookingItems.GRILLED_SQUID),
        ExactChoice(ItemStack.of(CookingItems.FIREFLY_SQUID)),
        0.45f,
        200,
    )

    private fun grilledMackerelFromSardine(): CampfireRecipe = CampfireRecipe(
        GRILLED_MACKEREL_FROM_SARDINE,
        ItemStack.of(CookingItems.GRILLED_MACKEREL),
        ExactChoice(ItemStack.of(CookingItems.SARDINE)),
        0.6f,
        200,
    )

    private fun salmonRoeSushiFromIngredients(): ShapedRecipe {
        val result = ItemStack.of(CookingItems.SALMON_ROE_SUSHI)
        val recipe = ShapedRecipe(SALMON_ROE_SUSHI_FROM_INGREDIENTS, result)
        recipe.shape("R", "S")
        recipe.setIngredient('R', ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
        recipe.setIngredient('S', ExactChoice(ItemStack.of(CookingItems.SALMON_ROE)))
        return recipe
    }
}
