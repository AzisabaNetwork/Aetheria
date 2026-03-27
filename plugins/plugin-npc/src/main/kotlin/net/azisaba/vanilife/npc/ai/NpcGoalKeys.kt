package net.azisaba.vanilife.npc.ai

import com.destroystokyo.paper.entity.ai.GoalKey
import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.entity.Mob
import org.koin.core.component.KoinComponent

object NpcGoalKeys : KoinComponent {
    val FOLLOW_OWNER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/follow_owner"))
    val GLANCE_AT_PLAYER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/glance_at_player"))
    val GROW_BABY_ANIMAL: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/grow_baby_animal"))
    val HOP_AROUND: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/hop_around"))
    val LOOK_AT_OWNER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/look_at_owner"))
    val SEEK_SHELTER_FROM_RAIN: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/seek_shelter_from_rain"))
    val VISIT_FLOWER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/visit_flower"))
    val WANDER_NEAR_OWNER: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/wander_near_owner"))
    val SIT: GoalKey<Chicken> = GoalKey.of(Chicken::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/sit"))
    val TRADING: GoalKey<Mob> = GoalKey.of(Mob::class.java, NamespacedKey(Vanilife.NAMESPACE, "npc/trading"))
}
