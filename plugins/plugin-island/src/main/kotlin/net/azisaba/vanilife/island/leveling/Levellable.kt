package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.leveling.score.ScoreSource
import org.jetbrains.annotations.Range

interface Levellable {
    val level: Int

    val score: Double

    suspend fun level(level: @Range(from = MIN_LEVEL.toLong(), to = MAX_LEVEL.toLong()) Int)

    suspend fun score(score: Double)

    suspend fun updateScore(source: ScoreSource)

    suspend fun resetScore() {
        score(0.0)
    }

    companion object {
        const val MIN_LEVEL: Int = 0
        const val MAX_LEVEL: Int = 50
    }
}
