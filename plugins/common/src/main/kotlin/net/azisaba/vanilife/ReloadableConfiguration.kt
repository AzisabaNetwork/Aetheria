package net.azisaba.vanilife

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

const val defaultName: String = "config.yml"

val defaultYaml: Yaml = Yaml(
    configuration = YamlConfiguration(
        polymorphismStyle = PolymorphismStyle.Property,
        polymorphismPropertyName = "kind",
    )
)

fun <T : Any> Plugin.reloadableConfig(default: T, serializer: KSerializer<T>): ReloadableConfiguration<T> {
    val reloadableConfig = ReloadableConfiguration(defaultName, defaultYaml, default, serializer)
    reloadableConfig.bootstrap(this)
    return reloadableConfig
}

class ReloadableConfiguration<T : Any>(
    val name: String,
    private val yaml: Yaml,
    private val default: T,
    private val serializer: KSerializer<T>,
) : ConfigurationHolder<T> {
    private val reference: AtomicReference<T?> = AtomicReference(null)

    override fun value(): T = reference.get()
        ?: throw IllegalStateException("You are trying to access configuration too early")

    fun bootstrap(plugin: Plugin) {
        val path = plugin.dataFolder.toPath().resolve(name)
        if (!path.exists()) {
            val content = yaml.encodeToString(serializer, default)
            path.parent?.let(Path::createDirectories)
            path.writeText(
                content, options = arrayOf(
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                )
            )
        }

        val pathContent = path.readText()
        val config = yaml.decodeFromString(serializer, pathContent)

        reference.set(config)
    }
}
