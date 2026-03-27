package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.kyori.adventure.key.Key
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Chicken
import java.util.EnumSet

internal class VisitFlowerGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var targetFlower: Block? = null
    private var ticksUntilRepath: Int = 0
    private var cooldownTicksRemaining: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.VISIT_FLOWER

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining = tickCooldown(cooldownTicksRemaining)
            return false
        }
        targetFlower = findNearestFlower() ?: return false
        return true
    }

    override fun shouldStayActive(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        val flower = targetFlower ?: return false
        return flower.world == npcWrapper.delegate.world
            && isFlower(flower)
            && flower.location.distanceSquared(npcWrapper.location) <= MAX_TARGET_DISTANCE_SQUARED
    }

    override fun start() {
        ticksUntilRepath = 0
    }

    override fun stop() {
        targetFlower = null
        npcWrapper.delegate.pathfinder.stopPathfinding()
    }

    override fun tick() {
        val flower = targetFlower ?: return
        val targetLocation = flower.location.add(0.5, 0.5, 0.5)
        npcWrapper.delegate.lookAt(targetLocation)

        if (--ticksUntilRepath <= 0) {
            ticksUntilRepath = REPATH_INTERVAL_TICKS
            npcWrapper.delegate.pathfinder.moveTo(targetLocation, MOVE_SPEED)
        }

        if (npcWrapper.location.distanceSquared(targetLocation) > ARRIVAL_DISTANCE_SQUARED) return
        npcWrapper.delegate.pathfinder.stopPathfinding()
        spawnHappyParticle(targetLocation)
        cooldownTicksRemaining = FLOWER_COOLDOWN_TICKS
        stop()
    }

    private fun findNearestFlower(): Block? {
        val origin = npcWrapper.location.block
        var bestBlock: Block? = null
        var bestDistanceSquared = Double.MAX_VALUE

        for (offsetX in -SEARCH_RADIUS..SEARCH_RADIUS) {
            for (offsetY in -VERTICAL_SEARCH_RADIUS..VERTICAL_SEARCH_RADIUS) {
                for (offsetZ in -SEARCH_RADIUS..SEARCH_RADIUS) {
                    val block = origin.getRelative(offsetX, offsetY, offsetZ)
                    if (!isFlower(block)) continue

                    val distanceSquared = block.location.distanceSquared(npcWrapper.location)
                    if (distanceSquared >= bestDistanceSquared) continue

                    bestBlock = block
                    bestDistanceSquared = distanceSquared
                }
            }
        }

        return bestBlock
    }

    private fun isFlower(block: Block): Boolean {
        val blockType = block.type.asBlockType() ?: return false
        return FLOWER_BLOCKS.contains(blockType)
    }

    private fun spawnHappyParticle(targetLocation: org.bukkit.Location) {
        val particleLocation = targetLocation.clone().add(0.0, 0.4, 0.0)
        npcWrapper.delegate.world.spawnParticle(
            Particle.HAPPY_VILLAGER,
            particleLocation,
            3,
            0.2,
            0.2,
            0.2,
            0.0,
        )
    }

    private companion object {
        val FLOWER_BLOCKS = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.BLOCK)
            .getTagValues(TagKey.create(RegistryKey.BLOCK, Key.key("minecraft:flowers")))

        const val MOVE_SPEED: Double = 1.0
        const val SEARCH_RADIUS: Int = 6
        const val VERTICAL_SEARCH_RADIUS: Int = 2
        const val REPATH_INTERVAL_TICKS: Int = 10
        const val FLOWER_COOLDOWN_TICKS: Int = 100
        const val ARRIVAL_DISTANCE_SQUARED: Double = 0.81
        const val MAX_TARGET_DISTANCE_SQUARED: Double = 100.0
    }
}
