package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import java.util.EnumSet
import java.util.concurrent.ThreadLocalRandom

internal class WanderNearOwnerGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var targetLocation: Location? = null
    private var ticksUntilRepath: Int = 0
    private var cooldownTicksRemaining: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.WANDER_NEAR_OWNER

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining = tickCooldown(cooldownTicksRemaining)
            return false
        }
        val owner = resolveOwner() ?: return false
        if (!canWanderNearOwner(owner)) return false
        targetLocation = findWanderLocation(owner) ?: return false
        return true
    }

    override fun shouldStayActive(): Boolean {
        val owner = resolveOwner() ?: return false
        val destination = targetLocation ?: return false
        return npcWrapper.isFreeMode()
            && canWanderNearOwner(owner)
            && destination.world == npcWrapper.delegate.world
            && npcWrapper.location.distanceSquared(destination) > ARRIVAL_DISTANCE_SQUARED
    }

    override fun start() {
        ticksUntilRepath = 0
    }

    override fun stop() {
        targetLocation = null
        npcWrapper.delegate.pathfinder.stopPathfinding()
        cooldownTicksRemaining = nextCooldownTicks(MIN_COOLDOWN_TICKS, MAX_COOLDOWN_TICKS)
    }

    override fun tick() {
        val owner = resolveOwner() ?: return
        val destination = targetLocation ?: return
        npcWrapper.delegate.lookAt(owner)

        if (--ticksUntilRepath <= 0) {
            ticksUntilRepath = REPATH_INTERVAL_TICKS
            npcWrapper.delegate.pathfinder.moveTo(destination, MOVE_SPEED)
        }
    }

    private fun resolveOwner(): Player? = npcWrapper.owner?.let(Bukkit::getPlayer)

    private fun canWanderNearOwner(owner: Player): Boolean {
        if (!npcWrapper.hasOwner) return false
        if (!owner.isOnline || owner.isDead || !owner.isValid) return false
        if (owner.world != npcWrapper.delegate.world) return false
        if (npcWrapper.location.distanceSquared(owner.location) > OWNER_ACTIVITY_DISTANCE_SQUARED) return false
        return true
    }

    private fun findWanderLocation(owner: Player): Location? {
        val ownerBlock = owner.location.block
        repeat(MAX_WANDER_LOCATION_ATTEMPTS) {
            val offsetX = ThreadLocalRandom.current().nextInt(-OWNER_WANDER_RADIUS, OWNER_WANDER_RADIUS + 1)
            val offsetZ = ThreadLocalRandom.current().nextInt(-OWNER_WANDER_RADIUS, OWNER_WANDER_RADIUS + 1)
            if (offsetX == 0 && offsetZ == 0) return@repeat

            val floorBlock = owner.world.getBlockAt(
                ownerBlock.x + offsetX,
                owner.world.getHighestBlockYAt(ownerBlock.x + offsetX, ownerBlock.z + offsetZ) - 1,
                ownerBlock.z + offsetZ,
            )
            val bodyBlock = floorBlock.getRelative(BlockFace.UP)
            val headBlock = bodyBlock.getRelative(BlockFace.UP)
            if (!canStandAt(floorBlock, bodyBlock, headBlock)) return@repeat

            return bodyBlock.toCenterLocation()
        }
        return null
    }

    private companion object {
        const val MOVE_SPEED: Double = 1.0
        const val OWNER_WANDER_RADIUS: Int = 4
        const val OWNER_ACTIVITY_DISTANCE_SQUARED: Double = 144.0
        const val ARRIVAL_DISTANCE_SQUARED: Double = 1.5
        const val REPATH_INTERVAL_TICKS: Int = 10
        const val MAX_WANDER_LOCATION_ATTEMPTS: Int = 12
        const val MIN_COOLDOWN_TICKS: Int = 40
        const val MAX_COOLDOWN_TICKS: Int = 100
    }
}
