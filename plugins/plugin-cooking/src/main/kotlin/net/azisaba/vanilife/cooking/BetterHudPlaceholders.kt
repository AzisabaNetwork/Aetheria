package net.azisaba.vanilife.cooking

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import net.azisaba.vanilife.cooking.CookingEffectSource
import net.azisaba.vanilife.cooking.Main
import org.bukkit.entity.Player

internal fun Main.setupCookingBetterHudPlaceholders() {
    val betterHud = BetterHud.getInstance()

    betterHud.placeholderManager.stringContainer.addPlaceholder(
        "cooking_active_effects",
        HudPlaceholder.of(
            HudPlaceholder.PlaceholderFunction.of { hudPlayer ->
                val player = hudPlayer.handle() as? Player ?: return@of null
                val active = CookingEffectSource.activeHudLabels(player)
                return@of if (active.isEmpty()) null else active.joinToString(", ")
            }
        )
    )

    betterHud.placeholderManager.booleanContainer.addPlaceholder(
        "cooking_has_effect",
        HudPlaceholder.of(
            HudPlaceholder.PlaceholderFunction.of { hudPlayer ->
                val player = hudPlayer.handle() as? Player ?: return@of false
                return@of CookingEffectSource.activeHudLabels(player).isNotEmpty()
            }
        )
    )
}
