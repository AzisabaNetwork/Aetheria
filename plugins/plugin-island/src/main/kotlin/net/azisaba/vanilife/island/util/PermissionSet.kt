package net.azisaba.vanilife.island.util

import kotlinx.serialization.Serializable

@Serializable
data class PermissionSet(
    val canPlaceBlock: Boolean,
    val canBreakBlock: Boolean,
    val canOpenChest: Boolean,
) {
    companion object {
        val FULL_ACCESS: PermissionSet = PermissionSet(
            canPlaceBlock = true, canBreakBlock = true, canOpenChest = true,
        )

        val NO_ACCESS: PermissionSet = PermissionSet(
            canPlaceBlock = false, canBreakBlock = false, canOpenChest = false,
        )
    }
}
