package net.azisaba.vanilife.island

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.bukkit.Location
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

interface SpawnDataAccessor {
    val spawnPoint: Location

    suspend fun spawnPoint(): Location

    suspend fun spawnPoint(spawnPoint: Location)

    companion object {
        fun create(
            position: IslandPosition,
            database: Database,
            offsetX: Double,
            offsetY: Double,
            offsetZ: Double,
            yaw: Float,
            pitch: Float,
        ): SpawnDataAccessor = SpawnDataAccessorImpl(position, database, offsetX, offsetY, offsetZ, yaw, pitch)
    }
}

private class SpawnDataAccessorImpl(
    private val position: IslandPosition,
    private val database: Database,
    private var offsetX: Double,
    private var offsetY: Double,
    private var offsetZ: Double,
    private var yaw: Float,
    private var pitch: Float,
) : SpawnDataAccessor {
    override val spawnPoint: Location = centerLocation().apply {
        add(offsetX, offsetY, offsetZ)
        setRotation(yaw, pitch)
    }

    private val columns: List<Column<*>> = listOf(
        IslandsTable.spawnOffsetX,
        IslandsTable.spawnOffsetY,
        IslandsTable.spawnOffsetZ,
        IslandsTable.spawnYaw,
        IslandsTable.spawnPitch,
    )

    override suspend fun spawnPoint(): Location = suspendTransaction(database) {
        val resultRow = IslandsTable.select(columns)
            .where { IslandsTable.id eq position.toLong() }
            .single()

        val offsetX = resultRow[IslandsTable.spawnOffsetX].also {
            this@SpawnDataAccessorImpl.offsetX = it
        }
        val offsetY = resultRow[IslandsTable.spawnOffsetY].also {
            this@SpawnDataAccessorImpl.offsetY = it
        }
        val offsetZ = resultRow[IslandsTable.spawnOffsetZ].also {
            this@SpawnDataAccessorImpl.offsetZ = it
        }
        val yaw = resultRow[IslandsTable.spawnYaw].also {
            this@SpawnDataAccessorImpl.yaw = it
        }
        val pitch = resultRow[IslandsTable.spawnPitch].also {
            this@SpawnDataAccessorImpl.pitch = it
        }

        centerLocation().apply {
            add(offsetX, offsetY, offsetZ)
            setRotation(yaw, pitch)
        }
    }

    override suspend fun spawnPoint(spawnPoint: Location) = suspendTransaction(database) {
        val centerLocation = centerLocation()

        val offsetX = (spawnPoint.x() - centerLocation.x()).also {
            this@SpawnDataAccessorImpl.offsetX = it
        }
        val offsetY = (spawnPoint.y() - centerLocation.y()).also {
            this@SpawnDataAccessorImpl.offsetY = it
        }
        val offsetZ = (spawnPoint.z() - centerLocation.z()).also {
            this@SpawnDataAccessorImpl.offsetZ = it
        }

        this@SpawnDataAccessorImpl.yaw = spawnPoint.yaw
        this@SpawnDataAccessorImpl.pitch = spawnPoint.pitch

        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = offsetX
            it[IslandsTable.spawnOffsetY] = offsetY
            it[IslandsTable.spawnOffsetZ] = offsetZ
            it[IslandsTable.spawnYaw] = yaw
            it[IslandsTable.spawnPitch] = pitch
        }

        Unit
    }

    private fun centerLocation(): Location = Location(
        Vanilife.getIslandsWorld(),
        position.centerBlockX().toDouble(),
        IslandsWorld.MIN_Y.toDouble(),
        position.centerBlockZ().toDouble(),
    )
}
