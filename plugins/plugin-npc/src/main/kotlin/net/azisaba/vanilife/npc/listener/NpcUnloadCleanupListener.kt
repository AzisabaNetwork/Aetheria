package net.azisaba.vanilife.npc.listener

import net.azisaba.vanilife.npc.NpcPersistentKeys
import org.bukkit.entity.Chicken
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.persistence.PersistentDataType

internal class NpcUnloadCleanupListener : Listener {
    @EventHandler
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        event.entities
            .filterIsInstance<Chicken>()
            .filter { chicken ->
                chicken.persistentDataContainer.has(NpcPersistentKeys.NPC_MARKER, PersistentDataType.BYTE)
            }
            .forEach { chicken ->
                chicken.remove()
            }
    }
}
