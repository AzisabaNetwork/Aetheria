package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.listener.NpcDelegateListener
import net.azisaba.vanilife.npc.listener.NpcDeathListener
import net.azisaba.vanilife.npc.listener.NpcNaturalSpawnListener
import net.azisaba.vanilife.npc.listener.NpcUnloadCleanupListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(NpcDeathListener(koin.get(), koin.get()), this)
    server.pluginManager.registerEvents(NpcDelegateListener(koin.get()), this)
    server.pluginManager.registerEvents(NpcNaturalSpawnListener(koin.get()), this)
    server.pluginManager.registerEvents(NpcUnloadCleanupListener(), this)
}
