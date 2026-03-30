package net.azisaba.vanilife.island.enchantment

import net.azisaba.exposed.key
import net.azisaba.vanilife.island.IslandsTable
import net.kyori.adventure.key.Key
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID

internal object IslandEnchantmentsTable : Table("island_enchantments") {
    val position: Column<EntityID<Long>> = reference("position", IslandsTable)

    val enchantment: Column<Key> = key("enchantment")

    init {
        uniqueIndex(position, enchantment)
    }
}
