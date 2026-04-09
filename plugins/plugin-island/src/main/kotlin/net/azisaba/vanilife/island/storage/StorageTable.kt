package net.azisaba.vanilife.island.storage

import net.azisaba.exposed.itemStack
import net.azisaba.vanilife.island.IslandsTable
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.EntityID

internal abstract class StorageTable(name: String) : Table(name) {
    val index: Column<Int> = integer("index").check { it.between(0, 53) }

    val itemStack: Column<ItemStack> = itemStack("item_stack")
}

internal object IslandStorageTable : StorageTable(name = "island_storage") {
    val position: Column<EntityID<Long>> = reference(IslandsTable.id.name, IslandsTable)

    override val primaryKey: PrimaryKey = PrimaryKey(position, index)
}
