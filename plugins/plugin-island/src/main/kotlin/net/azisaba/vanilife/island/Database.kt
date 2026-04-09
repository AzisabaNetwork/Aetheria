package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.address.IslandAddressesTable
import net.azisaba.vanilife.island.enchantments.IslandEnchantmentsTable
import net.azisaba.vanilife.island.leveling.IslandLevelsTable
import net.azisaba.vanilife.island.owner.PlayerIslandOwnersTable
import net.azisaba.vanilife.island.storage.IslandStorageTable
import net.azisaba.vanilife.island.visitors.IslandVisitorsTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal fun Database.setupTables(): Database = transaction(this) {
    SchemaUtils.create(
        IslandsTable,
        IslandAddressesTable,
        IslandEnchantmentsTable,
        IslandLevelsTable,
        PlayerIslandOwnersTable,
        IslandStorageTable,
        IslandVisitorsTable,
    )
    this@setupTables
}
