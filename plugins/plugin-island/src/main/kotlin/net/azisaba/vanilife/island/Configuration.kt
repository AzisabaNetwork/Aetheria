package net.azisaba.vanilife.island

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.island.leveling.LevelingConfiguration

@Serializable
internal data class Configuration(
    val database: DatabaseConfiguration = DatabaseConfiguration(),
    val leveling: LevelingConfiguration = LevelingConfiguration(),
)

@Serializable
internal data class DatabaseConfiguration(
    val url: String = "jdbc:postgresql://localhost:5432/vanilife",
    val usernameEnv: String = "DATABASE_USERNAME",
    val passwordEnv: String = "DATABASE_PASSWORD",
    val maxPoolSize: Int = 12,
)
