package net.azisaba.vanilife.enchanting.listener

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookSettings
import net.azisaba.vanilife.enchanting.RecipeBookStates
import org.bukkit.entity.Player

internal object RecipeBookPacketListener : PacketListener {
    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType != PacketType.Play.Server.RECIPE_BOOK_SETTINGS) {
            return
        }

        val player = event.getPlayer<Player>()
        val packet = event.lastUsedWrapper as? WrapperPlayServerRecipeBookSettings ?: return
        RecipeBookStates.cacheSettings(player, packet.settings)
    }
}
