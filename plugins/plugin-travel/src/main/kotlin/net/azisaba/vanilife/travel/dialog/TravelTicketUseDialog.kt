package net.azisaba.vanilife.travel.dialog

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.teleport
import net.azisaba.vanilife.travel.TravelFonts
import net.azisaba.vanilife.travel.TravelTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object TravelTicketUseDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(TravelFonts.TravelIcons.TICKET)
                .font(TravelFonts.TRAVEL_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_USE))
        .build()

    private val plugin: Plugin by inject()

    fun create(island: Island, ticket: ItemStack): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(
                        listOf(
                            DialogBody.plainMessage(
                                Component.translatable(
                                    TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_USE_MESSAGE,
                                    island.displayName,
                                )
                            )
                        )
                    )
                    .build()
            )
            .type(
                DialogType.confirmation(yesButton(island, ticket), noButton())
            )
    }

    private fun yesButton(island: Island, ticket: ItemStack) = ActionButton.builder(
        Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_USE_YES)
    ).action(
        DialogAction.customClick(
            { _, audience ->
                val player = audience as? Player ?: return@customClick
                plugin.launch(plugin.entityDispatcher(player)) {
                    player.teleport(island)
                    if (!player.gameMode.isInvulnerable) {
                        ticket.subtract()
                    }
                }
            },
            ClickCallback.Options.builder().build()
        )
    ).build()

    private fun noButton(): ActionButton = ActionButton.builder(
        Component.translatable(TravelTranslations.DIALOG_VANILIFE_TRAVEL_TICKET_USE_NO)
    ).build()
}
