package net.azisaba.vanilife.npc.listener

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.npc.spawn.NpcNaturalSpawner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

internal class NpcNaturalSpawnListener(
    private val spawnerReference: AtomicReference<NpcNaturalSpawner>, private val plugin: Plugin,
) : Listener {
    @EventHandler
    fun onServerTickStart(event: ServerTickStartEvent) {
        val world = Vanilife.getResourceWorld().takeIf { it.loadedChunks.isNotEmpty() } ?: return
        val random = Random(event.tickNumber)
        spawnerReference.get().tick(random, world, plugin)
    }
}
