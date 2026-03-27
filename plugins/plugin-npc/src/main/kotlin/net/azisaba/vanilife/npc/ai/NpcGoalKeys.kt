package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.GoalKey
import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.entity.Mob
import org.koin.core.component.KoinComponent

object NpcGoalKeys : KoinComponent {
    val FOLLOW_OWNER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/follow_owner"))
    val SIT: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/sit"))
    val TRADING: GoalKey<Mob> = GoalKey.of(Mob::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/trading"))
}
