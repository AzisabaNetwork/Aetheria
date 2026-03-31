package net.azisaba.vanilife.island.wrack

import kotlinx.serialization.Serializable
import net.azisaba.serialization.IntProvider

@Serializable
internal data class WrackConfiguration(
    val spawnLimit: Int = 16,
    val spawnIntervalTicks: IntProvider = IntProvider.Constant(200),
)
