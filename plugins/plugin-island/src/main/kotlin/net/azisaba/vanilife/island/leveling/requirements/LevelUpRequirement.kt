package net.azisaba.vanilife.island.leveling.requirements

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.island.Island
import kotlin.time.Duration

@Serializable
data class LevelUpRequirement(
    val minScore: Double,
    val minVisitors: Int,
    val minTotalStayTime: Duration,
) {
    fun test(island: Island): Boolean = testScore(island) && testVisitors(island) && testTotalStayTime(island)

    private fun testScore(island: Island): Boolean = island.score >= minScore

    private fun testVisitors(island: Island): Boolean = island.visitors.count() >= minVisitors

    private fun testTotalStayTime(island: Island): Boolean = island.totalStayTime >= minTotalStayTime
}
