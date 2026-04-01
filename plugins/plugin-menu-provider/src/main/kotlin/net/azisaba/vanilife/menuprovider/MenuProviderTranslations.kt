package net.azisaba.vanilife.menuprovider

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object MenuProviderTranslations {
    const val DIALOG_VANILIFE_DISCORD: String = "dialog.vanilife.discord"

    const val DIALOG_VANILIFE_MENU: String = "dialog.vanilife.menu"
    const val DIALOG_VANILIFE_MENU_DISCORD: String = "dialog.vanilife.menu.discord"
    const val DIALOG_VANILIFE_MENU_QUICK_ACTIONS: String = "dialog.vanilife.menu.quick_actions"
    const val DIALOG_VANILIFE_MENU_TRASH: String = "dialog.vanilife.menu.trash"

    const val INVENTORY_VANILIFE_TRASH: String = "inventory.vanilife.trash"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISCORD to Translation.literal("Join our Discord"),
        DIALOG_VANILIFE_MENU to Translation.literal("Vanilife Menu"),
        DIALOG_VANILIFE_MENU_DISCORD to Translation.literal("Discord"),
        DIALOG_VANILIFE_MENU_QUICK_ACTIONS to Translation.literal("Quick Actions"),
        DIALOG_VANILIFE_MENU_TRASH to Translation.literal("Trash"),
        INVENTORY_VANILIFE_TRASH to Translation.literal("To delete items, place them here"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISCORD to Translation.literal("Discordに参加する"),
        DIALOG_VANILIFE_MENU to Translation.literal("ばにらいふメニュー"),
        DIALOG_VANILIFE_MENU_DISCORD to Translation.literal("Discord"),
        DIALOG_VANILIFE_MENU_QUICK_ACTIONS to Translation.literal("クイックアクション"),
        DIALOG_VANILIFE_MENU_TRASH to Translation.literal("ごみ箱"),
        INVENTORY_VANILIFE_TRASH to Translation.literal("ここに置いたアイテムは削除されます"),
    )
}
