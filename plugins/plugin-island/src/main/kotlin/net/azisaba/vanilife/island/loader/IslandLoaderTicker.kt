package net.azisaba.vanilife.island.loader

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.azisaba.vanilife.ConfigurationHolder
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class IslandLoaderTicker(
    private val loader: IslandLoader,
    private val config: ConfigurationHolder<LoaderConfiguration>,
    plugin: Plugin,
) : AutoCloseable {
    private val job: Job = plugin.launch {
        var time = 0L
        while (isActive) {
            val interval = config.value().tickIntervalTicks.coerceAtLeast(1L)
            if (time % interval == 0L) {
                loader.tick()
            }

            time++
            delay(50L.milliseconds)
        }
    }

    override fun close() {
        job.cancel()
    }
}
