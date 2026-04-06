package net.azisaba.vanilife.travel.listener

import net.azisaba.vanilife.travel.TravelItems
import net.azisaba.vanilife.travel.dialog.TravelTicketDialog
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object TravelTicketListener : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return

        val ticket = event.item?.takeIf {
            it.isOf(TravelItems.TRAVEL_TICKET)
        } ?: return

        event.player.showDialog(TravelTicketDialog.create(event.player, ticket))
    }
}
