package net.azisaba.vanilife.island

interface IslandFeatureHolder {
    val level: Int

    val features: Set<IslandFeature>
        get() = IslandFeature.entries.filter(::isEnabled).toSet()

    val storageSize: Int
        get() = if (isEnabled(IslandFeature.EXPAND_STORAGE)) 54 else 18

    fun isEnabled(feature: IslandFeature): Boolean = feature.requiredLevel <= level
}
