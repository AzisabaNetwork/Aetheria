package net.azisaba.vanilife.enchanting

import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.reloadableConfig
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onEnable() {
        val config = reloadableConfig(Configuration(), Configuration.serializer())
        val database = config.value().database.createConnection()

        koinApp = startKoin {
            modules(
                module {
                    single<Plugin> { this@Main }
                    single<Database> { database }
                },
            )
        }

        setupEventListeners(koinApp.koin)

        launch {
            UnlockRateSource.bootstrap(database)
        }
    }

    override fun onDisable() {
        koinApp.close()
    }
}
