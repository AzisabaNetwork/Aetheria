package net.azisaba.vanilife.island.listener

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.azisaba.vanilife.island.enchantment.EnchantingTableBehaviour
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

internal class EnchantingTableListener(private val behaviour: EnchantingTableBehaviour) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val enchantingTable = event.clickedBlock
            ?.takeIf { it.type == Material.ENCHANTING_TABLE }
            ?: return

        event.isCancelled = true
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        if (event.action.isRightClick) {
            val itemStack = event.item ?: return
            behaviour.use(event.player, enchantingTable, itemStack)
        } else if (event.action.isLeftClick) {
            behaviour.pickup(event.player, enchantingTable)
        }
    }

    @EventHandler
    fun onServerTickStart(event: ServerTickStartEvent) {
        behaviour.tick(event.tickNumber.toLong())
    }
}
