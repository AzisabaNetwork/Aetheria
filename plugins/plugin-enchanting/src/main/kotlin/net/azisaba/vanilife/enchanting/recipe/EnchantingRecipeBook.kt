package net.azisaba.vanilife.enchanting.recipe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.recipe.display.ShapedCraftingRecipeDisplay
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCraftRecipeResponse
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareRecipes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.math.Position
import kotlinx.coroutines.delay
import net.azisaba.vanilife.enchanting.EnchantingTranslations
import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import net.azisaba.vanilife.island.getIslandAt
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal object EnchantingRecipeBook {
    fun hide(player: Player) {
        RecipeBookStates.cacheDiscovered(player)
        RecipeBookStates.runWithoutCaching(player) {
            display(player, emptyList())
            RecipeBookStates.lookup(player)?.settings?.let { settings ->
                PacketEvents.getAPI().playerManager.getUser(player)
                    .sendPacket(WrapperPlayServerRecipeBookSettings(RecipeBookStates.createExpandedSettings(settings)))
            }
        }
    }

    suspend fun sync(player: Player, centerItem: ItemStack?) {
        val recipes = visibleCandidates(player, centerItem)
        RecipeBookStates.runWithoutCaching(player) {
            display(player, recipes)
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
            val island = (player.world as? IslandsWorld)?.getIslandAt(Position.fine(player.location)) ?: return@launch
            if (!island.has(recipe.enchantment)) {
                return@launch
            }
            holder.selectRecipe(recipe)
            val populated = holder.tryPopulateRecipeInputs(player)
            holder.prepareRecipe(player)
            player.updateInventory()
            if (!populated) {
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

    internal fun display(player: Player, recipes: List<RecipeCandidate>) {
        val user = PacketEvents.getAPI().playerManager.getUser(player)
        if (recipes.isEmpty()) {
            user.sendPacket(WrapperPlayServerRecipeBookAdd(emptyList(), true))
        } else {
            user.sendPacket(WrapperPlayServerDeclareRecipes(emptyMap(), emptyList()))
            user.sendPacket(WrapperPlayServerRecipeBookAdd(recipes.map { recipe ->
                recipe.recipe.toRecipeBookEntry(recipe.index, recipe.level)
            }, true))
        }
    }

    suspend fun visibleCandidates(player: Player, centerItem: ItemStack?): List<RecipeCandidate> {
        val island = (player.world as? IslandsWorld)?.getIslandAt(Position.fine(player.location))
        return candidates(centerItem).filter { candidate ->
            island?.has(candidate.recipe.enchantment) != false
        }
    }

    fun findVisibleRecipe(centerCandidates: List<RecipeCandidate>, centerItem: ItemStack?, ingredientItem: ItemStack?): EnchantingRecipe? {
        return centerCandidates.firstOrNull { candidate ->
            candidate.recipe.matches(centerItem, ingredientItem)
        }?.recipe
    }

    private fun candidates(centerItem: ItemStack?): List<RecipeCandidate> {
        return EnchantingRecipe.toList().withIndex().mapNotNull { (index, recipe) ->
            val level = recipe.targetLevelFor(centerItem) ?: return@mapNotNull null
            RecipeCandidate(index, recipe, level)
        }
    }

    internal data class RecipeCandidate(
        val index: Int,
        val recipe: EnchantingRecipe,
        val level: Int,
    )
}
