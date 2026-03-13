package net.azisaba.vanilife.islands.portal

import io.papermc.paper.math.BlockPosition
import java.util.UUID

data class Portal(
    val id: Long?,
    val ownerUuid: UUID,
    val originWorldName: String,
    val originMin: BlockPosition,
    val originMax: BlockPosition,
    val orientation: net.azisaba.vanilife.islands.portal.finder.DetectedPortal.Orientation,
    val innerWidth: Int,
    val innerHeight: Int,
    val resourceWorldName: String,
    val resourceMin: BlockPosition,
    val resourceMax: BlockPosition,
    val hologramUuid: UUID?,
    val createdAt: Long,
    val active: Boolean,
)
