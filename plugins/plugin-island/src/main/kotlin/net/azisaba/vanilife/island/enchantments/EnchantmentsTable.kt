package net.azisaba.vanilife.island.enchantments

import io.papermc.paper.registry.RegistryKey
import net.azisaba.exposed.key
import net.azisaba.vanilife.island.IslandsTable
import net.kyori.adventure.key.Key
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID

internal abstract class EnchantmentsTable(name: String) : Table(name) {
    val enchantment: Column<Key> = key("enchantment").transform(
        { it.key() },
        { RegistryKey.ENCHANTMENT.typedKey(it) }
    )
}

internal object IslandEnchantmentsTable : EnchantmentsTable(name = "player_island_enchantments") {
    val position: Column<EntityID<Long>> = reference(IslandsTable.id.name, IslandsTable)

    init {
        uniqueIndex(position, enchantment)
    }
}
