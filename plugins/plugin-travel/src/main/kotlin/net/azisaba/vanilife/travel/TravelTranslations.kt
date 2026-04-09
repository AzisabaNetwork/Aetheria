package net.azisaba.vanilife.travel

import net.azisaba.packed.lang.PackLanguage
import net.azisaba.packed.lang.Translation

object TravelTranslations {
    const val DIALOG_VANILIFE_TRAVEL: String = "dialog.vanilife.travel"

    const val DIALOG_VANILIFE_TRAVEL_TICKET: String = "dialog.vanilife.travel_ticket"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_APPLY_FILTER: String = "dialog.vanilife.travel_ticket.apply_filter"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_BOX: String = "dialog.vanilife.travel_ticket.search_box"

    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH: String = "dialog.vanilife.travel_ticket_search"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_AGAIN: String = "dialog.vanilife.travel_ticket_search.again"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_LEVEL: String = "dialog.vanilife.travel_ticket_search.level"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_OWNER: String = "dialog.vanilife.travel_ticket_search.owner"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_VISITORS: String = "dialog.vanilife.travel_ticket_search.visitors"

    const val DIALOG_VANILIFE_TRAVEL_TICKET_USE: String = "dialog.vanilife.travel_ticket_use"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_USE_MESSAGE: String = "dialog.vanilife.travel_ticket_use.message"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_USE_NO: String = "dialog.vanilife.travel_ticket_use.no"
    const val DIALOG_VANILIFE_TRAVEL_TICKET_USE_YES: String = "dialog.vanilife.travel_ticket_use.yes"

    const val ITEM_VANILIFE_TRAVEL_TICKET = "item.vanilife.travel_ticket"

    fun us(): PackLanguage = mapOf(
        DIALOG_VANILIFE_TRAVEL to Translation.literal("Explore islands"),

        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_VISITORS to Translation.literal("Number of Visitors: ") + Translation.placeholder(),
        DIALOG_VANILIFE_TRAVEL_TICKET to Translation.literal("Chose a travel destination"),
        DIALOG_VANILIFE_TRAVEL_TICKET_APPLY_FILTER to Translation.literal("Search..."),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH to Translation.literal("Search results for \"") + Translation.placeholder() +
                Translation.literal("\" (") + Translation.placeholder() + Translation.literal(" results)"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_BOX to Translation.literal("Filter by keywords such as island name or MCID"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_AGAIN to Translation.literal("Search Again"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_LEVEL to Translation.literal("Level: ") + Translation.placeholder(),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_OWNER to Translation.literal("Owner: ") + Translation.placeholder(),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE to Translation.literal("Confirmation of travel destination"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_MESSAGE to Translation.literal("Will you use your travel ticket to visit \"") + Translation.placeholder() +
                Translation.literal("\""),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_YES to Translation.literal("Okay!"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_NO to Translation.literal("Pass for now"),
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("Travel Ticket"),
    )

    fun jp(): PackLanguage = mapOf(
        DIALOG_VANILIFE_TRAVEL to Translation.literal("島におでかけ"),

        DIALOG_VANILIFE_TRAVEL_TICKET to Translation.literal("旅行先をえらぶ"),
        DIALOG_VANILIFE_TRAVEL_TICKET_APPLY_FILTER to Translation.literal("検索..."),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH to Translation.literal("「") + Translation.placeholder() +
                Translation.literal("」の検索結果（") + Translation.placeholder() + Translation.literal("件）"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_BOX to Translation.literal("島名、MCIDなどで絞り込む"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_AGAIN to Translation.literal("もう一回検索する"),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_LEVEL to Translation.literal("レベル：") + Translation.placeholder(),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_OWNER to Translation.literal("島主：") + Translation.placeholder(),
        DIALOG_VANILIFE_TRAVEL_TICKET_SEARCH_VISITORS to Translation.literal("訪れた人：") + Translation.placeholder()
                + Translation.literal("人"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE to Translation.literal("旅行先の確認"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_MESSAGE to Translation.literal("旅行券を1枚使って「") + Translation.placeholder() +
                Translation.literal("」を訪問しますか？"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_YES to Translation.literal("オッケー！"),
        DIALOG_VANILIFE_TRAVEL_TICKET_USE_NO to Translation.literal("今はやめとく"),
        ITEM_VANILIFE_TRAVEL_TICKET to Translation.literal("旅行券"),
    )
}
