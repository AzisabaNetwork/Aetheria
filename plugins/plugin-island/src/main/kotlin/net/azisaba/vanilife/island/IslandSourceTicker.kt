package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class IslandSourceTicker(private val islandSource: IslandSource) : AutoCloseable {
    private var job: Job? = null

    fun start(plugin: Plugin) {
        check(job == null) { "Already started" }

        job = plugin.launch {
            var time = 0L
            while (isActive) {
                islandSource.tickIslands(time++)
                delay(50L.milliseconds)
            }
        }
    }

    override fun close() {
        job?.cancel()
    }
}
