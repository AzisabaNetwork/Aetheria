package net.azisaba.vanilife.travel.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.loadedIslands
import net.azisaba.vanilife.travel.TravelFonts
import net.azisaba.vanilife.travel.TravelTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.entity.Player

object TravelDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(TravelFonts.TravelIcons.TICKET)
                .font(TravelFonts.TRAVEL_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL))
        .build()

    fun create(player: Player): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .build()
            )
            .type(
                DialogType.notice()
                /* DialogType.multiAction(
                    Vanilife.getIslandsWorld().loadedIslands.map { it.toActionButton() }
                ).columns(1).build() */
            )
    }

    /* private fun Island.toActionButton(): ActionButton = ActionButton.builder(displayName)
        .tooltip(
            Component.text()
                .append(
                    Component.translatable(
                        TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_LEVEL,
                        NamedTextColor.GRAY,
                        Component.text()
                            .color(NamedTextColor.WHITE)
                            .append(
                                Component.text(IslandFonts.IslandLevelIcons.levelOf(level))
                                    .font(IslandFonts.ISLAND_LEVEL_ICONS)
                            )
                            .append(Component.text(level))
                            .build(),
                    )
                )
                .appendNewline()
                .append(
                    Component.translatable(
                        TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_VISITORS,
                        NamedTextColor.GRAY,
                        Component.text()
                            .color(NamedTextColor.WHITE)
                            .append(
                                Component.text(TravelFonts.TravelIcons.VISITORS, NamedTextColor.GRAY)
                                    .font(TravelFonts.TRAVEL_ICONS)
                            )
                            .append(Component.text(visitors.size))
                            .build(),
                    )
                )
                .appendNewline()
                .append(
                    Component.translatable(
                        TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_OWNER,
                        NamedTextColor.GRAY,
                        Component.text()
                            .color(NamedTextColor.WHITE)
                            .append(Component.`object`(ObjectContents.playerHead(ownerProfile)))
                            .appendSpace()
                            .append(Component.text(ownerProfile.name!!))
                            .build(),
                    )
                )
                .build()
        )
        .build() */
}
