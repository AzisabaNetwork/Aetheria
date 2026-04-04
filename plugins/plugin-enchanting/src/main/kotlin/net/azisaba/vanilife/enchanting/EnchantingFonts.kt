package net.azisaba.vanilife.enchanting

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object EnchantingFonts {
    val ENCHANTING_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "enchanting_icons")

    fun enchantingIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/enchanting/enchanting_table.png"),
                chars = listOf("${EnchantingIcons.ENCHANTING_TABLE}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/enchanting/experience.png"),
                chars = listOf("${EnchantingIcons.EXPERIENCE}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/enchanting/unavailable.png"),
                chars = listOf("${EnchantingIcons.UNAVAILABLE}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/enchanting/unlock_rate.png"),
                chars = listOf("${EnchantingIcons.UNLOCK_RATE}"),
                ascent = 7,
                height = 8,
            ),
        )
    )

    object EnchantingIcons : CharCodeFactory() {
        val ENCHANTING_TABLE: Char = nextChar()
        val EXPERIENCE: Char = nextChar()
        val UNAVAILABLE: Char = nextChar()
        val UNLOCK_RATE: Char = nextChar()
    }
}
