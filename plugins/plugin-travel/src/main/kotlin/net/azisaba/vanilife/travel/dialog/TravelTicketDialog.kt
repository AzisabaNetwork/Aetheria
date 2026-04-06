package net.azisaba.vanilife.travel.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.loadedIslands
import net.azisaba.vanilife.travel.TravelFonts
import net.azisaba.vanilife.travel.TravelTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal object TravelTicketDialog {
    val TITLE: Component = Component.text()
        .append(
            Component.text(TravelFonts.TravelIcons.TICKET)
                .font(TravelFonts.TRAVEL_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET))
        .build()

    fun create(player: Player, ticket: ItemStack): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .inputs(
                        listOf(
                            DialogInput.text(
                                "query",
                                Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_BOX)
                            ).build()
                        )
                    )
                    .pause(false)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .build()
            )
            .type(
                DialogType.multiAction(
                    buildList {
                        add(searchButton(player, ticket))

                        addAll(featured(player).map { island -> resultButton(island, ticket) })
                    }
                ).exitAction(exitButton()).columns(1).build()
            )
    }

    internal fun resultButton(island: Island, ticket: ItemStack): ActionButton = ActionButton.builder(
        Component.text()
            .append(Component.`object`(ObjectContents.playerHead(island.ownerProfile)))
            .appendSpace()
            .append(island.displayName)
            .build()
    ).tooltip(
        Component.text()
            .append(
                Component.translatable(
                    TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_LEVEL,
                    NamedTextColor.GRAY,
                    Component.text()
                        .color(NamedTextColor.WHITE)
                        .append(
                            Component.text(IslandFonts.LevelIcons.levelOf(island.level)).font(IslandFonts.LEVEL_ICONS)
                        )
                        .append(Component.text(island.level))
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
                        .append(Component.text(island.visitors.size))
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
                        .append(Component.`object`(ObjectContents.playerHead(island.ownerProfile)))
                        .appendSpace()
                        .append(Component.text(island.ownerProfile.name!!))
                        .build(),
                )
            )
            .build()
    ).action(
        DialogAction.customClick(
            { _, audience -> audience.showDialog(TravelTicketUseDialog.create(island, ticket)) },
            ClickCallback.Options.builder().build(),
        )
    ).build()

    private fun searchButton(player: Player, ticket: ItemStack): ActionButton = ActionButton.builder(
        Component.text()
            .append(
                Component.text(TravelFonts.TravelIcons.SEARCH)
                    .font(TravelFonts.TRAVEL_ICONS)
            )
            .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_APPLY_FILTER))
            .build()
    ).action(
        DialogAction.customClick(
            { response, audience ->
                val query = response.getText("query")!!
                if (query.isNotBlank()) {
                    audience.showDialog(TravelTicketSearchDialog.create(query, player, ticket))
                }
            },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).width(200).build()

    private fun exitButton(): ActionButton = ActionButton.builder(Component.translatable("gui.done")).action(
        DialogAction.customClick(
            { _, audience -> audience.closeDialog() },
            ClickCallback.Options.builder().build()
        )
    ).width(200).build()

    private fun featured(player: Player): List<Island> = Vanilife.getIslandsWorld().loadedIslands
        .filter { island -> island.owner != player.uniqueId }
        .sortedByDescending { it.level }
        .take(99)
}
