package net.azisaba.vanilife.island.loader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.azisaba.vanilife.DynamicContents
import net.azisaba.vanilife.island.IslandType
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.island.leveling.IslandLevelsTable
import net.azisaba.vanilife.island.visitors.IslandVisitorsTable
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Serializable
internal sealed interface IslandLoaderType {
    val maxLoadPerTick: Int

    val tickInterval: Duration

    suspend fun queryPositions(database: Database): List<IslandPosition>

    companion object : DynamicContents<IslandLoaderType>("island_loader", lazy { IslandLoaderType.serializer() })

    @Serializable
    @SerialName("PlayerIsland")
    data class PlayerIsland(
        override val maxLoadPerTick: Int = 2,
        override val tickInterval: Duration = 3.seconds,
        val minLevel: Int = 0,
        val maxIdleTime: Duration = 32.days,
    ) : IslandLoaderType {
        override suspend fun queryPositions(database: Database): List<IslandPosition> = suspendTransaction(database) {
            val now = Clock.System.now()
            val threshold = now - maxIdleTime

            val lastVisitMax = IslandVisitorsTable.lastVisitAt
                .max()
                .alias("last_visit_at_max")

            val visitorsAgg = IslandVisitorsTable.select(IslandVisitorsTable.position, lastVisitMax)
                .groupBy(IslandVisitorsTable.position)
                .alias("visitors_agg")

            val positionCol = visitorsAgg[IslandVisitorsTable.position]
            val lastVisitCol = visitorsAgg[lastVisitMax]

            (IslandsTable.join(
                otherTable = visitorsAgg,
                joinType = JoinType.INNER,
                onColumn = IslandsTable.id,
                otherColumn = positionCol,
            ).join(
                otherTable = IslandLevelsTable,
                joinType = JoinType.INNER,
                onColumn = IslandsTable.id,
                otherColumn = IslandLevelsTable.position,
            )).select(IslandsTable.id).where {
                (IslandsTable.type eq IslandType.PLAYER) and (IslandLevelsTable.level greaterEq minLevel) and (lastVisitCol greaterEq threshold)
            }.map { row ->
                IslandPosition.fromLong(row[IslandsTable.id].value)
            }
        }
    }
}
