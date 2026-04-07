package net.azisaba.vanilife.menuprovider.dialog

import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.keys.SoundEventKeys
import net.azisaba.vanilife.enchanting.dialog.EnchantmentsDialog
import net.azisaba.vanilife.island.dialog.MyIslandDialog
import net.azisaba.vanilife.island.dialog.ReturnDialog
import net.azisaba.vanilife.island.ownedIsland
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.azisaba.vanilife.menuprovider.inventory.StorageInventory
import net.azisaba.vanilife.menuprovider.inventory.TrashInventory
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object MenuDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.MENU, NamedTextColor.WHITE)
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU))
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
                DialogType.multiAction(
                    listOf(
                        returnButton(),
                        myIslandButton(),
                        enchantmentsButton(),
                        trashButton(),
                        storageButton(),
                    )
                ).exitAction(exitButton()).columns(1).build()
            )
    }

    private fun returnButton(): ActionButton = ActionButton.builder(ReturnDialog.TITLE)
        .action(
            DialogAction.customClick(
                { _, audience ->
                    val player = audience as? Player ?: return@customClick
                    player.showDialog(ReturnDialog.create(player))
                },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        ).build()

    private fun myIslandButton(): ActionButton = ActionButton.builder(MyIslandDialog.TITLE)
        .action(
            DialogAction.customClick(
                { _, audience ->
                    val player = audience as? Player ?: return@customClick
                    player.showDialog(MyIslandDialog.create(player))
                },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        )
        .build()

    private fun trashButton(): ActionButton = ActionButton.builder(
        Component.text()
            .append(
                Component.text(MenuProviderFonts.MenuIcons.TRASH)
                    .font(MenuProviderFonts.MENU_ICONS)
            )
            .appendSpace()
            .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_TRASH))
            .build()
    ).action(
        DialogAction.customClick(
            { _, audience -> (audience as? Player)?.openInventory(TrashInventory(plugin).inventory) },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).build()

    private fun storageButton(): ActionButton = ActionButton.builder(
        Component.text()
            .append(
                Component.text(MenuProviderFonts.MenuIcons.STORAGE)
                    .font(MenuProviderFonts.MENU_ICONS)
            )
            .appendSpace()
            .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_MENU_STORAGE))
            .build()
    ).action(
        DialogAction.customClick(
            { _, audience ->
                val player = audience as? Player ?: return@customClick
                val island = player.ownedIsland
                player.openInventory(StorageInventory(island, island.storageSize, plugin).inventory)
                player.playSound(Sound.sound(SoundEventKeys.BLOCK_ENDER_CHEST_OPEN, Sound.Source.UI, 1f, 1f))
            },
            ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .build()
        )
    ).build()

    private fun enchantmentsButton(): ActionButton = ActionButton.builder(EnchantmentsDialog.TITLE)
        .action(
            DialogAction.customClick(
                { _, audience ->
                    val player = audience as? Player ?: return@customClick
                    val island = player.ownedIsland
                    audience.showDialog(EnchantmentsDialog.create(island))
                },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        )
        .build()

    private fun exitButton(): ActionButton = ActionButton.builder(Component.translatable("gui.done"))
        .action(
            DialogAction.customClick(
                { _, audience -> audience.closeDialog() },
                ClickCallback.Options.builder()
                    .uses(ClickCallback.UNLIMITED_USES)
                    .build()
            )
        )
        .build()
}
