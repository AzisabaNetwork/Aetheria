package net.azisaba.vanilife.cooking

import org.bukkit.potion.PotionEffectType


data class EffectDef(val potion: PotionEffectType, val durationTicks: Int, val amplifier: Int, val hudKey: String)

object CookingEffects {
    // Keys are full namespaced item keys (e.g. "vanilife:coffee")
    private val map: Map<String, List<EffectDef>> = mapOf(
        "vanilife:bamboo_shoot_rice" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Satiated")),
        "vanilife:coffee" to listOf(EffectDef(PotionEffectType.getByName("SPEED")!!, 60 * 20, 0, "Caffeinated"), EffectDef(PotionEffectType.getByName("FAST_DIGGING")!!, 30 * 20, 0, "Caffeinated")),
        "vanilife:cotton_candy" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Sugar Rush"), EffectDef(PotionEffectType.getByName("SPEED")!!, 20 * 20, 0, "Sugar Rush")),
        "vanilife:curry_rice" to listOf(EffectDef(PotionEffectType.getByName("FIRE_RESISTANCE")!!, 90 * 20, 0, "Spicy Power"), EffectDef(PotionEffectType.getByName("INCREASE_DAMAGE")!!, 30 * 20, 0, "Spicy Power")),
        "vanilife:eel_rice_bowl" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Hearty"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 8 * 20, 0, "Hearty")),
        "vanilife:fried_horse_mackerel" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Crunchy"), EffectDef(PotionEffectType.getByName("ABSORPTION")!!, 12 * 20, 0, "Crunchy")),
        "vanilife:grilled_ayu" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 6 * 20, 0, "Grilled")),
        "vanilife:grilled_mackerel" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 6 * 20, 0, "Grilled")),
        "vanilife:grilled_pacific_saury" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Grilled"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 6 * 20, 0, "Grilled")),
        "vanilife:grilled_squid" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Sea Fresh"), EffectDef(PotionEffectType.getByName("WATER_BREATHING")!!, 20 * 20, 0, "Sea Fresh")),
        "vanilife:hamburg_steak" to listOf(EffectDef(PotionEffectType.getByName("INCREASE_DAMAGE")!!, 40 * 20, 0, "Energized"), EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Energized")),
        "vanilife:marinated_eggplant" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Preserved"), EffectDef(PotionEffectType.getByName("DAMAGE_RESISTANCE")!!, 20 * 20, 0, "Preserved")),
        "vanilife:miso_mackerel" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 9 * 20, 1, "Comfort"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 6 * 20, 0, "Comfort")),
        "vanilife:miso_soup" to listOf(EffectDef(PotionEffectType.getByName("FIRE_RESISTANCE")!!, 60 * 20, 0, "Warmth"), EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Warmth")),
        "vanilife:nikujaga" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Home Cooked"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 8 * 20, 0, "Home Cooked")),
        "vanilife:oden" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Hearty Stew"), EffectDef(PotionEffectType.getByName("DAMAGE_RESISTANCE")!!, 30 * 20, 0, "Hearty Stew")),
        "vanilife:paper_fan" to listOf(EffectDef(PotionEffectType.getByName("SLOW_FALLING")!!, 10 * 20, 0, "Cool Breeze")),
        "vanilife:parfait" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Delight"), EffectDef(PotionEffectType.getByName("SPEED")!!, 15 * 20, 0, "Delight")),
        "vanilife:salad" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Fresh"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 5 * 20, 0, "Fresh")),
        "vanilife:salmon_roe_sushi" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Luxurious"), EffectDef(PotionEffectType.getByName("ABSORPTION")!!, 10 * 20, 0, "Luxurious")),
        "vanilife:sausage" to listOf(EffectDef(PotionEffectType.getByName("INCREASE_DAMAGE")!!, 30 * 20, 0, "Hearty"), EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Hearty")),
        "vanilife:sea_urchin_sushi" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Umami"), EffectDef(PotionEffectType.getByName("NIGHT_VISION")!!, 20 * 20, 0, "Umami")),
        "vanilife:seafood_rice_fowl" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Sea Feast"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 8 * 20, 0, "Sea Feast")),
        "vanilife:shaved_ice" to listOf(EffectDef(PotionEffectType.getByName("SLOW")!!, 10 * 20, 0, "Chilled")),
        "vanilife:soba" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Comfort"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 6 * 20, 0, "Comfort")),
        "vanilife:soft_serve_ice_cream" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Sweet"), EffectDef(PotionEffectType.getByName("SPEED")!!, 10 * 20, 0, "Sweet")),
        "vanilife:squid_sushi" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Fresh Catch"), EffectDef(PotionEffectType.getByName("ABSORPTION")!!, 8 * 20, 0, "Fresh Catch")),
        "vanilife:steamed_rice" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Plain")),
        "vanilife:takoyaki" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 8 * 20, 1, "Street Food"), EffectDef(PotionEffectType.getByName("SPEED")!!, 10 * 20, 0, "Street Food")),
        "vanilife:tamago_sushi" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Comfort"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 5 * 20, 0, "Comfort")),
        "vanilife:teriyaki_yellowtail" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 9 * 20, 1, "Teriyaki Boost"), EffectDef(PotionEffectType.getByName("INCREASE_DAMAGE")!!, 20 * 20, 0, "Teriyaki Boost")),
        "vanilife:tonkatsu" to listOf(EffectDef(PotionEffectType.getByName("INCREASE_DAMAGE")!!, 40 * 20, 0, "Power Meal"), EffectDef(PotionEffectType.getByName("SATURATION")!!, 12 * 20, 1, "Power Meal")),
        "vanilife:tuna_sushi" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 6 * 20, 1, "Fresh"), EffectDef(PotionEffectType.getByName("ABSORPTION")!!, 8 * 20, 0, "Fresh")),
        "vanilife:udon" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Warm Bowl"), EffectDef(PotionEffectType.getByName("REGENERATION")!!, 8 * 20, 0, "Warm Bowl")),
        "vanilife:yakisoba" to listOf(EffectDef(PotionEffectType.getByName("SATURATION")!!, 10 * 20, 1, "Stir-Fried"), EffectDef(PotionEffectType.getByName("SPEED")!!, 15 * 20, 0, "Stir-Fried"))
    )

    fun getEffectsForKey(key: String): List<EffectDef>? = map[key]
}
