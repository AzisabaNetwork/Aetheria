package net.azisaba.vanilife.enchanting

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.Serializable
import net.azisaba.serialization.EnchantmentSerializer
import net.azisaba.serialization.KeySerializer
import net.azisaba.vanilife.DynamicContents
import net.azisaba.vanilife.item.ServerItem
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

@Serializable
data class EnchantingRecipe(
    @Serializable(with = EnchantmentSerializer::class)
    val enchantment: Enchantment,
    @Serializable(with = KeySerializer::class)
    val ingredient: Key,
) {
    fun targetLevelFor(itemStack: ItemStack): Int? {
        if (itemStack.type == Material.AIR) return null
        if (!enchantment.canEnchantItem(itemStack)) return null

        val currentLevel = itemStack.getEnchantmentLevel(enchantment)
        return when {
            currentLevel == 0 -> 1
            currentLevel < enchantment.maxLevel -> currentLevel + 1
            else -> null
        }
    }

    fun matches(itemStack: ItemStack?, ingredientStack: ItemStack?): Boolean {
        val targetStack = itemStack ?: return false
        val ingredientStackValue = ingredientStack ?: return false
        return targetLevelFor(targetStack) != null && createIngredientItem().isSimilar(ingredientStackValue)
    }

    fun requiredLevel(itemStack: ItemStack): Int? {
        val targetLevel = targetLevelFor(itemStack) ?: return null
        return enchantment.getMinModifiedCost(targetLevel)
    }

    fun createIngredientItem(): ItemStack {
        return resolveIngredientItem().clone().apply {
            amount = 1
        }
    }

    fun createResultItem(itemStack: ItemStack, level: Int): ItemStack {
        return itemStack.clone().apply {
            addUnsafeEnchantment(enchantment, level)
        }
    }

    fun createResultDisplayItem(
        itemStack: ItemStack,
        currentLevel: Int,
        level: Int,
        requiredLevel: Int,
        affordable: Boolean,
    ): ItemStack {
        return createResultItem(itemStack, level).apply {
            setData(DataComponentTypes.ITEM_NAME, createResultName(requiredLevel, affordable))
            setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                    .addHiddenComponents(DataComponentTypes.ENCHANTMENTS, DataComponentTypes.STORED_ENCHANTMENTS)
                    .build(),
            )
            lore(createResultLore(currentLevel, level))
        }
    }

    private fun resolveIngredientItem(): ItemStack {
        val registryAccess = RegistryAccess.registryAccess()
        val itemRegistry = registryAccess.getRegistry(RegistryKey.ITEM)
        val serverItemRegistry = registryAccess.getRegistry(RegistryKey.SERVER_ITEM)

        return when (val resolved = itemRegistry.get(ingredient) ?: serverItemRegistry.get(ingredient)) {
            is ItemType -> resolved.createItemStack(1)
            is ServerItem -> ItemStack.of(resolved, 1)
            else -> error("Cannot resolve ingredient item id: $ingredient")
        }
    }

    private fun createResultName(requiredLevel: Int, affordable: Boolean): Component {
        val requiredLevelColor = if (affordable) NamedTextColor.GREEN else NamedTextColor.RED
        return Component.text()
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(
                Component.text(EnchantingFonts.EnchantingIcons.EXPERIENCE, NamedTextColor.WHITE)
                    .font(EnchantingFonts.ENCHANTING_ICONS),
            )
            .appendSpace()
            .append(
                Component.translatable(
                    EnchantingTranslations.ENCHANTING_REQUIRED_LEVEL,
                    Component.text(requiredLevel, requiredLevelColor),
                ),
            )
            .build()
    }

    private fun createResultLore(currentLevel: Int, level: Int): List<Component> {
        val lineColor = if (currentLevel > 0) NamedTextColor.AQUA else NamedTextColor.GREEN
        val lore = mutableListOf<Component>()
        lore += Component.text()
            .color(lineColor)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(if (currentLevel > 0) "^" else "+"))
            .append(Component.text(" "))
            .append(Component.translatable(enchantment))
            .append(
                if (currentLevel > 0) {
                    Component.text(" ")
                        .append(Component.text("("))
                        .append(levelComponent(currentLevel))
                        .append(Component.text(" -> "))
                        .append(levelComponent(level))
                        .append(Component.text(")"))
                } else {
                    Component.empty()
                },
            )
            .build()
        return lore
    }

    private fun levelComponent(level: Int): Component {
        val translationKey = when (level) {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> "enchantment.level.$level"
            else -> null
        } ?: return Component.text(level)
        return Component.translatable(translationKey)
    }

    companion object : DynamicContents<EnchantingRecipe>("enchanting_recipe", lazy { EnchantingRecipe.serializer() })
}