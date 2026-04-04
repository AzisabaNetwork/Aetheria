package net.azisaba.vanilife.enchanting.recipe

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object RecipeBookToastSuppressor {
    private val suppressedPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun runWithoutToast(player: Player, action: () -> Unit) {
        suppressedPlayers.add(player.uniqueId)
        try {
            action()
        } finally {
            suppressedPlayers.remove(player.uniqueId)
        }
    }

    fun shouldSuppress(player: Player): Boolean = player.uniqueId in suppressedPlayers
}
