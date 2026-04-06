package net.azisaba.vanilife.travel.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.loadedIslands
import net.azisaba.vanilife.travel.TravelFonts
import net.azisaba.vanilife.travel.TravelTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal object TravelTicketSearchDialog {
    fun create(query: String, player: Player, ticket: ItemStack): Dialog = Dialog.create { builder ->
        val searchResult = search(query, player)
        builder.empty()
            .base(
                DialogBase.builder(
                    Component.text()
                        .append(
                            Component.text(TravelFonts.TravelIcons.TICKET)
                                .font(TravelFonts.TRAVEL_ICONS)
                        )
                        .append(
                            Component.translatable(
                                TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH,
                                Component.text(query, NamedTextColor.AQUA),
                                Component.text(
                                    searchResult.size,
                                    if (searchResult.isNotEmpty()) NamedTextColor.GRAY else NamedTextColor.RED,
                                ),
                            )
                        )
                        .build()
                ).inputs(
                    listOf(
                        DialogInput.text(
                            "query",
                            Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_BOX)
                        ).initial(query).build()
                    )
                ).pause(false).afterAction(DialogBase.DialogAfterAction.NONE).build()
            )
            .type(
                DialogType.multiAction(
                    buildList {
                        add(searchAgainButton(player, ticket))

                        addAll(searchResult.map { island -> TravelTicketDialog.resultButton(island, ticket) })
                    }
                ).exitAction(exitButton(player, ticket)).columns(1).build()
            )
    }

    private fun searchAgainButton(player: Player, ticket: ItemStack): ActionButton = ActionButton.builder(
        Component.text()
            .append(
                Component.text(TravelFonts.TravelIcons.SEARCH)
                    .font(TravelFonts.TRAVEL_ICONS)
            )
            .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_AGAIN))
            .build()
    ).action(
        DialogAction.customClick(
            { response, audience ->
                val newQuery = response.getText("query")!!
                if (newQuery.isNotBlank()) {
                    audience.showDialog(create(newQuery, player, ticket))
                }
            },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).width(200).build()

    private fun exitButton(player: Player, ticket: ItemStack): ActionButton =
        ActionButton.builder(Component.translatable("gui.back"))
            .action(
                DialogAction.customClick(
                    { _, audience -> audience.showDialog(TravelTicketDialog.create(player, ticket)) },
                    ClickCallback.Options.builder().build()
                )
            ).width(200).build()

    private fun search(query: String, player: Player): List<Island> = Vanilife.getIslandsWorld().loadedIslands
        .filter { island -> island.owner != player.uniqueId }
        .filter { island ->
            val ownerName = island.ownerProfile.name ?: ""
            val displayName = PlainTextComponentSerializer.plainText().serialize(island.displayName)
            ownerName.contains(query, ignoreCase = true) || displayName.contains(query, ignoreCase = true)
        }
}
