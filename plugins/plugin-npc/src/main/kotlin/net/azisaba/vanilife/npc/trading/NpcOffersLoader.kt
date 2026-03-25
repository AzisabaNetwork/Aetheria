package net.azisaba.vanilife.npc.trading

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import net.azisaba.vanilife.npc.NpcType
import org.bukkit.plugin.Plugin
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

internal class NpcOffersLoader(private val plugin: Plugin) {
    private var map: Map<NpcType, NpcOffers>? = null

    fun get(type: NpcType): NpcOffers = requireLoaded()[type]
        ?: error("NPC offers for ${type.key.asString()} are not loaded")

    fun reloadOne(type: NpcType) {
        val reloaded = requireLoaded().toMutableMap()
        reloaded[type] = loadOffers(type)
        map = reloaded.toMap()
    }

    fun loadAll() {
        map = NpcType.entries.associateWith(::loadOffers)
    }

    private fun requireLoaded(): Map<NpcType, NpcOffers> = requireNotNull(map) {
        "NPC offers have not been loaded yet"
    }

    private fun loadOffers(type: NpcType): NpcOffers {
        val offersDirectory = plugin.dataFolder
            .toPath()
            .resolve("offers")
            .resolve(type.key.asString())

        if (!offersDirectory.exists()) {
            offersDirectory.createDirectories()
            return NpcOffers(emptyList())
        }

        val offers = Files.list(offersDirectory).use { paths ->
            paths.filter(Path::isRegularFile)
                .filter { it.extension == EXTENSION }
                .sorted(compareBy(Path::name))
                .toList()
                .map { path ->
                    yaml.decodeFromString(NpcOffer.serializer(), path.readText())
                }
        }
        return NpcOffers(offers)
    }

    private companion object {
        const val EXTENSION: String = "yaml"

        val yaml = Yaml(
            configuration = YamlConfiguration(
                polymorphismStyle = PolymorphismStyle.Property,
                polymorphismPropertyName = "kind",
            ),
        )
    }
}
