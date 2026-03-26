package net.azisaba.vanilife.npc.spawn

import kotlinx.serialization.Serializable
import net.azisaba.serialization.KeySerializer
import net.kyori.adventure.key.Key
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import kotlin.math.floor
import kotlin.random.Random

@Serializable
data class NpcSpawnRule(
    val biomes: List<@Serializable(with = KeySerializer::class) Key>, val distribution: NpcSpawnDistribution,
) {
    internal fun supportsBiome(biome: Biome): Boolean = biome.key() in biomes

    internal fun coverageRadius(): Double = distribution.coverageRadius()

    internal fun prioritizedCandidateCells(
        players: List<Player>, activeChunkRadius: Int, focusedChunkRadius: Int, chunkSize: Int, random: Random,
    ): List<NpcSpawnCell> {
        val candidateCells = candidateCells(players, activeChunkRadius, chunkSize)
        val (focusedCells, fallbackCells) = candidateCells.partition { cell ->
            cell.isWithinFocus(players, distribution, focusedChunkRadius, chunkSize)
        }
        return focusedCells.shuffled(random) + fallbackCells.shuffled(random)
    }

    private fun candidateCells(players: List<Player>, activeChunkRadius: Int, chunkSize: Int): Set<NpcSpawnCell> {
        val candidateCells = mutableSetOf<NpcSpawnCell>()
        for (player in players) {
            val radiusBlocks = distribution.searchRadiusBlocks(activeChunkRadius, chunkSize)
            val minCellX = floor((player.location.x - radiusBlocks) / distribution.spacing).toInt()
            val maxCellX = floor((player.location.x + radiusBlocks) / distribution.spacing).toInt()
            val minCellZ = floor((player.location.z - radiusBlocks) / distribution.spacing).toInt()
            val maxCellZ = floor((player.location.z + radiusBlocks) / distribution.spacing).toInt()

            for (cellX in minCellX..maxCellX) {
                for (cellZ in minCellZ..maxCellZ) {
                    candidateCells += NpcSpawnCell(cellX, cellZ)
                }
            }
        }
        return candidateCells
    }
}
