package net.azisaba.vanilife.travel

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object TravelFonts {
    val TRAVEL_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "travel_icons")

    fun travelIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key("gui/sprites/icon/search.png"),
                chars = listOf(TravelIcons.SEARCH.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/travel/ticket.png"),
                chars = listOf(TravelIcons.TICKET.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/travel/visitors.png"),
                chars = listOf(TravelIcons.VISITORS.toString()),
                ascent = 8,
                height = 9,
            ),
        )
    )

    object TravelIcons : CharCodeFactory() {
        val SEARCH: Char = nextChar()
        val TICKET: Char = nextChar()
        val VISITORS: Char = nextChar()
    }
}
