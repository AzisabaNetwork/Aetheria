package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Particle
import org.bukkit.entity.Animals
import org.bukkit.entity.Chicken
import org.bukkit.entity.Tameable
import java.util.EnumSet

internal class GrowNearbyBabyAnimalGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var targetAnimal: Animals? = null
    private var ticksUntilRepath: Int = 0
    private var cooldownTicksRemaining: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.GROW_BABY_ANIMAL

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining = tickCooldown(cooldownTicksRemaining)
            return false
        }
        targetAnimal = findNearestEligibleBabyAnimal() ?: return false
        return true
    }

    override fun shouldStayActive(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        val animal = targetAnimal ?: return false
        return isEligibleBabyAnimal(animal)
            && animal.world == npcWrapper.delegate.world
            && animal.location.distanceSquared(npcWrapper.location) <= MAX_TARGET_DISTANCE_SQUARED
    }

    override fun start() {
        ticksUntilRepath = 0
    }

    override fun stop() {
        targetAnimal = null
        npcWrapper.delegate.pathfinder.stopPathfinding()
    }

    override fun tick() {
        val animal = targetAnimal ?: return
        npcWrapper.delegate.lookAt(animal)

        if (--ticksUntilRepath <= 0) {
            ticksUntilRepath = REPATH_INTERVAL_TICKS
            npcWrapper.delegate.pathfinder.moveTo(animal, MOVE_SPEED)
        }

        if (npcWrapper.location.distanceSquared(animal.location) > ARRIVAL_DISTANCE_SQUARED) return

        npcWrapper.delegate.pathfinder.stopPathfinding()
        animal.setAdult()
        spawnGrowthParticles(animal)
        cooldownTicksRemaining = GROWTH_COOLDOWN_TICKS
        stop()
    }

    private fun findNearestEligibleBabyAnimal(): Animals? {
        return npcWrapper.delegate.world.getNearbyEntities(
            npcWrapper.location,
            SEARCH_RADIUS,
            VERTICAL_SEARCH_RADIUS,
            SEARCH_RADIUS,
        ) { entity ->
            val animal = entity as? Animals ?: return@getNearbyEntities false
            isEligibleBabyAnimal(animal)
        }.asSequence()
            .filterIsInstance<Animals>()
            .minByOrNull { it.location.distanceSquared(npcWrapper.location) }
    }

    private fun isEligibleBabyAnimal(animal: Animals): Boolean {
        if (animal.uniqueId == npcWrapper.delegate.uniqueId) return false
        if (!animal.isValid || animal.isDead) return false
        if (animal.isAdult) return false
        if (animal is Tameable && animal.isTamed) return false
        return true
    }

    private fun spawnGrowthParticles(animal: Animals) {
        val particleLocation = animal.location.clone().add(0.0, animal.height * 0.5, 0.0)
        animal.world.spawnParticle(
            Particle.HEART,
            particleLocation,
            3,
            0.15,
            0.15,
            0.15,
            0.02,
        )
    }

    private companion object {
        const val MOVE_SPEED: Double = 1.0
        const val SEARCH_RADIUS: Double = 8.0
        const val VERTICAL_SEARCH_RADIUS: Double = 4.0
        const val REPATH_INTERVAL_TICKS: Int = 10
        const val GROWTH_COOLDOWN_TICKS: Int = 120
        const val ARRIVAL_DISTANCE_SQUARED: Double = 4.0
        const val MAX_TARGET_DISTANCE_SQUARED: Double = 144.0
    }
}
