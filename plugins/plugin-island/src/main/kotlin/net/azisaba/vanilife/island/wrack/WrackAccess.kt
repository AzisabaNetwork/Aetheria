package net.azisaba.vanilife.island.wrack

import net.azisaba.vanilife.island.enchantments.EnchantmentHolder
import org.bukkit.entity.Player

interface WrackAccess {
    fun addWrackViewer(player: Player)

    fun removeWrackViewer(player: Player)

    fun spawnWrack(wrackType: WrackType)

    suspend fun wrackTick(time: Long, level: Int, enchantments: EnchantmentHolder)
}
