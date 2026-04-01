package net.azisaba.vanilife.island.dialog

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object SpawnLocationDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(IslandFonts.IslandIcons.SPAWN_LOCATION)
                .shadowColor(ShadowColor.none())
                .font(IslandFonts.ISLAND_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(IslandTranslations.DIALOG_VANILIFE_SPAWN_LOCATION))
        .build()

    private val plugin: Plugin by inject()

    fun create(island: Island): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(
                        listOf(
                            DialogBody.plainMessage(Component.translatable(IslandTranslations.DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION))
                        )
                    )
                    .build()
            )
            .type(
                DialogType.confirmation(
                    yesButton(island),
                    ActionButton.builder(Component.translatable("gui.no")).build(),
                )
            )
    }

    private fun yesButton(island: Island): ActionButton = ActionButton.builder(Component.translatable("gui.yes"))
        .action(
            DialogAction.customClick(
                { _, audience ->
                    val player = audience as? Player ?: return@customClick
                    plugin.launch {
                        island.spawnPoint(player.location)
                    }
                    player.closeDialog()
                },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        )
        .build()
}
