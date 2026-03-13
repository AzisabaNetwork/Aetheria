package net.azisaba.vanilife.islands.portal

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import net.azisaba.vanilife.islands.portal.finder.DetectedPortal
import org.jetbrains.exposed.v1.core.java.javaUUID

internal interface PortalRepository {
    suspend fun insert(portal: Portal): Portal
    suspend fun findActive(): List<Portal>
    suspend fun delete(id: Long)
    suspend fun updateHologram(id: Long, hologramUuid: java.util.UUID?)
}

internal class DatabasePortalRepository(private val database: Database) : PortalRepository {
    override suspend fun insert(portal: Portal): Portal = suspendTransaction(database) {
        val id = PortalsTable.insertAndGetId {
            it[PortalsTable.owner] = portal.ownerUuid
            it[PortalsTable.originWorld] = portal.originWorldName
            it[PortalsTable.originMinX] = portal.originMin.blockX()
            it[PortalsTable.originMinY] = portal.originMin.blockY()
            it[PortalsTable.originMinZ] = portal.originMin.blockZ()
            it[PortalsTable.originMaxX] = portal.originMax.blockX()
            it[PortalsTable.originMaxY] = portal.originMax.blockY()
            it[PortalsTable.originMaxZ] = portal.originMax.blockZ()
            it[PortalsTable.orientation] = portal.orientation.ordinal
            it[PortalsTable.innerWidth] = portal.innerWidth
            it[PortalsTable.innerHeight] = portal.innerHeight
            it[PortalsTable.resourceWorld] = portal.resourceWorldName
            it[PortalsTable.resourceMinX] = portal.resourceMin.blockX()
            it[PortalsTable.resourceMinY] = portal.resourceMin.blockY()
            it[PortalsTable.resourceMinZ] = portal.resourceMin.blockZ()
            it[PortalsTable.resourceMaxX] = portal.resourceMax.blockX()
            it[PortalsTable.resourceMaxY] = portal.resourceMax.blockY()
            it[PortalsTable.resourceMaxZ] = portal.resourceMax.blockZ()
            it[PortalsTable.hologramUuid] = portal.hologramUuid
            it[PortalsTable.createdAt] = portal.createdAt
            it[PortalsTable.active] = portal.active
        }.value

        portal.copy(id = id)
    }

    override suspend fun findActive(): List<Portal> = suspendTransaction(database) {
        PortalsTable.selectAll().where { PortalsTable.active eq true }.map { it.toPortal() }
    }

    override suspend fun delete(id: Long) = suspendTransaction(database) {
        PortalsTable.update({ PortalsTable.id eq id }) { it[active] = false }
        Unit
    }

    override suspend fun updateHologram(id: Long, hologramUuid: java.util.UUID?) = suspendTransaction(database) {
        PortalsTable.update({ PortalsTable.id eq id }) { it[PortalsTable.hologramUuid] = hologramUuid }
        Unit
    }

    private fun ResultRow.toPortal(): Portal = Portal(
        get(PortalsTable.id).value,
        get(PortalsTable.owner),
        get(PortalsTable.originWorld),
        io.papermc.paper.math.Position.block(get(PortalsTable.originMinX), get(PortalsTable.originMinY), get(PortalsTable.originMinZ)),
        io.papermc.paper.math.Position.block(get(PortalsTable.originMaxX), get(PortalsTable.originMaxY), get(PortalsTable.originMaxZ)),
        DetectedPortal.Orientation.values()[get(PortalsTable.orientation)],
        get(PortalsTable.innerWidth),
        get(PortalsTable.innerHeight),
        get(PortalsTable.resourceWorld),
        io.papermc.paper.math.Position.block(get(PortalsTable.resourceMinX), get(PortalsTable.resourceMinY), get(PortalsTable.resourceMinZ)),
        io.papermc.paper.math.Position.block(get(PortalsTable.resourceMaxX), get(PortalsTable.resourceMaxY), get(PortalsTable.resourceMaxZ)),
        get(PortalsTable.hologramUuid),
        get(PortalsTable.createdAt),
        get(PortalsTable.active),
    )

}

// Exposed table object for portals (top-level so it can be created at startup)
object PortalsTable : LongIdTable(name = "portals") {
    val owner: Column<UUID> = javaUUID("owner")
    val originWorld: Column<String> = varchar("origin_world", 64)
    val originMinX: Column<Int> = integer("origin_min_x")
    val originMinY: Column<Int> = integer("origin_min_y")
    val originMinZ: Column<Int> = integer("origin_min_z")
    val originMaxX: Column<Int> = integer("origin_max_x")
    val originMaxY: Column<Int> = integer("origin_max_y")
    val originMaxZ: Column<Int> = integer("origin_max_z")
    val orientation: Column<Int> = integer("orientation")
    val innerWidth: Column<Int> = integer("inner_width")
    val innerHeight: Column<Int> = integer("inner_height")
    val resourceWorld: Column<String> = varchar("resource_world", 64)
    val resourceMinX: Column<Int> = integer("resource_min_x")
    val resourceMinY: Column<Int> = integer("resource_min_y")
    val resourceMinZ: Column<Int> = integer("resource_min_z")
    val resourceMaxX: Column<Int> = integer("resource_max_x")
    val resourceMaxY: Column<Int> = integer("resource_max_y")
    val resourceMaxZ: Column<Int> = integer("resource_max_z")
    val hologramUuid: Column<java.util.UUID?> = javaUUID("hologram_uuid").nullable()
    val createdAt: Column<Long> = long("created_at")
    val active: Column<Boolean> = bool("active").default(true)
}
