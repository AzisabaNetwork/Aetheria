package net.azisaba.vanilife.npc

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object NpcTranslations {
    const val COMMANDS_VANILIFE_NPC_RELOAD_ALL: String = "commands.vanilife.reload.all"
    const val COMMANDS_VANILIFE_NPC_RELOAD_ONE: String = "commands.vanilife.reload.one"
    const val COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS: String = "commands.vanilife.npc.summon.success"

    const val ITEM_VANILIFE_EXPERIENCE: String = "item.vanilife.experience"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE: String = "item.vanilife.unreadable_recipe"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION: String = "item.vanilife.unreadable_recipe.description"
    const val NPC_TRADE_EXPERIENCE_INSUFFICIENT: String = "npc.trade.experience_insufficient"

    fun us(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_ALL to Translation.literal("Reloaded all NPC types"),
        COMMANDS_VANILIFE_NPC_RELOAD_ONE to Translation.literal("Reloaded ") + Translation.placeholder(),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("Summoned new ") + Translation.placeholder(),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("Experience"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("Unreadable Recipe"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("Hmm. It's written in a script I've never seen before."),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.literal(""),
    )

    fun jp(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_ALL to Translation.literal("すべての種類のNPCを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_ONE to Translation.placeholder() + Translation.literal("を再読み込みしました"),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("新しく") + Translation.placeholder() + Translation.literal("を召喚しました"),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("経験値"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("読めないレシピ"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("うーん。見たことない字で書かれてる。"),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.placeholder() + Translation.literal("レベル不足しています。"),
    )
}
