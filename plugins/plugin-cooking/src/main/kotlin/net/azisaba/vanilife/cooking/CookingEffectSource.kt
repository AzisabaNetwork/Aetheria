package net.azisaba.vanilife.cooking

import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object CookingEffectSource {
    // playerId -> (hudKey -> expiryMillis)
    private val effects = ConcurrentHashMap<Int, ConcurrentHashMap<String, Long>>()

    fun add(player: Player, hudKey: String, durationTicks: Int) {
        val playerMap = effects.computeIfAbsent(player.entityId) { ConcurrentHashMap() }
        val expiry = System.currentTimeMillis() + durationTicks * 50L
        playerMap[hudKey] = expiry
    }

    fun isActive(player: Player, hudKey: String): Boolean {
        val playerMap = effects[player.entityId] ?: return false
        val expiry = playerMap[hudKey] ?: return false
        if (expiry <= System.currentTimeMillis()) {
            playerMap.remove(hudKey)
            return false
        }
        return true
    }

    fun activeHudLabels(player: Player): List<String> {
        val now = System.currentTimeMillis()
        val playerMap = effects[player.entityId] ?: return emptyList()
        val active = mutableListOf<String>()
        val iterator = playerMap.entries.iterator()
        while (iterator.hasNext()) {
            val (k, v) = iterator.next()
            if (v <= now) iterator.remove() else active.add(k)
        }
        return active
    }
}
