package net.azisaba.vanilife.menuprovider.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.set.RegistrySet
import net.azisaba.vanilife.island.IslandFeature
import net.azisaba.vanilife.island.dialog.DisplayNameDialog
import net.azisaba.vanilife.island.dialog.SkyColorDialog
import net.azisaba.vanilife.island.dialog.SpawnLocationDialog
import net.azisaba.vanilife.island.ownedIsland
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player

internal object SettingsDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.SETTINGS)
                .shadowColor(ShadowColor.none())
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_SETTINGS))
        .build()

    suspend fun create(player: Player): Dialog {
        val island = player.ownedIsland()

        return Dialog.create { builder ->
            builder.empty()
                .base(DialogBase.builder(TITLE).build())
                .type(
                    DialogType.dialogList(
                        RegistrySet.valueSet(
                            RegistryKey.DIALOG,
                            buildList {
                                add(DisplayNameDialog.create(island))

                                if (island.isEnabled(IslandFeature.CUSTOM_SPAWN_POINT)) {
                                    add(SpawnLocationDialog.create(island))
                                }

                                if (island.isEnabled(IslandFeature.CUSTOM_SKY_COLOR)) {
                                    add(SkyColorDialog.create())
                                }
                            }
                        )
                    ).exitAction(MenuDialog.backToMenuButton()).build()
                )
        }
    }
}
