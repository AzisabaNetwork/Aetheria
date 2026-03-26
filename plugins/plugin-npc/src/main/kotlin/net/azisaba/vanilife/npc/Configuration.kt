package net.azisaba.vanilife.npc

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.azisaba.vanilife.npc.spawn.NpcNaturalSpawner
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val defaultYaml: Yaml = Yaml.default

internal fun Main.yamlConfig(yaml: Yaml = defaultYaml): Configuration {
    val path = dataFolder.toPath().resolve("config.yaml")
    if (!path.exists()) {
        val content = yaml.encodeToString(Configuration.serializer(), Configuration())
        path.parent?.createDirectories()
        path.writeText(
            content,
            options = arrayOf(
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
            )
        )
    }
    val pathContent = path.readText()
    return yaml.decodeFromString(Configuration.serializer(), pathContent)
}

@Serializable
internal data class Configuration(
    val naturalSpawner: NpcNaturalSpawner.Configuration = NpcNaturalSpawner.Configuration(),
)
