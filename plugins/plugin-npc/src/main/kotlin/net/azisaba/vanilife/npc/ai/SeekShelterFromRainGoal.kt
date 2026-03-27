package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Chicken
import java.util.EnumSet

internal class SeekShelterFromRainGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var shelterLocation: Location? = null
    private var ticksUntilRepath: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.SEEK_SHELTER_FROM_RAIN

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (!shouldAvoidRain()) return false
        shelterLocation = findNearbyShelter() ?: return false
        return true
    }

    override fun shouldStayActive(): Boolean {
        val shelter = shelterLocation ?: return false
        if (!npcWrapper.isFreeMode()) return false
        if (!npcWrapper.delegate.world.hasStorm()) return false
        return npcWrapper.location.distanceSquared(shelter) > ARRIVAL_DISTANCE_SQUARED
            || hasRoofAbove(npcWrapper.delegate.location.toCenterLocation().block)
    }

    override fun start() {
        ticksUntilRepath = 0
    }

    override fun stop() {
        shelterLocation = null
        npcWrapper.delegate.pathfinder.stopPathfinding()
        npcWrapper.standUp()
    }

    override fun tick() {
        val shelter = shelterLocation ?: return
        npcWrapper.delegate.lookAt(shelter)
        val currentBlock = npcWrapper.delegate.location.toCenterLocation().block
        if (hasRoofAbove(currentBlock)) {
            npcWrapper.delegate.pathfinder.stopPathfinding()
            npcWrapper.sitDown()
            return
        }

        npcWrapper.standUp()
        if (npcWrapper.location.distanceSquared(shelter) <= ARRIVAL_DISTANCE_SQUARED) {
            shelterLocation = findNearbyShelter()
            ticksUntilRepath = 0
            return
        }

        if (--ticksUntilRepath <= 0) {
            ticksUntilRepath = REPATH_INTERVAL_TICKS
            npcWrapper.delegate.pathfinder.moveTo(shelter, MOVE_SPEED)
        }
    }

    private fun shouldAvoidRain(): Boolean {
        val world = npcWrapper.delegate.world
        if (!world.hasStorm()) return false
        return !hasRoofAbove(npcWrapper.delegate.location.toCenterLocation().block)
    }

    private fun findNearbyShelter(): Location? {
        val origin = npcWrapper.location.block
        var bestLocation: Location? = null
        var bestScore = Double.MAX_VALUE

        for (offsetX in -SEARCH_RADIUS..SEARCH_RADIUS) {
            for (offsetZ in -SEARCH_RADIUS..SEARCH_RADIUS) {
                val candidate = findShelterInColumn(origin.x + offsetX, origin.z + offsetZ) ?: continue
                val score = scoreShelter(candidate)
                if (score >= bestScore) continue

                bestScore = score
                bestLocation = candidate
            }
        }

        return bestLocation
    }

    private fun findShelterInColumn(x: Int, z: Int): Location? {
        val world = npcWrapper.delegate.world
        val surfaceY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE)
        val maxBodyY = minOf(surfaceY + 1, npcWrapper.location.blockY + MAX_ASCEND_ABOVE_CURRENT_Y)
        val minBodyY = maxOf(world.minHeight + 1, surfaceY - MAX_SHELTER_DEPTH_BELOW_SURFACE)

        for (bodyY in maxBodyY downTo minBodyY) {
            val bodyBlock = world.getBlockAt(x, bodyY, z)
            val floorBlock = bodyBlock.getRelative(BlockFace.DOWN)
            val headBlock = bodyBlock.getRelative(BlockFace.UP)
            if (!isShelteredStandingPosition(floorBlock, bodyBlock, headBlock)) continue

            return bodyBlock.toCenterLocation()
        }

        return null
    }

    private fun isShelteredStandingPosition(
        floorBlock: Block,
        bodyBlock: Block,
        headBlock: Block,
    ): Boolean {
        if (!canStandAt(floorBlock, bodyBlock, headBlock)) return false
        if (!hasRoofAbove(bodyBlock)) return false
        return true
    }

    private fun hasRoofAbove(bodyBlock: Block): Boolean {
        val highestY = bodyBlock.world.getHighestBlockYAt(bodyBlock.x, bodyBlock.z, HeightMap.WORLD_SURFACE)
        return highestY > bodyBlock.y + 1
    }

    private fun scoreShelter(location: Location): Double {
        val horizontalDistanceSquared = horizontalDistanceSquared(npcWrapper.location, location)
        val verticalDistance = kotlin.math.abs(location.y - npcWrapper.location.y)
        return horizontalDistanceSquared + verticalDistance * VERTICAL_DISTANCE_WEIGHT
    }

    private fun horizontalDistanceSquared(a: Location, b: Location): Double {
        val deltaX = a.x - b.x
        val deltaZ = a.z - b.z
        return deltaX * deltaX + deltaZ * deltaZ
    }

    private companion object {
        const val MOVE_SPEED: Double = 1.15
        const val SEARCH_RADIUS: Int = 8
        const val REPATH_INTERVAL_TICKS: Int = 10
        const val ARRIVAL_DISTANCE_SQUARED: Double = 0.25
        const val MAX_SHELTER_DEPTH_BELOW_SURFACE: Int = 4
        const val MAX_ASCEND_ABOVE_CURRENT_Y: Int = 3
        const val VERTICAL_DISTANCE_WEIGHT: Double = 6.0
    }
}
