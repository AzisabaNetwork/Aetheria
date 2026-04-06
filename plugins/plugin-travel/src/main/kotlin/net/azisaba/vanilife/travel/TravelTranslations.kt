package net.azisaba.vanilife.travel

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object TravelTranslations {
    const val DIALOG_VANILIFE_TRAVEL = "dialog.vanilife.travel"

    const val ITEM_VANILIFE_TRAVEL_TICKET = "item.vanilife.travel_ticket"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_TRAVEL to Translation.literal("Chose a travel destination"),
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("Travel Ticket"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_TRAVEL to Translation.literal("旅行先をえらぶ"),
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("旅行券"),
    )
}
