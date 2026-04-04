package net.azisaba.vanilife.enchanting.recipe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.recipe.display.ShapedCraftingRecipeDisplay
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCraftRecipeResponse
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareRecipes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.delay
import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal object EnchantingRecipeBook {
    fun hide(player: Player) {
        RecipeBookStates.cacheDiscovered(player)
        RecipeBookStates.runWithoutCaching(player) {
            clear(player)
            RecipeBookStates.lookup(player)?.settings?.let { settings ->
                PacketEvents.getAPI().playerManager.getUser(player)
                    .sendPacket(WrapperPlayServerRecipeBookSettings(RecipeBookStates.createExpandedSettings(settings)))
            }
        }
    }

    fun sync(player: Player, centerItem: ItemStack?) {
        RecipeBookStates.runWithoutCaching(player) {
            val recipes = EnchantingRecipe.toList().withIndex().filter { (_, recipe) ->
                recipe.canApplyTo(centerItem)
            }
            if (recipes.isEmpty()) {
                clear(player)
            } else {
                show(player, recipes)
            }
        }
    }

    fun preview(player: Player, windowId: Int, display: ShapedCraftingRecipeDisplay) {
        PacketEvents.getAPI().playerManager.getUser(player).sendPacket(
            WrapperPlayServerCraftRecipeResponse(
                windowId,
                display
            )
        )
    }

    fun handleSelection(
        player: Player,
        plugin: Plugin,
        recipe: EnchantingRecipe,
        windowId: Int?,
        previewDisplay: ShapedCraftingRecipeDisplay,
    ) {
        plugin.launch(plugin.entityDispatcher(player)) {
            val holder = player.openInventory.topInventory.holder as? EnchantingInventory ?: return@launch
            holder.selectRecipe(recipe)
            val populated = holder.tryPopulateRecipeInputs(player)
            val craftable = holder.prepareRecipe()
            player.updateInventory()
            if (!populated || !craftable) {
                val resolvedWindowId = windowId ?: player.openInventory.containerId
                preview(player, resolvedWindowId, previewDisplay)
            }
            sync(player, holder.snapshotCenterItem())
        }
    }

    fun restore(player: Player, plugin: Plugin) {
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

    private fun clear(player: Player) {
        PacketEvents.getAPI().playerManager.getUser(player)
            .sendPacket(WrapperPlayServerRecipeBookAdd(emptyList(), true))
    }

    private fun show(player: Player, recipes: List<IndexedValue<EnchantingRecipe>>) {
        val user = PacketEvents.getAPI().playerManager.getUser(player)
        user.sendPacket(WrapperPlayServerDeclareRecipes(emptyMap(), emptyList()))
        user.sendPacket(WrapperPlayServerRecipeBookAdd(recipes.map { (index, recipe) ->
            recipe.toRecipeBookEntry(index)
        }, true))
    }
}
