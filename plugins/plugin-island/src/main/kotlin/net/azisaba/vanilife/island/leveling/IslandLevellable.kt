package net.azisaba.vanilife.island.leveling

import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.leveling.score.ScoringManager
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

internal class IslandLevellable(position: IslandPosition, private val database: Database) : Levellable {
    override val level: Int
        get() = requireLoaded().level

    override val score: Double
        get() = requireLoaded().score

    private val positionId: Long = position.toLong()

    private val scoringManager: ScoringManager = ScoringManager()

    private var cacheData: CacheData? = null

    override suspend fun level(level: Int) = suspendTransaction(database) {
        requireLoaded()

        require(level in Levellable.MIN_LEVEL..Levellable.MAX_LEVEL) {
            "Level must be between ${Levellable.MIN_LEVEL} and ${Levellable.MAX_LEVEL}, but was $level"
        }

        IslandLevelsTable.update(where = { IslandLevelsTable.position eq positionId }) {
            it[IslandLevelsTable.level] = level
            it[IslandLevelsTable.score] = 0.0
        }

        cacheData = CacheData(level, 0.0)
    }

    override suspend fun score(score: Double) = suspendTransaction(database) {
        requireLoaded()

        IslandLevelsTable.update(where = { IslandLevelsTable.position eq positionId }) {
            it[IslandLevelsTable.score] = score
        }

        cacheData = requireLoaded().copy(score = score)
    }

    override suspend fun updateScore(source: ScoreSource) {
        val give = scoringManager.computeScore(source)
        val newScore = score + give
        score(newScore)
    }

    suspend fun bootstrap() = suspendTransaction(database) {
        IslandLevelsTable.insertIgnore {
            it[IslandLevelsTable.position] = positionId
        }

        val row = IslandLevelsTable.select(IslandLevelsTable.level, IslandLevelsTable.score)
            .where { IslandLevelsTable.position eq positionId }
            .single()

        cacheData = CacheData(
            level = row[IslandLevelsTable.level],
            score = row[IslandLevelsTable.score],
        )
    }

    private fun requireLoaded(): CacheData =
        cacheData ?: throw IllegalStateException("Level data has not yet been loaded")

    private data class CacheData(val level: Int, val score: Double)
}
