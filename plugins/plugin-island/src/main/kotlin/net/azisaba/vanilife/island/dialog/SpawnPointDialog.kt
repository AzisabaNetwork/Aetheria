package net.azisaba.vanilife.island.dialog

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object SpawnPointDialog : KoinComponent {
    private val plugin: Plugin by inject()

    fun create(island: Island, location: Location): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(MyIslandDialog.TITLE)
                    .externalTitle(Component.translatable(IslandTranslations.DIALOG_VANILIFE_MY_ISLAND_SPAWN_POINT))
                    .body(
                        listOf(
                            DialogBody.plainMessage(Component.translatable(IslandTranslations.DIALOG_VANILIFE_SPAWN_POINT_DESCRIPTION)),
                        )
                    )
                    .build()
            )
            .type(
                DialogType.confirmation(
                    yesButton(island, location),
                    ActionButton.builder(Component.translatable("gui.no")).build(),
                )
            )
    }

    private fun yesButton(island: Island, location: Location): ActionButton =
        ActionButton.builder(Component.translatable("gui.yes"))
            .action(
                DialogAction.customClick(
                    { _, _ ->
                        plugin.launch {
                            island.spawnPoint(location)
                        }
                    },
                    ClickCallback.Options.builder()
                        .uses(ClickCallback.UNLIMITED_USES)
                        .build()
                )
            )
            .build()
}
