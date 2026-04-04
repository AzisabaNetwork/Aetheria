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
import net.azisaba.vanilife.enchanting.recipe.EnchantingRecipe
import net.azisaba.vanilife.enchanting.recipe.EnchantingRecipeBook
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
    private val inventory: Inventory = Bukkit.createInventory(this, InventoryType.WORKBENCH, TITLE)
    private var selectedRecipe: EnchantingRecipe = EnchantingRecipe.first()
    private var craftable: Boolean = false

    override fun getInventory(): Inventory = inventory

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun selectRecipe(recipe: EnchantingRecipe) {
        selectedRecipe = recipe
    }

    fun snapshotCenterItem(): ItemStack? = inventory.getItem(CENTER_SLOT)?.clone()

    fun prepareRecipe(): Boolean {
        updateResult()
        return craftable
    }

    fun tryPopulateRecipeInputs(player: Player): Boolean {
        val ingredientTemplate = selectedRecipe.createIngredientItem()
        val lapisTemplate = LAPIS_TEMPLATE.clone()
        val missingIngredientSlots = INGREDIENT_SLOTS.filter { isSlotEmpty(it) }
        val missingLapisSlots = LAPIS_SLOTS.filter { isSlotEmpty(it) }

        if (!hasPlayerItems(player, ingredientTemplate, missingIngredientSlots.size) ||
            !hasPlayerItems(player, lapisTemplate, missingLapisSlots.size)
        ) {
            return false
        }

        return fillMissingSlots(player, missingIngredientSlots, ingredientTemplate) &&
            fillMissingSlots(player, missingLapisSlots, lapisTemplate)
    }

    fun confirmCraft(player: Player): ItemStack? {
        if (!craftable || !hasRequiredIngredients()) return null

        val result = createResultItem() ?: return null
        playCraftEffect(player)
        clearResult()
        clearRecipeInputs()
        inventory.setItem(CENTER_SLOT, null)
        craftable = false
        return result
    }

    fun rollbackCrafting(player: Player) {
        restoreRecipeInputs(player)
        clearResult()
        craftable = false
    }

    fun isCraftInputSlot(slot: Int): Boolean = slot in ALL_CRAFT_SLOTS

    fun isInputSlot(slot: Int): Boolean = slot == CENTER_SLOT || isCraftInputSlot(slot)

    fun isPlaceholderSlot(slot: Int): Boolean = slot in LAPIS_SLOTS

    fun placeholderSlots(): List<Int> = LAPIS_SLOTS

    fun hasPlaceholderItem(slot: Int): Boolean = isPlaceholderSlot(slot) && isSlotEmpty(slot)

    fun createPlaceholderItem(slot: Int): ItemStack = when (slot) {
        in LAPIS_SLOTS -> ItemStack(Material.STICK).apply {
            setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay().hideTooltip(true).build()
            )
            setData(DataComponentTypes.ITEM_MODEL, EnchantingItemModels.B)
        }
        else -> error("Unsupported placeholder slot: $slot")
    }

    fun firstEmptyInputSlotFor(item: ItemStack): Int? {
        return ALL_CRAFT_SLOTS.firstOrNull { slot ->
            isSlotEmpty(slot) && isCompatibleWithSlot(slot, item)
        }
    }

    fun placeShiftItem(slot: Int, item: ItemStack): Boolean = placeItem(slot, item)

    fun sync(plugin: Plugin, player: Player) {
        plugin.launch(plugin.entityDispatcher(player)) {
            delay(1L.milliseconds)
            updateResult()
            player.updateInventory()
            syncPlaceholders(player)
            EnchantingRecipeBook.sync(player, snapshotCenterItem())
        }
    }

    private fun syncPlaceholders(player: Player) {
        val user = PacketEvents.getAPI().playerManager.getUser(player)
        val view = player.openInventory
        val windowId = view.containerId
        val stateId = view.stateId

        for (slot in placeholderSlots()) {
            if (!hasPlaceholderItem(slot)) {
                continue
            }
            val item = SpigotConversionUtil.fromBukkitItemStack(createPlaceholderItem(slot))
            user.sendPacket(WrapperPlayServerSetSlot(windowId, stateId, slot, item))
        }
    }

    private fun hasRequiredIngredients(): Boolean {
        val centerItem = inventory.getItem(CENTER_SLOT) ?: return false
        if (centerItem.type == Material.AIR) return false
        return ALL_CRAFT_SLOTS.all { slot ->
            val item = inventory.getItem(slot) ?: return@all false
            item.isSimilar(requiredItemForSlot(slot))
        }
    }

    private fun createResultItem(): ItemStack? {
        val centerItem = inventory.getItem(CENTER_SLOT) ?: return null
        if (centerItem.type == Material.AIR) return null
        return selectedRecipe.createResultItem(centerItem)
    }

    private fun clearRecipeInputs() {
        ALL_CRAFT_SLOTS.forEach { inventory.setItem(it, null) }
    }

    private fun restoreRecipeInputs(player: Player) {
        ALL_CRAFT_SLOTS.forEach { slot ->
            val item = inventory.getItem(slot) ?: return@forEach
            player.inventory.addItem(item.clone()).values.forEach { leftover ->
                player.world.dropItemNaturally(player.location, leftover)
            }
            inventory.setItem(slot, null)
        }
    }

    private fun clearResult() {
        inventory.setItem(RESULT_SLOT, null)
    }

    private fun requiredItemForSlot(slot: Int): ItemStack {
        return when (slot) {
            in INGREDIENT_SLOTS -> selectedRecipe.createIngredientItem()
            in LAPIS_SLOTS -> LAPIS_TEMPLATE.clone()
            else -> error("Unsupported slot: $slot")
        }
    }

    private fun isCompatibleWithSlot(slot: Int, item: ItemStack): Boolean = item.isSimilar(requiredItemForSlot(slot))

    private fun placeItem(slot: Int, item: ItemStack): Boolean {
        if (!isCompatibleWithSlot(slot, item) || !isSlotEmpty(slot)) return false
        inventory.setItem(slot, item.clone().apply { amount = 1 })
        return true
    }

    private fun isSlotEmpty(slot: Int): Boolean {
        val current = inventory.getItem(slot) ?: return true
        return current.type == Material.AIR
    }

    private fun fillMissingSlots(player: Player, slots: List<Int>, template: ItemStack): Boolean {
        for (slot in slots) {
            if (!removePlayerItem(player, template)) return false
            inventory.setItem(slot, template.clone())
        }
        return true
    }

    private fun hasPlayerItems(player: Player, template: ItemStack, amount: Int): Boolean {
        if (amount <= 0) return true
        val matchingAmount = player.inventory.contents.filterNotNull().sumOf { item ->
            if (item.isSimilar(template)) item.amount else 0
        }
        return matchingAmount >= amount
    }

    private fun removePlayerItem(player: Player, template: ItemStack): Boolean {
        return player.inventory.removeItem(template.clone().apply { amount = 1 }).isEmpty()
    }

    private fun updateResult() {
        craftable = hasRequiredIngredients()
        if (craftable) {
            inventory.setItem(RESULT_SLOT, createResultItem())
        } else {
            clearResult()
        }
    }

    private fun playCraftEffect(player: Player) {
        val location = player.location.clone().add(0.5, 1.0, 0.5)
        player.world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f)
        player.world.spawnParticle(Particle.ENCHANT, location, 24, 0.5, 0.5, 0.5, 0.0)
    }

    private companion object {
        const val RESULT_SLOT = 0
        const val CENTER_SLOT = 5
        val INGREDIENT_SLOTS = listOf(1, 3, 7, 9)
        val LAPIS_SLOTS = listOf(2, 4, 6, 8)
        val ALL_CRAFT_SLOTS = INGREDIENT_SLOTS + LAPIS_SLOTS
        private val LAPIS_TEMPLATE = ItemStack(Material.LAPIS_LAZULI)

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
