package net.azisaba.vanilife.island.address

import net.azisaba.vanilife.island.IslandsTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID

internal abstract class AddressesTable(name: String) : Table(name) {
    val address: Column<Address> = varchar("address", Address.MAX_LENGTH).transform(
        { Address(it) },
        { it.value }
    )

    val canPlaceBlock: Column<Boolean> = bool("can_place_block")

    val canBreakBlock: Column<Boolean> = bool("can_break_block")

    val canOpenChest: Column<Boolean> = bool("can_open_chest")
}

internal object IslandAddressesTable : AddressesTable(name = "island_addresses") {
    val position: Column<EntityID<Long>> = reference(IslandsTable.id.name, IslandsTable)

    val spawnOffsetX: Column<Double> = double("spawn_offset_x")

    val spawnOffsetY: Column<Double> = double("spawn_offset_y")

    val spawnOffsetZ: Column<Double> = double("spawn_offset_z")

    val spawnYaw: Column<Float> = float("spawn_yaw")

    val spawnPitch: Column<Float> = float("spawn_pitch")

    override val primaryKey: PrimaryKey = PrimaryKey(address)
}
