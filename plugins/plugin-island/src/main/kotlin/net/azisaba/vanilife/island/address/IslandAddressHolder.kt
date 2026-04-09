package net.azisaba.vanilife.island.address

import net.azisaba.vanilife.island.util.PermissionSet
import net.azisaba.vanilife.world.IslandPosition
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class IslandAddressHolder(position: IslandPosition, private val database: Database) : AddressHolder {
    override val addresses: Collection<Address>
        get() = requireLoaded().keys.toSet()

    private val positionId: Long = position.toLong()

    private var cacheMap: ConcurrentMap<Address, PermissionSet>? = null

    override fun getPermissionSet(address: Address): PermissionSet? = requireLoaded()[address]

    override suspend fun bindAddress(address: Address, permissionSet: PermissionSet) = suspendTransaction(database) {
        val cacheMap = requireLoaded()

        IslandAddressesTable.insert {
            it[IslandAddressesTable.address] = address
            it[IslandAddressesTable.position] = positionId
            it.applyPermissionSet(permissionSet)
        }

        cacheMap[address] = permissionSet
    }

    override suspend fun bindRandomAddress(permissionSet: PermissionSet, maxAttempts: Int): Address {
        val cacheMap = requireLoaded()

        return suspendTransaction(database) {
            repeat(maxAttempts) {
                val address = Address.random()

                if (cacheMap.containsKey(address)) return@repeat

                try {
                    IslandAddressesTable.insert {
                        it[IslandAddressesTable.address] = address
                        it[IslandAddressesTable.position] = positionId
                        it.applyPermissionSet(permissionSet)
                    }

                    cacheMap[address] = permissionSet
                    return@suspendTransaction address
                } catch (_: Throwable) {
                }
            }

            throw IllegalStateException("Failed to generate unique address after $maxAttempts attempts")
        }
    }

    override suspend fun unbindAddress(address: Address) {
        val cacheMap = requireLoaded()

        suspendTransaction(database) {
            IslandAddressesTable.deleteWhere {
                (IslandAddressesTable.address eq address) and (IslandAddressesTable.position eq positionId)
            }
        }

        cacheMap.remove(address)
    }

    override suspend fun unbindAddresses() {
        val cacheMap = requireLoaded()

        suspendTransaction(database) {
            IslandAddressesTable.deleteWhere {
                IslandAddressesTable.position eq positionId
            }
        }

        cacheMap.clear()
    }

    override suspend fun updateAddress(address: Address, permissionSet: PermissionSet) {
        val cacheMap = requireLoaded()

        suspendTransaction(database) {
            IslandAddressesTable.update(where = { (IslandAddressesTable.address eq address) and (IslandAddressesTable.position eq positionId) }) {
                it.applyPermissionSet(permissionSet)
            }
        }

        cacheMap[address] = permissionSet
    }

    suspend fun bootstrap() {
        val loaded = suspendTransaction(database) {
            IslandAddressesTable.selectAll()
                .where { IslandAddressesTable.position eq positionId }
                .associate { row ->
                    val address = row[IslandAddressesTable.address]
                    val permissionSet = row.toPermissionSet()
                    address to permissionSet
                }
                .toMap()
        }

        cacheMap = ConcurrentHashMap(loaded)
    }

    private fun requireLoaded(): ConcurrentMap<Address, PermissionSet> =
        cacheMap ?: throw IllegalStateException("Island addresses has not yet been loaded")

    private fun UpdateBuilder<*>.applyPermissionSet(permissionSet: PermissionSet) {
        set(IslandAddressesTable.canPlaceBlock, permissionSet.canPlaceBlock)
        set(IslandAddressesTable.canBreakBlock, permissionSet.canBreakBlock)
        set(IslandAddressesTable.canOpenChest, permissionSet.canOpenChest)
    }

    private fun ResultRow.toPermissionSet(): PermissionSet = PermissionSet(
        canPlaceBlock = get(IslandAddressesTable.canPlaceBlock),
        canBreakBlock = get(IslandAddressesTable.canBreakBlock),
        canOpenChest = get(IslandAddressesTable.canOpenChest),
    )
}
