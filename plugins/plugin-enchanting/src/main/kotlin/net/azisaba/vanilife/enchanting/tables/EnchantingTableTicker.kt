package net.azisaba.vanilife.enchanting.tables

import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.milliseconds

internal class EnchantingTableTicker(plugin: Plugin) : AutoCloseable {
    private val job: Job = plugin.launch {
        var tick = 0L
        while (isActive) {
            EnchantingTableBehaviour.tick(tick, plugin)
            tick++
            delay(50L.milliseconds)
        }
    }

    override fun close() {
        job.cancel()
    }
}
