package net.azisaba.vanilife.npc

import org.bukkit.entity.Chicken
import java.util.concurrent.ConcurrentHashMap

internal class NpcContainer {
    private val npcByDelegate: MutableMap<Int, NpcWrapper> = ConcurrentHashMap()

    fun getByDelegate(delegate: Chicken): NpcWrapper? = getByDelegateId(delegate.entityId)

    fun getByDelegateId(delegateId: Int): NpcWrapper? = npcByDelegate[delegateId]

    fun put(npc: NpcWrapper) {
        val delegateId = npc.delegate.entityId
        npcByDelegate[delegateId] = npc
    }

    fun remove(npc: NpcWrapper) {
        val delegateId = npc.delegate.entityId
        npcByDelegate.remove(delegateId)
    }
}
