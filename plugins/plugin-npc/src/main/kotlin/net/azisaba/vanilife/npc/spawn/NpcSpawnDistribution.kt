package net.azisaba.vanilife.npc.spawn

import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
data class NpcSpawnDistribution(val spacing: Double, val minDistance: Int, val jitter: Double) {
    fun searchRadiusBlocks(activeChunkRadius: Int, chunkSize: Int): Double =
        activeChunkRadius * chunkSize + spacing

    fun focusRadiusBlocks(focusedChunkRadius: Int, chunkSize: Int): Double =
        focusedChunkRadius * chunkSize + spacing / 2.0

    fun coverageRadius(): Double = max(minDistance.toDouble(), spacing / 2.0)
}
