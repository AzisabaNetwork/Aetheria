# Implementable Cooking Recipes

This document lists concrete, implementable recipe definitions for the `plugin-cooking` module. Each recipe entry includes:
- A `NamespacedKey` suggestion to follow the repo pattern.
- Recipe type (Shaped, Shapeless, Furnace/Campfire).
- Output item (from `CookingItems`).
- Ingredient list using `CookingItems.*` keys (use `ItemStack.of(typedKey)` or `RecipeChoice.ExactChoice(ItemStack.of(...))` in code).
- For shaped recipes: an ASCII grid pattern mapping.
- For cook recipes: suggested experience and cook times (mirrors `MiningRecipes.kt` conventions).

Use the examples below as drop-in code in a new `CookingRecipes.kt` object. Follow `MiningRecipes.kt` for style: define `val NAME: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "...")`, builder functions returning `ShapedRecipe`/`ShapelessRecipe`/`FurnaceRecipe`/`CampfireRecipe`, and a `bootstrap(server: Server)` method that calls `server.addRecipe(...)`.

---

## 1) Steamed Rice
- Key: `steamed_rice_from_rice`
- Type: `ShapelessRecipe`
- Output: `CookingItems.STEAMED_RICE`
- Ingredients:
  - `CookingItems.RICE` x1
- Example Kotlin snippet:

```kotlin
val STEAMED_RICE_FROM_RICE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "steamed_rice_from_rice")

private fun steamedRiceFromRice(): ShapelessRecipe {
    val result = ItemStack.of(CookingItems.STEAMED_RICE)
    val recipe = ShapelessRecipe(STEAMED_RICE_FROM_RICE, result)
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.RICE)))
    return recipe
}
```

Notes: This converts raw `RICE` into `STEAMED_RICE`. Use shapeless so order doesn't matter.

---

## 2) Bamboo Shoot Rice
- Key: `bamboo_shoot_rice_from_bamboo_shoot_and_rice`
- Type: `ShapelessRecipe` (or `ShapedRecipe` if you prefer a layout)
- Output: `CookingItems.BAMBOO_SHOOT_RICE`
- Ingredients:
  - `CookingItems.BAMBOO_SHOOT` x1
  - `CookingItems.RICE` x1
  - optional: `CookingItems.BUTTER` x1 (omit if not present)
- Kotlin snippet:

```kotlin
val BAMBOO_SHOOT_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "bamboo_shoot_rice_from_ingredients")

private fun bambooShootRiceFromIngredients(): ShapelessRecipe {
    val result = ItemStack.of(CookingItems.BAMBOO_SHOOT_RICE)
    val recipe = ShapelessRecipe(BAMBOO_SHOOT_RICE_FROM_INGREDIENTS, result)
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.BAMBOO_SHOOT)))
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.RICE)))
    // recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.BUTTER))) // optional
    return recipe
}
```

---

## 3) Curry Rice
- Key: `curry_rice_from_ingredients`
- Type: `ShapedRecipe`
- Output: `CookingItems.CURRY_RICE`
- Ingredients (substitutions: use `SAUSAGE` for meat if needed):
  - `CookingItems.RICE` (STEAMED_RICE or RICE)
  - `CookingItems.ONION`
  - `CookingItems.MISO` (used as curry roux substitute)
  - `CookingItems.SAUSAGE` (as meat substitute)
- Suggested Pattern (3x3):
  - " M "
  - "OSR"
  - "   "
  - Map: M = `MISO`, O = `ONION`, S = `SAUSAGE`, R = `STEAMED_RICE`

Kotlin snippet:

```kotlin
val CURRY_RICE_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "curry_rice_from_ingredients")

private fun curryRiceFromIngredients(): ShapedRecipe {
    val result = ItemStack.of(CookingItems.CURRY_RICE)
    val recipe = ShapedRecipe(CURRY_RICE_FROM_INGREDIENTS, result)
    recipe.shape(" M ", "OSR", "   ")
    recipe.setIngredient('M', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.MISO)))
    recipe.setIngredient('O', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.ONION)))
    recipe.setIngredient('S', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.SAUSAGE)))
    recipe.setIngredient('R', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
    return recipe
}
```

Notes: adjust pattern or quantities if you want multi-count inputs (the Bukkit `ShapedRecipe` API allows using ItemStack quantities by using multiple slots of the same ingredient).

---

## 4) Miso Soup
- Key: `miso_soup_from_miso`
- Type: `ShapelessRecipe`
- Output: `CookingItems.MISO_SOUP`
- Ingredients:
  - `CookingItems.MISO` x1
  - optional: `CookingItems.GREEN_ONION` x1
- Kotlin snippet:

```kotlin
val MISO_SOUP_FROM_MISO: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "miso_soup_from_miso")

private fun misoSoupFromMiso(): ShapelessRecipe {
    val result = ItemStack.of(CookingItems.MISO_SOUP)
    val recipe = ShapelessRecipe(MISO_SOUP_FROM_MISO, result)
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.MISO)))
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.GREEN_ONION)))
    return recipe
}
```

---

## 5) Parfait
- Key: `parfait_from_icecream_and_fruit`
- Type: `ShapelessRecipe`
- Output: `CookingItems.PARFAIT`
- Ingredients:
  - `CookingItems.SOFT_SERVE_ICE_CREAM` or `CookingItems.SOFT_SERVE_ICE_CREAM` (prepared) x1
  - `CookingItems.STRAWBERRY` or `CookingItems.BANANA` or `CookingItems.BLUEBERRY` (any fruit) x1
- Kotlin snippet:

```kotlin
val PARFAIT_FROM_ICECREAM_AND_FRUIT: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "parfait_from_icecream_and_fruit")

private fun parfaitFromIcecreamAndFruit(): ShapelessRecipe {
    val result = ItemStack.of(CookingItems.PARFAIT)
    val recipe = ShapelessRecipe(PARFAIT_FROM_ICECREAM_AND_FRUIT, result)
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.SOFT_SERVE_ICE_CREAM)))
    recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack.of(CookingItems.STRAWBERRY)))
    return recipe
}
```

---

## 6) Grilled Squid (cook)
- Key: `grilled_squid_from_firefly_squid`
- Type: `CampfireRecipe` (or `FurnaceRecipe`)
- Output: `CookingItems.GRILLED_SQUID`
- Input: `CookingItems.FIREFLY_SQUID`
- Suggested campfire values: experience `0.45f`, cook time `200` (ms ticks as used in `MiningRecipes`).

Kotlin snippet:

```kotlin
val GRILLED_SQUID_FROM_FIREFLY_SQUID: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_squid_from_firefly_squid")

private fun grilledSquidFromFireflySquid(): CampfireRecipe = CampfireRecipe(
    GRILLED_SQUID_FROM_FIREFLY_SQUID,
    ItemStack.of(CookingItems.GRILLED_SQUID),
    RecipeChoice.ExactChoice(ItemStack.of(CookingItems.FIREFLY_SQUID)),
    0.45f,
    200,
)
```

---

## 7) Grilled Mackerel (cook)
- Key: `grilled_mackerel_from_sardine`
- Type: `CampfireRecipe`
- Output: `CookingItems.GRILLED_MACKEREL`
- Input: `CookingItems.SARDINE` (or `SKIPJACK_TUNA` substitute)
- Suggested values: experience `0.6f`, cook time `200`.

Kotlin snippet:

```kotlin
val GRILLED_MACKEREL_FROM_SARDINE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "grilled_mackerel_from_sardine")

private fun grilledMackerelFromSardine(): CampfireRecipe = CampfireRecipe(
    GRILLED_MACKEREL_FROM_SARDINE,
    ItemStack.of(CookingItems.GRILLED_MACKEREL),
    RecipeChoice.ExactChoice(ItemStack.of(CookingItems.SARDINE)),
    0.6f,
    200,
)
```

---

## 8) Salmon Roe Sushi (shaped/simple)
- Key: `salmon_roe_sushi_from_roe_and_steamed_rice`
- Type: `ShapedRecipe` (1x2 vertical)
- Output: `CookingItems.SALMON_ROE_SUSHI`
- Ingredients:
  - `CookingItems.STEAMED_RICE`
  - `CookingItems.SALMON_ROE`
- Pattern:
  - "R"
  - "S"

Kotlin snippet:

```kotlin
val SALMON_ROE_SUSHI_FROM_INGREDIENTS: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "salmon_roe_sushi_from_ingredients")

private fun salmonRoeSushiFromIngredients(): ShapedRecipe {
    val result = ItemStack.of(CookingItems.SALMON_ROE_SUSHI)
    val recipe = ShapedRecipe(SALMON_ROE_SUSHI_FROM_INGREDIENTS, result)
    recipe.shape("R", "S")
    recipe.setIngredient('R', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.STEAMED_RICE)))
    recipe.setIngredient('S', RecipeChoice.ExactChoice(ItemStack.of(CookingItems.SALMON_ROE)))
    return recipe
}
```

---

## Registration example
Create a new `plugins/plugin-cooking/src/main/kotlin/net/azisaba/vanilife/cooking/CookingRecipes.kt` file with the pattern below (mirror `MiningRecipes.kt` style):

```kotlin
object CookingRecipes {
    val STEAMED_RICE_FROM_RICE: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "steamed_rice_from_rice")
    // ... other keys

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

    // ... implement builder functions (see snippets above)
}
```

Call `CookingRecipes.bootstrap(server)` during plugin initialization. Because `plugin-cooking` uses Paper bootstrap lifecycle, call `CookingRecipes.bootstrap(server)` from an appropriate lifecycle handler or the plugin `onEnable` (inject `Plugin` or `Server`) — for example add a lifecycle handler in `Bootstrap.kt` or call from `Main.onEnable` after the server is available.

---

## Implementation notes and assumptions
- I used only `CookingItems` keys present in `CookingItems.kt`. When a doc recipe referenced missing items, I substituted per `docs/cooking_recipes.md` notes (e.g., `SAUSAGE` for small meat portions, `BUCKWHEAT` for batter/flour).
- For shaped recipes, when an ingredient needs multiple copies, place the same `RecipeChoice` in multiple shape slots (Bukkit handles slot-based counts); if you need to output multiple quantities, set the output `ItemStack` amount by calling `ItemStack.of(key).apply { amount = N }`.
- For cook recipes I used `CampfireRecipe` with `200` cook time and experience values similar to `MiningRecipes.kt`; change to `FurnaceRecipe`/`BlastingRecipe` if desired.
- If you want, I can implement the `CookingRecipes.kt` file now and add a `Bootstrap` call to register them — tell me whether to make code changes and I will implement them.

---

Files referenced:
- `docs/cooking_recipes.md:1` (source guidance)
- `plugins/plugin-cooking/src/main/kotlin/net/azisaba/vanilife/cooking/CookingItems.kt:1` (items list)
- `plugins/plugin-mining/src/main/kotlin/net/azisaba/vanilife/mining/MiningRecipes.kt:1` (style example for recipes)

If you'd like, I will now implement `CookingRecipes.kt` and register it in the plugin bootstrap. If so, confirm and I will proceed to create code changes.