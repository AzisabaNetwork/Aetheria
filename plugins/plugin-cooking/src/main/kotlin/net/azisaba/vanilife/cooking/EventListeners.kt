package net.azisaba.vanilife.cooking

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(CookingListener(), this)
}
