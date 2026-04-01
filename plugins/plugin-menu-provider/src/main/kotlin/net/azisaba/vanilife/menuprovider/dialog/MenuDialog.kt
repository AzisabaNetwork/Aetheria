package net.azisaba.vanilife.menuprovider.dialog

import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.azisaba.vanilife.menuprovider.inventory.TrashInventory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object MenuDialog : KoinComponent {
    private val plugin: Plugin by inject()

    fun bootstrap(builder: DialogRegistryEntry.Builder) {
        builder
            .base(
                DialogBase.builder(
                    Component.text()
                        .append(
                            Component.text(MenuProviderFonts.MenuIcons.MENU)
                                .font(MenuProviderFonts.MENU_ICONS)
                                .shadowColor(ShadowColor.none())
                        )
                        .appendSpace()
                        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU))
                        .build()
                ).externalTitle(
                    Component.text()
                        .append(
                            Component.text(MenuProviderFonts.MenuIcons.MENU)
                                .font(MenuProviderFonts.MENU_ICONS)
                                .shadowColor(ShadowColor.none())
                        )
                        .appendSpace()
                        .append(
                            Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU, NamedTextColor.YELLOW)
                        )
                        .build()
                ).build()
            )
            .type(
                DialogType.multiAction(
                    listOf(
                        ActionButton.builder(
                            Component.text()
                                .append(
                                    Component.text(MenuProviderFonts.MenuIcons.QUICK_ACTIONS)
                                        .shadowColor(ShadowColor.none())
                                        .font(MenuProviderFonts.MENU_ICONS)
                                )
                                .appendSpace()
                                .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_QUICK_ACTIONS))
                                .build(),
                        ).build(),
                        ActionButton.builder(
                            Component.text()
                                .append(
                                    Component.text(MenuProviderFonts.MenuIcons.TRASH)
                                        .shadowColor(ShadowColor.none())
                                        .font(MenuProviderFonts.MENU_ICONS)
                                )
                                .appendSpace()
                                .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_TRASH))
                                .build(),
                        ).action(
                            DialogAction.customClick(
                                { _, audience -> (audience as? Player)?.openInventory(TrashInventory(plugin).inventory) },
                                ClickCallback.Options.builder()
                                    .uses(ClickCallback.UNLIMITED_USES)
                                    .build()
                            )
                        ).build(),
                        ActionButton.builder(
                            Component.text()
                                .append(
                                    Component.text(MenuProviderFonts.MenuIcons.DISCORD)
                                        .shadowColor(ShadowColor.none())
                                        .font(MenuProviderFonts.MENU_ICONS)
                                )
                                .appendSpace()
                                .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_DISCORD))
                                .build(),
                        ).action(
                            DialogAction.customClick(
                                { _, audience -> audience.showDialog(DiscordDialog.create()) },
                                ClickCallback.Options.builder()
                                    .uses(ClickCallback.UNLIMITED_USES)
                                    .build()
                            )
                        ).build(),
                    )
                ).columns(1).exitAction(
                    ActionButton.builder(Component.translatable("gui.done"))
                        .build()
                ).build()
            )
    }
}
