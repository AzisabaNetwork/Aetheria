package net.azisaba.vanilife.island

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import net.azisaba.vanilife.ReloadableConfiguration
import net.azisaba.vanilife.island.leveling.ScoreSource
import net.azisaba.vanilife.island.wrack.WrackType
import net.azisaba.vanilife.reloadableConfig
import net.azisaba.vanilife.island.leveling.LevelingConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.onClose

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        val config = reloadableConfig(Configuration(), Configuration.serializer())
        val database = setupDatabase(config.value().database).setupTables()

        PacketEvents.getAPI().init()
        EntityLib.init(SpigotEntityLibPlatform(this), APIConfig(PacketEvents.getAPI()))

        ScoreSource.bootstrap(this)
        WrackType.bootstrap(this)

        val cacheMap = IslandCacheMap(database)
        val ticker = IslandTicker(
            this,
            cacheMap,
            levelUpRequirements = config.map(Configuration::leveling)
                .map(LevelingConfiguration::levelUpRequirements),
            levelUpCheckIntervalTicks = config.map(Configuration::leveling)
                .map(LevelingConfiguration::levelUpCheckIntervalTicks),
        )

        koinApp = startKoin {
            modules(
                module {
                    single<Plugin> { this@Main }
                    single<ReloadableConfiguration<Configuration>> { config }
                    single<Database> { database }
                    single<IslandTicker> { ticker } onClose { it?.close() }
                    single<IslandCacheMap> { cacheMap }
                },
            )
        }

        setupEventListeners(koinApp.koin)
        setupIslandBetterHudPlaceholders()
    }

    override fun onDisable() {
        koinApp.close()
        PacketEvents.getAPI().terminate()
    }
}
