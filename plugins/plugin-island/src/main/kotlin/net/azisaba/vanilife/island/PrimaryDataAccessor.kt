package net.azisaba.vanilife.island

import net.azisaba.vanilife.Vanilife
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
    val level: Int

    val score: Double

    val displayName: Component?

    val spawnOffsetX: Double

    val spawnOffsetY: Double

    val spawnOffsetZ: Double

    val spawnYaw: Float

    val spawnPitch: Float

    val spawnLocation: Location

    suspend fun level(): Int

    suspend fun level(level: Int)

    suspend fun score(): Double

    suspend fun score(score: Double)

    suspend fun displayName(): Component?

    suspend fun displayName(displayName: Component?)

    suspend fun spawnOffsetX(): Double

    suspend fun spawnOffsetX(spawnOffsetX: Double)

    suspend fun spawnOffsetY(): Double

    suspend fun spawnOffsetY(spawnOffsetY: Double)

    suspend fun spawnOffsetZ(): Double

    suspend fun spawnOffsetZ(spawnOffsetZ: Double)

    suspend fun spawnYaw(): Float

    suspend fun spawnYaw(spawnYaw: Float)

    suspend fun spawnPitch(): Float

    suspend fun spawnPitch(spawnPitch: Float)

    suspend fun spawnLocation(): Location

    suspend fun spawnLocation(spawnLocation: Location)

    fun spawnPoint(position: IslandPosition, world: World): Location = Location(
        world,
        position.centerBlockX().toDouble(),
        (IslandsWorld.MIN_Y + IslandsWorld.HEIGHT / 2).toDouble(),
        position.centerBlockZ().toDouble(),
    )

    companion object {
        fun create(
            position: IslandPosition,
            database: Database,
            level: Int,
            score: Double,
            displayName: Component?,
            spawnOffsetX: Double,
            spawnOffsetY: Double,
            spawnOffsetZ: Double,
            spawnYaw: Float,
            spawnPitch: Float,
        ): PrimaryDataAccessor = PrimaryDataAccessorImpl(
            position,
            database,
            level,
            score,
            displayName,
            spawnOffsetX,
            spawnOffsetY,
            spawnOffsetZ,
            spawnYaw,
            spawnPitch,
        )
    }
}

private class PrimaryDataAccessorImpl(
    private val position: IslandPosition,
    private val database: Database,
    level: Int,
    score: Double,
    displayName: Component?,
    spawnOffsetX: Double,
    spawnOffsetY: Double,
    spawnOffsetZ: Double,
    spawnYaw: Float,
    spawnPitch: Float,
) : PrimaryDataAccessor {
    override var level: Int = level
        private set

    override var score: Double = score
        private set

    override var displayName: Component? = displayName
        private set

    override var spawnOffsetX: Double = spawnOffsetX
        private set

    override var spawnOffsetY: Double = spawnOffsetY
        private set

    override var spawnOffsetZ: Double = spawnOffsetZ
        private set

    override var spawnYaw: Float = spawnYaw
        private set

    override var spawnPitch: Float = spawnPitch
        private set

    override val spawnLocation: Location
        get() = Location(
            Vanilife.getIslandsWorld(),
            position.centerBlockX() + spawnOffsetX,
            IslandsWorld.MIN_Y + spawnOffsetY,
            position.centerBlockZ() + spawnOffsetZ,
        )

    override suspend fun level(): Int = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.level)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.level]
    }.also { this@PrimaryDataAccessorImpl.level = it }

    override suspend fun level(level: Int) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.level] = level
            it[IslandsTable.score] = 0.0
        }
        score = 0.0
        this@PrimaryDataAccessorImpl.level = level
    }

    override suspend fun score(): Double = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.score)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.score]
    }.also { this@PrimaryDataAccessorImpl.score = it }

    override suspend fun score(score: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.score] = score
        }
        this@PrimaryDataAccessorImpl.score = score
    }

    override suspend fun displayName(): Component? = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.displayName)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.displayName]
    }.also { this@PrimaryDataAccessorImpl.displayName = it }

    override suspend fun displayName(displayName: Component?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.displayName] = displayName
        }
        this@PrimaryDataAccessorImpl.displayName = displayName
    }

    override suspend fun spawnOffsetX(): Double = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.spawnOffsetX)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.spawnOffsetX]
    }.also { spawnOffsetX = it }

    override suspend fun spawnOffsetX(spawnOffsetX: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = spawnOffsetX
        }
        this@PrimaryDataAccessorImpl.spawnOffsetX = spawnOffsetX
    }

    override suspend fun spawnOffsetY(): Double = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.spawnOffsetY)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.spawnOffsetY]
    }.also { spawnOffsetX = it }

    override suspend fun spawnOffsetY(spawnOffsetY: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetY] = spawnOffsetY
        }
        this@PrimaryDataAccessorImpl.spawnOffsetY = spawnOffsetY
    }

    override suspend fun spawnOffsetZ(): Double = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.spawnOffsetZ)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.spawnOffsetZ]
    }.also { spawnOffsetZ = it }

    override suspend fun spawnOffsetZ(spawnOffsetZ: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetZ] = spawnOffsetZ
        }
        this@PrimaryDataAccessorImpl.spawnOffsetZ = spawnOffsetZ
    }

    override suspend fun spawnYaw(): Float = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.spawnYaw)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.spawnYaw]
    }.also { spawnYaw = it }

    override suspend fun spawnYaw(spawnYaw: Float) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnYaw] = spawnYaw
        }
        this@PrimaryDataAccessorImpl.spawnYaw = spawnYaw
    }

    override suspend fun spawnPitch(): Float = suspendTransaction(database) {
        IslandsTable.select(IslandsTable.spawnPitch)
            .where { IslandsTable.id eq position.toLong() }
            .single()[IslandsTable.spawnPitch]
    }.also { spawnPitch = it }

    override suspend fun spawnPitch(spawnPitch: Float) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnPitch] = spawnPitch
        }
        this@PrimaryDataAccessorImpl.spawnPitch = spawnPitch
    }

    override suspend fun spawnLocation(): Location = suspendTransaction(database) {
        val resultRow = IslandsTable.select(
            IslandsTable.spawnOffsetX,
            IslandsTable.spawnOffsetY,
            IslandsTable.spawnOffsetZ,
            IslandsTable.spawnYaw,
            IslandsTable.spawnPitch,
        ).where { IslandsTable.id eq position.toLong() }.single()

        spawnOffsetX = resultRow[IslandsTable.spawnOffsetX]
        spawnOffsetY = resultRow[IslandsTable.spawnOffsetY]
        spawnOffsetZ = resultRow[IslandsTable.spawnOffsetZ]
        spawnYaw = resultRow[IslandsTable.spawnYaw]
        spawnPitch = resultRow[IslandsTable.spawnPitch]

        spawnLocation
    }

    override suspend fun spawnLocation(spawnLocation: Location) = suspendTransaction(database) {
        spawnOffsetX = spawnLocation.x() - position.centerBlockX()
        spawnOffsetY = spawnLocation.y() - IslandsWorld.MIN_Y
        spawnOffsetZ = spawnLocation.z() - position.centerBlockZ()
        spawnYaw = spawnLocation.yaw
        spawnPitch = spawnLocation.pitch

        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.spawnOffsetX] = spawnOffsetX
            it[IslandsTable.spawnOffsetY] = spawnOffsetY
            it[IslandsTable.spawnOffsetZ] = spawnOffsetZ
            it[IslandsTable.spawnYaw] = spawnYaw
            it[IslandsTable.spawnPitch] = spawnPitch
        }

        Unit
    }
}
