package net.azisaba.vanilife.npc.listener

import net.azisaba.vanilife.npc.NpcContainer
import org.bukkit.entity.Chicken
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent

internal class NpcDelegateListener(private val container: NpcContainer) : Listener {
    @EventHandler
    fun onEntityRemove(event: EntityRemoveEvent) {
        val delegate = event.entity as? Chicken ?: return
        val npc = container.getByDelegate(delegate) ?: return
        container.remove(npc)
        npc.dispose()
    }
}
