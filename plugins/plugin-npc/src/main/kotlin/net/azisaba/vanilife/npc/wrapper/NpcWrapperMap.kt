package net.azisaba.vanilife.npc.wrapper

import org.bukkit.entity.Chicken
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class NpcWrapperMap(private val map: ConcurrentMap<Int, NpcWrapper> = ConcurrentHashMap()) {
    fun byEntityId(entityId: Int): NpcWrapper? = map[entityId]

    fun byDelegate(chicken: Chicken): NpcWrapper? = byEntityId(chicken.entityId)

    fun isDelegate(chicken: Chicken): Boolean = chicken.entityId in map

    fun register(npc: NpcWrapper) {
        val delegateId = npc.delegate.entityId
        map[delegateId] = npc
    }

    fun unregister(npc: NpcWrapper) {
        map.remove(npc.delegate.entityId)
    }
}
