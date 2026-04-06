package net.azisaba.vanilife.island.loader

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
internal data class LoaderConfiguration(
    val enabled: Boolean = true,
    val minLevel: Int = 0,
    val maxIdle: Duration = 1.days,
    val queueCapacity: Int = 64,
    val loadPerTick: Int = 2,
    val tickIntervalTicks: Long = 20,
)
