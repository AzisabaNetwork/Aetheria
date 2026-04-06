package net.azisaba.vanilife.travel

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.event.RegistryComposeEvent
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.item.ServerItem
import net.azisaba.vanilife.registry.data.ServerItemCategory
import net.azisaba.vanilife.registry.data.ServerItemRegistryEntry
import net.kyori.adventure.key.Key

object TravelItems {
    val TRAVEL_TICKET: TypedKey<ServerItem> = RegistryKey.SERVER_ITEM.typedKey(Key.key(Vanilife.NAMESPACE, "travel_ticket"))

    internal fun bootstrap(event: RegistryComposeEvent<ServerItem, ServerItemRegistryEntry.Builder>) {
        event.registry().register(TRAVEL_TICKET, ::travelTicket)
    }

    private fun travelTicket(builder: ServerItemRegistryEntry.Builder) {
        builder.translationKey(TravelTranslations.ITEM_VANILIFE_TRAVEL_TICKET)
            .category(ServerItemCategory.TOOL)
            .itemModel(TravelItemModels.TRAVEL_TICKET)
    }
}
