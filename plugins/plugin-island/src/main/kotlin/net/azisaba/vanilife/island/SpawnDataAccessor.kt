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

    suspend fun spawnPoint(spawnPoint: Location)

    suspend fun bootstrapSpawnData()

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): SpawnDataAccessor =
            SpawnDataAccessorImpl(position, database)
    }
}

private class SpawnDataAccessorImpl(
    private val position: IslandPosition, private val database: Database,
) : SpawnDataAccessor {
    override val spawnPoint: Location
        get() = centerLocation().apply {
            add(requireLoaded().offsetX, requireLoaded().offsetY, requireLoaded().offsetZ)
            setRotation(requireLoaded().yaw, requireLoaded().pitch)
        }

    private val positionId: Long = position.toLong()

    private var cacheData: CacheData? = null

    private val columns: List<Column<*>> = listOf(
        IslandsTable.spawnOffsetX,
        IslandsTable.spawnOffsetY,
        IslandsTable.spawnOffsetZ,
        IslandsTable.spawnYaw,
        IslandsTable.spawnPitch,
    )

    override suspend fun spawnPoint(spawnPoint: Location) = suspendTransaction(database) {
        val centerLocation = centerLocation()

        val offsetX = (spawnPoint.x() - centerLocation.x())
        val offsetY = (spawnPoint.y() - centerLocation.y())
        val offsetZ = (spawnPoint.z() - centerLocation.z())

        val yaw = spawnPoint.yaw
        val pitch = spawnPoint.pitch

        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = offsetX
            it[IslandsTable.spawnOffsetY] = offsetY
            it[IslandsTable.spawnOffsetZ] = offsetZ
            it[IslandsTable.spawnYaw] = yaw
            it[IslandsTable.spawnPitch] = pitch
        }

        cacheData = CacheData(offsetX, offsetY, offsetZ, yaw, pitch)
    }

    override suspend fun bootstrapSpawnData() = suspendTransaction(database) {
        val row = IslandsTable.select(columns)
            .where { IslandsTable.id eq positionId }
            .single()

        cacheData = CacheData(
            offsetX = row[IslandsTable.spawnOffsetX],
            offsetY = row[IslandsTable.spawnOffsetY],
            offsetZ = row[IslandsTable.spawnOffsetZ],
            yaw = row[IslandsTable.spawnYaw],
            pitch = row[IslandsTable.spawnPitch],
        )
    }

    private fun requireLoaded(): CacheData = cacheData
        ?: throw IllegalStateException("Spawn data has not yet been loaded")

    private fun centerLocation(): Location = Location(
        Vanilife.getIslandsWorld(),
        position.centerBlockX().toDouble(),
        IslandsWorld.MIN_Y.toDouble(),
        position.centerBlockZ().toDouble(),
    )

    private data class CacheData(
        val offsetX: Double,
        val offsetY: Double,
        val offsetZ: Double,
        val yaw: Float,
        val pitch: Float,
    )
}
