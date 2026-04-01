package net.azisaba.vanilife.island.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor

object DisplayNameDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(IslandFonts.IslandIcons.DISPLAY_NAME)
                .shadowColor(ShadowColor.none())
                .font(IslandFonts.ISLAND_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME))
        .build()

    fun create(): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .inputs(
                        listOf(
                            DialogInput.text(
                                "new_name",
                                Component.translatable(IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME)
                            ).build(),
                        )
                    )
                    .build()
            )
            .type(DialogType.notice())
    }
}
