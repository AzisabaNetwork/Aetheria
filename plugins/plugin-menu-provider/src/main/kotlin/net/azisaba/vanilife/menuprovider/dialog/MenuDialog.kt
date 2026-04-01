package net.azisaba.vanilife.menuprovider.dialog

import com.github.shynixn.mccoroutine.folia.launch
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.menuprovider.MenuProviderDialogs
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.azisaba.vanilife.menuprovider.inventory.TrashInventory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object MenuDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.MENU, NamedTextColor.WHITE)
                .font(MenuProviderFonts.MENU_ICONS)
                .shadowColor(ShadowColor.none())
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU))
        .build()

    val SETTINGS: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.SETTINGS)
                .shadowColor(ShadowColor.none())
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_SETTINGS))
        .build()

    val TRASH: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.TRASH)
                .shadowColor(ShadowColor.none())
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_TRASH))
        .build()

    val DISCORD: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.DISCORD)
                .shadowColor(ShadowColor.none())
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_DISCORD))
        .build()

    private val plugin: Plugin by inject()

    fun bootstrap(builder: DialogRegistryEntry.Builder) {
        builder
            .base(
                DialogBase.builder(TITLE)
                    .externalTitle(TITLE.color(NamedTextColor.YELLOW))
                    .pause(false)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .build()
            )
            .type(
                DialogType.multiAction(listOf(settingsButton(), trashButton(), discordButton()))
                    .columns(1)
                    .exitAction(
                        ActionButton.builder(Component.translatable("gui.done"))
                            .action(
                                DialogAction.customClick(
                                    { _, audience -> audience.closeDialog() },
                                    ClickCallback.Options.builder()
                                        .uses(ClickCallback.UNLIMITED_USES)
                                        .build()
                                )
                            )
                            .build()
                    ).build()
            )
    }

    fun backToMenuButton(): ActionButton = ActionButton.builder(Component.translatable("gui.back"))
        .action(
            DialogAction.staticAction(
                ClickEvent.showDialog(
                    RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.DIALOG)
                        .getOrThrow(MenuProviderDialogs.MENU)
                )
            )
        )
        .build()

    private fun settingsButton(): ActionButton = ActionButton.builder(SETTINGS).action(
        DialogAction.customClick(
            { _, audience ->
                (audience as? Player)?.let { player ->
                    plugin.launch {
                        player.showDialog(SettingsDialog.create(player))
                    }
                }
            },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).build()

    private fun trashButton(): ActionButton = ActionButton.builder(TRASH)
        .action(
            DialogAction.customClick(
                { _, audience -> (audience as? Player)?.openInventory(TrashInventory(plugin).inventory) },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        ).build()

    private fun discordButton(): ActionButton = ActionButton.builder(DISCORD)
        .action(
            DialogAction.customClick(
                { _, audience -> audience.showDialog(DiscordDialog.create()) },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        ).build()
}
