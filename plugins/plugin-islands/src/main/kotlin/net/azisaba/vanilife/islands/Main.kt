package net.azisaba.vanilife.islands

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import net.azisaba.vanilife.islands.storage.DatabaseIslandRepository
import net.azisaba.vanilife.islands.storage.IslandRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
        val config = tomlConfig()
        val database = setupDatabase(config.database).setupTables()

        PacketEvents.getAPI().init()
        EntityLib.init(SpigotEntityLibPlatform(this), APIConfig(PacketEvents.getAPI()))

        koinApp = startKoin {
            modules(module {
                single<Plugin> { this@Main }
                single { config }
                single { database }
                single<IslandRepository> { DatabaseIslandRepository(get()) }
                single<net.azisaba.vanilife.islands.portal.PortalRepository> { net.azisaba.vanilife.islands.portal.DatabasePortalRepository(get()) }
                single { net.azisaba.vanilife.islands.portal.PortalManager(this@Main, get(), get()) }
                // ensure PortalHologram (wrapper) class is available to Koin consumers if needed later
                single { net.azisaba.vanilife.islands.portal.PortalHologram::class }
                single<IslandManager> { IslandManager(get(), Bukkit.getIslandsWorld(), get()) }
            })
        }

        setupEventListeners(koinApp.koin)

        // load portals into memory and register commands
        try {
            val pm = koinApp.koin.get<net.azisaba.vanilife.islands.portal.PortalManager>()
            pm.loadAll()
        } catch (_: Exception) {
        }

        try {
            val portalCmd = net.azisaba.vanilife.islands.portal.PortalCommands()
            server.getPluginCommand("portal")?.setExecutor(portalCmd)
        } catch (_: Exception) {
        }
    }

    override fun onDisable() {
        koinApp.close()
        PacketEvents.getAPI().terminate()
    }
}
