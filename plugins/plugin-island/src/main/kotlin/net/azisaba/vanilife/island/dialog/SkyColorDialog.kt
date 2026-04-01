package net.azisaba.vanilife.island.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor

object SkyColorDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(IslandFonts.IslandIcons.SKY_COLOR)
                .shadowColor(ShadowColor.none())
                .font(IslandFonts.ISLAND_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(IslandTranslations.DIALOG_VANILIFE_SKY_COLOR))
        .build()

    fun create(): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(DialogBase.builder(TITLE).build())
            .type(DialogType.notice())
    }
}
