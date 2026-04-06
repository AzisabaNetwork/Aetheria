package net.azisaba.vanilife.travel

import net.azisaba.packed.PackedKey
import net.azisaba.packed.itemModel
import net.azisaba.packed.items.PackItemModel
import net.azisaba.packed.items.properties.PackModelItemModelProperties
import net.azisaba.vanilife.Vanilife

object TravelItemModels {
    val TRAVEL_TICKET: PackedKey<PackItemModel> = PackedKey.itemModel(Vanilife.NAMESPACE, "travel_ticket")

    fun travelTicket(): PackItemModel = PackItemModel(PackModelItemModelProperties(TravelModels.TRAVEL_TICKET))
}
