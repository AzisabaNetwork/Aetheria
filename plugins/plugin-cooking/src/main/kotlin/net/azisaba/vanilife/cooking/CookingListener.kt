package net.azisaba.vanilife.cooking

import net.azisaba.vanilife.Vanilife
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect

class CookingListener : Listener {
    @EventHandler
    fun onPlayerConsume(event: PlayerItemConsumeEvent) {
        val item = event.item ?: return
        val key = try {
            item.type.key().value()
        } catch (e: Exception) {
            return
        }

        val effects = CookingEffects.getEffectsForKey(key) ?: return
        val player = event.player

        for (def in effects) {
            val pe = PotionEffect(def.potion, def.durationTicks, def.amplifier)
            player.addPotionEffect(pe)
            CookingEffectSource.add(player, def.hudKey, def.durationTicks)
        }
    }
}
