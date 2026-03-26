package net.azisaba.vanilife.npc.spawn

import com.charleskorn.kaml.Yaml
import net.azisaba.vanilife.npc.NpcType
import org.bukkit.plugin.Plugin
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText

internal class NpcSpawnRuleLoader(private val plugin: Plugin) {
    private var map: Map<NpcType, NpcSpawnRule?>? = null

    fun get(type: NpcType): NpcSpawnRule? = requireLoaded()[type]

    fun loadOne(type: NpcType) {
        val reloaded = requireLoaded().toMutableMap()
        reloaded[type] = loadRule(type)
        map = reloaded.toMap()
    }

    fun loadAll() {
        map = NpcType.entries.associateWith(::loadRule)
    }

    private fun requireLoaded(): Map<NpcType, NpcSpawnRule?> = requireNotNull(map) {
        "NPC spawn rules have not been loaded yet"
    }

    private fun loadRule(type: NpcType): NpcSpawnRule? {
        val spawnsDirectory = plugin.dataFolder.toPath().resolve(SPAWNS_DIRECTORY)
        if (!spawnsDirectory.exists()) {
            spawnsDirectory.createDirectories()
        }

        val rulePath = spawnsDirectory.resolve("${type.key.value()}.$EXTENSION")
        if (!rulePath.exists()) {
            return null
        }

        return Yaml.default.decodeFromString(NpcSpawnRule.serializer(), rulePath.readText())
    }

    private companion object {
        const val EXTENSION: String = "yaml"
        const val SPAWNS_DIRECTORY: String = "spawns"
    }
}
