package net.azisaba.vanilife.island

fun interface IslandTicker<T : Island> {
    suspend fun tick(island: T, time: Long)
}
