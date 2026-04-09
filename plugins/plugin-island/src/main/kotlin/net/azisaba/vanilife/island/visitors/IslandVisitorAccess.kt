package net.azisaba.vanilife.island.visitors

import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

internal class IslandVisitorAccess(position: IslandPosition, private val database: Database) : VisitorAccess {
    override val visitors: Set<UUID>
        get() = requireLoaded().visitors

    override val totalStayTime: Duration
        get() = requireLoaded().totalStayTime

    private val positionId: Long = position.toLong()

    private var cacheData: CacheData? = null

    override suspend fun beginVisit(uuid: UUID) = suspendTransaction(database) {
        val now = Clock.System.now()

        IslandVisitorsTable.insertIgnore {
            it[position] = positionId
            it[visitor] = uuid
            it[firstVisitAt] = now
            it[lastVisitAt] = now
            it[stayTime] = Duration.ZERO
        }

        IslandVisitorsTable.update(where = { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }) {
            it[lastVisitAt] = now
        }

        if (!hasVisited(uuid)) {
            cacheData = requireLoaded().copy(visitors = requireLoaded().visitors + uuid)
        }
    }

    override suspend fun endVisit(uuid: UUID) = suspendTransaction(database) {
        val now = Clock.System.now()

        val row =
            IslandVisitorsTable.select(IslandVisitorsTable.lastVisitAt, IslandVisitorsTable.stayTime)
                .where { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }
                .singleOrNull() ?: return@suspendTransaction

        val lastVisit = row[IslandVisitorsTable.lastVisitAt]
        val currentStayTime = row[IslandVisitorsTable.stayTime]

        val delta = now - lastVisit

        if (delta.isNegative()) return@suspendTransaction

        cacheData = requireLoaded().copy(totalStayTime = requireLoaded().totalStayTime + delta)

        IslandVisitorsTable.update(where = { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }) {
            it[IslandVisitorsTable.lastVisitAt] = now
            it[IslandVisitorsTable.stayTime] = currentStayTime + delta
        }
    }

    override suspend fun firstVisitAt(uuid: UUID): Instant? = suspendTransaction(database) {
        IslandVisitorsTable.select(IslandVisitorsTable.firstVisitAt)
            .where { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }
            .singleOrNull()
            ?.get(IslandVisitorsTable.firstVisitAt)
    }

    override suspend fun lastVisitAt(uuid: UUID): Instant? = suspendTransaction(database) {
        IslandVisitorsTable.select(IslandVisitorsTable.lastVisitAt)
            .where { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }
            .singleOrNull()
            ?.get(IslandVisitorsTable.lastVisitAt)
    }

    override suspend fun stayTimeOf(uuid: UUID): Duration = suspendTransaction(database) {
        IslandVisitorsTable.select(IslandVisitorsTable.stayTime)
            .where { (IslandVisitorsTable.position eq positionId) and (IslandVisitorsTable.visitor eq uuid) }
            .singleOrNull()
            ?.get(IslandVisitorsTable.stayTime) ?: Duration.ZERO
    }

    suspend fun bootstrap() = suspendTransaction(database) {
        val visitors = IslandVisitorsTable.select(IslandVisitorsTable.visitor)
            .where { IslandVisitorsTable.position eq positionId }
            .map { it[IslandVisitorsTable.visitor] }
            .toSet()

        val stayTimeSumExpression = IslandVisitorsTable.stayTime.sum()
        val totalStayTime = IslandVisitorsTable.select(stayTimeSumExpression)
            .where { IslandVisitorsTable.position eq positionId }
            .firstOrNull()
            ?.get(stayTimeSumExpression) ?: Duration.ZERO

        cacheData = CacheData(visitors, totalStayTime)
    }

    private fun requireLoaded(): CacheData =
        cacheData ?: throw IllegalStateException("Visitors data has not yet been loaded")

    private data class CacheData(val visitors: Set<UUID>, val totalStayTime: Duration)
}
