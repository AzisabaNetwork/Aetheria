package net.azisaba.vanilife.island

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

internal fun Main.setupIslandBetterHudPlaceholders() {
    val betterHud = BetterHud.getInstance()

    val miniMessage = MiniMessage.miniMessage()

    betterHud.placeholderManager.stringContainer.addPlaceholder(
        "island_display_name",
        HudPlaceholder.of(
            HudPlaceholder.PlaceholderFunction.of { hudPlayer ->
                val player = hudPlayer.handle() as? Player ?: return@of null
                val island = IslandPlayerMap.lookup(player.uniqueId)
                return@of island?.displayName?.let(miniMessage::serialize) ?: ""
            }
        )
    )

    betterHud.placeholderManager.numberContainer.addPlaceholder(
        "island_level",
        HudPlaceholder.of(
            HudPlaceholder.PlaceholderFunction.of { hudPlayer ->
                val player = hudPlayer.handle() as? Player ?: return@of null
                val island = IslandPlayerMap.lookup(player.uniqueId)
                return@of island?.level ?: 0
            }
        )
    )

    betterHud.placeholderManager.booleanContainer.addPlaceholder(
        "island_visible",
        HudPlaceholder.of(
            HudPlaceholder.PlaceholderFunction.of { hudPlayer ->
                val player = hudPlayer.handle() as? Player ?: return@of false
                return@of IslandPlayerMap.lookup(player.uniqueId) != null
            }
        )
    )
}
