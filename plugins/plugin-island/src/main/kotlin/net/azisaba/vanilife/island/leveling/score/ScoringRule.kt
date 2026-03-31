package net.azisaba.vanilife.island.leveling.score

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class ScoringRule(
    val baseScore: Double,
    val minMultiplier: Double,
    val decayCount: Int,
    val recoveryDuration: Duration,
) {
    fun calculate(context: ScoringContext, nowMillis: Long): Result {
        val elapsedSinceLastUpdated = (nowMillis - context.lastUpdatedAtMillis)
            .coerceAtLeast(0)
            .milliseconds
        val recoveredMultiplier = recoveredMultiplier(context.multiplier, elapsedSinceLastUpdated)
        val score = baseScore * recoveredMultiplier
        val nextMultiplier = decayedMultiplier(recoveredMultiplier)
        return Result(score, ScoringContext(nextMultiplier, nowMillis))
    }

    private fun recoveredMultiplier(currentMultiplier: Double, elapsedSinceLastUpdated: Duration): Double {
        if (recoveryDuration == Duration.ZERO) {
            return 1.0
        }

        val clampedMultiplier = clampMultiplier(currentMultiplier)
        val recoveryProgress = progressWithin(elapsedSinceLastUpdated, recoveryDuration)
        return clampedMultiplier + (1.0 - clampedMultiplier) * recoveryProgress
    }

    private fun decayedMultiplier(currentMultiplier: Double): Double {
        if (decayCount <= 0) {
            return minMultiplier
        }

        val clampedMultiplier = clampMultiplier(currentMultiplier)
        val decayAmount = (1.0 - minMultiplier) / decayCount
        return clampMultiplier(clampedMultiplier - decayAmount)
    }

    private fun clampMultiplier(multiplier: Double): Double = multiplier.coerceIn(minMultiplier, 1.0)

    private fun progressWithin(elapsed: Duration, duration: Duration): Double = (elapsed / duration).coerceIn(0.0, 1.0)

    data class Result(val score: Double, val nextContext: ScoringContext)
}
