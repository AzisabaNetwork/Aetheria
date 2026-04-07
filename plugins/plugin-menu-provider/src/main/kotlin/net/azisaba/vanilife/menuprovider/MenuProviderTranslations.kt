package net.azisaba.vanilife.menuprovider

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object MenuProviderTranslations {
    const val DIALOG_VANILIFE_DISCORD: String = "dialog.vanilife.discord"

    const val DIALOG_VANILIFE_SETTINGS: String = "dialog.vanilife.settings"

    const val DIALOG_VANILIFE_MENU: String = "dialog.vanilife.menu"
    const val DIALOG_VANILIFE_MENU_DISCORD: String = "dialog.vanilife.menu.discord"
    const val DIALOG_VANILIFE_MENU_RETURN_TO_ISLAND: String = "dialog.vanilife.menu.return_to_island"
    const val DIALOG_VANILIFE_MENU_SETTINGS: String = "dialog.vanilife.menu.settings"
    const val DIALOG_VANILIFE_MENU_STORAGE: String = "dialog.vanilife.menu.storage"
    const val DIALOG_VANILIFE_MENU_TRASH: String = "dialog.vanilife.menu.trash"

    const val INVENTORY_VANILIFE_STORAGE: String = "inventory.vanilife.storage"
    const val INVENTORY_VANILIFE_TRASH: String = "inventory.vanilife.trash"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISCORD to Translation.literal("Join our Discord"),
        DIALOG_VANILIFE_SETTINGS to Translation.literal("Game Settings"),
        DIALOG_VANILIFE_MENU to Translation.literal("Vanilife Menu"),
        DIALOG_VANILIFE_MENU_DISCORD to Translation.literal("Discord"),
        DIALOG_VANILIFE_MENU_RETURN_TO_ISLAND to Translation.literal("Return to Island"),
        DIALOG_VANILIFE_MENU_SETTINGS to Translation.literal("Island Management"),
        DIALOG_VANILIFE_MENU_STORAGE to Translation.literal("Storage"),
        DIALOG_VANILIFE_MENU_TRASH to Translation.literal("Trash"),
        INVENTORY_VANILIFE_STORAGE to Translation.literal("Storage"),
        INVENTORY_VANILIFE_TRASH to Translation.literal("To delete items, place them here"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISCORD to Translation.literal("Discordに参加する"),
        DIALOG_VANILIFE_SETTINGS to Translation.literal("島の管理"),
        DIALOG_VANILIFE_MENU to Translation.literal("ばにらいふメニュー"),
        DIALOG_VANILIFE_MENU_DISCORD to Translation.literal("Discord"),
        DIALOG_VANILIFE_MENU_RETURN_TO_ISLAND to Translation.literal("島に戻る"),
        DIALOG_VANILIFE_MENU_SETTINGS to Translation.literal("島の管理"),
        DIALOG_VANILIFE_MENU_STORAGE to Translation.literal("収納"),
        DIALOG_VANILIFE_MENU_TRASH to Translation.literal("ごみ箱"),
        INVENTORY_VANILIFE_STORAGE to Translation.literal("収納"),
        INVENTORY_VANILIFE_TRASH to Translation.literal("ここに置いたアイテムは削除されます"),
    )
}
