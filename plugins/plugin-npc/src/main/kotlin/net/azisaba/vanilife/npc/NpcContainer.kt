package net.azisaba.vanilife.npc

import org.bukkit.entity.Chicken
import java.util.concurrent.ConcurrentHashMap

internal class NpcContainer {
    private val npcByDelegate: MutableMap<Int, Npc> = ConcurrentHashMap()

    fun getByDelegate(delegate: Chicken): Npc? = getByDelegateId(delegate.entityId)

    fun getByDelegateId(delegateId: Int): Npc? = npcByDelegate[delegateId]

    fun put(npc: Npc) {
        val delegateId = npc.delegate.entityId
        npcByDelegate[delegateId] = npc
    }

    fun remove(npc: Npc) {
        val delegateId = npc.delegate.entityId
        npcByDelegate.remove(delegateId)
    }
}
