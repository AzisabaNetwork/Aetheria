package net.azisaba.vanilife.menuprovider.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.azisaba.vanilife.ReloadableConfiguration
import net.azisaba.vanilife.menuprovider.Configuration
import net.azisaba.vanilife.menuprovider.MenuProviderFonts
import net.azisaba.vanilife.menuprovider.MenuProviderTranslations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextDecoration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object DiscordDialog : KoinComponent {
    val TITLE: Component = Component.text()
        .append(
            Component.text(MenuProviderFonts.MenuIcons.DISCORD)
                .shadowColor(ShadowColor.none())
                .font(MenuProviderFonts.MENU_ICONS)
        )
        .appendSpace()
        .append(Component.translatable(MenuProviderTranslations.DIALOG_VANILIFE_DISCORD))
        .build()

    private val config: ReloadableConfiguration<Configuration> by inject()

    fun create(): Dialog = Dialog.create { builder ->
        builder.empty()
            .base(
                DialogBase.builder(TITLE)
                    .body(
                        listOf(
                            DialogBody.plainMessage(
                                Component.text(
                                    config.value().discordUrl,
                                    NamedTextColor.BLUE,
                                    TextDecoration.UNDERLINED
                                )
                                    .clickEvent(ClickEvent.openUrl(config.value().discordUrl))
                            ),
                        )
                    ).build()
            )
            .type(DialogType.notice(MenuDialog.backToMenuButton()))
    }
}
