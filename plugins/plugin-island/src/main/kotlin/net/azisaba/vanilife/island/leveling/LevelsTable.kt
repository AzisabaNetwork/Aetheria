package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.IslandsTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.EntityID

internal abstract class LevelsTable(name: String) : Table(name) {
    val level: Column<Int> = integer("level")
        .clientDefault { Levellable.MIN_LEVEL }
        .check { it.between(Levellable.MIN_LEVEL, Levellable.MAX_LEVEL) }

    val score: Column<Double> = double("score")
        .clientDefault { 0.0 }
}

internal object IslandLevelsTable : LevelsTable(name = "island_levels") {
    val position: Column<EntityID<Long>> = reference(IslandsTable.id.name, foreign = IslandsTable)

    override val primaryKey: PrimaryKey = PrimaryKey(position)
}
