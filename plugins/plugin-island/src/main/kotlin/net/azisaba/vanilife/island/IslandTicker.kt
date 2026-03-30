package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal class IslandTicker(plugin: Plugin, private val cacheMap: IslandCacheMap) : AutoCloseable {
    private val job: Job = plugin.launch {
        var time = 0L
        while (isActive) {
            for (island in cacheMap) {
                val tickLevel = island.tickLevel()
                if (time % tickLevel.tickInterval == 0L) {
                    tickLevel.tick(island, time)
                }
            }
            time++
            delay(50L)
        }
    }

    override fun close() {
        job.cancel()
    }

    private fun Island.tickLevel(): TickLevel = when {
        IslandPlayerMap.collect(this).isNotEmpty() -> TickLevel.ACTIVE
        Bukkit.getPlayer(owner) != null -> TickLevel.OWNER_ONLINE
        else -> TickLevel.IDLE
    }

    private enum class TickLevel(val tickInterval: Long) {
        ACTIVE(1L) {
            override suspend fun tick(island: Island, time: Long) {
                if (IslandPlayerMap.lookup(island.owner) === island) {
                    island.wrackTick(time)
                }
                island.waveTick(time)
            }
        },
        OWNER_ONLINE(3L) {
            override suspend fun tick(island: Island, time: Long) {
                island.wrackTick(time)
            }
        },
        IDLE(20L * 5) {
            override suspend fun tick(island: Island, time: Long) {
                island.wrackTick(time)
            }
        };

        abstract suspend fun tick(island: Island, time: Long)
    }
}
