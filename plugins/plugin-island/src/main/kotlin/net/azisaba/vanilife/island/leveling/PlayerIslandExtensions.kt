package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.leveling.animation.LevelUpAnimator
import net.azisaba.vanilife.island.leveling.requirements.LevelUpRequirementProvider
import net.azisaba.vanilife.island.PlayerIsland
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal suspend fun PlayerIsland.tryLevelUp(requirements: LevelUpRequirementProvider, plugin: Plugin): Boolean {
    val currentLevel = level
    val nextLevel = currentLevel + 1
    if (nextLevel > Levellable.MAX_LEVEL) return false

    val requirement = requirements.requirementAt(nextLevel)

    return if (requirement.test(this)) {
        level(nextLevel)
        owner.id?.let(Bukkit::getPlayer)?.let { owner ->
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
