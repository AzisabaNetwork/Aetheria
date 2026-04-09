package net.azisaba.vanilife.island

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3d
import org.joml.Vector3dc

internal class IslandPrimaryDataAccess(
    private val position: IslandPosition, private val database: Database,
) : PrimaryDataAccess {
    override val displayName: Component
        get() = requireLoaded().displayName

    override val description: Component
        get() = requireLoaded().description

    override val defaultSpawnPoint: Location
        get() = centerLocation().apply {
            val offsets = requireLoaded().spawnOffsets
            val rotation = requireLoaded().spawnRotation
            add(offsets.x(), offsets.y(), offsets.z())
            setRotation(rotation.x(), rotation.y())
        }

    private val positionId: Long = position.toLong()

    private var cacheData: CacheData? = null

    override suspend fun displayName(displayName: Component) {
        val cacheData = requireLoaded()

        suspendTransaction(database) {
            IslandsTable.update(where = { IslandsTable.id eq positionId }) {
                it[IslandsTable.displayName] = displayName
            }
        }

        this.cacheData = cacheData.copy(displayName = displayName)
    }

    override suspend fun description(description: Component) {
        val cacheData = requireLoaded()

        suspendTransaction(database) {
            IslandsTable.update(where = { IslandsTable.id eq positionId }) {
                it[IslandsTable.description] = description
            }
        }

        this.cacheData = cacheData.copy(description = description)
    }

    override suspend fun defaultSpawnPoint(defaultSpawnPoint: Location) {
        val cacheData = requireLoaded()
        val centerLocation = centerLocation()

        val offsetX = defaultSpawnPoint.x() - centerLocation.x()
        val offsetY = defaultSpawnPoint.y() - centerLocation.y()
        val offsetZ = defaultSpawnPoint.z() - centerLocation.z()

        val yaw = defaultSpawnPoint.yaw
        val pitch = defaultSpawnPoint.pitch

        suspendTransaction(database) {
            IslandsTable.update(where = { IslandsTable.id eq positionId }) {
                it[IslandsTable.spawnOffsetX] = offsetX
                it[IslandsTable.spawnOffsetY] = offsetY
                it[IslandsTable.spawnOffsetZ] = offsetZ
                it[IslandsTable.spawnYaw] = yaw
                it[IslandsTable.spawnPitch] = pitch
            }
        }

        this.cacheData = cacheData.copy(
            spawnOffsets = Vector3d(offsetX, offsetY, offsetZ),
            spawnRotation = Vector2f(yaw, pitch),
        )
    }

    suspend fun bootstrap() {
        val row = suspendTransaction(database) {
            IslandsTable.select(
                IslandsTable.displayName,
                IslandsTable.description,
                IslandsTable.spawnOffsetX,
                IslandsTable.spawnOffsetY,
                IslandsTable.spawnOffsetZ,
                IslandsTable.spawnYaw,
                IslandsTable.spawnPitch,
            )
                .where { IslandsTable.id eq positionId }
                .single()
        }

        cacheData = CacheData(
            displayName = row[IslandsTable.displayName],
            description = row[IslandsTable.description],
            spawnOffsets = Vector3d(
                row[IslandsTable.spawnOffsetX],
                row[IslandsTable.spawnOffsetY],
                row[IslandsTable.spawnOffsetZ],
            ),
            spawnRotation = Vector2f(
                row[IslandsTable.spawnYaw],
                row[IslandsTable.spawnPitch],
            ),
        )
    }

    private fun requireLoaded(): CacheData =
        cacheData ?: throw IllegalStateException("Primary data has not yet been loaded")

    private fun centerLocation(): Location = Location(
        Vanilife.getIslandsWorld(),
        position.centerBlockX().toDouble(),
        IslandsWorld.MIN_Y.toDouble(),
        position.centerBlockZ().toDouble(),
    )

    private data class CacheData(
        val displayName: Component,
        val description: Component,
        val spawnOffsets: Vector3dc,
        val spawnRotation: Vector2fc,
    )
}
