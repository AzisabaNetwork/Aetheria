package net.azisaba.vanilife.island.leveling.requirements

import kotlinx.serialization.Serializable
import net.azisaba.vanilife.island.PlayerIsland
import kotlin.time.Duration

@Serializable
data class LevelUpRequirement(
    val minScore: Double,
    val minVisitors: Int,
    val minTotalStayTime: Duration,
) {
    fun test(island: PlayerIsland): Boolean = testScore(island) && testVisitors(island) && testTotalStayTime(island)

    private fun testScore(island: PlayerIsland): Boolean = island.score >= minScore

    private fun testVisitors(island: PlayerIsland): Boolean = island.visitors.count() >= minVisitors

    private fun testTotalStayTime(island: PlayerIsland): Boolean = island.totalStayTime >= minTotalStayTime
}
