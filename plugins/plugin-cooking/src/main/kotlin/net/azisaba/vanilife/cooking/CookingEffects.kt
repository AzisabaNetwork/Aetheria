package net.azisaba.vanilife.cooking

import io.papermc.paper.registry.TypedKey
import net.azisaba.vanilife.item.ServerItem
import org.bukkit.potion.PotionEffectType


data class EffectDef(val potion: PotionEffectType, val durationTicks: Int, val amplifier: Int, val hudKey: String)

object CookingEffects {
    // Keys are full namespaced item keys (e.g. CookingItems.COFFEE)
    private val map: Map<TypedKey<ServerItem>, List<EffectDef>> = mapOf(
        CookingItems.BAMBOO_SHOOT_RICE to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Satiated")),
        CookingItems.COFFEE to listOf(EffectDef(PotionEffectType.SPEED, 60 * 20, 0, "Caffeinated"), EffectDef(PotionEffectType.HASTE, 30 * 20, 0, "Caffeinated")),
        CookingItems.COTTON_CANDY to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Sugar Rush"), EffectDef(PotionEffectType.SPEED, 20 * 20, 0, "Sugar Rush")),
        CookingItems.CURRY_RICE to listOf(EffectDef(PotionEffectType.FIRE_RESISTANCE, 90 * 20, 0, "Spicy Power"), EffectDef(PotionEffectType.STRENGTH, 30 * 20, 0, "Spicy Power")),
        CookingItems.EEL_RICE_BOWL to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Hearty"), EffectDef(PotionEffectType.REGENERATION, 8 * 20, 0, "Hearty")),
        CookingItems.FRIED_HORSE_MACKEREL to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Crunchy"), EffectDef(PotionEffectType.ABSORPTION, 12 * 20, 0, "Crunchy")),
        CookingItems.GRILLED_AYU to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.REGENERATION, 6 * 20, 0, "Grilled")),
        CookingItems.GRILLED_MACKEREL to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.REGENERATION, 6 * 20, 0, "Grilled")),
        CookingItems.GRILLED_PACIFIC_SAURY to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.REGENERATION, 6 * 20, 0, "Grilled")),
        CookingItems.GRILLED_SQUID to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Sea Fresh"), EffectDef(PotionEffectType.WATER_BREATHING, 20 * 20, 0, "Sea Fresh")),
        CookingItems.HAMBURG_STEAK to listOf(EffectDef(PotionEffectType.STRENGTH, 40 * 20, 0, "Energized"), EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Energized")),
        CookingItems.MARINATED_EGGPLANT to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Preserved"), EffectDef(PotionEffectType.RESISTANCE, 20 * 20, 0, "Preserved")),
        CookingItems.MISO_MACKEREL to listOf(EffectDef(PotionEffectType.SATURATION, 9 * 20, 1, "Comfort"), EffectDef(PotionEffectType.REGENERATION, 6 * 20, 0, "Comfort")),
        CookingItems.MISO_SOUP to listOf(EffectDef(PotionEffectType.FIRE_RESISTANCE, 60 * 20, 0, "Warmth"), EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Warmth")),
        CookingItems.NIKUJAGA to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Home Cooked"), EffectDef(PotionEffectType.REGENERATION, 8 * 20, 0, "Home Cooked")),
        CookingItems.ODEN to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Hearty Stew"), EffectDef(PotionEffectType.RESISTANCE, 30 * 20, 0, "Hearty Stew")),
        CookingItems.PAPER_FAN to listOf(EffectDef(PotionEffectType.SLOW_FALLING, 10 * 20, 0, "Cool Breeze")),
        CookingItems.PARFAIT to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Delight"), EffectDef(PotionEffectType.SPEED, 15 * 20, 0, "Delight")),
        CookingItems.SALAD to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Fresh"), EffectDef(PotionEffectType.REGENERATION, 5 * 20, 0, "Fresh")),
        CookingItems.SALMON_ROE_SUSHI to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Luxurious"), EffectDef(PotionEffectType.ABSORPTION, 10 * 20, 0, "Luxurious")),
        CookingItems.SAUSAGE to listOf(EffectDef(PotionEffectType.STRENGTH, 30 * 20, 0, "Hearty"), EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Hearty")),
        CookingItems.SEA_URCHIN_SUSHI to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Umami"), EffectDef(PotionEffectType.NIGHT_VISION, 20 * 20, 0, "Umami")),
        CookingItems.SEAFOOD_RICE_FOWL to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Sea Feast"), EffectDef(PotionEffectType.REGENERATION, 8 * 20, 0, "Sea Feast")),
        CookingItems.SHAVED_ICE to listOf(EffectDef(PotionEffectType.SLOWNESS, 10 * 20, 0, "Chilled")),
        CookingItems.SOBA to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Comfort"), EffectDef(PotionEffectType.REGENERATION, 6 * 20, 0, "Comfort")),
        CookingItems.SOFT_SERVE_ICE_CREAM to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Sweet"), EffectDef(PotionEffectType.SPEED, 10 * 20, 0, "Sweet")),
        CookingItems.SQUID_SUSHI to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Fresh Catch"), EffectDef(PotionEffectType.ABSORPTION, 8 * 20, 0, "Fresh Catch")),
        CookingItems.STEAMED_RICE to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Plain")),
        CookingItems.TAKOYAKI to listOf(EffectDef(PotionEffectType.SATURATION, 8 * 20, 1, "Street Food"), EffectDef(PotionEffectType.SPEED, 10 * 20, 0, "Street Food")),
        CookingItems.TAMAGO_SUSHI to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Comfort"), EffectDef(PotionEffectType.REGENERATION, 5 * 20, 0, "Comfort")),
        CookingItems.TERIYAKI_YELLOWTAIL to listOf(EffectDef(PotionEffectType.SATURATION, 9 * 20, 1, "Teriyaki Boost"), EffectDef(PotionEffectType.STRENGTH, 20 * 20, 0, "Teriyaki Boost")),
        CookingItems.TONKATSU to listOf(EffectDef(PotionEffectType.STRENGTH, 40 * 20, 0, "Power Meal"), EffectDef(PotionEffectType.SATURATION, 12 * 20, 1, "Power Meal")),
        CookingItems.TUNA_SUSHI to listOf(EffectDef(PotionEffectType.SATURATION, 6 * 20, 1, "Fresh"), EffectDef(PotionEffectType.ABSORPTION, 8 * 20, 0, "Fresh")),
        CookingItems.UDON to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Warm Bowl"), EffectDef(PotionEffectType.REGENERATION, 8 * 20, 0, "Warm Bowl")),
        CookingItems.YAKISOBA to listOf(EffectDef(PotionEffectType.SATURATION, 10 * 20, 1, "Stir-Fried"), EffectDef(PotionEffectType.SPEED, 15 * 20, 0, "Stir-Fried"))
    )

    fun getEffectsForKey(key: TypedKey<ServerItem>): List<EffectDef>? = map[key]
}
