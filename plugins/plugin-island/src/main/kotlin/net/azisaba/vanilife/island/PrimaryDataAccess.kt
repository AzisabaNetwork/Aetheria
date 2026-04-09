package net.azisaba.vanilife.island

import net.kyori.adventure.text.Component
import org.bukkit.Location

interface PrimaryDataAccess {
    val displayName: Component

    val description: Component

    val defaultSpawnPoint: Location

    suspend fun displayName(displayName: Component)

    suspend fun description(description: Component)

    suspend fun defaultSpawnPoint(defaultSpawnPoint: Location)
}
