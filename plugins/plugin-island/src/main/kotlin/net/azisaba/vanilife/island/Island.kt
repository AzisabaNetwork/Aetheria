package net.azisaba.vanilife.island

import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.leveling.ScoreSource
import net.azisaba.vanilife.island.leveling.ScoringManager
import net.azisaba.vanilife.island.visitors.VisitorsAccessor
import net.azisaba.vanilife.island.waves.WaveAccessor
import net.azisaba.vanilife.island.wrack.WrackAccessor
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.GlobalContext
import java.util.*

class Island internal constructor(
    val position: IslandPosition,
    val owner: UUID,
    level: Int,
    score: Double,
    displayName: Component?,
    private val database: Database,
) :
    ForwardingAudience,
    PrimaryDataAccessor by PrimaryDataAccessor.create(position, database, level, score, displayName),
    EnchantmentAccessor by EnchantmentAccessor.fromDatabase(position, database),
    VisitorsAccessor by VisitorsAccessor.fromDatabase(position, database),
    WaveAccessor by WaveAccessor.create(position),
    WrackAccessor by WrackAccessor.create(position, Vanilife.getIslandsWorld(), GlobalContext.get().get()) {
    private val scoringManager: ScoringManager = ScoringManager()

    override fun audiences(): Iterable<Audience> = IslandPlayerMap.collect(this).mapNotNull(Bukkit::getPlayer)

    suspend fun updateScore(source: ScoreSource) {
        val score = scoringManager.computeScore(source)
        score(this.score + score)
    }

    internal suspend fun addPlayer(player: Player) {
        if (ScoreSource.all().any { it is ScoreSource.VisitPlayer }) {
            val isFirstVisit = hasVisited(player)

            ScoreSource.all()
                .filterIsInstance<ScoreSource.VisitPlayer>()
                .filter { !it.firstVisitOnly || isFirstVisit }
                .forEach { source ->
                    updateScore(source)
                }
        }

        beginVisit(player)
        addWaveViewer(player.uniqueId)
        addWrackViewer(player)
    }

    internal suspend fun removePlayer(player: Player) {
        endVisit(player)
        removeWaveViewer(player.uniqueId)
        removeWrackViewer(player)
    }
}
