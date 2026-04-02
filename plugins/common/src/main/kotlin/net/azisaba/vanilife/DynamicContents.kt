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
    val name: String, private val lazySerializer: Lazy<KSerializer<T>>, private val yaml: Yaml = DEFAULT_YAML,
) : Contents<T> {
    private val mapReference: AtomicReference<Map<Key, T>?> = AtomicReference(null)

    override fun byKey(key: Key): T? = requireLoaded()[key]

    fun bootstrap(plugin: Plugin) {
        val contentsRoot = plugin.dataFolder.toPath().resolve(name)
        contentsRoot.createDirectories()

        require(contentsRoot.isDirectory()) {
            "Path is not a directory: $contentsRoot"
        }

        val newMap = contentsRoot.walk()
            .filter { it.isRegularFile() && it.extension == "yml" }
            .associate { file -> file.keyFromRoot(contentsRoot) to file.deserialized() }

        mapReference.set(newMap)
    }

    override fun iterator(): Iterator<T> = requireLoaded().values.iterator()

    private fun requireLoaded(): Map<Key, T> = mapReference.get()
        ?: throw IllegalStateException("You are trying to access contents '$name' too early")

    private fun Path.keyFromRoot(root: Path): Key {
        val relative = root.relativize(this)

        val namespace = relative.getName(0).toString()

        val value = (1 until relative.nameCount)
            .joinToString("/") { relative.getName(it).toString() }
            .removeSuffix(".yml")

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
            yaml.decodeFromString(lazySerializer.value, text)
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
}
