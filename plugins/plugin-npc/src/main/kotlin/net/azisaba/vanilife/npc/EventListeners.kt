package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.listener.NpcDelegateListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(NpcDelegateListener(koin.get()), this)
}
