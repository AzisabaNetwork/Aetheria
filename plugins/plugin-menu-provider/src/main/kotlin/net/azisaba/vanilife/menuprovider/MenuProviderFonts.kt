package net.azisaba.vanilife.menuprovider

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object MenuProviderFonts {
    val MENU_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "menu_icons")

    fun menuIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/menu/discord.png"),
                chars = listOf(MenuIcons.DISCORD.toString()),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/menu/menu.png"),
                chars = listOf(MenuIcons.MENU.toString()),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/menu/settings.png"),
                chars = listOf(MenuIcons.SETTINGS.toString()),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/menu/trash.png"),
                chars = listOf(MenuIcons.TRASH.toString()),
                ascent = 7,
                height = 8,
            ),
        )
    )

    object MenuIcons : CharCodeFactory() {
        val MENU: Char = nextChar()
        val DISCORD: Char = nextChar()
        val SETTINGS: Char = nextChar()
        val TRASH: Char = nextChar()
    }
}
