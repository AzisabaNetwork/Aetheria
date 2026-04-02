package net.azisaba.vanilife.enchanting.listener

import net.azisaba.vanilife.enchanting.UnlockRateSource
import net.azisaba.vanilife.island.event.IslandEnchantmentAddEvent
import net.azisaba.vanilife.island.event.IslandEnchantmentRemoveEvent
import net.azisaba.vanilife.island.event.IslandInitEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal object UnlockRateSourceListener : Listener {
    @EventHandler
    fun onIslandInit(event: IslandInitEvent) {
        UnlockRateSource.handleIslandInit()
    }

    @EventHandler
    fun onIslandEnchantmentAdd(event: IslandEnchantmentAddEvent) {
        UnlockRateSource.handleEnchantmentAdd(event.enchantment)
    }

    @EventHandler
    fun onIslandEnchantmentRemove(event: IslandEnchantmentRemoveEvent) {
        UnlockRateSource.handleEnchantmentRemove(event.enchantment)
    }
}
