package net.azisaba.vanilife.enchanting

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import net.azisaba.vanilife.enchanting.listener.EnchantingInventoryListener
import net.azisaba.vanilife.enchanting.listener.EnchantingTableListener
import net.azisaba.vanilife.enchanting.listener.RecipeBookPacketListener
import net.azisaba.vanilife.enchanting.listener.UnlockRateSourceListener

internal fun Main.setupEventListeners() {
    server.pluginManager.registerEvents(EnchantingTableListener(), this)
    server.pluginManager.registerEvents(EnchantingInventoryListener, this)
    server.pluginManager.registerEvents(UnlockRateSourceListener, this)

    PacketEvents.getAPI().eventManager.registerListener(RecipeBookPacketListener, PacketListenerPriority.NORMAL)
}
