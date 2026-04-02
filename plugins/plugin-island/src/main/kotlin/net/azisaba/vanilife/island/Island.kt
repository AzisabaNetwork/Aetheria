package net.azisaba.vanilife.island

import net.azisaba.serialization.IntProvider
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.leveling.LevelDataAccessor
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.storage.StorageAccessor
import net.azisaba.vanilife.island.visitors.VisitorsAccessor
import net.azisaba.vanilife.island.waves.WaveAccessor
import net.azisaba.vanilife.island.wrack.WrackAccessor
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.*

class Island internal constructor(
    position: IslandPosition,
    owner: UUID,
    level: Int,
    score: Double,
    displayName: Component?,
    spawnOffsetX: Double,
    spawnOffsetY: Double,
    spawnOffsetZ: Double,
    spawnYaw: Float,
    spawnPitch: Float,
    private val spawnLimit: ConfigurationHolder<Int>,
    private val spawnIntervalTicks: ConfigurationHolder<IntProvider>,
    private val database: Database,
    private val plugin: Plugin,
) :
    ForwardingAudience, IslandFeatureHolder,
    EnchantmentAccessor by EnchantmentAccessor.fromDatabase(position, database),
    StorageAccessor by StorageAccessor.fromDatabase(position, database),
    VisitorsAccessor by VisitorsAccessor.fromDatabase(position, database),
    WaveAccessor by WaveAccessor.create(position),
    WrackAccessor by WrackAccessor.create(
        position,
        spawnLimit,
        spawnIntervalTicks,
        plugin,
    ),
    PrimaryDataAccessor by PrimaryDataAccessor.create(
        position,
        owner,
        displayName,
        database,
    ),
    SpawnDataAccessor by SpawnDataAccessor.create(
        position,
        database,
        spawnOffsetX,
        spawnOffsetY,
        spawnOffsetZ,
        spawnYaw,
        spawnPitch,
    ),
    LevelDataAccessor by LevelDataAccessor.create(
        position,
        level,
        score,
        database,
    ) {
    override fun audiences(): Iterable<Audience> = IslandPlayerMap.collect(this).mapNotNull(Bukkit::getPlayer)

    internal suspend fun addPlayer(player: Player) {
        if (isEnabled(IslandFeature.FLIGHT)) {
            player.allowFlight = true
        }

        if (ScoreSource.all().any { it is ScoreSource.VisitPlayer }) {
            val isFirstVisit = hasVisited(player)

            ScoreSource.all(level)
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

        if (!player.gameMode.isInvulnerable) {
            player.allowFlight = false
        }
    }

    internal suspend fun bootstrap() {
        bootstrapStorage()
    }

    companion object {
        const val MIN_LEVEL: Int = 1
        const val MAX_LEVEL: Int = 50
    }
}
