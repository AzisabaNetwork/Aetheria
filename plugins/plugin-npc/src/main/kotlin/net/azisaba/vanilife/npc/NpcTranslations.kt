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

    const val DIALOG_VANILIFE_NPC_MENU_DONE: String = "dialog.vanilife.npc_menu.done"

    const val NPC_MODE: String = "npc.mode"
    const val NPC_MODE_FOLLOW: String = "npc.mode.follow"
    const val NPC_MODE_FREE: String = "npc.mode.free"
    const val NPC_MODE_SIT: String = "npc.mode.sit"

    fun us(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_CONFIG to Translation.literal("Reloaded config.yaml"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ALL to Translation.literal("Reloaded all NPC offers"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULES_ALL to Translation.literal("Reloaded all NPC spawn rules"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ONE to Translation.literal("Reloaded offers for ") + Translation.placeholder(),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULE_ONE to Translation.literal("Reloaded spawn rule for ") + Translation.placeholder(),
        COMMANDS_VANILIFE_NPC_RELOADING to Translation.literal("Reloading resources..."),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("Summoned new ") + Translation.placeholder(),

        DIALOG_VANILIFE_NPC_MENU_DONE to Translation.literal("Okay"),

        NPC_MODE to Translation.literal("Mode"),
        NPC_MODE_FOLLOW to Translation.literal("Follow"),
        NPC_MODE_FREE to Translation.literal("Free"),
        NPC_MODE_SIT to Translation.literal("Sit"),
    )

    fun jp(): PackLanguage = mapOf(
        COMMANDS_VANILIFE_NPC_RELOAD_CONFIG to Translation.literal("config.yamlを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ALL to Translation.literal("すべての種類のNPCのオファーを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULES_ALL to Translation.literal("すべての種類のNPCのスポーンルールを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_OFFERS_ONE to Translation.placeholder() + Translation.literal("のオファーを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOAD_SPAWN_RULE_ONE to Translation.placeholder() + Translation.literal("のスポーンルールを再読み込みしました"),
        COMMANDS_VANILIFE_NPC_RELOADING to Translation.literal("リソースを再読み込みしています..."),
        COMMANDS_VANILIFE_NPC_SUMMON_SUCCESS to Translation.literal("新しく") + Translation.placeholder() + Translation.literal("を召喚しました"),

        DIALOG_VANILIFE_NPC_MENU_DONE to Translation.literal("オッケー！"),

        NPC_MODE to Translation.literal("モード"),
        NPC_MODE_FOLLOW to Translation.literal("追従"),
        NPC_MODE_FREE to Translation.literal("自由行動"),
        NPC_MODE_SIT to Translation.literal("待機"),
    )
}
