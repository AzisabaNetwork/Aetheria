package net.azisaba.vanilife.islands.portal

import io.papermc.paper.math.BlockPosition
import net.azisaba.vanilife.islands.IslandPos
import org.bukkit.World
import java.util.UUID

data class Portal(
    val id: Long?,
    val ownerUuid: UUID,
    val originWorldName: String,
    val originMin: BlockPosition,
    val originMax: BlockPosition,
    val orientation: DetectedPortal.Orientation,
    val innerWidth: Int,
    val innerHeight: Int,
    val resourceWorldName: String,
    val resourceMin: BlockPosition,
    val resourceMax: BlockPosition,
    val createdAt: Long,
    val active: Boolean,
)
