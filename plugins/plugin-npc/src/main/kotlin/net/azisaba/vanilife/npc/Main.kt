package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.spawn.NpcNaturalSpawner
import net.azisaba.vanilife.npc.spawn.NpcSpawnRuleLoader
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onEnable() {
        val config = yamlConfig()

        val npcOffersLoader = NpcOffersLoader(this).also(NpcOffersLoader::loadAll)
        val npcSpawnRuleLoader = NpcSpawnRuleLoader(this).also(NpcSpawnRuleLoader::loadAll)

        koinApp = startKoin {
            modules(module {
                single<Configuration> { config }
                single<Plugin> { this@Main }
                single<NpcNaturalSpawner> {
                    NpcNaturalSpawner(get<Configuration>().naturalSpawner, npcSpawnRuleLoader)
                }
                single<NpcOffersLoader> { npcOffersLoader }
                single<NpcSpawnRuleLoader> { npcSpawnRuleLoader }
                single<NpcContainer> { NpcContainer() }
            })
        }

        setupEventListeners(koinApp.koin)
    }

    override fun onDisable() {
        koinApp.close()
    }
}
