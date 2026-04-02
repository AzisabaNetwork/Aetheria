package net.azisaba.vanilife.menuprovider

import net.azisaba.vanilife.menuprovider.listener.StorageInventoryListener
import net.azisaba.vanilife.menuprovider.listener.TrashInventoryListener

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(StorageInventoryListener, this)
    server.pluginManager.registerEvents(TrashInventoryListener, this)
}
