package net.azisaba.vanilife.npc.wrapper

import kr.toxicity.model.api.bukkit.platform.BukkitEntity
import kr.toxicity.model.api.tracker.Tracker
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.ai.SitGoal
import net.azisaba.vanilife.npc.ai.TradingGoal
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class NpcWrapper private constructor(
    val npcType: NpcType,
    internal val delegate: Chicken,
    private val tracker: Tracker = npcType.modelOrThrow().create(BukkitEntity(delegate)),
) : Audience,
    NpcMerchantAccessor by NpcMerchantAccessorImpl(npcType),
    NpcOwnerAccessor by NpcOwnerAccessorImpl(delegate),
    NpcPoseAccessor by NpcPoseAccessorImpl(tracker) {
    val location: Location
        get() = delegate.location

    val velocity: Vector
        get() = delegate.velocity

    var isDisposed: Boolean = false
        private set

    init {
        delegate.isSilent = true
        delegate.persistentDataContainer.set(NPC_TYPE_KEY, PersistentDataType.KEY, npcType.key)

        Bukkit.getMobGoals().addGoal(delegate, 3, TradingGoal(this, delegate, tracker))
        Bukkit.getMobGoals().addGoal(delegate, 2, SitGoal(this, delegate, tracker))
    }

    fun dispose() {
        tracker.close()
        isDisposed = true
    }

    internal companion object {
        val NPC_TYPE_KEY: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "npc")

        fun wrap(npcType: NpcType, delegate: Chicken): NpcWrapper = NpcWrapper(npcType, delegate)

        fun tryWrap(delegate: Chicken): NpcWrapper? {
            val npcType = delegate.persistentDataContainer.get(NPC_TYPE_KEY, PersistentDataType.KEY)
                ?.let(NpcType::byKey) ?: return null
            return NpcWrapper(npcType, delegate)
        }
    }
}
