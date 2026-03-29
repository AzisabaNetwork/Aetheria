package net.azisaba.vanilife.island

import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

interface PrimaryDataAccessor {
    suspend fun level(): Int

    suspend fun level(level: Int)

    suspend fun displayName(): Component?

    suspend fun displayName(displayName: Component?)

    fun spawnPoint(position: IslandPosition, world: World): Location = Location(
        world,
        position.centerBlockX().toDouble(),
        (IslandsWorld.MIN_Y + IslandsWorld.HEIGHT / 2).toDouble(),
        position.centerBlockZ().toDouble(),
    )

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): PrimaryDataAccessor =
            PrimaryDataAccessorImpl(position, database)
    }
}

private class PrimaryDataAccessorImpl(
    private val position: IslandPosition, private val database: Database,
) : PrimaryDataAccessor {
    override suspend fun level(): Int = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.level)
            .where { IslandsTable.id eq position.toLong() }
            .first()[IslandsTable.level]
    }

    override suspend fun level(level: Int) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.level] = level
        }
        Unit
    }

    override suspend fun displayName(): Component? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.displayName)
            .where { IslandsTable.id eq position.toLong() }
            .first()[IslandsTable.displayName]
    }

    override suspend fun displayName(displayName: Component?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.displayName] = displayName
        }
        Unit
    }
}
