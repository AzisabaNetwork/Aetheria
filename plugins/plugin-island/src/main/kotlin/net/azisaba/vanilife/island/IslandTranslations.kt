package net.azisaba.vanilife.island

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object IslandTranslations {
    const val ISLAND_LEVEL_UP: String = "island.level_up"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10: String = "island.level_up.unlocked.feature.lv10"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20: String = "island.level_up.unlocked.feature.lv20"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30: String = "island.level_up.unlocked.feature.lv30"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40: String = "island.level_up.unlocked.feature.lv40"
    const val ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50: String = "island.level_up.unlocked.feature.lv50"
    const val ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES: String = "island.level_up.unlocked.wrack_types"
    const val ISLAND_SPAWN_LOCATION: String = "island.spawn_location"

    fun us(): PackLanguage = mapOf(
        ISLAND_LEVEL_UP to Translation.literal("Level Up!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10 to Translation.literal("You can now visit other players' islands!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20 to Translation.literal("You can now change your spawn point!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30 to Translation.literal("You can now change the sky color!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40 to Translation.literal("Your building height limit has been expanded!"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50 to Translation.literal("You can now fly!"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal(" new types of wrack can now appear!"),
        ISLAND_SPAWN_LOCATION to Translation.literal("Spawn Location"),
    )

    fun jp(): PackLanguage = mapOf(
        ISLAND_LEVEL_UP to Translation.literal("レベルアップ！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV10 to Translation.literal("他のプレイヤーの島へ行けるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV20 to Translation.literal("スポーン地点を変更できるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV30 to Translation.literal("建築できる高さの範囲が広がりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV40 to Translation.literal("空の色を変更できるようになりました！"),
        ISLAND_LEVEL_UP_UNLOCKED_FEATURE_LV50 to Translation.literal("飛行モードを解放しました！"),
        ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES to Translation.placeholder() + Translation.literal("種類の新しい漂流物を解放しました！"),
        ISLAND_SPAWN_LOCATION to Translation.literal("スポーン地点"),
    )
}
