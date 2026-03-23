package net.azisaba.vanilife.islands.listener

import net.azisaba.vanilife.event.DragonBuffQueryEvent
import net.azisaba.vanilife.event.DragonBuff
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffectType
import org.bukkit.Registry

internal class DefaultDragonBuffProvider : Listener {
    @EventHandler
    fun onDragonBuffQuery(event: DragonBuffQueryEvent) {
        // Default buffs: Resistance + Haste
        Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("resistance"))?.let {
            event.accumulator.add(DragonBuff(it, 1))
        }
        Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("haste"))?.let {
            event.accumulator.add(DragonBuff(it, 1))
        }
    }
}
