package net.azisaba.vanilife.island

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.wrack.WrackType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        val config = yamlConfig()
        val database = setupDatabase(config.database).setupTables()

        PacketEvents.getAPI().init()
        EntityLib.init(SpigotEntityLibPlatform(this), APIConfig(PacketEvents.getAPI()))

        WrackType.bootstrap(this)

        koinApp = startKoin {
            modules(
                module {
                    single<Plugin> { this@Main }
                    single<Configuration> { config }
                    single<Database> { database }
                    single<IslandMap> { IslandMap(get(), get()) }
                },
            )
        }

        setupEventListeners(koinApp.koin)
    }

    override fun onDisable() {
        koinApp.close()
        PacketEvents.getAPI().terminate()
    }
}
