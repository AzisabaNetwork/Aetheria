package net.azisaba.vanilife.island.wrack

import io.papermc.paper.math.Position
import net.azisaba.vanilife.island.CoastSide
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import net.azisaba.vanilife.island.boundaryBlock
import org.bukkit.World
import org.bukkit.plugin.Plugin
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class DriftPath(val startPos: Position, val endPos: Position, val random: Random) {
    private val horizontalAmplitude: Double = HORIZONTAL_AMPLITUDE

    private val frequency: Double = 2.0

    private val phase: Double = random.nextDouble(0.0, PI * 2)

    fun computePos(progress: Double): Position {
        val t = progress.coerceIn(0.0, 1.0)

        val baseX = lerp(startPos.x(), endPos.x(), t)
        val baseZ = lerp(startPos.z(), endPos.z(), t)

        val dirX = endPos.x() - startPos.x()
        val dirZ = endPos.z() - startPos.z()
        val length = hypot(dirX, dirZ)

        if (length == 0.0) return startPos

        val normDirX = dirX / length
        val normDirZ = dirZ / length

        val orthoX = -normDirZ
        val orthoZ = normDirX

        val envelope = sin(t * PI)

        val horizontalWave = sin(t * PI * frequency + phase)
        val horizontalOffset = horizontalWave * horizontalAmplitude * envelope

        val verticalWave = sin(t * PI * (frequency * 0.7) + phase * 0.5)
        val verticalProgress = ((verticalWave * envelope) + 1.0) * 0.5

        val finalX = baseX + orthoX * horizontalOffset
        val finalZ = baseZ + orthoZ * horizontalOffset
        val finalY = lerp(IslandsWorld.SEA_LEVEL.toDouble(), IslandsWorld.SEA_LEVEL - 1.0, verticalProgress)

        return Position.fine(finalX, finalY, finalZ)
    }

    private fun lerp(a: Double, b: Double, t: Double): Double {
        return a + (b - a) * t
    }

    companion object {
        private const val HORIZONTAL_AMPLITUDE: Double = 6.0

        suspend fun random(islandPosition: IslandPosition, coastSide: CoastSide, world: World, plugin: Plugin): DriftPath {
            val salt = System.nanoTime()
            val random = Random(islandPosition.computeSeed(world.seed) xor coastSide.ordinal.toLong() xor salt)

            val landFinder = LandFinder(random)

            val driftY = IslandsWorld.SEA_LEVEL.toDouble()

            val minX = islandPosition.minBlockX().toDouble()
            val maxX = islandPosition.maxBlockX().toDouble()
            val minZ = islandPosition.minBlockZ().toDouble()
            val maxZ = islandPosition.maxBlockZ().toDouble()

            val boundary = islandPosition.boundaryBlock(coastSide).toDouble()
            val rawEndX = if (coastSide.axisX) boundary else random.nextDouble(minX, maxX)
            val rawEndZ = if (coastSide.axisZ) boundary else random.nextDouble(minZ, maxZ)
            val rawEndPos = Position.fine(rawEndX, driftY, rawEndZ)
            val finalEndPos = landFinder.find(world, rawEndPos, plugin) ?: rawEndPos

            val normX = if (coastSide.axisX) coastSide.coastNormalSign else 0.0
            val normZ = if (coastSide.axisZ) coastSide.coastNormalSign else 0.0

            val tanX = if (coastSide.axisZ) 1.0 else 0.0
            val tanZ = if (coastSide.axisX) 1.0 else 0.0

            val seaOffset = 32.0 * (0.5 + random.nextDouble())

            val startX = finalEndPos.x() + normX * seaOffset + tanX * random.nextDouble(
                -32.0,
                32.0
            )
            val startZ = finalEndPos.z() + normZ * seaOffset + tanZ * random.nextDouble(
                -32.0,
                32.0
            )

            return DriftPath(
                Position.fine(startX, driftY, startZ),
                finalEndPos,
                random
            )
        }
    }
}
