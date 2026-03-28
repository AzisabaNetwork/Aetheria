package net.azisaba.vanilife

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.KSerializer
import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.*

abstract class DynamicContents<T>(
    val name: String, private val serializer: KSerializer<T>, private val yaml: Yaml = DEFAULT_YAML,
) {
    private val mapReference: AtomicReference<Map<Key, T>?> = AtomicReference(null)

    fun byKey(key: Key): Holder<T>? = requireLoaded()[key]?.let { Holder(key, it) }

    fun all(): Collection<T> = requireLoaded().values

    fun bootstrap(plugin: Plugin) {
        val contentsRoot = plugin.dataFolder.toPath().resolve(name)
        contentsRoot.createDirectories()

        require(contentsRoot.isDirectory()) {
            "Path is not a directory: $contentsRoot"
        }

        val newMap = contentsRoot.listDirectoryEntries()
            .filter(Path::isDirectory)
            .flatMap { namespaceDir ->
                namespaceDir.listDirectoryEntries("*.yml")
                    .sortedBy(Path::name)
                    .map { file -> file.key() to file.deserialized() }
            }
            .toMap()

        mapReference.set(newMap)
    }

    private fun requireLoaded(): Map<Key, T> =
        mapReference.get() ?: throw IllegalStateException("You are trying to access contents '$name' too early")

    private fun Path.key(): Key {
        val namespace = parent?.name ?: throw IllegalArgumentException("Cannot derive namespace from path: $this")
        val value = nameWithoutExtension
        return try {
            Key.key(namespace, value)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid content key: $namespace:$value ($this)", e)
        }
    }

    private fun Path.deserialized(): T {
        val text = try {
            readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read file: $this", e)
        }

        return try {
            yaml.decodeFromString(serializer, text)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse YAML file: $this\n$text", e)
        }
    }

    private companion object {
        val DEFAULT_YAML: Yaml = Yaml(
            configuration = YamlConfiguration(
                polymorphismStyle = PolymorphismStyle.Property,
                polymorphismPropertyName = "kind",
            )
        )
    }

    data class Holder<T>(val key: Key, val value: T)
}
