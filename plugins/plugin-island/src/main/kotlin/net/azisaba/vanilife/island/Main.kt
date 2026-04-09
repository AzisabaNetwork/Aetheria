package net.azisaba.vanilife.island

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.folia.launch
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import kotlinx.coroutines.launch
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.island.loader.IslandLoader
import net.azisaba.vanilife.island.loader.IslandLoaderType
import net.azisaba.vanilife.island.util.EnchantmentUnlockRates
import net.azisaba.vanilife.island.wrack.WrackType
import net.azisaba.vanilife.reloadableConfig
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.onClose

internal class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    private val islandLoaders: MutableList<IslandLoader> = mutableListOf()

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        val config = reloadableConfig(Configuration(), Configuration.serializer())
        val database = config.value().database.createConnection().setupTables()

        PacketEvents.getAPI().init()
        EntityLib.init(SpigotEntityLibPlatform(this), APIConfig(PacketEvents.getAPI()))

        IslandLoaderType.bootstrap(this)
        ScoreSource.bootstrap(this)
        WrackType.bootstrap(this)

        launch {
            EnchantmentUnlockRates.bootstrap(database)
        }

        koinApp = startKoin {
            modules(
                module {
                    single<Plugin> { this@Main }
                    single<ConfigurationHolder<Configuration>> { config }
                    single<Database> { database }
                    single<IslandSource>(createdAtStart = true) { IslandSource(get(), get()) }
                    single<IslandSourceTicker>(createdAtStart = true) { IslandSourceTicker(get()).apply { start(get()) } } onClose { it?.close() }
                },
            )
        }

        islandLoaders.addAll(IslandLoaderType.map { IslandLoader(it, koinApp.koin.get()) })
        islandLoaders.forEach { it.start(this@Main, koinApp.koin.get()) }

        setupEventListeners(koinApp.koin)
        setupIslandBetterHudPlaceholders()
    }

    override fun onDisable() {
        koinApp.close()
        islandLoaders.forEach(IslandLoader::close)
        PacketEvents.getAPI().terminate()
    }
}
