package net.azisaba.vanilife.island

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object IslandTranslations {
    const val DIALOG_VANILIFE_DISPLAY_NAME: String = "dialog.vanilife.display_name"
    const val DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME: String = "dialog.vanilife.display_name.new_name"
    const val DIALOG_VANILIFE_SKY_COLOR: String = "dialog.vanilife.sky_color"
    const val DIALOG_VANILIFE_SPAWN_LOCATION: String = "dialog.vanilife.spawn_location"
    const val DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION: String = "dialog.vanilife.spawn_location.description"

    const val ISLAND_LEVEL_UP: String = "island.level_up"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10: String = "island.level_up.unlocked.feature.lv10"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20: String = "island.level_up.unlocked.feature.lv20"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30: String = "island.level_up.unlocked.feature.lv30"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40: String = "island.level_up.unlocked.feature.lv40"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50: String = "island.level_up.unlocked.feature.lv50"
    const val ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES: String = "island.level_up.unlocked.wrack_types"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISPLAY_NAME to Translation.literal("Change island's name"),
        DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME to Translation.literal("New Name"),
        DIALOG_VANILIFE_SKY_COLOR to Translation.literal("Change island's sky color"),
        DIALOG_VANILIFE_SPAWN_LOCATION to Translation.literal("Change island's spawn point"),
        DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION to Translation.literal("Do you want to change your island spawn point to your current location?"),
        ISLAND_LEVEL_UP to Translation.literal("Level Up!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10 to Translation.literal("You can now visit other players' islands!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20 to Translation.literal("You can now change your spawn point!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30 to Translation.literal("You can now change the sky color!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40 to Translation.literal("Your building height limit has been expanded!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50 to Translation.literal("You can now fly!"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal(" new types of wrack can now appear!"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISPLAY_NAME to Translation.literal("島の名前を変更"),
        DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME to Translation.literal("新しい名前"),
        DIALOG_VANILIFE_SKY_COLOR to Translation.literal("島の空の着色を変更"),
        DIALOG_VANILIFE_SPAWN_LOCATION to Translation.literal("島のスポーン地点を変更"),
        DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION to Translation.literal("島のスポーン地点を現在の位置に変更しますか？"),
        ISLAND_LEVEL_UP to Translation.literal("レベルアップ！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10 to Translation.literal("他のプレイヤーの島へ行けるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20 to Translation.literal("スポーン地点を変更できるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30 to Translation.literal("建築できる高さの範囲が広がりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40 to Translation.literal("空の色を変更できるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50 to Translation.literal("飛行モードを解放しました！"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal("種類の新しい漂流物を解放しました！"),
    )
}
