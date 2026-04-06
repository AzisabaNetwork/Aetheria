package net.azisaba.vanilife.travel

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object TravelTranslations {
    const val ITEM_VANILIFE_TRAVEL_TICKET = "item.vanilife.travel_ticket"

    fun us(): PackLanguage = mapOf(
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("Travel Ticket"),
    )

    fun jp(): PackLanguage = mapOf(
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("旅行券"),
    )
}
