package net.azisaba.vanilife.island.dialog

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.azisaba.vanilife.island.isInOwnedIsland
import net.azisaba.vanilife.island.ownedIsland
import net.azisaba.vanilife.island.teleport
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ReturnDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(IslandFonts.IslandIcons.PORTAL)
                .font(IslandFonts.ISLAND_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(IslandTranslations.DIALOG_VANILIFE_RETURN))
        .build()

    private val plugin: Plugin by inject()

    fun create(player: Player): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(listOf(messageBody(player)))
                    .build()
            )
            .type(
                DialogType.confirmation(
                    yesButton(),
                    noButton()
                )
            )
    }

    private fun messageBody(player: Player): DialogBody = if (player.isInOwnedIsland) {
        DialogBody.plainMessage(Component.translatable(IslandTranslations.DIALOG_VANILIFE_RETURN_MESSAGE_ALREADY_ON_ISLAND))
    } else {
        DialogBody.plainMessage(Component.translatable(IslandTranslations.DIALOG_VANILIFE_RETURN_MESSAGE))
    }

    private fun yesButton(): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_RETURN_YES)
    ).action(
        DialogAction.customClick(
            { _, audience ->
                val player = audience as? Player ?: return@customClick
                plugin.launch {
                    player.teleport(player.ownedIsland)
                }
            },
            ClickCallback.Options.builder().build()
        )
    ).build()

    private fun noButton(): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_RETURN_NO)
    ).build()
}
