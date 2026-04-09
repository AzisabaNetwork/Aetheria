package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.listener.FlightListener
import net.azisaba.vanilife.island.listener.PlayerListener
import net.azisaba.vanilife.island.listener.ScoreSourceListener
import net.azisaba.vanilife.island.listener.SpawnLocationListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(FlightListener(koin.get()), this)
    server.pluginManager.registerEvents(PlayerListener(koin.get()), this)
    server.pluginManager.registerEvents(ScoreSourceListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(SpawnLocationListener(koin.get(), koin.get()), koin.get())
}
