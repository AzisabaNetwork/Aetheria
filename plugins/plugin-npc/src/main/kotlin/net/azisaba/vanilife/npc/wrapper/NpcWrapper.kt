package net.azisaba.vanilife.npc.wrapper

import kr.toxicity.model.api.bukkit.platform.BukkitEntity
import kr.toxicity.model.api.bukkit.platform.BukkitPlayer
import kr.toxicity.model.api.event.hitbox.HitBoxInteractEvent
import kr.toxicity.model.api.tracker.Tracker
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.NpcMode
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.ai.TradingGoal
import net.azisaba.vanilife.npc.dialog.NpcMenuDialog
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class NpcWrapper private constructor(
    val npcType: NpcType,
    internal val delegate: Chicken,
    internal val tracker: Tracker = npcType.modelOrThrow().create(BukkitEntity(delegate)),
) : Audience,
    NpcNameAccessor by NpcNameAccessorImpl(delegate),
    NpcModeAccessor by NpcModeAccessorImpl(delegate),
    NpcOwnerAccessor by NpcOwnerAccessorImpl(delegate),
    NpcMerchantAccessor by NpcMerchantAccessorImpl(npcType),
    NpcPoseAccessor by NpcPoseAccessorImpl(tracker) {
    val location: Location
        get() = delegate.location

    val velocity: Vector
        get() = delegate.velocity

    var isDisposed: Boolean = false
        private set

    init {
        delegate.isSilent = true
        delegate.eggLayTime = Int.MAX_VALUE
        delegate.setBreed(false)
        delegate.persistentDataContainer.set(NPC_TYPE_KEY, PersistentDataType.KEY, npcType.key)

        registerGoals()

        tracker.listenHitBox(HitBoxInteractEvent::class.java, ::handleHitBoxInteract)
    }

    fun dispose() {
        tracker.close()
        isDisposed = true
    }

    private fun registerGoals() {
        Bukkit.getMobGoals().addGoal(delegate, 3, TradingGoal(this, delegate, tracker))
        NpcMode.entries.forEach { npcMode ->
            val goal = npcMode.createGoal(this) ?: return@forEach
            val priority = npcMode.goalPriority ?: return@forEach
            Bukkit.getMobGoals().addGoal(delegate, priority, goal)
        }
    }

    private fun handleHitBoxInteract(event: HitBoxInteractEvent) {
        val player = (event.who as? BukkitPlayer)?.source() ?: return
        if (tryTame(player)) return
        if (!player.isSneaking || !isOwner(player)) return
        player.showDialog(NpcMenuDialog.create(this))
    }

    private fun tryTame(player: Player): Boolean {
        if (hasOwner) return false

        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type != Material.BREAD) return false

        tame(player)
        if (player.gameMode != GameMode.CREATIVE) {
            heldItem.amount -= 1
        }
        return true
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
