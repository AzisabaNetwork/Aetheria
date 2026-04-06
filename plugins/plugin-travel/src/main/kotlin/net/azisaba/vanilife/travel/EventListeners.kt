package net.azisaba.vanilife.travel

import net.azisaba.vanilife.travel.listener.TravelTicketListener

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(TravelTicketListener, this)
}
