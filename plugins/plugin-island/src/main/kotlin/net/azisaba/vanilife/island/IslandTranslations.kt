package net.azisaba.vanilife.island

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object IslandTranslations {
    const val DIALOG_VANILIFE_DISPLAY_NAME: String = "dialog.vanilife.display_name"
    const val DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME: String = "dialog.vanilife.display_name.new_name"
    const val DIALOG_VANILIFE_SKY_COLOR: String = "dialog.vanilife.sky_color"
    const val DIALOG_VANILIFE_SPAWN_LOCATION: String = "dialog.vanilife.spawn_location"
    const val DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION: String = "dialog.vanilife.spawn_location.description"

    const val ISLAND_FEATURE_VISIT_OTHER_ISLANDS: String = "island.feature.visit_other_islands"
    const val ISLAND_FEATURE_CUSTOM_SPAWN_POINT: String = "island.feature.custom_spawn_point"
    const val ISLAND_FEATURE_EXPAND_STORAGE: String = "island.feature.expand_storage"
    const val ISLAND_FEATURE_CUSTOM_SKY_COLOR: String = "island.feature.custom_sky_color"
    const val ISLAND_FEATURE_FLIGHT: String = "island.feature.flight"

    const val ISLAND_LEVEL_UP: String = "island.level_up"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE: String = "island.level_up.unlocked.feature"
    const val ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES: String = "island.level_up.unlocked.wrack_types"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_DISPLAY_NAME to Translation.literal("Change island's name"),
        DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME to Translation.literal("New Name"),
        DIALOG_VANILIFE_SKY_COLOR to Translation.literal("Change island's sky color"),
        DIALOG_VANILIFE_SPAWN_LOCATION to Translation.literal("Change island's spawn point"),
        DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION to Translation.literal("Do you want to change your island spawn point to your current location?"),
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
        DIALOG_VANILIFE_DISPLAY_NAME to Translation.literal("島の名前を変更"),
        DIALOG_VANILIFE_DISPLAY_NAME_NEW_NAME to Translation.literal("新しい名前"),
        DIALOG_VANILIFE_SKY_COLOR to Translation.literal("島の空の着色を変更"),
        DIALOG_VANILIFE_SPAWN_LOCATION to Translation.literal("島のスポーン地点を変更"),
        DIALOG_VANILIFE_SPAWN_LOCATION_DESCRIPTION to Translation.literal("島のスポーン地点を現在の位置に変更しますか？"),
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
