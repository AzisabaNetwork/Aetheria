package net.azisaba.vanilife.npc.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.npc.NpcMode
import net.azisaba.vanilife.npc.NpcTranslations
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback

object NpcMenuDialog {
    fun create(npcWrapper: NpcWrapper): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(
                    Component.text().append(npcWrapper.npcType.iconComponent())
                        .append(npcWrapper.customName() ?: Component.text("ねこ" +
                                "")).build()
                )
                    .inputs(
                        listOf(
                            DialogInput.singleOption(
                                "mode",
                                Component.translatable(NpcTranslations.NPC_MODE),
                                NpcMode.entries.map { npcMode ->
                                    SingleOptionDialogInput.OptionEntry.create(
                                        npcMode.key.value(),
                                        Component.translatable(npcMode.translationKey()),
                                        npcWrapper.mode == npcMode,
                                    )
                                }
                            ).build()
                        )
                    ).build()
            )
            .type(
                DialogType.multiAction(
                    listOf(
                        ActionButton.builder(Component.translatable(NpcTranslations.DIALOG_VANILIFE_NPC_MENU_DONE))
                            .action(
                                DialogAction.customClick(
                                    { response, _ ->
                                        updateMode(npcWrapper, response.getText("mode"))
                                    },
                                    ClickCallback.Options.builder()
                                        .uses(ClickCallback.UNLIMITED_USES)
                                        .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                        .build()
                                )
                            )
                            .build(),
                    )
                ).build()
            )
    }

    private fun updateMode(npcWrapper: NpcWrapper, value: String?) {
        val mode = value?.let(NpcMode::byValue) ?: return
        npcWrapper.mode = mode
    }
}
