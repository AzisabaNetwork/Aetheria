package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onEnable() {
        val npcOffersLoader = NpcOffersLoader(this).also(NpcOffersLoader::loadAll)
        val npcContainer = NpcContainer()

        koinApp = startKoin {
            modules(module {
                single<Plugin> { this@Main }
                single { npcOffersLoader }
                single { npcContainer }
            })
        }

        setupEventListeners(koinApp.koin)
    }

    override fun onDisable() {
        koinApp.close()
    }
}
