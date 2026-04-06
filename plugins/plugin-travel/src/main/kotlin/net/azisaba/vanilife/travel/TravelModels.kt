package net.azisaba.vanilife.travel

import net.azisaba.packed.PackedKey
import net.azisaba.packed.model
import net.azisaba.packed.models.PackModel
import net.azisaba.vanilife.Vanilife
import net.kyori.adventure.key.Key

object TravelModels {
    val TRAVEL_TICKET: PackedKey<PackModel> = PackedKey.model(Vanilife.NAMESPACE, "item/travel_ticket")

    fun travelTicket(): PackModel = PackModel.item(Key.key(Vanilife.NAMESPACE, "item/travel_ticket"))
}
