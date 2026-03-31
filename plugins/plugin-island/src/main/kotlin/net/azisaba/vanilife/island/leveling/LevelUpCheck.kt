package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirementProvider

internal suspend fun Island.tryLevelUp(requirements: LevelUpRequirementProvider): Boolean {
    val nextLevel = level + 1
    if (nextLevel > Island.MAX_LEVEL) return false

    val requirement = requirements.requirementAt(nextLevel)

    return if (requirement.test(this)) {
        level(nextLevel)
        true
    } else false
}
