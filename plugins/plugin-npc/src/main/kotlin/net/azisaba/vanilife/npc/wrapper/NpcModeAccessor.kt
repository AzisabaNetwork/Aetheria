package net.azisaba.vanilife.npc.wrapper

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.NpcMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.persistence.PersistentDataType

interface NpcModeAccessor {
    var mode: NpcMode
}

internal class NpcModeAccessorImpl(private val delegate: Chicken) : NpcModeAccessor {
    override var mode: NpcMode
        get() = delegate.persistentDataContainer.get(MODE_KEY, PersistentDataType.KEY)?.let(NpcMode::byKey)
            ?: NpcMode.FREE
        set(value) {
            delegate.persistentDataContainer.set(MODE_KEY, PersistentDataType.KEY, value.key)
        }

    private companion object {
        val MODE_KEY: NamespacedKey = NamespacedKey(Vanilife.NAMESPACE, "mode")
    }
}
