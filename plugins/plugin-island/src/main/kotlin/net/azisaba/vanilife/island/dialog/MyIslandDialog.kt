package net.azisaba.vanilife.island.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.set.RegistrySet
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.azisaba.vanilife.island.isInOwnedIsland
import net.azisaba.vanilife.island.ownedIsland
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player

object MyIslandDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(IslandFonts.IslandIcons.SETTINGS)
                .font(IslandFonts.ISLAND_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(IslandTranslations.DIALOG_VANILIFE_MY_ISLAND))
        .build()

    fun create(player: Player): Dialog = Dialog.create { builder ->
        val island = player.ownedIsland

        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(
                        listOf(
                            DialogBody.plainMessage(
                                Component.text()
                                    .append(island.displayName)
                                    .appendSpace()
                                    .append(
                                        Component.text(IslandFonts.IslandLevelIcons.levelOf(island.level))
                                            .font(IslandFonts.ISLAND_LEVEL_ICONS)
                                    )
                                    .append(Component.text(island.level))
                                    .build()
                            ),
                        )
                    ).build()
            )
            .type(
                DialogType.dialogList(
                    RegistrySet.valueSet(
                        RegistryKey.DIALOG,
                        buildList {
                            add(DisplayNameDialog.create(island))

                            if (player.isInOwnedIsland) {
                                add(SpawnPointDialog.create(island, player.location))
                            }
                        }
                    )
                ).exitAction(exitButton()).columns(1).build()
            )
    }

    private fun displayNameButton(): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_MY_ISLAND_DISPLAY_NAME)
    ).build()

    private fun spawnPointButton(): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_MY_ISLAND_SPAWN_POINT)
    ).build()

    private fun exitButton(): ActionButton = ActionButton.builder(Component.translatable("gui.done"))
        .action(
            DialogAction.customClick(
                { _, audience -> audience.closeDialog() },
                ClickCallback.Options.builder().build()
            )
        ).build()
}
