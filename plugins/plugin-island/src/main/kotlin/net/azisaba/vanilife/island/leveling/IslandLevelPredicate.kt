package net.azisaba.vanilife.island.leveling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface IslandLevelPredicate {
    fun matches(level: Int): Boolean

    @Serializable
    @SerialName("Exact")
    data class Exact(val value: Int) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = level == value
    }

    @Serializable
    @SerialName("AtLeast")
    data class AtLeast(val min: Int) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = level >= min
    }

    @Serializable
    @SerialName("AtMost")
    data class AtMost(val max: Int) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = level <= max
    }

    @Serializable
    @SerialName("Between")
    data class Between(val min: Int, val max: Int) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = level in min..max
    }

    @Serializable
    @SerialName("Always")
    object Always : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = true
    }

    @Serializable
    @SerialName("AnyOf")
    data class AnyOf(val predicates: List<IslandLevelPredicate>) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = predicates.any { it.matches(level) }
    }

    @Serializable
    @SerialName("AllOf")
    data class AllOf(val predicates: List<IslandLevelPredicate>) : IslandLevelPredicate {
        override fun matches(level: Int): Boolean = predicates.all { it.matches(level) }
    }
}
