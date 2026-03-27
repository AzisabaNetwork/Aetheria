package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import org.bukkit.Bukkit
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import java.util.EnumSet

internal class LookAtNearbyOwnerGoal(private val npcWrapper: NpcWrapper) : Goal<Chicken> {
    override fun getKey(): GoalKey<Chicken> = NpcGoalKeys.LOOK_AT_OWNER

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.LOOK)

    override fun shouldActivate(): Boolean {
        val owner = resolveOwner() ?: return false
        return npcWrapper.isFreeMode() && canLookAtOwner(owner)
    }

    override fun shouldStayActive(): Boolean {
        val owner = resolveOwner() ?: return false
        return npcWrapper.isFreeMode() && canLookAtOwner(owner)
    }

    override fun tick() {
        val owner = resolveOwner() ?: return
        npcWrapper.delegate.lookAt(owner)
    }

    private fun resolveOwner(): Player? = npcWrapper.owner?.let(Bukkit::getPlayer)

    private fun canLookAtOwner(owner: Player): Boolean {
        if (!npcWrapper.hasOwner) return false
        if (!owner.isOnline || owner.isDead || !owner.isValid) return false
        if (owner.world != npcWrapper.delegate.world) return false
        if (npcWrapper.location.distanceSquared(owner.location) > OWNER_LOOK_DISTANCE_SQUARED) return false
        return true
    }

    private companion object {
        const val OWNER_LOOK_DISTANCE_SQUARED: Double = 64.0
    }
}
