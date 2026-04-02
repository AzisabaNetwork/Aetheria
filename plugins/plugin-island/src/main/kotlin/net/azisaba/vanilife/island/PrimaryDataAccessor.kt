package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

interface PrimaryDataAccessor {
    val position: IslandPosition

    val owner: UUID

    val displayName: Component

    suspend fun displayName(displayName: Component)

    @ApiStatus.Internal
    suspend fun bootstrapPrimaryData()

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): PrimaryDataAccessor =
            PrimaryDataAccessorImpl(position, database)
    }
}

private class PrimaryDataAccessorImpl(override val position: IslandPosition, private val database: Database) :
    PrimaryDataAccessor {
    override val owner: UUID
        get() = requireLoaded().owner

    override val displayName: Component
        get() = requireLoaded().displayName

    private var cacheData: CacheData? = null

    private val positionId: Long = position.toLong()

    override suspend fun displayName(displayName: Component) = suspendTransaction(database) {
        requireLoaded()
        IslandsTable.update(where = { IslandsTable.id eq positionId }) {
            it[IslandsTable.displayName] = displayName
        }
        cacheData = requireLoaded().copy(displayName = displayName)
    }

    override suspend fun bootstrapPrimaryData() = suspendTransaction(database) {
        val row = IslandsTable.select(IslandsTable.owner, IslandsTable.displayName)
            .where { IslandsTable.id eq positionId }
            .single()
        cacheData = CacheData(
            owner = row[IslandsTable.owner],
            displayName = row[IslandsTable.displayName],
        )
    }

    private fun requireLoaded(): CacheData =
        cacheData ?: throw IllegalStateException("Primary data has not yet been loaded")

    private data class CacheData(val owner: UUID, val displayName: Component)
}
