package net.azisaba.vanilife.npc.listener

import net.azisaba.vanilife.npc.NpcContainer
import org.bukkit.entity.Chicken
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Level

internal class NpcDeathListener(
    private val container: NpcContainer,
    private val plugin: Plugin,
) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val delegate = event.entity as? Chicken ?: return
        val npc = container.getByDelegate(delegate) ?: return
        val lastDamageCause = delegate.lastDamageCause
        val cause = lastDamageCause?.cause?.name ?: "UNKNOWN"
        val killer = delegate.killer?.name ?: "none"

        plugin.logger.log(
            Level.INFO,
            "NPC died: type={0} world={1} x={2} y={3} z={4} cause={5} killer={6}",
            arrayOf<Any>(
                npc.npcType.key.asString(),
                delegate.world.name,
                delegate.location.blockX,
                delegate.location.blockY,
                delegate.location.blockZ,
                cause,
                killer,
            ),
        )
    }
}
