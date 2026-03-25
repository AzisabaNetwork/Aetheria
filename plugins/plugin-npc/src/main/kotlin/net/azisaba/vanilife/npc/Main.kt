package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication
    internal lateinit var npcOffersLoader: NpcOffersLoader
        private set

    override fun onEnable() {
        npcOffersLoader = NpcOffersLoader(this).also { it.loadAll() }
        koinApp = startKoin {
            modules(module {
                single<Plugin> { this@Main }
                single<NpcOffersLoader> { npcOffersLoader }
            })
        }
    }

    override fun onDisable() {
        koinApp.close()
    }
}
