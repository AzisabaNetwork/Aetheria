package net.azisaba.vanilife.menuprovider

import net.azisaba.vanilife.ReloadableConfiguration
import net.azisaba.vanilife.reloadableConfig
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onEnable() {
        val config = reloadableConfig(Configuration(), Configuration.serializer())

        koinApp = startKoin {
            modules(
                module {
                    single<Plugin> { this@Main }
                    single<ReloadableConfiguration<Configuration>> { config }
                },
            )
        }

        setupEventListeners()
    }

    override fun onDisable() {
        koinApp.close()
    }
}
