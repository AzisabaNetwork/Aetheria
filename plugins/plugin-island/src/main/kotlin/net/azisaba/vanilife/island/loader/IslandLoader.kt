package net.azisaba.vanilife.island.loader

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.azisaba.vanilife.island.IslandSource
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.jdbc.Database
import java.lang.AutoCloseable

internal class IslandLoader(val loaderType: IslandLoaderType, private val database: Database) : AutoCloseable {
    private val queue: ArrayDeque<IslandPosition> = ArrayDeque()
    private val mutex: Mutex = Mutex()

    private var job: Job? = null

    fun start(plugin: Plugin, islandSource: IslandSource) {
        check(job == null) { "Already started" }

        job = plugin.launch {
            fillQueue()

            while (isActive) {
                val processed = tick(islandSource)

                if (!processed) break

                delay(loaderType.tickInterval)
            }
        }
    }

    override fun close() {
        job?.cancel()
    }

    private suspend fun tick(islandSource: IslandSource): Boolean {
        var processedAny = false

        repeat(loaderType.maxLoadPerTick) {
            val position = pollPosition() ?: return@repeat
            islandSource.loadIsland(position)
            processedAny = true
        }

        return processedAny
    }

    private suspend fun pollPosition(): IslandPosition? = mutex.withLock {
        queue.removeFirstOrNull()
    }

    private suspend fun fillQueue() {
        val positions = loaderType.queryPositions(database).distinct()

        mutex.withLock {
            if (queue.isNotEmpty()) return
            queue.addAll(positions)
        }
    }
}
