package net.azisaba.vanilife.npc.listener

import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.azisaba.vanilife.npc.wrapper.NpcWrapperMap
import org.bukkit.entity.Chicken
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEnterLoveModeEvent
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent

internal class NpcDelegateListener(private val map: NpcWrapperMap) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val chicken = event.entity as? Chicken ?: return
        if (map.isDelegate(chicken)) {
            event.drops.clear()
            event.droppedExp = 0
        }
    }

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

    @EventHandler
    fun onEntityEnterLoveMode(event: EntityEnterLoveModeEvent) {
        val chicken = event.entity as? Chicken ?: return
        if (map.isDelegate(chicken)) event.isCancelled = true
    }

    @EventHandler
    fun onEntityBreed(event: EntityBreedEvent) {
        val mother = event.mother as? Chicken
        if (mother != null && map.isDelegate(mother)) {
            event.isCancelled = true
            return
        }
        val father = event.father as? Chicken
        if (father != null && map.isDelegate(father)) {
            event.isCancelled = true
        }
    }
}
