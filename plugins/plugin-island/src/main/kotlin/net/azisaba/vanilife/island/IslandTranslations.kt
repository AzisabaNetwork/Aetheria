package net.azisaba.vanilife.island

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object IslandTranslations {
    const val DIALOG_VANILIFE_DISPLAY_NAME_LABEL: String = "dialog.vanilife.display_name.label"
    const val DIALOG_VANILIFE_DISPLAY_NAME_NO: String = "dialog.vanilife.display_name.no"
    const val DIALOG_VANILIFE_DISPLAY_NAME_RULE: String = "dialog.vanilife.display_name.rule"
    const val DIALOG_VANILIFE_DISPLAY_NAME_YES: String = "dialog.vanilife.display_name.yes"

    const val DIALOG_VANILIFE_MY_ISLAND: String = "dialog.vanilife.my_island"
    const val DIALOG_VANILIFE_MY_ISLAND_DISPLAY_NAME: String = "dialog.vanilife.my_island.display_name"
    const val DIALOG_VANILIFE_MY_ISLAND_SPAWN_POINT: String = "dialog.vanilife.my_island.spawn_point"

    const val DIALOG_VANILIFE_RETURN: String = "dialog.vanilife.return"
    const val DIALOG_VANILIFE_RETURN_MESSAGE: String = "dialog.vanilife.return.message"
    const val DIALOG_VANILIFE_RETURN_MESSAGE_ALREADY_ON_ISLAND: String = "dialog.vanilife.return.message.already_on_island"
    const val DIALOG_VANILIFE_RETURN_NO: String = "dialog.vanilife.return.no"
    const val DIALOG_VANILIFE_RETURN_YES: String = "dialog.vanilife.return.yes"

    const val DIALOG_VANILIFE_SPAWN_POINT: String = "dialog.vanilife.spawn_point"
    const val DIALOG_VANILIFE_SPAWN_POINT_DESCRIPTION: String = "dialog.vanilife.spawn_point.description"

    const val ISLAND_FEATURE_VISIT_OTHER_ISLANDS: String = "island.feature.visit_other_islands"
    const val ISLAND_FEATURE_CUSTOM_SPAWN_POINT: String = "island.feature.custom_spawn_point"
    const val ISLAND_FEATURE_EXPAND_STORAGE: String = "island.feature.expand_storage"
    const val ISLAND_FEATURE_CUSTOM_SKY_COLOR: String = "island.feature.custom_sky_color"
    const val ISLAND_FEATURE_FLIGHT: String = "island.feature.flight"

    const val ISLAND_LEVEL_UP: String = "island.level_up"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE: String = "island.level_up.unlocked.feature"
    const val ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES: String = "island.level_up.unlocked.wrack_types"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISPLAY_NAME_LABEL to Translation.literal("Island Name"),
        DIALOG_VANILIFE_DISPLAY_NAME_NO to Translation.literal("Reconsider"),
        DIALOG_VANILIFE_DISPLAY_NAME_RULE to Translation.literal("The island's name is visible to other players"),
        DIALOG_VANILIFE_DISPLAY_NAME_YES to Translation.literal("Okay!"),

        DIALOG_VANILIFE_MY_ISLAND to Translation.literal("My Island"),
        DIALOG_VANILIFE_MY_ISLAND_DISPLAY_NAME to Translation.literal("Change name"),
        DIALOG_VANILIFE_MY_ISLAND_SPAWN_POINT to Translation.literal("Change spawn point"),

        DIALOG_VANILIFE_RETURN to Translation.literal("Return to Island"),
        DIALOG_VANILIFE_RETURN_MESSAGE to Translation.literal("Are you sure you want to return to your island?"),
        DIALOG_VANILIFE_RETURN_MESSAGE_ALREADY_ON_ISLAND to Translation.literal("You are already on your island. Do you want to move to your island's spawn point?"),
        DIALOG_VANILIFE_RETURN_NO to Translation.literal("Pass for Now"),
        DIALOG_VANILIFE_RETURN_YES to Translation.literal("Okay!"),

        DIALOG_VANILIFE_SPAWN_POINT to Translation.literal("Change island's spawn point"),
        DIALOG_VANILIFE_SPAWN_POINT_DESCRIPTION to Translation.literal("Do you want to change your island spawn point to your current location?"),
        ISLAND_FEATURE_VISIT_OTHER_ISLANDS to Translation.literal("Travel"),
        ISLAND_FEATURE_CUSTOM_SPAWN_POINT to Translation.literal("Change Spawn Point"),
        ISLAND_FEATURE_EXPAND_STORAGE to Translation.literal("Expanded Storage"),
        ISLAND_FEATURE_CUSTOM_SKY_COLOR to Translation.literal("Sky Color"),
        ISLAND_FEATURE_FLIGHT to Translation.literal("Flight Mode"),

        ISLAND_LEVEL_UP to Translation.literal("Level Up!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE to Translation.placeholder() + Translation.literal(" is now available!"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal(" new types of wrack can now appear!"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISPLAY_NAME_LABEL to Translation.literal("島の名前"),
        DIALOG_VANILIFE_DISPLAY_NAME_NO to Translation.literal("考え直す"),
        DIALOG_VANILIFE_DISPLAY_NAME_RULE to Translation.literal("島の名前は他の人にも見えるものです"),
        DIALOG_VANILIFE_DISPLAY_NAME_YES to Translation.literal("オッケー！"),

        DIALOG_VANILIFE_MY_ISLAND to Translation.literal("島の管理"),
        DIALOG_VANILIFE_MY_ISLAND_DISPLAY_NAME to Translation.literal("島名を変更する"),
        DIALOG_VANILIFE_MY_ISLAND_SPAWN_POINT to Translation.literal("スポーン地点を変更する"),

        DIALOG_VANILIFE_RETURN to Translation.literal("島に帰る"),
        DIALOG_VANILIFE_RETURN_MESSAGE to Translation.literal("この場所を離れて自分の島に帰りますか？"),
        DIALOG_VANILIFE_RETURN_MESSAGE_ALREADY_ON_ISLAND to Translation.literal("すでに自分の島にいるようです　島のスポーン地点に移動しますか？"),
        DIALOG_VANILIFE_RETURN_NO to Translation.literal("今はやめておく"),
        DIALOG_VANILIFE_RETURN_YES to Translation.literal("オッケー！"),
        DIALOG_VANILIFE_SPAWN_POINT to Translation.literal("島のスポーン地点を変更"),
        DIALOG_VANILIFE_SPAWN_POINT_DESCRIPTION to Translation.literal("現在地をスポーン地点にしますか？"),

        ISLAND_FEATURE_VISIT_OTHER_ISLANDS to Translation.literal("旅行"),
        ISLAND_FEATURE_CUSTOM_SPAWN_POINT to Translation.literal("スポーン地点の変更"),
        ISLAND_FEATURE_EXPAND_STORAGE to Translation.literal("おおきな収納"),
        ISLAND_FEATURE_CUSTOM_SKY_COLOR to Translation.literal("そらの着色"),
        ISLAND_FEATURE_FLIGHT to Translation.literal("飛行モード"),

        ISLAND_LEVEL_UP to Translation.literal("レベルアップ！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE to Translation.placeholder() + Translation.literal("を利用できるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal("種類の新しい漂流物を解放しました！"),
    )
}
