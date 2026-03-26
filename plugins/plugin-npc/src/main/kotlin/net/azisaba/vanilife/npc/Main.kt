package net.azisaba.vanilife.npc

import net.azisaba.vanilife.npc.wrapper.NpcWrapperMap
import net.azisaba.vanilife.npc.spawn.NpcNaturalSpawner
import net.azisaba.vanilife.npc.spawn.NpcSpawnRuleLoader
import net.azisaba.vanilife.npc.trading.NpcOffersLoader
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicReference

class Main : JavaPlugin() {
    private lateinit var koinApp: KoinApplication

    override fun onEnable() {
        val config = yamlConfig()

        val npcOffersLoader = NpcOffersLoader(this).also(NpcOffersLoader::loadAll)
        val npcSpawnRuleLoader = NpcSpawnRuleLoader(this).also(NpcSpawnRuleLoader::loadAll)
        val configurationReference = AtomicReference(config)
        val npcNaturalSpawnerReference = AtomicReference(NpcNaturalSpawner(config.naturalSpawner, npcSpawnRuleLoader))

        koinApp = startKoin {
            modules(module {
                single<Plugin> { this@Main }
                single<AtomicReference<Configuration>> { configurationReference }
                single<AtomicReference<NpcNaturalSpawner>> { npcNaturalSpawnerReference }
                single<NpcOffersLoader> { npcOffersLoader }
                single<NpcSpawnRuleLoader> { npcSpawnRuleLoader }
                single<NpcWrapperMap> { NpcWrapperMap() }
            })
        }

        setupEventListeners(koinApp.koin)
    }

    override fun onDisable() {
        koinApp.close()
    }
}
