package net.azisaba.vanilife.npc

import net.azisaba.packed.PackedKey
import net.azisaba.packed.font
import net.azisaba.packed.font.CharCodeFactory
import net.azisaba.packed.font.PackFont
import net.azisaba.packed.font.provider.PackBitmapFontProvider
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object NpcFonts {
    val NPC_ICONS: PackedKey<PackFont> = PackedKey.font(Vanilife.NAMESPACE, "npc_icons")

    fun npcIcons(): PackFont = PackFont(
        listOf(
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/npc/caveman.png"),
                chars = listOf("${NpcIcons.CAVEMAN}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/npc/creeper.png"),
                chars = listOf("${NpcIcons.CREEPER}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/npc/ender.png"),
                chars = listOf("${NpcIcons.ENDER}"),
                ascent = 7,
                height = 8,
            ),
            PackBitmapFontProvider(
                file = Key.key(Vanilife.NAMESPACE, "icon/npc/neko.png"),
                chars = listOf("${NpcIcons.NEKO}"),
                ascent = 7,
                height = 8,
            ),
        )
    )

    object NpcIcons : CharCodeFactory() {
        val CAVEMAN: Char = nextChar()
        val CREEPER: Char = nextChar()
        val ENDER: Char = nextChar()
        val NEKO: Char = nextChar()
    }
}
