package net.azisaba.vanilife.islands.listener

import net.azisaba.vanilife.event.DragonBuffQueryEvent
import net.azisaba.vanilife.islands.DragonBuff
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffectType

internal class DefaultDragonBuffProvider : Listener {
    @EventHandler
    fun onDragonBuffQuery(event: DragonBuffQueryEvent) {
        // Default buffs: Damage Resistance (amplifier 1) and Haste (FAST_DIGGING, amplifier 1)
        event.accumulator.add(DragonBuff(PotionEffectType.DAMAGE_RESISTANCE, amplifier = 1))
        event.accumulator.add(DragonBuff(PotionEffectType.FAST_DIGGING, amplifier = 1))
    }
}
