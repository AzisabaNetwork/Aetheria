package net.azisaba.vanilife.enchanting.recipe

import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes
import com.github.retrooper.packetevents.protocol.item.type.ItemType
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayEntry
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayId
import com.github.retrooper.packetevents.protocol.recipe.category.RecipeBookCategories
import com.github.retrooper.packetevents.protocol.recipe.display.ShapedCraftingRecipeDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.EmptySlotDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.ItemStackSlotDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.SlotDisplay
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.Serializable
import net.azisaba.serialization.EnchantmentSerializer
import net.azisaba.serialization.KeySerializer
import net.azisaba.vanilife.DynamicContents
import net.azisaba.vanilife.item.ServerItem
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItemStack
import com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment as PacketEnchantment

@Serializable
data class EnchantingRecipe(
    @Serializable(with = EnchantmentSerializer::class)
    val enchantment: Enchantment,
    @Serializable(with = KeySerializer::class)
    val ingredientItemId: Key,
) {
    fun targetLevelFor(centerItem: ItemStack?): Int? {
        val item = centerItem ?: return null
        if (item.type == Material.AIR) return null
        if (!enchantment.canEnchantItem(item)) return null

        val currentLevel = item.getEnchantmentLevel(enchantment)
        return when {
            currentLevel == 0 -> 1
            currentLevel < enchantment.maxLevel -> currentLevel + 1
            else -> null
        }
    }

    fun canApplyTo(centerItem: ItemStack?): Boolean = targetLevelFor(centerItem) != null

    fun createIngredientItem(): ItemStack {
        return resolveIngredientItem().clone().apply {
            amount = 1
        }
    }

    fun toCraftingRequirements(): List<MappedEntitySet<ItemType>> {
        val ingredientType = SpigotConversionUtil.fromBukkitItemStack(createIngredientItem()).type
        return listOf(
            ingredientType,
            ItemTypes.LAPIS_LAZULI,
            ingredientType,
            ItemTypes.LAPIS_LAZULI,
            ingredientType,
            ItemTypes.LAPIS_LAZULI,
            ingredientType,
            ItemTypes.LAPIS_LAZULI,
        ).map { type -> mappedSet(type) }
    }

    fun createResultItem(sourceItem: ItemStack, level: Int): ItemStack {
        val result = sourceItem.clone()
        result.addUnsafeEnchantment(enchantment, level)
        return result
    }

    fun toRecipeBookEntry(index: Int, level: Int): WrapperPlayServerRecipeBookAdd.AddEntry {
        return WrapperPlayServerRecipeBookAdd.AddEntry(
            RecipeDisplayEntry(
                RecipeDisplayId(index),
                toBookDisplay(level),
                null,
                RecipeBookCategories.CRAFTING_MISC,
                toCraftingRequirements(),
            ),
            false,
            false,
        )
    }

    fun toBookDisplay(level: Int): ShapedCraftingRecipeDisplay {
        val book = createBookDisplayItem(level)
        return createDisplay(
            centerDisplay = ItemStackSlotDisplay(book),
            resultDisplay = ItemStackSlotDisplay(book.copy()),
        )
    }

    fun toPreviewDisplay(centerItem: PacketItemStack?, level: Int): ShapedCraftingRecipeDisplay {
        val ingredientDisplay = ItemStackSlotDisplay(createDisplayItem(SpigotConversionUtil.fromBukkitItemStack(createIngredientItem()).type))
        val lapisDisplay = ItemStackSlotDisplay(createDisplayItem(ItemTypes.LAPIS_LAZULI))
        val centerDisplay = centerItem?.let {
            ItemStackSlotDisplay(it)
        } ?: EmptySlotDisplay.INSTANCE
        val resultDisplay = centerItem?.let {
            ItemStackSlotDisplay(createResultDisplayItem(it, level))
        } ?: EmptySlotDisplay.INSTANCE

        return ShapedCraftingRecipeDisplay(
            3,
            3,
            listOf(
                ingredientDisplay,
                lapisDisplay,
                ingredientDisplay,
                lapisDisplay,
                centerDisplay,
                lapisDisplay,
                ingredientDisplay,
                lapisDisplay,
                ingredientDisplay,
            ),
            resultDisplay,
            ItemStackSlotDisplay(createDisplayItem(ItemTypes.CRAFTING_TABLE)),
        )
    }

    private fun resolveIngredientItem(): ItemStack {
        val registryAccess = RegistryAccess.registryAccess()
        val itemRegistry = registryAccess.getRegistry(RegistryKey.ITEM)
        val serverItemRegistry = registryAccess.getRegistry(RegistryKey.SERVER_ITEM)

        return when (val resolved = itemRegistry.get(ingredientItemId) ?: serverItemRegistry.get(ingredientItemId)) {
            is org.bukkit.inventory.ItemType -> resolved.createItemStack(1)
            is ServerItem -> ItemStack.of(resolved, 1)
            else -> error("Cannot resolve ingredient item id: $ingredientItemId")
        }
    }

    private fun createDisplayItem(type: ItemType): PacketItemStack {
        return PacketItemStack.builder()
            .type(type)
            .amount(1)
            .build()
    }

    private fun createBookDisplayItem(level: Int): PacketItemStack {
        return createDisplayItem(ItemTypes.ENCHANTED_BOOK).apply {
            setComponent(
                ComponentTypes.CUSTOM_NAME,
                enchantment.displayName(level),
            )
        }
    }

    private fun createResultDisplayItem(sourceItem: PacketItemStack, level: Int): PacketItemStack {
        val packetEnchantment = enchantment.toPacketEnchantmentType()
        return sourceItem.copy().apply {

            enchantments = listOf(
                PacketEnchantment.builder()
                    .type(packetEnchantment)
                    .level(level)
                    .build(),
            )
        }
    }

    private fun Enchantment.toPacketEnchantmentType(): EnchantmentType {
        return EnchantmentTypes.getByName(key().asString())
            ?: error("Cannot resolve packet enchantment type: ${key().asString()}")
    }

    private fun createDisplay(
        centerDisplay: SlotDisplay<*>,
        resultDisplay: SlotDisplay<*>,
    ): ShapedCraftingRecipeDisplay {
        val slots = listOf(
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
            centerDisplay,
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
            EmptySlotDisplay.INSTANCE,
        )

        return ShapedCraftingRecipeDisplay(
            3,
            3,
            slots,
            resultDisplay,
            ItemStackSlotDisplay(createDisplayItem(ItemTypes.CRAFTING_TABLE)),
        )
    }


    companion object : DynamicContents<EnchantingRecipe>("enchanting_recipe", lazy { EnchantingRecipe.serializer() }) {
        private fun mappedSet(
            type: ItemType
        ): MappedEntitySet<ItemType> =
            MappedEntitySet(mutableListOf(type))
    }
}
