package net.azisaba.vanilife.npc.spawn

import io.papermc.paper.math.Position
import org.bukkit.entity.Player
import kotlin.random.Random

internal data class NpcSpawnCell(val x: Int, val z: Int) {
    fun sampleXZPositions(random: Random, distribution: NpcSpawnDistribution, sampleCount: Int): List<Position> {
        return buildList(sampleCount.coerceAtLeast(1)) {
            repeat(sampleCount.coerceAtLeast(1)) {
                add(nextXZPosition(distribution, random))
            }
        }
    }

    fun isWithinFocus(
        players: List<Player>, distribution: NpcSpawnDistribution, focusedChunkRadius: Int, chunkSize: Int,
    ): Boolean {
        val centerX = (x + 0.5) * distribution.spacing
        val centerZ = (z + 0.5) * distribution.spacing
        val focusRadiusBlocks = distribution.focusRadiusBlocks(focusedChunkRadius, chunkSize)
        val focusRadiusSquared = focusRadiusBlocks * focusRadiusBlocks

        return players.any { player ->
            val dx = player.location.x - centerX
            val dz = player.location.z - centerZ
            dx * dx + dz * dz <= focusRadiusSquared
        }
    }

    private fun nextXZPosition(distribution: NpcSpawnDistribution, random: Random): Position {
        val centerX = (x + 0.5) * distribution.spacing
        val centerZ = (z + 0.5) * distribution.spacing
        val candidateX = centerX + random.nextDouble(-distribution.jitter, distribution.jitter)
        val candidateZ = centerZ + random.nextDouble(-distribution.jitter, distribution.jitter)
        return Position.fine(candidateX, 0.0, candidateZ)
    }
}
