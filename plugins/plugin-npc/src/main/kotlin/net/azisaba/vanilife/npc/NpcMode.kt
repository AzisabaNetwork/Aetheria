package net.azisaba.vanilife.npc

import com.destroystokyo.paper.entity.ai.Goal
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.ai.FollowOwnerGoal
import net.azisaba.vanilife.npc.ai.SitGoal
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.translation.Translatable
import org.bukkit.entity.Chicken

enum class NpcMode(val key: Key) : Keyed, Translatable {
    FREE(Key.key(Vanilife.NAMESPACE, "free")),
    SIT(Key.key(Vanilife.NAMESPACE, "sit")) {
        override val goalPriority: Int = 2

        override fun createGoal(npcWrapper: NpcWrapper): Goal<Chicken> {
            return SitGoal(npcWrapper)
        }
    },
    FOLLOW(Key.key(Vanilife.NAMESPACE, "follow")) {
        override val goalPriority: Int = 4

        override fun createGoal(npcWrapper: NpcWrapper): Goal<Chicken> {
            return FollowOwnerGoal(npcWrapper)
        }
    };

    override fun key(): Key = key

    override fun translationKey(): String = "npc.mode.${key.value()}"

    open val goalPriority: Int? = null

    open fun createGoal(npcWrapper: NpcWrapper): Goal<Chicken>? = null

    companion object {
        private val BY_KEY: Map<Key, NpcMode> = entries.associateBy { it.key }

        fun byKey(key: Key): NpcMode? = BY_KEY[key]

        fun byValue(value: String): NpcMode? = entries.firstOrNull { it.key.value() == value }
    }
}
