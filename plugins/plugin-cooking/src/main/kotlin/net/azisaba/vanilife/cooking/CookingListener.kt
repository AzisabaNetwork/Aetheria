package net.azisaba.vanilife.cooking

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.koin.java.KoinJavaComponent.inject

class CookingListener : Listener {
    private val plugin: Plugin by inject(Plugin::class.java)

    @EventHandler
    fun onPlayerConsume(event: PlayerItemConsumeEvent) {
        val item = event.item
        val itemKey = item.serverItemKey() ?: return

        val effects = CookingEffects.getEffectsForKey(itemKey) ?: run {
            plugin.slF4JLogger.info("No effects found for $itemKey")
            return
        }

        val player = event.player

        for (def in effects) {
            val pe = PotionEffect(def.potion, def.durationTicks, def.amplifier)
            player.addPotionEffect(pe)
            CookingEffectSource.add(player, def.hudKey, def.durationTicks)
        }
    }
}
