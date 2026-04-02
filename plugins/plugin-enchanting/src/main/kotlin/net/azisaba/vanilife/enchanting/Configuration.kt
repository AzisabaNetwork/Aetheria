package net.azisaba.vanilife.enchanting

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.DatabaseConfiguration

@Serializable
internal data class Configuration(
    val database: DatabaseConfiguration = DatabaseConfiguration()
)