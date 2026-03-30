package net.azisaba.vanilife.cooking

import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.*
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.ItemStack

object CookingRecipes {
    val STEAMED_RICE_FROM_RICE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "steamed_rice_from_rice")

    val GRILLED_SQUID_FROM_FIREFLY_SQUID: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_squid_from_firefly_squid")
    val GRILLED_MACKEREL_FROM_SARDINE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_mackerel_from_sardine")
    val GRILLED_AYU_FROM_PIKE_CONGER: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_ayu_from_pike_conger")
    val GRILLED_PACIFIC_SAURY_FROM_SKIPJACK: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_pacific_saury_from_skipjack")

    val BAMBOO_SHOOT_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "bamboo_shoot_rice_from_ingredients")
    val CURRY_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "curry_rice_from_ingredients")
    val EEL_RICE_BOWL_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "eel_rice_bowl_from_ingredients")
    val FRIED_HORSE_MACKEREL_FROM_SARDINE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "fried_horse_mackerel_from_sardine")
    val HAMBURG_STEAK_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "hamburg_steak_from_ingredients")
    val MARINATED_EGGPLANT_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "marinated_eggplant_from_ingredients")
    val MISO_MACKEREL_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "miso_mackerel_from_ingredients")
    val MISO_SOUP_FROM_MISO: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "miso_soup_from_miso")
    val NIKUJAGA_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "nikujaga_from_ingredients")
    val ODEN_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "oden_from_ingredients")
    val PARFAIT_FROM_ICECREAM_AND_FRUIT: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "parfait_from_icecream_and_fruit")
    val SALAD_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "salad_from_ingredients")
    val SALMON_ROE_SUSHI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "salmon_roe_sushi_from_ingredients")
    val SEA_URCHIN_SUSHI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "sea_urchin_sushi_from_ingredients")
    val SEAFOOD_RICE_FOWL_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "seafood_rice_fowl_from_ingredients")
    val SOBA_FROM_BUCKWHEAT: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "soba_from_buckwheat")
    val TAKOYAKI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "takoyaki_from_ingredients")
    val TAMAGO_SUSHI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "tamago_sushi_from_ingredients")
    val TERIYAKI_YELLOWTAIL_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "teriyaki_yellowtail_from_ingredients")
    val TONKATSU_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "tonkatsu_from_ingredients")
    val TUNA_SUSHI_FROM_SKIPJACK: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "tuna_sushi_from_skipjack")
    val UDON_FROM_SOYBEANS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "udon_from_soybeans")
    val YAKISOBA_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "yakisoba_from_ingredients")

    fun bootstrap(server: Server) {
        server.addRecipe(steamedRiceFromRice())

        server.addRecipe(grilledSquidFromFireflySquid())
        server.addRecipe(grilledMackerelFromSardine())
        server.addRecipe(grilledAyuFromPikeConger())
        server.addRecipe(grilledPacificSauryFromSkipjack())

        server.addRecipe(bambooShootRiceFromIngredients())
        server.addRecipe(curryRiceFromIngredients())
        server.addRecipe(eelRiceBowlFromIngredients())
        server.addRecipe(seafoodRiceFowlFromIngredients())
        server.addRecipe(salmonRoeSushiFromIngredients())
        server.addRecipe(seaUrchinSushiFromIngredients())
        server.addRecipe(tamagoSushiFromIngredients())
        server.addRecipe(tunaSushiFromSkipjack())
        server.addRecipe(friedHorseMackerelFromSardine())
        server.addRecipe(misoMackerelFromIngredients())
        server.addRecipe(teriyakiYellowtailFromIngredients())
        server.addRecipe(hamburgSteakFromIngredients())
        server.addRecipe(tonkatsuFromIngredients())
        server.addRecipe(misoSoupFromMiso())
        server.addRecipe(nikujagaFromIngredients())
        server.addRecipe(odenFromIngredients())
        server.addRecipe(marinatedEggplantFromIngredients())
        server.addRecipe(saladFromIngredients())
        server.addRecipe(sobaFromBuckwheat())
        server.addRecipe(udonFromSoybeans())
        server.addRecipe(yakisobaFromIngredients())
        server.addRecipe(takoyakiFromIngredients())
        server.addRecipe(parfaitFromIcecreamAndFruit())
    }

    private fun steamedRiceFromRice(): FurnaceRecipe = FurnaceRecipe(
        STEAMED_RICE_FROM_RICE,
        ItemStack.of(CookingItems.STEAMED_RICE),
        ExactChoice(ItemStack.of(CookingItems.RICE)),
        0.35f,
        200,
    )

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

    private fun grilledAyuFromPikeConger(): CampfireRecipe = CampfireRecipe(
        GRILLED_AYU_FROM_PIKE_CONGER,
        ItemStack.of(CookingItems.GRILLED_AYU),
        ExactChoice(ItemStack.of(CookingItems.PIKE_CONGER)),
        0.45f,
        200,
    )

    private fun grilledPacificSauryFromSkipjack(): CampfireRecipe = CampfireRecipe(
        GRILLED_PACIFIC_SAURY_FROM_SKIPJACK,
        ItemStack.of(CookingItems.GRILLED_PACIFIC_SAURY),
        ExactChoice(ItemStack.of(CookingItems.SKIPJACK_TUNA)),
        0.6f,
        200,
    )

    private fun bambooShootRiceFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.BAMBOO_SHOOT_RICE)
        return ShapelessRecipe(BAMBOO_SHOOT_RICE_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BAMBOO_SHOOT)))
        }
    }

    private fun curryRiceFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.CURRY_RICE)
        return ShapelessRecipe(CURRY_RICE_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.ONION)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
        }
    }

    private fun eelRiceBowlFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.EEL_RICE_BOWL)
        return ShapelessRecipe(EEL_RICE_BOWL_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.PIKE_CONGER)))
        }
    }

    private fun seafoodRiceFowlFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.SEAFOOD_RICE_FOWL)
        return ShapelessRecipe(SEAFOOD_RICE_FOWL_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SALMON_ROE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SKIPJACK_TUNA)))
        }
    }

    private fun salmonRoeSushiFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.SALMON_ROE_SUSHI)
        return ShapelessRecipe(SALMON_ROE_SUSHI_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SALMON_ROE)))
        }
    }

    private fun seaUrchinSushiFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.SEA_URCHIN_SUSHI)
        return ShapelessRecipe(SEA_URCHIN_SUSHI_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SALMON_ROE)))
        }
    }

    private fun tamagoSushiFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.TAMAGO_SUSHI)
        return ShapelessRecipe(TAMAGO_SUSHI_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUTTER)))
        }
    }

    private fun tunaSushiFromSkipjack(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.TUNA_SUSHI)
        return ShapelessRecipe(TUNA_SUSHI_FROM_SKIPJACK, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SKIPJACK_TUNA)))
        }
    }

    private fun friedHorseMackerelFromSardine(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.FRIED_HORSE_MACKEREL)
        return ShapelessRecipe(FRIED_HORSE_MACKEREL_FROM_SARDINE, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SARDINE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUCKWHEAT)))
        }
    }

    private fun misoMackerelFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.MISO_MACKEREL)
        return ShapelessRecipe(MISO_MACKEREL_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.MISO)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SARDINE)))
        }
    }

    private fun teriyakiYellowtailFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.TERIYAKI_YELLOWTAIL)
        return ShapelessRecipe(TERIYAKI_YELLOWTAIL_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SKIPJACK_TUNA)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.MISO)))
        }
    }

    private fun hamburgSteakFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.HAMBURG_STEAK)
        return ShapelessRecipe(HAMBURG_STEAK_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.ONION)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUTTER)))
        }
    }

    private fun tonkatsuFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.TONKATSU)
        return ShapelessRecipe(TONKATSU_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUCKWHEAT)))
        }
    }

    private fun misoSoupFromMiso(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.MISO_SOUP)
        return ShapelessRecipe(MISO_SOUP_FROM_MISO, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.MISO)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.GREEN_ONION)))
        }
    }

    private fun nikujagaFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.NIKUJAGA)
        return ShapelessRecipe(NIKUJAGA_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SWEET_POTATO)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.ONION)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
        }
    }

    private fun odenFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.ODEN)
        return ShapelessRecipe(ODEN_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.JAPANESE_RADISH)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUCKWHEAT)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
        }
    }

    private fun marinatedEggplantFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.MARINATED_EGGPLANT)
        return ShapelessRecipe(MARINATED_EGGPLANT_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.EGGPLANT)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.MISO)))
        }
    }

    private fun saladFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.SALAD)
        return ShapelessRecipe(SALAD_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.LETTUCE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.TOMATO)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.CUCUMBER)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.ONION)))
        }
    }

    private fun sobaFromBuckwheat(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.SOBA)
        return ShapelessRecipe(SOBA_FROM_BUCKWHEAT, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUCKWHEAT)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.GREEN_ONION)))
        }
    }

    private fun udonFromSoybeans(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.UDON)
        return ShapelessRecipe(UDON_FROM_SOYBEANS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SOYBEANS)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.GREEN_ONION)))
        }
    }

    private fun yakisobaFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.YAKISOBA)
        return ShapelessRecipe(YAKISOBA_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.NAPPA_CABBAGE)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.ONION)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
        }
    }

    private fun takoyakiFromIngredients(): ShapelessRecipe {
        val result = ItemStack.of(CookingItems.TAKOYAKI)
        return ShapelessRecipe(TAKOYAKI_FROM_INGREDIENTS, result).apply {
            addIngredient(ExactChoice(ItemStack.of(CookingItems.BUCKWHEAT)))
            addIngredient(ExactChoice(ItemStack.of(CookingItems.FIREFLY_SQUID)))
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
}
