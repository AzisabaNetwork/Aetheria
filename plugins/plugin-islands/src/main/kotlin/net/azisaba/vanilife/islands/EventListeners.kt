package net.azisaba.vanilife.islands

import net.azisaba.vanilife.islands.listener.IslandPlayerListener
import net.azisaba.vanilife.islands.listener.DragonKillListener
import net.azisaba.vanilife.islands.listener.DefaultDragonBuffProvider
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(IslandPlayerListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(DragonKillListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(DefaultDragonBuffProvider(), this)
}
