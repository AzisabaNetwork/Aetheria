package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

interface PrimaryDataAccessor {
    val position: IslandPosition

    val owner: UUID

    val displayName: Component?

    suspend fun displayName(displayName: Component?)

    companion object {
        fun create(
            position: IslandPosition,
            owner: UUID,
            displayName: Component?,
            database: Database
        ): PrimaryDataAccessor = PrimaryDataAccessorImpl(position, owner, displayName, database)
    }
}

private class PrimaryDataAccessorImpl(
    override val position: IslandPosition,
    override val owner: UUID,
    override var displayName: Component?,
    private val database: Database,
) : PrimaryDataAccessor {
    override suspend fun displayName(displayName: Component?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.displayName] = displayName
        }
        this@PrimaryDataAccessorImpl.displayName = displayName
    }
}
