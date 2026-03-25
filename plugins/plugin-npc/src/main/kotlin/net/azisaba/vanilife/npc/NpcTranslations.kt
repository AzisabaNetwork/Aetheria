package net.azisaba.vanilife.npc

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object NpcTranslations {
    const val COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ALL: String = "commands.vanilife.reload_npc_offers.all"
    const val COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ONE: String = "commands.vanilife.reload_npc_offers.one"
    const val COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_UNKNOWN_NPC_TYPE: String = "commands.vanilife.reload_npc_offers.unknown_npc_type"

    const val ITEM_VANILIFE_EXPERIENCE: String = "item.vanilife.experience"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE: String = "item.vanilife.unreadable_recipe"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION: String = "item.vanilife.unreadable_recipe.description"
    const val NPC_TRADE_EXPERIENCE_INSUFFICIENT: String = "npc.trade.experience_insufficient"

    fun us(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ALL to Translation.literal("Reloaded NPC offers for all NPC types."),
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ONE to Translation.literal("Reloaded NPC offers for ") + Translation.placeholder() + Translation.literal("."),
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_UNKNOWN_NPC_TYPE to Translation.literal("Unknown NPC type."),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("Experience"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("Unreadable Recipe"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("Hmm. It's written in a script I've never seen before."),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.literal(""),
    )

    fun jp(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ALL to Translation.literal("すべてのNPCタイプのオファーを再読み込みしました。"),
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_ONE to Translation.placeholder() + Translation.literal(" のオファーを再読み込みしました。"),
        COMMANDS_VANILIFE_RELOAD_NPC_OFFERS_UNKNOWN_NPC_TYPE to Translation.literal("不明なNPCタイプです。"),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("経験値"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("読めないレシピ"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("うーん。見たことない字で書かれてる。"),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.placeholder() + Translation.literal("レベル不足しています。"),
    )
}
