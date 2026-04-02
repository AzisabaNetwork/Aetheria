package net.azisaba.vanilife.enchanting

import net.azisaba.vanilife.enchanting.listener.UnlockRateSourceListener
import org.koin.core.Koin

internal fun Main.setupEventListeners(koin: Koin) {
    server.pluginManager.registerEvents(UnlockRateSourceListener, this)
}
