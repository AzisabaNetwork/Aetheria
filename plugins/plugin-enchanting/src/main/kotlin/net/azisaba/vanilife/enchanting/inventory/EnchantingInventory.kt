package net.azisaba.vanilife.enchanting.inventory

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import kotlinx.coroutines.delay
import net.azisaba.vanilife.enchanting.EnchantingFonts
import net.azisaba.vanilife.enchanting.EnchantingItemModels
import net.azisaba.vanilife.enchanting.EnchantingRecipe
import net.azisaba.vanilife.enchanting.recipebook.EnchantingRecipeBook
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class EnchantingInventory : InventoryHolder {
    var recipeBook: EnchantingRecipeBook = EnchantingRecipeBook.empty()
        private set

    private val inventory: Inventory = Bukkit.createInventory(this, InventoryType.WORKBENCH, TITLE)

    private var selectedRecipe: EnchantingRecipe = EnchantingRecipe.first()

    private var craftable: Boolean = false

    override fun getInventory(): Inventory = inventory

    fun selectRecipe(player: Player, plugin: Plugin, candidate: EnchantingRecipeBook.Candidate, windowId: Int?) {
        val targetItemStack = inventory.getItem(TARGET_SLOT)?.clone() ?: return
        ALL_CRAFT_SLOTS.forEach { slot ->
            val itemStack = inventory.getItem(slot) ?: return@forEach
            player.inventory.addItem(itemStack.clone()).values.forEach { leftover ->
                player.world.dropItemNaturally(player.location, leftover)
            }
            inventory.setItem(slot, null)
        }
        selectedRecipe = candidate.recipe
        val populated = tryPopulateRecipeInputs(player)
        updateResult(player)
        player.updateInventory()

        if (!populated) {
            val resolvedWindowId = windowId ?: player.openInventory.containerId
            recipeBook.sendPreview(player, resolvedWindowId, candidate, targetItemStack)
        }

        sync(plugin, player)
    }

    fun confirmCraft(player: Player): ItemStack? {
        val itemStack = inventory.getItem(TARGET_SLOT) ?: return null
        val recipe = resolveRecipe(itemStack) ?: return null
        if (!craftable) return null
        if (itemStack.type == Material.AIR) return null
        if (recipe.targetLevelFor(itemStack) == null) return null
        if (!ALL_CRAFT_SLOTS.all { slot ->
                val ingredientStack = inventory.getItem(slot) ?: return@all false
                ingredientStack.isSimilar(requiredItemForSlot(recipe, slot))
            }
        ) return null

        val resultLevel = recipe.targetLevelFor(itemStack) ?: return null
        val requiredLevel = recipe.requiredLevel(itemStack) ?: return null
        val result = recipe.createResultItem(itemStack, resultLevel)
        val location = player.location.clone().add(0.5, 1.0, 0.5)
        player.world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f)
        player.world.spawnParticle(Particle.ENCHANT, location, 24, 0.5, 0.5, 0.5, 0.0)
        player.giveExpLevels(-requiredLevel)
        inventory.setItem(RESULT_SLOT, null)
        ALL_CRAFT_SLOTS.forEach { inventory.setItem(it, null) }
        inventory.setItem(TARGET_SLOT, null)
        craftable = false
        return result
    }

    fun firstEmptyInputSlotFor(itemStack: ItemStack): Int? {
        return ALL_CRAFT_SLOTS.firstOrNull { slot ->
            isSlotEmpty(slot) && when (slot) {
                in INGREDIENT_SLOTS -> itemStack.isSimilar(selectedRecipe.createIngredientItem())
                in LAPIS_SLOTS -> itemStack.isSimilar(ItemStack(Material.LAPIS_LAZULI))
                else -> false
            }
        }
    }

    fun placeItemStack(slot: Int, itemStack: ItemStack): Boolean {
        if (!isSlotEmpty(slot)) return false
        val compatible = when (slot) {
            in INGREDIENT_SLOTS -> itemStack.isSimilar(selectedRecipe.createIngredientItem())
            in LAPIS_SLOTS -> itemStack.isSimilar(ItemStack(Material.LAPIS_LAZULI))
            else -> false
        }
        if (!compatible) return false
        inventory.setItem(slot, itemStack.clone().apply { amount = 1 })
        return true
    }

    fun sync(plugin: Plugin, player: Player) {
        plugin.launch(plugin.entityDispatcher(player)) {
            delay(1L.milliseconds)
            val targetItemStack = inventory.getItem(TARGET_SLOT)?.clone()
            val recipes = EnchantingRecipeBook.fromContext(player, targetItemStack)
            recipeBook = recipes
            updateResult(player)
            player.updateInventory()
            val user = PacketEvents.getAPI().playerManager.getUser(player)
            val view = player.openInventory
            val windowId = view.containerId
            val stateId = view.stateId

            for (slot in LAPIS_SLOTS) {
                if (!isSlotEmpty(slot)) {
                    continue
                }
                val itemStack = SpigotConversionUtil.fromBukkitItemStack(
                    ItemStack(Material.STICK).apply {
                        setData(
                            DataComponentTypes.TOOLTIP_DISPLAY,
                            TooltipDisplay.tooltipDisplay().hideTooltip(true).build(),
                        )
                        setData(DataComponentTypes.ITEM_MODEL, EnchantingItemModels.B)
                    },
                )
                user.sendPacket(WrapperPlayServerSetSlot(windowId, stateId, slot, itemStack))
            }
            recipes.sendRecipeBook(player)
        }
    }

    fun rollbackCrafting(player: Player) {
        (listOf(TARGET_SLOT) + ALL_CRAFT_SLOTS).forEach { slot ->
            val itemStack = inventory.getItem(slot) ?: return@forEach
            player.inventory.addItem(itemStack.clone()).values.forEach { leftover ->
                player.world.dropItemNaturally(player.location, leftover)
            }
            inventory.setItem(slot, null)
        }
        inventory.setItem(RESULT_SLOT, null)
        craftable = false
    }

    private fun tryPopulateRecipeInputs(player: Player): Boolean {
        val ingredientTemplate = selectedRecipe.createIngredientItem()
        val lapisTemplate = ItemStack(Material.LAPIS_LAZULI)
        val missingIngredientSlots = INGREDIENT_SLOTS.filter { isSlotEmpty(it) }
        val missingLapisSlots = LAPIS_SLOTS.filter { isSlotEmpty(it) }
        val ingredientAmount = player.inventory.contents.filterNotNull().sumOf { itemStack ->
            if (itemStack.isSimilar(ingredientTemplate)) itemStack.amount else 0
        }
        val lapisAmount = player.inventory.contents.filterNotNull().sumOf { itemStack ->
            if (itemStack.isSimilar(lapisTemplate)) itemStack.amount else 0
        }

        if (ingredientAmount < missingIngredientSlots.size || lapisAmount < missingLapisSlots.size) {
            return false
        }

        for (slot in missingIngredientSlots) {
            if (!player.inventory.removeItem(ingredientTemplate.clone().apply { amount = 1 }).isEmpty()) {
                return false
            }
            inventory.setItem(slot, ingredientTemplate.clone())
        }
        for (slot in missingLapisSlots) {
            if (!player.inventory.removeItem(lapisTemplate.clone().apply { amount = 1 }).isEmpty()) {
                return false
            }
            inventory.setItem(slot, lapisTemplate.clone())
        }
        return true
    }

    private fun requiredItemForSlot(recipe: EnchantingRecipe, slot: Int): ItemStack {
        return when (slot) {
            in INGREDIENT_SLOTS -> recipe.createIngredientItem()
            in LAPIS_SLOTS -> ItemStack(Material.LAPIS_LAZULI)
            else -> error("Unsupported slot: $slot")
        }
    }

    private fun isSlotEmpty(slot: Int): Boolean {
        val current = inventory.getItem(slot) ?: return true
        return current.type == Material.AIR
    }

    private fun updateResult(player: Player) {
        val itemStack = inventory.getItem(TARGET_SLOT) ?: run {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }
        val recipe = resolveRecipe(itemStack) ?: run {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }
        selectedRecipe = recipe

        if (itemStack.type == Material.AIR || recipe.targetLevelFor(itemStack) == null) {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }

        if (!ALL_CRAFT_SLOTS.all { slot ->
                val ingredientStack = inventory.getItem(slot) ?: return@all false
                ingredientStack.isSimilar(requiredItemForSlot(recipe, slot))
            }
        ) {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }

        val level = recipe.targetLevelFor(itemStack) ?: run {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }
        val requiredLevel = recipe.requiredLevel(itemStack) ?: run {
            craftable = false
            inventory.setItem(RESULT_SLOT, null)
            return
        }
        val currentLevel = itemStack.getEnchantmentLevel(recipe.enchantment)
        inventory.setItem(
            RESULT_SLOT,
            recipe.createResultDisplayItem(
                itemStack,
                currentLevel,
                level,
                requiredLevel,
                player.level >= requiredLevel,
            )
        )
        craftable = player.level >= requiredLevel
    }

    private fun resolveRecipe(itemStack: ItemStack): EnchantingRecipe? {
        val ingredientStack = INGREDIENT_SLOTS
            .mapNotNull { slot -> inventory.getItem(slot) }
            .firstOrNull { it.type != Material.AIR }
            ?: return null

        return recipeBook.findVisibleRecipe(itemStack, ingredientStack)
    }

    companion object {
        const val RESULT_SLOT: Int = 0
        const val TARGET_SLOT: Int = 5

        val INGREDIENT_SLOTS: List<Int> = listOf(1, 3, 7, 9)
        val LAPIS_SLOTS: List<Int> = listOf(2, 4, 6, 8)
        val ALL_CRAFT_SLOTS: List<Int> = INGREDIENT_SLOTS + LAPIS_SLOTS

        val TITLE: Component = Component.text()
            .append(
                Component.text(EnchantingFonts.EnchantingIcons.ENCHANTING_TABLE, NamedTextColor.WHITE)
                    .font(EnchantingFonts.ENCHANTING_ICONS),
            )
            .appendSpace()
            .append(Component.translatable("container.enchant"))
            .build()
    }
}
