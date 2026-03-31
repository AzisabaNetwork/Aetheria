package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirementProvider
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal suspend fun Island.tryLevelUp(requirements: LevelUpRequirementProvider, plugin: Plugin): Boolean {
    val currentLevel = level
    val nextLevel = currentLevel + 1
    if (nextLevel > Island.MAX_LEVEL) return false

    val requirement = requirements.requirementAt(nextLevel)

    return if (requirement.test(this)) {
        level(nextLevel)
        Bukkit.getPlayer(owner)?.let { owner ->
            LevelUpAnimator.animate(
                island = this,
                player = owner,
                newLevel = nextLevel,
                oldLevel = currentLevel,
                plugin,
            )
        }
        true
    } else false
}
