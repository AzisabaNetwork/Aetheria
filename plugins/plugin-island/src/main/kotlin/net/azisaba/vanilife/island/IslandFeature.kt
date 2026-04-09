package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.leveling.LevelPredicate
import net.kyori.adventure.translation.Translatable

enum class IslandFeature(val requiredLevel: Int, private val translationKey: String) : Translatable {
    VISIT_OTHER_ISLANDS(10, "island.feature.visit_other_islands"),
    CUSTOM_SPAWN_POINT(20, "island.feature.custom_spawn_point"),
    EXPAND_STORAGE(30, "island.feature.expand_storage"),
    CUSTOM_SKY_COLOR(40, "island.feature.custom_sky_color"),
    FLIGHT(50, "island.feature.flight");

    override fun translationKey(): String = translationKey

    fun asLevelPredicate(): LevelPredicate = LevelPredicate.AtLeast(requiredLevel)
}
