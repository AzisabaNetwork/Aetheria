package net.azisaba.vanilife.enchanting.listener

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCraftRecipeRequest
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSetDisplayedRecipe
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.enchanting.inventory.EnchantingInventory
import net.azisaba.vanilife.enchanting.EnchantingRecipe
import net.azisaba.vanilife.enchanting.recipebook.RecipeBookStates
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

internal class RecipeBookPacketListener(private val plugin: Plugin) : PacketListener {
    override fun onPacketSend(event: PacketSendEvent) {
        val player = event.safePlayer() ?: return
        val playerId = event.user.uuid
        if (RecipeBookStates.isCachingSuppressed(player)) {
            return
        }

        when (event.packetType) {
            PacketType.Play.Server.RECIPE_BOOK_SETTINGS -> {
                val packet = event.lastUsedWrapper as? WrapperPlayServerRecipeBookSettings ?: return
                RecipeBookStates.cacheSettings(playerId, packet.settings)
            }

            PacketType.Play.Server.RECIPE_BOOK_ADD -> {
                val packet = event.lastUsedWrapper as? WrapperPlayServerRecipeBookAdd ?: return
                val entries = packet.entries
                if (entries.any { entry -> EnchantingRecipe.toList().getOrNull(entry.contents.id.id) != null }) {
                    return
                }
                RecipeBookStates.cacheDisplay(playerId, entries, packet.isReplace)
            }
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val packetType = event.packetType
        if (packetType != PacketType.Play.Client.SET_DISPLAYED_RECIPE &&
            packetType != PacketType.Play.Client.CRAFT_RECIPE_REQUEST
        ) {
            return
        }

        val player = event.safePlayer() ?: return
        val (recipeId, windowId) = when (packetType) {
            PacketType.Play.Client.SET_DISPLAYED_RECIPE -> {
                WrapperPlayClientSetDisplayedRecipe(event).recipeId.id to null
            }

            PacketType.Play.Client.CRAFT_RECIPE_REQUEST -> {
                val packet = WrapperPlayClientCraftRecipeRequest(event)
                packet.recipeId.id to packet.windowId
            }

            else -> return
        }

        event.isCancelled = true
        plugin.launch(plugin.entityDispatcher(player)) {
            val holder = player.openInventory.topInventory.holder as? EnchantingInventory ?: return@launch
            val candidate = holder.recipeBook.candidate(recipeId) ?: return@launch
            holder.selectRecipe(player, plugin, candidate, windowId)
        }
    }

    private fun PacketSendEvent.safePlayer(): Player? {
        return runCatching { getPlayer<Any>() as? Player }.getOrNull()
    }

    private fun PacketReceiveEvent.safePlayer(): Player? {
        return runCatching { getPlayer<Any>() as? Player }.getOrNull()
    }
}
