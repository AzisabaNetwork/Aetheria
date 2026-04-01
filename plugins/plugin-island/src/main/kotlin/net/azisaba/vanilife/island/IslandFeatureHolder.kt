package net.azisaba.vanilife.island

interface IslandFeatureHolder {
    val level: Int

    val features: Set<IslandFeature>
        get() = IslandFeature.entries.filter { it.level.matches(level) }.toSet()

    fun isEnabled(feature: IslandFeature): Boolean = feature.level.matches(level)

    fun canVisitOtherIslands(): Boolean = isEnabled(IslandFeature.VISIT_OTHER_ISLANDS)

    fun canChangeSpawnPoint(): Boolean = isEnabled(IslandFeature.CHANGE_SPAWN_POINT)

    fun canExpandBuildHeight(): Boolean = isEnabled(IslandFeature.EXPAND_BUILD_HEIGHT)

    fun canChangeSkyColor(): Boolean = isEnabled(IslandFeature.CHANGE_SKY_COLOR)

    fun canFly(): Boolean = isEnabled(IslandFeature.ENABLE_FLIGHT)
}
