package net.azisaba.vanilife.npc.spawn

import kotlinx.serialization.Serializable
import net.azisaba.serialization.KeySerializer
import net.kyori.adventure.key.Key

@Serializable
data class NpcSpawnRule(
    val biomes: List<@Serializable(with = KeySerializer::class) Key>,
    val distribution: NpcSpawnDistribution,
)

@Serializable
data class NpcSpawnDistribution(
    val spacing: Double,
    val minDistance: Int,
    val jitter: Double,
)
