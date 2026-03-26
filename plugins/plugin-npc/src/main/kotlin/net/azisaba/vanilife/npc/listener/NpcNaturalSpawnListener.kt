package net.azisaba.vanilife.npc.listener

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.spawn.NpcNaturalSpawner
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.random.Random

internal class NpcNaturalSpawnListener(
    private val spawner: NpcNaturalSpawner, private val world: World, private val plugin: Plugin,
) : Listener {
    @EventHandler
    fun onServerTickStart(event: ServerTickStartEvent) {
        val world = Vanilife.getResourceWorld().takeIf {
            it.loadedChunks.isNotEmpty()
        } ?: return

        spawner.tick(Random(event.tickNumber.toLong()), world, plugin)
    }
}
