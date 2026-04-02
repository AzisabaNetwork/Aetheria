package net.azisaba.vanilife.island

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.DatabaseConfiguration
import net.azisaba.vanilife.island.leveling.LevelingConfiguration
import net.azisaba.vanilife.island.wrack.WrackConfiguration

@Serializable
internal data class Configuration(
    val database: DatabaseConfiguration = DatabaseConfiguration(),
    val leveling: LevelingConfiguration = LevelingConfiguration(),
    val wrack: WrackConfiguration = WrackConfiguration(),
)