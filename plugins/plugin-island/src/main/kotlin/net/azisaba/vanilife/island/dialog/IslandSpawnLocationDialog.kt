package net.azisaba.vanilife.island.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component

internal object IslandSpawnLocationDialog {
    fun create(): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(
                    Component.text()
                        .append(Component.text(IslandFonts.IslandIcons.SPAWN_LOCATION).font(IslandFonts.ISLAND_ICONS))
                        .appendSpace()
                        .append(Component.translatable(IslandTranslations.ISLAND_SPAWN_LOCATION))
                        .build()
                ).build()
            )
            .type(
                DialogType.notice()
            )
    }
}
