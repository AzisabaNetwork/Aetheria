package net.azisaba.vanilife.island.dialog

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object DisplayNameDialog : KoinComponent {
    private val plugin: Plugin by inject()

    fun create(island: Island): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(MyIslandDialog.TITLE)
                    .externalTitle(Component.translatable(IslandTranslations.DIALOG_VANILIFE_MY_ISLAND_DISPLAY_NAME))
                    .body(
                        listOf(
                            DialogBody.plainMessage(
                                Component.translatable(
                                    IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME_RULE,
                                    NamedTextColor.RED,
                                )
                            )
                        )
                    )
                    .inputs(
                        listOf(
                            DialogInput.text(
                                "display_name",
                                Component.translatable(IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME_LABEL)
                            ).initial(
                                MiniMessage.miniMessage().serialize(island.displayName)
                            ).build(),
                        )
                    )
                    .build()
            )
            .type(
                DialogType.confirmation(yesButton(island), noButton())
            )
    }

    private fun yesButton(island: Island): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME_YES)
    ).action(
        DialogAction.customClick(
            { response, _ ->
                val raw = response.getText("display_name")?.takeIf(String::isNotBlank) ?: return@customClick
                val newDisplayName = Component.text(raw)
                plugin.launch {
                    island.displayName(newDisplayName)
                }
            },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).build()

    private fun noButton(): ActionButton = ActionButton.builder(
        Component.translatable(IslandTranslations.DIALOG_VANILIFE_DISPLAY_NAME_NO)
    ).build()
}
