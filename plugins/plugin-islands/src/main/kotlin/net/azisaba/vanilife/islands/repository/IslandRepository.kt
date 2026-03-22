package net.azisaba.vanilife.islands.repository

import net.azisaba.vanilife.islands.IslandInfoLookup
import net.azisaba.vanilife.world.IslandPos
import net.azisaba.vanilife.islands.IslandSummary
import net.kyori.adventure.text.Component
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3d
import org.joml.Vector3dc
import java.util.*

internal interface IslandRepository : IslandInfoLookup {
    override suspend fun lookupByOwner(ownerUuid: UUID): IslandSummary?

    override suspend fun lookupByPos(islandPos: IslandPos): IslandSummary?

    suspend fun insert(ownerUuid: UUID, primaryData: PrimaryIslandData = PrimaryIslandData.Snapshot()): IslandSummary

    // Wipe handling: called when island/world is wiped to clear per-island transient state
    suspend fun handleWipe(where: IslandPos)

    suspend fun updateDisplayName(where: IslandPos, displayName: Component?)

    suspend fun updateSpawnOffset(where: IslandPos, offset: Vector3dc)

    suspend fun updateSpawnRotation(where: IslandPos, rotation: Vector2fc)

    // Dragon metadata helpers
    suspend fun updateDragonInstalled(where: IslandPos, installed: Boolean)

    suspend fun updateDragonPresetAir(where: IslandPos, presetKey: String?)

    suspend fun updateDragonPresetWater(where: IslandPos, presetKey: String?)

    suspend fun updateDragonLegacy(where: IslandPos, exists: Boolean, boostCredit: Int, lastPresetAir: String?)
}

internal class DatabaseIslandRepository(private val database: Database) : IslandRepository {
    override suspend fun insert(ownerUuid: UUID, primaryData: PrimaryIslandData): IslandSummary = suspendTransaction(database) {
        val islandPos = IslandsTable.insertAndGetId {
            it[IslandsTable.owner] = ownerUuid
            it[IslandsTable.displayName] = primaryData.displayName
            it[IslandsTable.spawnOffsetX] = primaryData.spawnOffset.x()
            it[IslandsTable.spawnOffsetY] = primaryData.spawnOffset.y()
            it[IslandsTable.spawnOffsetZ] = primaryData.spawnOffset.z()
            it[IslandsTable.spawnRotationYaw] = primaryData.spawnRotation.x()
            it[IslandsTable.spawnRotationPitch] = primaryData.spawnRotation.y()
            it[IslandsTable.dragonInstalled] = false
            it[IslandsTable.dragonPresetAir] = null
            it[IslandsTable.dragonPresetWater] = null
            it[IslandsTable.dragonLegacyExists] = false
            it[IslandsTable.dragonLegacyBoostCredit] = 0
            it[IslandsTable.dragonLegacyLastPresetAir] = null
        }.value
        IslandSummary(deserializePos(islandPos), ownerUuid, primaryData, DragonMetadata.Snapshot())
    }

    override suspend fun lookupByPos(islandPos: IslandPos): IslandSummary? = suspendTransaction(database) {
        IslandsTable
            .selectAll()
            .where { IslandsTable.id eq serializePos(islandPos) }
            .firstOrNull()
            ?.toIslandInfo()
    }

    override suspend fun lookupByOwner(ownerUuid: UUID): IslandSummary? = suspendTransaction(database) {
        IslandsTable
            .selectAll()
            .where { IslandsTable.owner eq ownerUuid }
            .firstOrNull()
            ?.toIslandInfo()
    }

    override suspend fun updateDisplayName(where: IslandPos, displayName: Component?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.displayName] = displayName
        }
        Unit
    }

    override suspend fun updateSpawnOffset(where: IslandPos, offset: Vector3dc) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.spawnOffsetX] = offset.x()
            it[IslandsTable.spawnOffsetY] = offset.y()
            it[IslandsTable.spawnOffsetZ] = offset.z()
        }
        Unit
    }

    override suspend fun updateSpawnRotation(where: IslandPos, rotation: Vector2fc) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.spawnRotationYaw] = rotation.x()
            it[IslandsTable.spawnRotationPitch] = rotation.y()
        }
        Unit
    }

    override suspend fun handleWipe(where: IslandPos) = suspendTransaction(database) {
        // Clear dragon color presets but grant legacy ticket if installed
        val row = IslandsTable.selectAll().where { IslandsTable.id eq serializePos(where) }.firstOrNull() ?: return@suspendTransaction
        val installed = row[IslandsTable.dragonInstalled]
        val lastAir = row[IslandsTable.dragonPresetAir]

        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.dragonPresetAir] = null
            it[IslandsTable.dragonPresetWater] = null
            it[IslandsTable.dragonInstalled] = false
            if (installed) {
                it[IslandsTable.dragonLegacyExists] = true
                it[IslandsTable.dragonLegacyBoostCredit] = 1
                it[IslandsTable.dragonLegacyLastPresetAir] = lastAir
            }
        }
        Unit
    }

    private fun serializePos(islandPos: IslandPos): Long {
        val x = islandPos.x().toLong()
        val z = islandPos.z().toLong()

        require(x in 0 until POS_WIDTH) { "x out of range: $x (expected 0..${POS_WIDTH - 1})" }
        require(z >= 0) { "z must be >= 0: $z" }

        return z * POS_WIDTH + x + 1L
    }

    private fun deserializePos(long: Long): IslandPos {
        require(long >= 1L) { "value must be >= 1: $long" }

        val id0 = long - 1L
        val x = (id0 % POS_WIDTH).toInt()
        val zLong = id0 / POS_WIDTH

        require(zLong <= Int.MAX_VALUE.toLong()) { "z out of Int range: $zLong" }
        val z = zLong.toInt()

        return IslandPos(x, z)
    }

    private fun ResultRow.toIslandInfo(): IslandSummary = IslandSummary(
        deserializePos(get(IslandsTable.id).value),
        get(IslandsTable.owner),
        PrimaryIslandData.Snapshot(
            get(IslandsTable.displayName),
            Vector3d(
                get(IslandsTable.spawnOffsetX),
                get(IslandsTable.spawnOffsetY),
                get(IslandsTable.spawnOffsetZ),
            ),
            Vector2f(
                get(IslandsTable.spawnRotationYaw),
                get(IslandsTable.spawnRotationPitch),
            )
        ),
        DragonMetadata.Snapshot(
            installed = get(IslandsTable.dragonInstalled),
            presetAir = get(IslandsTable.dragonPresetAir),
            presetWater = get(IslandsTable.dragonPresetWater),
            legacyExists = get(IslandsTable.dragonLegacyExists),
            legacyBoostCredit = get(IslandsTable.dragonLegacyBoostCredit),
            legacyLastPresetAir = get(IslandsTable.dragonLegacyLastPresetAir),
        )
    )

    private companion object {
        const val POS_WIDTH: Long = 4096L
    }

    override suspend fun updateDragonInstalled(where: IslandPos, installed: Boolean) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.dragonInstalled] = installed
        }
        Unit
    }

    override suspend fun updateDragonPresetAir(where: IslandPos, presetKey: String?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.dragonPresetAir] = presetKey
        }
        Unit
    }

    override suspend fun updateDragonPresetWater(where: IslandPos, presetKey: String?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.dragonPresetWater] = presetKey
        }
        Unit
    }

    override suspend fun updateDragonLegacy(where: IslandPos, exists: Boolean, boostCredit: Int, lastPresetAir: String?) = suspendTransaction(database) {
        IslandsTable.update(where = { IslandsTable.id eq serializePos(where) }) {
            it[IslandsTable.dragonLegacyExists] = exists
            it[IslandsTable.dragonLegacyBoostCredit] = boostCredit
            it[IslandsTable.dragonLegacyLastPresetAir] = lastPresetAir
        }
        Unit
    }

    object IslandsTable : LongIdTable(name = "islands", columnName = "pos") {
        val owner: Column<UUID> = javaUUID("owner").uniqueIndex()
        val displayName: Column<Component?> = component("display_name").nullable()
        val spawnOffsetX: Column<Double> = double("spawn_offset_x").default(0.0)
        val spawnOffsetY: Column<Double> = double("spawn_offset_y").default(0.0)
        val spawnOffsetZ: Column<Double> = double("spawn_offset_z").default(0.0)
        val spawnRotationYaw: Column<Float> = float("spawn_rotation_yaw").default(0f)
        val spawnRotationPitch: Column<Float> = float("spawn_rotation_pitch").default(0f)

        // Dragon metadata
        val dragonInstalled: Column<Boolean> = bool("dragon_installed").default(false)
        val dragonPresetAir: Column<String?> = varchar("dragon_preset_air", length = 128).nullable()
        val dragonPresetWater: Column<String?> = varchar("dragon_preset_water", length = 128).nullable()
        val dragonLegacyExists: Column<Boolean> = bool("dragon_legacy_exists").default(false)
        val dragonLegacyBoostCredit: Column<Int> = integer("dragon_legacy_boost_credit").default(0)
        val dragonLegacyLastPresetAir: Column<String?> = varchar("dragon_legacy_last_preset_air", length = 128).nullable()
    }
}
