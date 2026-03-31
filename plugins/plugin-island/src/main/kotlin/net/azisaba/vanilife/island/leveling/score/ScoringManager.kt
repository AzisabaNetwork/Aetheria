package net.azisaba.vanilife.island.leveling.score

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class ScoringManager {
    private val contextMap: ConcurrentMap<ScoreSource, ScoringContext> = ConcurrentHashMap()

    fun computeScore(source: ScoreSource): Double {
        val nowMillis = System.currentTimeMillis()
        var result: ScoringRule.Result? = null

        contextMap.compute(source) { _, oldContext ->
            val context = oldContext ?: ScoringContext(1.0, nowMillis)
            val computed = source.score(context, nowMillis)
            result = computed
            computed.nextContext
        }

        return result!!.score
    }
}
