package net.azisaba.vanilife.enchanting

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object EnchantingTranslations {
    const val DIALOG_VANILIFE_ENCHANTMENTS: String = "dialog.vanilife.enchantmnents"
    const val DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL: String = "dialog.vanilife.enchantments.required_level"
    const val DIALOG_VANILIFE_ENCHANTMENTS_SECTION: String = "dialog.vanilife.enchantments.section"
    const val DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY: String = "dialog.vanilife.enchantments.summary"
    const val DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE: String = "dialog.vanilife.enchantments.unlock_rate"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_ENCHANTMENTS to Translation.literal("Enchantment Encyclopedia"),
        DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL to Translation.literal("Appears on island with level ") + Translation.placeholder() + Translation.literal(" or higher"),
        DIALOG_VANILIFE_ENCHANTMENTS_SECTION to Translation.placeholder() + Translation.literal("-") + Translation.placeholder(),
        DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY to Translation.placeholder() + Translation.literal(" unlocked"),
        DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE to Translation.placeholder() + Translation.literal(" percent of players unlocked"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_ENCHANTMENTS to Translation.literal("エンチャント図鑑"),
        DIALOG_VANILIFE_ENCHANTMENTS_REQUIRED_LEVEL to Translation.placeholder() + Translation.literal("レベル以上の島に漂着します"),
        DIALOG_VANILIFE_ENCHANTMENTS_SECTION to Translation.placeholder() + Translation.literal("〜") + Translation.placeholder(),
        DIALOG_VANILIFE_ENCHANTMENTS_SUMMARY to Translation.placeholder() + Translation.literal(" 解放済み"),
        DIALOG_VANILIFE_ENCHANTMENTS_UNLOCK_RATE to Translation.placeholder() + Translation.literal("パーセントのプレイヤーが解放しました"),
    )
}
