package net.azisaba.vanilife.island

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.event.IslandEnchantmentAddEvent
import net.azisaba.vanilife.island.event.IslandEnchantmentRemoveEvent
import net.azisaba.vanilife.island.leveling.LevelDataAccessor
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.storage.StorageAccessor
import net.azisaba.vanilife.island.visitors.VisitorsAccessor
import net.azisaba.vanilife.island.waves.WaveAccessor
import net.azisaba.vanilife.island.wrack.WrackAccessor
import net.azisaba.vanilife.island.wrack.WrackConfiguration
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.jdbc.Database

class Island internal constructor(
    position: IslandPosition,
    private val wrackConfig: ConfigurationHolder<WrackConfiguration>,
    private val database: Database,
    private val plugin: Plugin,
    private val primaryDataAccessor: PrimaryDataAccessor = PrimaryDataAccessor.fromDatabase(position, database),
    private val spawnDataAccessor: SpawnDataAccessor = SpawnDataAccessor.fromDatabase(position, database),
    private val levelDataAccessor: LevelDataAccessor = LevelDataAccessor.fromDatabase(position, database),
    private val enchantmentAccessor: EnchantmentAccessor = EnchantmentAccessor.fromDatabase(position, database),
    private val storageAccessor: StorageAccessor = StorageAccessor.fromDatabase(position, database),
    private val visitorsAccessor: VisitorsAccessor = VisitorsAccessor.fromDatabase(position, database),
    private val waveAccessor: WaveAccessor = WaveAccessor.create(position),
    private val wrackAccessor: WrackAccessor = WrackAccessor.create(
        position,
        spawnLimit = wrackConfig.map(WrackConfiguration::spawnLimit),
        spawnIntervalTicks = wrackConfig.map(WrackConfiguration::spawnIntervalTicks),
        plugin,
    ),
) :
    ForwardingAudience, IslandFeatureHolder,
    PrimaryDataAccessor by primaryDataAccessor,
    SpawnDataAccessor by spawnDataAccessor,
    LevelDataAccessor by levelDataAccessor,
    EnchantmentAccessor by enchantmentAccessor,
    StorageAccessor by storageAccessor,
    VisitorsAccessor by visitorsAccessor,
    WaveAccessor by waveAccessor,
    WrackAccessor by wrackAccessor {
    override fun audiences(): Iterable<Audience> = IslandsPlayerAccessor.byIsland(this).mapNotNull(Bukkit::getPlayer)

    override suspend fun addEnchantment(enchantment: TypedKey<Enchantment>): Boolean {
        if (!IslandEnchantmentAddEvent(this, enchantment).callEvent()) return false
        return enchantmentAccessor.addEnchantment(enchantment)
    }

    override suspend fun addEnchantment(enchantment: Enchantment): Boolean =
        addEnchantment(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    override suspend fun removeEnchantment(enchantment: TypedKey<Enchantment>): Boolean {
        if (!IslandEnchantmentRemoveEvent(this, enchantment).callEvent()) return false
        return enchantmentAccessor.removeEnchantment(enchantment)
    }

    override suspend fun removeEnchantment(enchantment: Enchantment): Boolean =
        removeEnchantment(RegistryKey.ENCHANTMENT.typedKey(enchantment.key()))

    internal suspend fun addPlayer(player: Player) {
        if (isEnabled(IslandFeature.FLIGHT)) {
            player.allowFlight = true
        }

        if (ScoreSource.any { it is ScoreSource.VisitPlayer }) {
            val isFirstVisit = hasVisited(player)

            ScoreSource.byLevel(level)
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
        bootstrapPrimaryData()
        bootstrapSpawnData()
        bootstrapLevelData()
        bootstrapEnchantments()
        bootstrapStorage()
    }

    companion object {
        const val MIN_LEVEL: Int = 1
        const val MAX_LEVEL: Int = 50
    }
}
