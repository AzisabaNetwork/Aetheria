package net.azisaba.vanilife.enchanting

import net.azisaba.vanilife.enchanting.listener.EnchantingTableListener
import net.azisaba.vanilife.enchanting.listener.UnlockRateSourceListener

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(EnchantingTableListener(), this)
    server.pluginManager.registerEvents(UnlockRateSourceListener, this)
}
