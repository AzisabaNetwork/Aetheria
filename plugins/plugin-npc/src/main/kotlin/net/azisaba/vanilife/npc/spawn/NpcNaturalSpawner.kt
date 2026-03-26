package net.azisaba.vanilife.npc.spawn

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import io.papermc.paper.math.Position
import kotlinx.serialization.Serializable
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.getNearbyNPCsByType
import net.azisaba.vanilife.npc.spawn
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.random.Random

internal class NpcNaturalSpawner(private val config: Configuration, private val spawnRules: NpcSpawnRuleLoader) {
    private val positionFinder = NpcSpawnPositionFinder(config.positionFinder)

    fun tick(random: Random, world: World, plugin: Plugin) {
        if (config.maxSpawnsPerTick <= 0) return

        val relevantPlayers = world.players.filter { player ->
            !player.isDead && player.gameMode != GameMode.SPECTATOR
        }.takeIf { it.isNotEmpty() } ?: return

        for (npcType in NpcType.entries) {
            val spawnRule = spawnRules.get(npcType) ?: continue
            val npcTypeRandom = Random(random.nextLong() xor npcType.key.asString().hashCode().toLong())

            npcTypeTick(npcType, spawnRule, relevantPlayers, npcTypeRandom, world, plugin)
        }
    }

    private fun npcTypeTick(
        npcType: NpcType,
        spawnRule: NpcSpawnRule,
        relevantPlayers: List<Player>,
        random: Random,
        world: World,
        plugin: Plugin,
    ) {
        val prioritizedCells = spawnRule.prioritizedCandidateCells(
            relevantPlayers, config.activeChunkRadius, config.focusedChunkRadius, config.chunkSize, random,
        )

        for (cell in prioritizedCells.take(config.maxSpawnsPerTick)) {
            val sampledPositions = cell.sampleXZPositions(random, spawnRule.distribution, config.xzSamplesPerCell)
            val position = sampledPositions.first()
            val chunkX = position.blockX() shr config.chunkShift
            val chunkZ = position.blockZ() shr config.chunkShift

            plugin.launch(plugin.regionDispatcher(world, chunkX, chunkZ)) {
                trySpawnAt(npcType, spawnRule, sampledPositions, positionFinder, world)
            }
        }
    }

    private fun trySpawnAt(
        npcType: NpcType,
        spawnRule: NpcSpawnRule,
        sampledPositions: List<Position>,
        positionFinder: NpcSpawnPositionFinder,
        world: World,
    ) {
        val position = sampledPositions.firstNotNullOfOrNull { xzPosition ->
            val chunkX = xzPosition.blockX() shr config.chunkShift
            val chunkZ = xzPosition.blockZ() shr config.chunkShift
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                positionFinder.find(world, xzPosition, spawnRule)
            } else null
        } ?: return

        val coverageRadius = spawnRule.coverageRadius()
        if (world.getNearbyNPCsByType(npcType, position, coverageRadius, config.searchYRadius).isNotEmpty()) return

        world.spawn(position, npcType)
    }

    @Serializable
    data class Configuration(
        val activeChunkRadius: Int = 8,
        val focusedChunkRadius: Int = 4,
        val chunkShift: Int = 4,
        val chunkSize: Int = 16,
        val maxSpawnsPerTick: Int = 4,
        val searchYRadius: Double = 24.0,
        val xzSamplesPerCell: Int = 8,
        val positionFinder: NpcSpawnPositionFinder.Configuration = NpcSpawnPositionFinder.Configuration(),
    )
}
