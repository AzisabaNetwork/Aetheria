package net.azisaba.vanilife.travel.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.loadedIslands
import net.azisaba.vanilife.travel.TravelFonts
import net.azisaba.vanilife.travel.TravelTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

object TravelDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(TravelFonts.TravelIcons.TICKET)
                .font(TravelFonts.TRAVEL_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL))
        .build()

    fun create(itemStack: ItemStack): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .inputs(
                        listOf(
                            DialogInput.text("query", Component.text("検索..."))
                                .build()
                        )
                    )
                    .build()
            )
            .type(
                DialogType.multiAction(
                    buildList {
                        add(
                            ActionButton.builder(Component.text("検索"))
                                .build()
                        )

                        addAll(
                            Vanilife.getIslandsWorld()
                                .loadedIslands
                                .map { it.islandButton() }
                        )
                    }
                ).columns(1).build()
            )
    }

    private fun Island.islandButton(): ActionButton {
        return ActionButton.builder(displayName)
            .tooltip(
                Component.text()
                    .append(Component.`object`(ObjectContents.playerHead(owner)))
                    .appendSpace()
                    .append(Component.text(Bukkit.getOfflinePlayer(owner).name!!))
                    .appendNewline()
                    .append(
                        Component.text(IslandFonts.LevelIcons.levelOf(level))
                            .font(IslandFonts.LEVEL_ICONS)
                    )
                    .append(Component.text(level))
                    .build()
            )
            .build()
    }
}
