package net.azisaba.vanilife.enchanting.recipebook

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayEntry
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayId
import com.github.retrooper.packetevents.protocol.recipe.category.RecipeBookCategories
import com.github.retrooper.packetevents.protocol.recipe.display.ShapedCraftingRecipeDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.EmptySlotDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.ItemStackSlotDisplay
import com.github.retrooper.packetevents.protocol.recipe.display.slot.SlotDisplay
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCraftRecipeResponse
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareRecipes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.math.Position
import kotlinx.coroutines.delay
import net.azisaba.vanilife.enchanting.EnchantingRecipe
import net.azisaba.vanilife.island.getIslandAt
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketItemStack
import com.github.retrooper.packetevents.protocol.item.type.ItemType as PacketItemType
import kotlin.time.Duration.Companion.milliseconds

internal class EnchantingRecipeBook(private val candidates: List<Candidate>) {
    fun candidate(index: Int): Candidate? = candidates.firstOrNull { it.index == index }

    fun findVisibleRecipe(itemStack: ItemStack?, ingredientStack: ItemStack?): EnchantingRecipe? {
        return candidates.firstOrNull { candidate ->
            candidate.recipe.matches(itemStack, ingredientStack)
        }?.recipe
    }

    fun sendRecipeBook(player: Player) {
        val user = PacketEvents.getAPI().playerManager.getUser(player)
        if (candidates.isEmpty()) {
            user.sendPacket(WrapperPlayServerRecipeBookAdd(emptyList(), true))
            return
        }

        user.sendPacket(WrapperPlayServerDeclareRecipes(emptyMap(), emptyList()))
        user.sendPacket(
            WrapperPlayServerRecipeBookAdd(
                candidates.map { candidate -> candidate.recipe.toRecipeBookEntry(candidate.index, candidate.level) },
                true,
            ),
        )
    }

    fun sendEmptyRecipeBook(player: Player) {
        RecipeBookStates.cacheDiscovered(player)
        RecipeBookStates.runWithoutCaching(player) {
            empty().sendRecipeBook(player)
            RecipeBookStates.lookup(player)?.settings?.let { settings ->
                PacketEvents.getAPI().playerManager.getUser(player)
                    .sendPacket(WrapperPlayServerRecipeBookSettings(RecipeBookStates.createExpandedSettings(settings)))
            }
        }
    }

    fun sendPreview(player: Player, windowId: Int, display: ShapedCraftingRecipeDisplay) {
        PacketEvents.getAPI().playerManager.getUser(player).sendPacket(
            WrapperPlayServerCraftRecipeResponse(windowId, display),
        )
    }

    fun sendPreview(player: Player, windowId: Int, candidate: Candidate, itemStack: ItemStack) {
        val requiredLevel = candidate.recipe.requiredLevel(itemStack) ?: return
        val affordable = player.level >= requiredLevel
        sendPreview(
            player,
            windowId,
            candidate.recipe.toPreviewDisplay(
                SpigotConversionUtil.fromBukkitItemStack(itemStack),
                candidate.level,
                affordable,
                requiredLevel,
            ),
        )
    }

    fun sendRestoredRecipeBook(player: Player, plugin: Plugin) {
        val snapshot = RecipeBookStates.lookup(player) ?: return
        plugin.launch(plugin.entityDispatcher(player)) {
            delay(1L.milliseconds)
            val user = PacketEvents.getAPI().playerManager.getUser(player)
            RecipeBookStates.runWithoutCaching(player) {
                RecipeBookToastSuppressor.runWithoutToast(player) {
                    user.sendPacket(WrapperPlayServerRecipeBookAdd(emptyList(), true))
                    if (snapshot.discovered.isNotEmpty()) {
                        player.undiscoverRecipes(snapshot.discovered)
                        player.discoverRecipes(snapshot.discovered)
                    }
                }
                snapshot.settings?.let { settings ->
                    user.sendPacket(WrapperPlayServerRecipeBookSettings(settings))
                }
            }
        }
    }

    companion object {
        fun empty(): EnchantingRecipeBook = EnchantingRecipeBook(emptyList())

        suspend fun fromContext(player: Player, itemStack: ItemStack?): EnchantingRecipeBook {
            val island = (player.world as? IslandsWorld)?.getIslandAt(Position.fine(player.location))
            val target = itemStack ?: return empty()
            val candidates = EnchantingRecipe.toList().withIndex().mapNotNull { (index, recipe) ->
                val level = recipe.targetLevelFor(target) ?: return@mapNotNull null
                if (island?.has(recipe.enchantment) == false) {
                    return@mapNotNull null
                }
                Candidate(index, level, recipe)
            }
            return EnchantingRecipeBook(candidates)
        }
    }

    data class Candidate(val index: Int, val level: Int, val recipe: EnchantingRecipe)
}

private fun EnchantingRecipe.toRecipeBookEntry(index: Int, level: Int): WrapperPlayServerRecipeBookAdd.AddEntry {
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

private fun EnchantingRecipe.toBookDisplay(level: Int): ShapedCraftingRecipeDisplay {
    val book = createBookDisplayItem(level)
    return createDisplay(
        targetDisplay = ItemStackSlotDisplay(book),
        resultDisplay = ItemStackSlotDisplay(book.copy()),
    )
}

private fun EnchantingRecipe.toPreviewDisplay(
    itemStack: PacketItemStack?,
    level: Int,
    affordable: Boolean,
    requiredLevel: Int,
): ShapedCraftingRecipeDisplay {
    val ingredientDisplay = ItemStackSlotDisplay(
        createDisplayItem(SpigotConversionUtil.fromBukkitItemStack(createIngredientItem()).type),
    )
    val lapisDisplay = ItemStackSlotDisplay(createDisplayItem(ItemTypes.LAPIS_LAZULI))
    val targetDisplay = itemStack?.let { ItemStackSlotDisplay(it) } ?: EmptySlotDisplay.INSTANCE
    val resultDisplay = itemStack?.let { packetItemStack ->
        val bukkitItem = SpigotConversionUtil.toBukkitItemStack(packetItemStack)
        val currentLevel = bukkitItem.getEnchantmentLevel(enchantment)
        ItemStackSlotDisplay(
            SpigotConversionUtil.fromBukkitItemStack(
                createResultDisplayItem(bukkitItem, currentLevel, level, requiredLevel, affordable),
            ),
        )
    } ?: EmptySlotDisplay.INSTANCE

    return ShapedCraftingRecipeDisplay(
        3,
        3,
        listOf(
            ingredientDisplay,
            lapisDisplay,
            ingredientDisplay,
            lapisDisplay,
            targetDisplay,
            lapisDisplay,
            ingredientDisplay,
            lapisDisplay,
            ingredientDisplay,
        ),
        resultDisplay,
        ItemStackSlotDisplay(createDisplayItem(ItemTypes.CRAFTING_TABLE)),
    )
}

private fun EnchantingRecipe.toCraftingRequirements(): List<MappedEntitySet<PacketItemType>> {
    val ingredientType = resolveIngredientPacketType()
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

private fun EnchantingRecipe.createDisplayItem(type: PacketItemType): PacketItemStack {
    return PacketItemStack.builder()
        .type(type)
        .amount(1)
        .build()
}

private fun EnchantingRecipe.createBookDisplayItem(level: Int): PacketItemStack {
    return createDisplayItem(ItemTypes.ENCHANTED_BOOK).apply {
        setComponent(
            ComponentTypes.CUSTOM_NAME,
            enchantment.displayName(level)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false),
        )
    }
}

private fun EnchantingRecipe.resolveIngredientPacketType(): PacketItemType {
    return SpigotConversionUtil.fromBukkitItemStack(createIngredientItem()).type
}

private fun createDisplay(
    targetDisplay: SlotDisplay<*>,
    resultDisplay: SlotDisplay<*>,
): ShapedCraftingRecipeDisplay {
    val slots = listOf(
        EmptySlotDisplay.INSTANCE,
        EmptySlotDisplay.INSTANCE,
        EmptySlotDisplay.INSTANCE,
        EmptySlotDisplay.INSTANCE,
        targetDisplay,
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
        ItemStackSlotDisplay(PacketItemStack.builder().type(ItemTypes.CRAFTING_TABLE).amount(1).build()),
    )
}

private fun mappedSet(type: PacketItemType): MappedEntitySet<PacketItemType> {
    return MappedEntitySet(mutableListOf(type))
}
