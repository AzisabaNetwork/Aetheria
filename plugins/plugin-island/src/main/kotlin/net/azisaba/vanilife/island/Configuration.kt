package net.azisaba.vanilife.island

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val defaultYaml: Yaml = Yaml.default

internal fun Main.yamlConfig(yaml: Yaml = defaultYaml): Configuration {
    val path = dataFolder.toPath().resolve("config.yml")
    if (!path.exists()) {
        val content = yaml.encodeToString(Configuration.serializer(), Configuration())
        path.parent?.let { Files.createDirectories(it) }
        path.writeText(
            content, options = arrayOf(
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            )
        )
    }
    val pathContent = path.readText()
    return yaml.decodeFromString(Configuration.serializer(), pathContent)
}

@Serializable
internal data class Configuration(
    val database: DatabaseConfiguration = DatabaseConfiguration(),
)

@Serializable
internal data class DatabaseConfiguration(
    val url: String = "jdbc:postgresql://localhost:5432/vanilife",
    val usernameEnv: String = "DATABASE_USERNAME",
    val passwordEnv: String = "DATABASE_PASSWORD",
    val maxPoolSize: Int = 12,
)
