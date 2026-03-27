package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.GameMode
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import java.util.EnumSet
internal class GlanceAtNearbyPlayerGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    private var targetPlayer: Player? = null
    private var activeTicksRemaining: Int = 0
    private var cooldownTicksRemaining: Int = 0

    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.GLANCE_AT_PLAYER

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        if (!npcWrapper.isFreeMode()) return false
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining = tickCooldown(cooldownTicksRemaining)
            return false
        }
        val player = findNearestInterestingPlayer() ?: return false
        targetPlayer = player
        activeTicksRemaining = nextCooldownTicks(MIN_GLANCE_TICKS, MAX_GLANCE_TICKS)
        return true
    }

    override fun shouldStayActive(): Boolean {
        val player = targetPlayer ?: return false
        return npcWrapper.isFreeMode()
            && activeTicksRemaining > 0
            && canGlanceAt(player)
    }

    override fun stop() {
        targetPlayer = null
        cooldownTicksRemaining = nextCooldownTicks(MIN_COOLDOWN_TICKS, MAX_COOLDOWN_TICKS)
    }

    override fun tick() {
        val player = targetPlayer ?: return
        activeTicksRemaining--
        npcWrapper.delegate.lookAt(player)
    }

    private fun findNearestInterestingPlayer(): Player? {
        return npcWrapper.delegate.world.players.asSequence()
            .filter(::canGlanceAt)
            .minByOrNull { it.location.distanceSquared(npcWrapper.location) }
    }

    private fun canGlanceAt(player: Player): Boolean {
        if (!player.isOnline || player.isDead || !player.isValid) return false
        if (player.gameMode == GameMode.SPECTATOR) return false
        if (player.world != npcWrapper.delegate.world) return false
        if (player.location.distanceSquared(npcWrapper.location) > MAX_DISTANCE_SQUARED) return false
        return true
    }

    private companion object {
        const val MAX_DISTANCE_SQUARED: Double = 36.0
        const val MIN_GLANCE_TICKS: Int = 15
        const val MAX_GLANCE_TICKS: Int = 40
        const val MIN_COOLDOWN_TICKS: Int = 30
        const val MAX_COOLDOWN_TICKS: Int = 80
    }
}
