package net.azisaba.vanilife.npc

import net.azisaba.vanilife.Vanilife
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

interface NpcOwnerAccessor {
    var owner: UUID?

    val hasOwner: Boolean
        get() = owner != null

    fun isOwner(player: Player): Boolean = player.uniqueId == owner
}

internal class NpcOwnerAccessorImpl(private val dataHolder: PersistentDataHolder) : NpcOwnerAccessor {
    override var owner: UUID?
        get() = dataHolder.persistentDataContainer.get(OWNER_KEY, PersistentDataType.UUID)
        set(value) {
            if (value != null) {
                dataHolder.persistentDataContainer.set(OWNER_KEY, PersistentDataType.UUID, value)
            } else {
                dataHolder.persistentDataContainer.remove(OWNER_KEY)
            }
        }

    private companion object {
        val OWNER_KEY: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "owner")
    }
}
