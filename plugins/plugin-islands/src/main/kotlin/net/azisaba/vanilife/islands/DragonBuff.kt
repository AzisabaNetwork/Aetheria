package net.azisaba.vanilife.islands

import org.bukkit.potion.PotionEffectType

/**
 * Simple DTO describing a potion-like buff provided by Dragon feature.
 */
data class DragonBuff(
    val type: PotionEffectType,
    val amplifier: Int = 0,
    val durationTicks: Int = 200 // default 10s
)
