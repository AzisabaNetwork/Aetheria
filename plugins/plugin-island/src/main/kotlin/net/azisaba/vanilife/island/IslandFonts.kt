package net.azisaba.vanilife.island

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object IslandFonts {
    private const val WAVE_ASCENT: Int = 511
    private const val WAVE_HEIGHT: Int = 512

    val ENCHANTS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "enchants")
    val ISLAND_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "island_icons")
    val LEVEL_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "level_icons")
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

    fun islandIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key("item/name_tag.png"),
                chars = listOf(IslandIcons.DISPLAY_NAME.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/island/sky_color.png"),
                chars = listOf(IslandIcons.SKY_COLOR.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/island/spawn_location.png"),
                chars = listOf(IslandIcons.SPAWN_LOCATION.toString()),
                ascent = 8,
                height = 9,
            ),
        )
    )

    fun levelIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_blue.png"),
                chars = listOf(LevelIcons.LEVEL_BLUE.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_green.png"),
                chars = listOf(LevelIcons.LEVEL_GREEN.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_pink.png"),
                chars = listOf(LevelIcons.LEVEL_PINK.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_purple.png"),
                chars = listOf(LevelIcons.LEVEL_PURPLE.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_yellow.png"),
                chars = listOf(LevelIcons.LEVEL_YELLOW.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_up_blue.png"),
                chars = listOf(LevelIcons.LEVEL_UP_BLUE.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_up_green.png"),
                chars = listOf(LevelIcons.LEVEL_UP_GREEN.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_up_pink.png"),
                chars = listOf(LevelIcons.LEVEL_UP_PINK.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_up_purple.png"),
                chars = listOf(LevelIcons.LEVEL_UP_PURPLE.toString()),
                ascent = 8,
                height = 9,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/level/level_up_yellow.png"),
                chars = listOf(LevelIcons.LEVEL_UP_YELLOW.toString()),
                ascent = 8,
                height = 9,
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

    object IslandIcons : CharCodeFactory() {
        val DISPLAY_NAME: Char = nextChar()
        val SKY_COLOR: Char = nextChar()
        val SPAWN_LOCATION: Char = nextChar()
    }

    object LevelIcons : CharCodeFactory() {
        val LEVEL_BLUE: Char = nextChar()
        val LEVEL_GREEN: Char = nextChar()
        val LEVEL_PINK: Char = nextChar()
        val LEVEL_PURPLE: Char = nextChar()
        val LEVEL_YELLOW: Char = nextChar()

        val LEVEL_UP_BLUE: Char = nextChar()
        val LEVEL_UP_GREEN: Char = nextChar()
        val LEVEL_UP_PINK: Char = nextChar()
        val LEVEL_UP_PURPLE: Char = nextChar()
        val LEVEL_UP_YELLOW: Char = nextChar()

        fun levelOf(level: Int): Char = when (level) {
            in 1..9 -> LEVEL_GREEN
            in 10..19 -> LEVEL_BLUE
            in 20..29 -> LEVEL_YELLOW
            in 30..39 -> LEVEL_PINK
            else -> LEVEL_PURPLE
        }

        fun levelUpOf(level: Int): Char = when (level) {
            in 1..9 -> LEVEL_UP_GREEN
            in 10..19 -> LEVEL_UP_BLUE
            in 20..29 -> LEVEL_UP_YELLOW
            in 30..39 -> LEVEL_UP_PINK
            else -> LEVEL_UP_PURPLE
        }
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
