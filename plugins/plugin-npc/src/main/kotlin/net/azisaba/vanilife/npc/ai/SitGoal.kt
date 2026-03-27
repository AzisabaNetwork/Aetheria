package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.NpcMode
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Bukkit
import org.bukkit.entity.Chicken
import java.util.*

internal class SitGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.SIT

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.JUMP)

    override fun shouldActivate(): Boolean = npcWrapper.mode == NpcMode.SIT
            && npcWrapper.delegate.isOnGround
            && !npcWrapper.delegate.isInWater

    override fun shouldStayActive(): Boolean = shouldActivate()

    override fun start() {
        npcWrapper.delegate.pathfinder.stopPathfinding()
        npcWrapper.sitDown()
    }

    override fun stop() {
        npcWrapper.standUp()
    }

    override fun tick() {
        npcWrapper.delegate.pathfinder.stopPathfinding()
        val ownerId = npcWrapper.owner ?: return
        val owner = Bukkit.getPlayer(ownerId) ?: return
        if (!owner.isOnline || owner.isDead || !owner.isValid) return
        if (owner.world != npcWrapper.delegate.world) return
        if (npcWrapper.location.distanceSquared(owner.location) > OWNER_LOOK_DISTANCE_SQUARED) return
        npcWrapper.delegate.lookAt(owner)
    }

    private companion object {
        const val OWNER_LOOK_DISTANCE_SQUARED: Double = 64.0
    }
}
