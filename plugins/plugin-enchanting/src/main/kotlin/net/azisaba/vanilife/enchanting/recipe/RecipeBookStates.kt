package net.azisaba.vanilife.enchanting.recipe

import com.github.retrooper.packetevents.protocol.recipe.RecipeBookSettings
import com.github.retrooper.packetevents.protocol.recipe.RecipeBookType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRecipeBookAdd
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object RecipeBookStates {
    private val snapshotMap: MutableMap<UUID, Snapshot> = ConcurrentHashMap()
    private val suppressedCachingPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun lookup(player: Player): Snapshot? = snapshotMap[player.uniqueId]

    fun cacheDiscovered(player: Player, discovered: Set<NamespacedKey> = player.discoveredRecipes) {
        updateSnapshot(player.uniqueId) { snapshot -> snapshot.copy(discovered = discovered) }
    }

    fun cacheSettings(playerId: UUID, settings: RecipeBookSettings) {
        updateSnapshot(playerId) { snapshot ->
            snapshot.copy(settings = RecipeBookSettings(settings.states.toMap()))
        }
    }

    fun createExpandedSettings(settings: RecipeBookSettings): RecipeBookSettings {
        val expanded = RecipeBookSettings(settings.states.toMap())
        for (type in RecipeBookType.values()) {
            expanded.getState(type).setOpen(true)
        }
        return expanded
    }

    fun cacheDisplay(playerId: UUID, entries: List<WrapperPlayServerRecipeBookAdd.AddEntry>, replace: Boolean) {
        updateSnapshot(playerId) { snapshot ->
            val merged = if (replace || snapshot.display == null) {
                DisplaySnapshot(entries.toList(), replace)
            } else {
                snapshot.display.merge(entries, replace)
            }
            snapshot.copy(display = merged)
        }
    }

    fun runWithoutCaching(player: Player, action: () -> Unit) {
        suppressedCachingPlayers.add(player.uniqueId)
        try {
            action()
        } finally {
            suppressedCachingPlayers.remove(player.uniqueId)
        }
    }

    fun isCachingSuppressed(player: Player): Boolean = player.uniqueId in suppressedCachingPlayers

    private fun updateSnapshot(playerId: UUID, transform: (Snapshot) -> Snapshot) {
        snapshotMap.compute(playerId) { _, snapshot ->
            transform(snapshot ?: Snapshot())
        }
    }

    data class Snapshot(
        val discovered: Set<NamespacedKey> = emptySet(),
        val settings: RecipeBookSettings? = null,
        val display: DisplaySnapshot? = null,
    )

    data class DisplaySnapshot(val entries: List<WrapperPlayServerRecipeBookAdd.AddEntry>, val replace: Boolean) {
        fun merge(newEntries: List<WrapperPlayServerRecipeBookAdd.AddEntry>, replace: Boolean): DisplaySnapshot {
            if (replace) {
                return copy(entries = newEntries.toList(), replace = true)
            }

            val mergedEntries = entries.toMutableList()
            val existingIds = mergedEntries.mapTo(mutableSetOf()) { it.contents.id.id }
            for (entry in newEntries) {
                if (existingIds.add(entry.contents.id.id)) {
                    mergedEntries.add(entry)
                }
            }
            return copy(entries = mergedEntries, replace = false)
        }
    }
}
