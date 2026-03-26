package net.azisaba.vanilife.npc.listener

import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.azisaba.vanilife.npc.wrapper.NpcWrapperMap
import org.bukkit.entity.Chicken
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent

internal class NpcDelegateListener(private val map: NpcWrapperMap) : Listener {
    @EventHandler
    fun onEntityRemove(event: EntityRemoveEvent) {
        val delegate = event.entity as? Chicken ?: return
        val npcWrapper = map.byDelegate(delegate) ?: return
        map.unregister(npcWrapper)
        npcWrapper.dispose()
    }

    @EventHandler
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        for (chicken in event.entities.filterIsInstance<Chicken>()) {
            NpcWrapper.tryWrap(chicken)?.apply(map::register)
        }
    }

    @EventHandler
    fun onEntitiesUnload(event: EntitiesUnloadEvent) {
        event.entities.filterIsInstance<Chicken>()
            .filter { it.persistentDataContainer.has(NpcWrapper.NPC_TYPE_KEY) }
            .mapNotNull(map::byDelegate)
            .forEach { npcWrapper ->
                map.unregister(npcWrapper)
                npcWrapper.dispose()
            }
    }
}
