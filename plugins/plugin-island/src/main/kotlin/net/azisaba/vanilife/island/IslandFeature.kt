package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.leveling.IslandLevelPredicate

enum class IslandFeature(val level: IslandLevelPredicate) {
    VISIT_OTHER_ISLANDS(IslandLevelPredicate.AtLeast(10)),
    CHANGE_SPAWN_POINT(IslandLevelPredicate.AtLeast(20)),
    EXPAND_BUILD_HEIGHT(IslandLevelPredicate.AtLeast(30)),
    CHANGE_SKY_COLOR(IslandLevelPredicate.AtLeast(40)),
    ENABLE_FLIGHT(IslandLevelPredicate.AtLeast(50));
}
