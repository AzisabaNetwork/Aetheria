package net.azisaba.vanilife.islands

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key

object IslandsFonts {
    private const val WAVE_ASCENT: Int = 511
    private const val WAVE_HEIGHT: Int = 512

    val ENCHANTS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "enchants")
    val WAVES: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "waves")

    fun enchants(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "enchanting_table/enchanting_table.png"),
                chars = listOf(Enchants.ENCHANTING_TABLE.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key("item/enchanted_book.png"),
                chars = listOf(Enchants.ENCHANTED_BOOK.toString()),
                ascent = 12,
                height = 16,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "enchanting_table/book.png"),
                chars = listOf(Enchants.BOOK.toString()),
                ascent = 0,
                height = 52,
            ),
        )
    )

    fun waves(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave0.png"),
                chars = listOf(Waves.FRAME0.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave1.png"),
                chars = listOf(Waves.FRAME1.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave2.png"),
                chars = listOf(Waves.FRAME2.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave3.png"),
                chars = listOf(Waves.FRAME3.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave4.png"),
                chars = listOf(Waves.FRAME4.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave5.png"),
                chars = listOf(Waves.FRAME5.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave6.png"),
                chars = listOf(Waves.FRAME6.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave7.png"),
                chars = listOf(Waves.FRAME7.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave8.png"),
                chars = listOf(Waves.FRAME8.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave9.png"),
                chars = listOf(Waves.FRAME9.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "wave/wave10.png"),
                chars = listOf(Waves.FRAME10.toString()),
                ascent = WAVE_ASCENT,
                height = WAVE_HEIGHT,
            ),
        )
    )

    object Enchants : CharCodeFactory() {
        val ENCHANTING_TABLE: Char = nextChar()
        val ENCHANTED_BOOK: Char = nextChar()
        val BOOK: Char = nextChar()
    }

    object Waves : CharCodeFactory() {
        val FRAME0: Char = nextChar()
        val FRAME1: Char = nextChar()
        val FRAME2: Char = nextChar()
        val FRAME3: Char = nextChar()
        val FRAME4: Char = nextChar()
        val FRAME5: Char = nextChar()
        val FRAME6: Char = nextChar()
        val FRAME7: Char = nextChar()
        val FRAME8: Char = nextChar()
        val FRAME9: Char = nextChar()
        val FRAME10: Char = nextChar()
    }
}
