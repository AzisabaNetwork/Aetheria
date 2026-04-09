package net.azisaba.vanilife.island.leveling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface LevelPredicate {
    val min: Int

    fun matches(level: Int): Boolean

    @Serializable
    @SerialName("Exact")
    data class Exact(val value: Int) : LevelPredicate {
        override val min: Int = value

        override fun matches(level: Int): Boolean = level == value
    }

    @Serializable
    @SerialName("AtLeast")
    data class AtLeast(override val min: Int) : LevelPredicate {
        override fun matches(level: Int): Boolean = level >= min
    }

    @Serializable
    @SerialName("AtMost")
    data class AtMost(val max: Int) : LevelPredicate {
        override val min: Int = Levellable.MIN_LEVEL

        override fun matches(level: Int): Boolean = level <= max
    }

    @Serializable
    @SerialName("Between")
    data class Between(override val min: Int, val max: Int) : LevelPredicate {
        override fun matches(level: Int): Boolean = level in min..max
    }

    @Serializable
    @SerialName("Always")
    object Always : LevelPredicate {
        override val min: Int = Levellable.MIN_LEVEL

        override fun matches(level: Int): Boolean = true
    }

    @Serializable
    @SerialName("AnyOf")
    data class AnyOf(val predicates: List<LevelPredicate>) : LevelPredicate {
        override val min: Int = predicates.minOf { it.min }

        init {
            require(predicates.isNotEmpty()) { "predicates cannot be empty" }
        }

        override fun matches(level: Int): Boolean = predicates.any { it.matches(level) }
    }

    @Serializable
    @SerialName("AllOf")
    data class AllOf(val predicates: List<LevelPredicate>) : LevelPredicate {
        override val min: Int = predicates.maxOf { it.min }

        init {
            require(predicates.isNotEmpty()) { "predicates cannot be empty" }
        }

        override fun matches(level: Int): Boolean = predicates.all { it.matches(level) }
    }
}
