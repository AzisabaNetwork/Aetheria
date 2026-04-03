package net.azisaba.vanilife.enchanting

import com.github.retrooper.packetevents.protocol.recipe.RecipeBookSettings
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal object RecipeBookStates {
    private val snapshotMap: MutableMap<UUID, Snapshot> = ConcurrentHashMap()

    fun lookup(player: Player): Snapshot? = snapshotMap[player.uniqueId]

    fun cacheDiscovered(player: Player, discovered: Set<NamespacedKey> = player.discoveredRecipes) {
        snapshotMap.compute(player.uniqueId) { _, snapshot ->
            (snapshot ?: Snapshot()).copy(discovered = discovered)
        }
    }

    fun cacheSettings(player: Player, settings: RecipeBookSettings) {
        snapshotMap.compute(player.uniqueId) { _, snapshot ->
            (snapshot ?: Snapshot()).copy(settings = RecipeBookSettings(settings.states.toMap()))
        }
    }

    data class Snapshot(
        val discovered: Set<NamespacedKey> = emptySet(),
        val settings: RecipeBookSettings? = null,
    )
}
