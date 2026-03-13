package net.azisaba.vanilife.islands.portal

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

internal interface PortalRepository {
    suspend fun insert(portal: Portal): Portal
    suspend fun findActive(): List<Portal>
    suspend fun delete(id: Long)
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
            it[PortalsTable.resourceX] = portal.resourceCenterX
            it[PortalsTable.resourceY] = portal.resourceCenterY
            it[PortalsTable.resourceZ] = portal.resourceCenterZ
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
        get(PortalsTable.resourceX),
        get(PortalsTable.resourceY),
        get(PortalsTable.resourceZ),
        get(PortalsTable.createdAt),
        get(PortalsTable.active),
    )

    private object PortalsTable : LongIdTable(name = "portals") {
        val owner = org.jetbrains.exposed.v1.core.java.javaUUID("owner")
        val originWorld = org.jetbrains.exposed.v1.core.varchar("origin_world", 64)
        val originMinX = org.jetbrains.exposed.v1.core.integer("origin_min_x")
        val originMinY = org.jetbrains.exposed.v1.core.integer("origin_min_y")
        val originMinZ = org.jetbrains.exposed.v1.core.integer("origin_min_z")
        val originMaxX = org.jetbrains.exposed.v1.core.integer("origin_max_x")
        val originMaxY = org.jetbrains.exposed.v1.core.integer("origin_max_y")
        val originMaxZ = org.jetbrains.exposed.v1.core.integer("origin_max_z")
        val orientation = org.jetbrains.exposed.v1.core.integer("orientation")
        val innerWidth = org.jetbrains.exposed.v1.core.integer("inner_width")
        val innerHeight = org.jetbrains.exposed.v1.core.integer("inner_height")
        val resourceWorld = org.jetbrains.exposed.v1.core.varchar("resource_world", 64)
        val resourceX = org.jetbrains.exposed.v1.core.integer("resource_x")
        val resourceY = org.jetbrains.exposed.v1.core.integer("resource_y")
        val resourceZ = org.jetbrains.exposed.v1.core.integer("resource_z")
        val createdAt = org.jetbrains.exposed.v1.core.long("created_at")
        val active = org.jetbrains.exposed.v1.core.bool("active").default(true)
    }
}
