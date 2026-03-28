package net.azisaba.vanilife.island.visitors

import net.azisaba.vanilife.island.IslandsTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.duration
import org.jetbrains.exposed.v1.datetime.timestamp
import java.util.*
import kotlin.time.Duration
import kotlin.time.Instant

internal object IslandVisitorsTable : Table("island_visitors") {
    val position: Column<EntityID<Long>> = reference("position", IslandsTable)

    val visitor: Column<UUID> = javaUUID("visitor")

    val firstVisitAt: Column<Instant> = timestamp("first_visit_at")

    val lastVisitAt: Column<Instant> = timestamp("last_visit_at")

    val totalStayTime: Column<Duration> = duration("total_stay_time")

    override val primaryKey: PrimaryKey = PrimaryKey(position, visitor)
}
