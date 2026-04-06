package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.leveling.score.ScoringManager
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

interface LevelDataAccessor {
    val level: Int

    val score: Double

    suspend fun level(level: Int)

    suspend fun score(score: Double)

    suspend fun updateScore(source: ScoreSource)

    @ApiStatus.Internal
    suspend fun bootstrapLevelData()

    companion object {
        fun fromDatabase(position: IslandPosition, database: Database): LevelDataAccessor =
            LevelDataAccessorImpl(position, database)
    }
}

private class LevelDataAccessorImpl(
    private val position: IslandPosition, private val database: Database,
) : LevelDataAccessor {
    override val level: Int
        get() = requireLoaded().level

    override val score: Double
        get() = requireLoaded().score

    private var cacheData: CacheData? = null

    private val positionId: Long = position.toLong()

    private val scoringManager: ScoringManager = ScoringManager()

    override suspend fun level(level: Int) = suspendTransaction(database) {
        require(level in Island.MIN_LEVEL..Island.MAX_LEVEL) {
            "Island level must be between ${Island.MIN_LEVEL}..${Island.MAX_LEVEL}"
        }
        IslandsTable.update(where = { IslandsTable.id eq positionId }) {
            it[IslandsTable.level] = level
            it[IslandsTable.score] = 0.0
        }
        cacheData = requireLoaded().copy(level = level, score = 0.0)
    }

    override suspend fun score(score: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq positionId }) {
            it[IslandsTable.score] = score
        }
        cacheData = requireLoaded().copy(score = score)
    }

    override suspend fun updateScore(source: ScoreSource) {
        val give = scoringManager.computeScore(source)
        val newScore = score + give
        score(newScore)
    }

    override suspend fun bootstrapLevelData() = suspendTransaction(database) {
        val row = IslandsTable.select(IslandsTable.level, IslandsTable.score)
            .where { IslandsTable.id eq positionId }
            .single()
        cacheData = CacheData(
            level = row[IslandsTable.level],
            score = row[IslandsTable.score],
        )
    }

    private fun requireLoaded(): CacheData = cacheData
        ?: throw IllegalStateException("Level data has not yet been loaded")

    private data class CacheData(val level: Int, val score: Double)
}
