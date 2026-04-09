package net.azisaba.vanilife.island.address

import net.azisaba.vanilife.island.util.PermissionSet

interface AddressHolder {
    val addresses: Collection<Address>

    fun getPermissionSet(address: Address): PermissionSet?

    suspend fun bindAddress(address: Address, permissionSet: PermissionSet)

    suspend fun bindRandomAddress(permissionSet: PermissionSet, maxAttempts: Int = 128): Address

    suspend fun unbindAddress(address: Address)

    suspend fun unbindAddresses()

    suspend fun updateAddress(address: Address, permissionSet: PermissionSet)
}
