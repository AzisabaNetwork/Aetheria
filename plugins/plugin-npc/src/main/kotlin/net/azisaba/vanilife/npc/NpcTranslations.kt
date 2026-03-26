package net.azisaba.vanilife.npc

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object NpcTranslations {
    const val COMMANDS_VANILIFE_NPC_RELOAD_CONFIG: String = "commands.vanilife.npc.reload.config"
    const val COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ALL: String = "commands.vanilife.reload.offers.all"
    const val COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULES_ALL: String = "commands.vanilife.npc.reload.spawn_rules.all"
    const val COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ONE: String = "commands.vanilife.npc.reload.offers.one"
    const val COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULE_ONE: String = "commands.vanilife.npc.reload.spawn_rule.one"
    const val COMMANDS_VANILIFE_NPC_RELOADING: String = "commands.vanilife.npc.reload.reloading"
    const val COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS: String = "commands.vanilife.npc.summon.success"

    const val ITEM_VANILIFE_EXPERIENCE: String = "item.vanilife.experience"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE: String = "item.vanilife.unreadable_recipe"
    const val ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION: String = "item.vanilife.unreadable_recipe.description"
    const val NPC_TRADE_EXPERIENCE_INSUFFICIENT: String = "npc.trade.experience_insufficient"

    fun us(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_CONFIG to Translation.literal("Reloaded config.yaml"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ALL to Translation.literal("Reloaded all NPC offers"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULES_ALL to Translation.literal("Reloaded all NPC spawn rules"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ONE to Translation.literal("Reloaded offers for ") + Translation.placeholder(),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULE_ONE to Translation.literal("Reloaded spawn rule for ") + Translation.placeholder(),
        COMMANDS_VANILIFE_NPC_RELOADING to Translation.literal("Reloading resources..."),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("Summoned new ") + Translation.placeholder(),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("Experience"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("Unreadable Recipe"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("Hmm. It's written in a script I've never seen before."),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.literal(""),
    )

    fun jp(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_CONFIG to Translation.literal("config.yamlを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ALL to Translation.literal("すべての種類のNPCのオファーを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULES_ALL to Translation.literal("すべての種類のNPCのスポーンルールを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ONE to Translation.placeholder() + Translation.literal("のオファーを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULE_ONE to Translation.placeholder() + Translation.literal("のスポーンルールを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOADING to Translation.literal("リソースを再読み込みしています..."),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("新しく") + Translation.placeholder() + Translation.literal("を召喚しました"),
        ITEM_VANILIFE_EXPERIENCE to Translation.literal("経験値"),
        ITEM_VANILIFE_UNREADABLE_RECIPE to Translation.literal("読めないレシピ"),
        ITEM_VANILIFE_UNREADABLE_RECIPE_DESCRIPTION to Translation.literal("うーん。見たことない字で書かれてる。"),
        NPC_TRADE_EXPERIENCE_INSUFFICIENT to Translation.placeholder() + Translation.literal("レベル不足しています。"),
    )
}
