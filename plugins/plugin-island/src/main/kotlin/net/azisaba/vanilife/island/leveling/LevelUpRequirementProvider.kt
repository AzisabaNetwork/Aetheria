package net.azisaba.vanilife.island.leveling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.azisaba.vanilife.island.Island
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration

@Serializable
sealed interface LevelUpRequirementProvider {
    fun requirementAt(level: Int): LevelUpRequirement

    @Serializable
    @SerialName("Constant")
    data class Constant(val value: LevelUpRequirement) : LevelUpRequirementProvider {
        override fun requirementAt(level: Int): LevelUpRequirement = value
    }

    @Serializable
    @SerialName("Linear")
    data class Linear(val min: LevelUpRequirement, val max: LevelUpRequirement) : LevelUpRequirementProvider {
        override fun requirementAt(level: Int): LevelUpRequirement {
            requireLevel(level)

            val p = progress(level)

            return LevelUpRequirement(
                minScore = lerp(min.minScore, max.minScore, p),
                minVisitors = lerpInt(min.minVisitors, max.minVisitors, p),
                minTotalStayTime = lerpDuration(min.minTotalStayTime, max.minTotalStayTime, p),
            )
        }
    }

    @Serializable
    @SerialName("EaseIn")
    data class EaseIn(
        val min: LevelUpRequirement, val max: LevelUpRequirement, val exponent: Double,
    ) : LevelUpRequirementProvider {
        init {
            require(exponent > 0) {
                "exponent must be > 0, but was $exponent"
            }
        }

        override fun requirementAt(level: Int): LevelUpRequirement {
            requireLevel(level)

            val p = progress(level)
            val t = p.pow(exponent)

            return LevelUpRequirement(
                minScore = lerp(min.minScore, max.minScore, t),
                minVisitors = lerpInt(min.minVisitors, max.minVisitors, t),
                minTotalStayTime = lerpDuration(min.minTotalStayTime, max.minTotalStayTime, t),
            )
        }
    }

    @Serializable
    @SerialName("EaseOut")
    data class EaseOut(
        val min: LevelUpRequirement, val max: LevelUpRequirement, val exponent: Double,
    ) : LevelUpRequirementProvider {
        init {
            require(exponent > 0) {
                "exponent must be > 0, but was $exponent"
            }
        }

        override fun requirementAt(level: Int): LevelUpRequirement {
            requireLevel(level)

            val p = progress(level)
            val t = 1 - (1 - p).pow(exponent)

            return LevelUpRequirement(
                minScore = lerp(min.minScore, max.minScore, t),
                minVisitors = lerpInt(min.minVisitors, max.minVisitors, t),
                minTotalStayTime = lerpDuration(min.minTotalStayTime, max.minTotalStayTime, t),
            )
        }
    }

    @Serializable
    @SerialName("EaseInOut")
    data class EaseInOut(
        val min: LevelUpRequirement, val max: LevelUpRequirement, val exponent: Double,
    ) : LevelUpRequirementProvider {
        init {
            require(exponent > 0) {
                "exponent must be > 0, but was $exponent"
            }
        }

        override fun requirementAt(level: Int): LevelUpRequirement {
            requireLevel(level)

            val p = progress(level)
            val t = if (p < 0.5) {
                0.5 * (2 * p).pow(exponent)
            } else {
                1 - 0.5 * (2 * (1 - p)).pow(exponent)
            }

            return LevelUpRequirement(
                minScore = lerp(min.minScore, max.minScore, t),
                minVisitors = lerpInt(min.minVisitors, max.minVisitors, t),
                minTotalStayTime = lerpDuration(min.minTotalStayTime, max.minTotalStayTime, t),
            )
        }
    }

    @Serializable
    @SerialName("Exponential")
    data class Exponential(
        val min: LevelUpRequirement, val max: LevelUpRequirement, val base: Double,
    ) : LevelUpRequirementProvider {
        init {
            require(base > 0 && abs(base - 1.0) > 1e-6) {
                "base must be > 0 and not approximately 1.0, but was $base"
            }
        }

        override fun requirementAt(level: Int): LevelUpRequirement {
            requireLevel(level)

            val p = progress(level)
            val t = if (p <= 0.0) 0.0 else (base.pow(p) - 1) / (base - 1)

            return LevelUpRequirement(
                minScore = lerp(min.minScore, max.minScore, t),
                minVisitors = lerpInt(min.minVisitors, max.minVisitors, t),
                minTotalStayTime = lerpDuration(min.minTotalStayTime, max.minTotalStayTime, t),
            )
        }
    }

    @Serializable
    @SerialName("Clamp")
    data class Clamp(
        val base: LevelUpRequirementProvider, val min: LevelUpRequirement?, val max: LevelUpRequirement?,
    ) : LevelUpRequirementProvider {
        override fun requirementAt(level: Int): LevelUpRequirement {
            val r = base.requirementAt(level)

            return LevelUpRequirement(
                minScore = r.minScore
                    .let { if (min != null) maxOf(it, min.minScore) else it }
                    .let { if (max != null) minOf(it, max.minScore) else it },
                minVisitors = r.minVisitors
                    .let { if (min != null) maxOf(it, min.minVisitors) else it }
                    .let { if (max != null) minOf(it, max.minVisitors) else it },
                minTotalStayTime = r.minTotalStayTime
                    .let { if (min != null) maxOf(it, min.minTotalStayTime) else it }
                    .let { if (max != null) minOf(it, max.minTotalStayTime) else it },
            )
        }
    }

    @Serializable
    @SerialName("Override")
    data class Override(
        val base: LevelUpRequirementProvider, val overrides: Map<Int, LevelUpRequirementProvider>,
    ) : LevelUpRequirementProvider {
        override fun requirementAt(level: Int): LevelUpRequirement {
            val provider = overrides[level] ?: base
            return provider.requirementAt(level)
        }
    }

    private companion object {
        const val START_LEVEL: Int = Island.MIN_LEVEL + 1

        fun requireLevel(level: Int) = require(level in START_LEVEL..Island.MAX_LEVEL) {
            "Level $level is out of range [$START_LEVEL..${Island.MAX_LEVEL}]"
        }

        fun progress(level: Int): Double {
            val range = Island.MAX_LEVEL - START_LEVEL
            if (range <= 0) return 1.0

            val raw = (level - START_LEVEL).toDouble() / range
            return raw.coerceIn(0.0, 1.0)
        }

        fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t

        fun lerpInt(a: Int, b: Int, t: Double): Int = (a + (b - a) * t).roundToInt()

        fun lerpDuration(a: Duration, b: Duration, t: Double): Duration = a + (b - a) * t
    }
}
