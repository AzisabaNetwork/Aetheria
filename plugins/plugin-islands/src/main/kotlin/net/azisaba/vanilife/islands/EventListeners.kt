package net.azisaba.vanilife.islands

import net.azisaba.vanilife.islands.listener.IslandPlayerListener
import net.azisaba.vanilife.islands.portal.listener.IgniteListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(IslandPlayerListener(koin.get(), koin.get()), this)
    // register portal ignite listener if available in classpath
    try {
        server.pluginManager.registerEvents(IgniteListener(koin.get()), this)
    } catch (ignored: Exception) {
    }

    try {
        server.pluginManager.registerEvents(net.azisaba.vanilife.islands.portal.listener.PortalEventListener(this), this)
    } catch (ignored: Exception) {
    }
}
