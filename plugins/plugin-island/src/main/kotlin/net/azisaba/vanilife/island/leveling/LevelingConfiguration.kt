package net.azisaba.vanilife.island.leveling

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirement
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirementProvider
import kotlin.time.Duration

@Serializable
data class LevelingConfiguration(
    val levelUpCheckIntervalTicks: Long = 20L * 60,
    val levelUpRequirements: LevelUpRequirementProvider = LevelUpRequirementProvider.Constant(
        LevelUpRequirement(minScore = 10.0, minVisitors = 0, minTotalStayTime = Duration.ZERO),
    ),
)
