package net.azisaba.vanilife.npc.wrapper

import org.bukkit.entity.Chicken
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class NpcWrapperMap(private val map: ConcurrentMap<Int, NpcWrapper> = ConcurrentHashMap()) {
    fun byEntityId(delegateId: Int): NpcWrapper? = map[delegateId]

    fun byDelegate(delegate: Chicken): NpcWrapper? = byEntityId(delegate.entityId)

    fun register(npc: NpcWrapper) {
        val delegateId = npc.delegate.entityId
        map[delegateId] = npc
    }

    fun unregister(npc: NpcWrapper) {
        map.remove(npc.delegate.entityId)
    }
}
