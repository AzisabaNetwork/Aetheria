package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Chicken
import org.bukkit.util.Vector
import java.util.EnumSet
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

internal class HopAroundGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var cooldownTicksRemaining: Int = 0
    private var hopVector: Vector? = null

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.HOP_AROUND

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.JUMP)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (!npcWrapper.delegate.isOnGround || npcWrapper.delegate.isInWater) return false
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining = tickCooldown(cooldownTicksRemaining)
            return false
        }
        hopVector = findHopVector() ?: return false
        return true
    }

    override fun shouldStayActive(): Boolean = false

    override fun start() {
        val vector = hopVector ?: return
        npcWrapper.delegate.pathfinder.stopPathfinding()
        npcWrapper.delegate.velocity = vector
        cooldownTicksRemaining = nextCooldownTicks(MIN_COOLDOWN_TICKS, MAX_COOLDOWN_TICKS)
    }

    override fun stop() {
        hopVector = null
    }

    private fun findHopVector(): Vector? {
        repeat(MAX_DIRECTION_ATTEMPTS) {
            val angle = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2)
            val horizontal = Vector(cos(angle), 0.0, sin(angle)).multiply(HORIZONTAL_SPEED)
            val target = npcWrapper.location.clone().add(horizontal)
            if (!isSafeHopTarget(target)) return@repeat
            return horizontal.setY(VERTICAL_SPEED)
        }
        return null
    }

    private fun isSafeHopTarget(location: Location): Boolean {
        val floorBlock = location.block.getRelative(BlockFace.DOWN)
        val bodyBlock = location.block
        val headBlock = bodyBlock.getRelative(BlockFace.UP)
        return canStandAt(floorBlock, bodyBlock, headBlock)
    }

    private companion object {
        const val HORIZONTAL_SPEED: Double = 0.18
        const val VERTICAL_SPEED: Double = 0.24
        const val MAX_DIRECTION_ATTEMPTS: Int = 8
        const val MIN_COOLDOWN_TICKS: Int = 40
        const val MAX_COOLDOWN_TICKS: Int = 100
    }
}
