package net.azisaba.vanilife.enchanting

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object EnchantingTranslations {
    const val BLOCK_MINECRAFT_ENCHANTING_TABLE: String = "block.minecraft.enchanting_table"

    const val DIALOG_VANILIFE_ENCHANTMENTS: String = "dialog.vanilife.enchantmnents"
    const val DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL: String = "dialog.vanilife.enchantments.required_level"
    const val DIALOG_VANILIFE_ENCHANTMENTS_SECTION: String = "dialog.vanilife.enchantments.section"
    const val DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY: String = "dialog.vanilife.enchantments.summary"
    const val DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE: String = "dialog.vanilife.enchantments.unlock_rate"

    const val ENCHANTING_NO_ISLAND: String = "enchanting.no_island"
    const val ENCHANTING_REQUIRED_LEVEL: String = "enchanting.required_level"

    fun us(): PackLanguage = mapOf(
        BLOCK_MINECRAFT_ENCHANTING_TABLE to Translation.literal("Enchantment Crafting Table"),
        DIALOG_VANILIFE_ENCHANTMENTS to Translation.literal("Enchantments"),
        DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL to Translation.literal("Appears on island with level ") + Translation.placeholder() + Translation.literal(" or higher"),
        DIALOG_VANILIFE_ENCHANTMENTS_SECTION to Translation.placeholder() + Translation.literal("-") + Translation.placeholder(),
        DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY to Translation.placeholder() + Translation.literal(" unlocked"),
        DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE to Translation.placeholder() + Translation.literal(" percent of players unlocked"),
        ENCHANTING_NO_ISLAND to Translation.literal("Enchanting can only be done on islands"),
        ENCHANTING_REQUIRED_LEVEL to Translation.literal("Experience Cost: ") + Translation.placeholder(),
    )

    fun jp(): PackLanguage = mapOf(
        BLOCK_MINECRAFT_ENCHANTING_TABLE to Translation.literal("エンチャント作業台"),
        DIALOG_VANILIFE_ENCHANTMENTS to Translation.literal("エンチャント図鑑"),
        DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL to Translation.placeholder() + Translation.literal("レベル以上の島に漂着します"),
        DIALOG_VANILIFE_ENCHANTMENTS_SECTION to Translation.placeholder() + Translation.literal("〜") + Translation.placeholder(),
        DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY to Translation.placeholder() + Translation.literal(" 解放済み"),
        DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE to Translation.placeholder() + Translation.literal("パーセントのプレイヤーが解放しました"),
        ENCHANTING_NO_ISLAND to Translation.literal("島でだけエンチャントすることができます"),
        ENCHANTING_REQUIRED_LEVEL to Translation.literal("経験値コスト: ") + Translation.placeholder(),
    )
}
