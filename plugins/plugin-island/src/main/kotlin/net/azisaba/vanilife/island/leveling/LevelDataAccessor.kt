package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandsTable
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.leveling.score.ScoringManager
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

interface LevelDataAccessor {
    val level: Int

    val score: Double

    suspend fun level(level: Int)

    suspend fun score(score: Double)

    suspend fun updateScore(source: ScoreSource)

    companion object {
        fun create(
            position: IslandPosition,
            level: Int,
            score: Double,
            database: Database,
        ): LevelDataAccessor = LevelDataAccessorImpl(position, level, score, database)
    }
}

private class LevelDataAccessorImpl(
    private val position: IslandPosition,
    override var level: Int,
    override var score: Double,
    private val database: Database,
) : LevelDataAccessor {
    private val scoringManager: ScoringManager = ScoringManager()

    override suspend fun level(level: Int) = suspendTransaction(database) {
        require(level in Island.MIN_LEVEL..Island.MAX_LEVEL) {
            "Island level must be between ${Island.MIN_LEVEL}..${Island.MAX_LEVEL}"
        }
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.level] = level
            it[IslandsTable.score] = 0.0
        }
        this@LevelDataAccessorImpl.level = level
        this@LevelDataAccessorImpl.score = 0.0
    }

    override suspend fun score(score: Double) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq position.toLong() }) {
            it[IslandsTable.score] = score
        }
        this@LevelDataAccessorImpl.score = score
    }

    override suspend fun updateScore(source: ScoreSource) {
        val give = scoringManager.computeScore(source)
        val newScore = score + give
        score(newScore)
        score = newScore
    }
}
