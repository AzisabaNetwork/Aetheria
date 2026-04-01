package net.azisaba.vanilife.menuprovider

import net.azisaba.vanilife.menuprovider.listener.TrashInventoryListener

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(TrashInventoryListener, this)
}
