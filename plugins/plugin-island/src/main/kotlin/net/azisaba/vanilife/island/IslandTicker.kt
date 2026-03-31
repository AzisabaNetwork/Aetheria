package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirementProvider
import net.azisaba.vanilife.island.leveling.tryLevelUp
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class IslandTicker(
    private val plugin: Plugin,
    private val cacheMap: IslandCacheMap,
    private val levelUpRequirements: ConfigurationHolder<LevelUpRequirementProvider>,
    private val levelUpCheckIntervalTicks: ConfigurationHolder<Long>,
) : AutoCloseable {
    private val job: Job = plugin.launch {
        var time = 0L
        while (isActive) {
            for (island in cacheMap) {
                island.tick(time)
            }
            time++
            delay(50L.milliseconds)
        }
    }

    override fun close() {
        job.cancel()
    }

    private suspend fun Island.tick(time: Long) {
        val tickLevel = tickLevel()
        if (time % tickLevel.tickInterval == 0L) {
            tickLevel.tick(this, time)
        }

        if (time % levelUpCheckIntervalTicks.value() == 0L && IslandPlayerMap.lookup(owner) === this) {
            tryLevelUp(levelUpRequirements.value(), plugin)
        }
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
                    island.wrackTick(time, island.level)
                }
                island.waveTick(time)
            }
        },
        OWNER_ONLINE(3L) {
            override suspend fun tick(island: Island, time: Long) {
                island.wrackTick(time, island.level)
            }
        },
        IDLE(20L * 5) {
            override suspend fun tick(island: Island, time: Long) {
                island.wrackTick(time, island.level)
            }
        };

        abstract suspend fun tick(island: Island, time: Long)
    }
}
