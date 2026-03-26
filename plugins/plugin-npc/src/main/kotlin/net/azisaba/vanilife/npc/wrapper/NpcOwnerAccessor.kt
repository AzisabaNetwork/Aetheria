package net.azisaba.vanilife.npc.wrapper

import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*

interface NpcOwnerAccessor {
    val owner: UUID?

    val hasOwner: Boolean
        get() = owner != null

    fun isOwner(player: Player): Boolean = player.uniqueId == owner

    fun tame(player: Player)

    fun untame()
}

internal class NpcOwnerAccessorImpl(private val delegate: Chicken) : NpcOwnerAccessor {
    override val owner: UUID?
        get() = delegate.persistentDataContainer.get(OWNER_KEY, PersistentDataType.UUID)

    init {
        delegate.isPersistent = hasOwner
    }

    override fun tame(player: Player) {
        delegate.isPersistent = true
        delegate.persistentDataContainer.set(
            OWNER_KEY,
            PersistentDataType.UUID,
            player.uniqueId,
        )

        spawnTamingParticles()
    }

    override fun untame() {
        delegate.isPersistent = false
        delegate.persistentDataContainer.remove(OWNER_KEY)
    }

    private fun spawnTamingParticles() {
        val particleLocation = delegate.location.add(0.0, 0.5, 0.0)
        delegate.world.spawnParticle(
            Particle.HEART,
            particleLocation,
            2,
            0.1,
            0.1,
            0.1,
            0.05,
        )
    }

    private companion object {
        val OWNER_KEY: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "owner")
    }
}
