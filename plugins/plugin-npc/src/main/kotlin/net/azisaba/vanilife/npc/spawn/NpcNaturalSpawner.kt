package net.azisaba.vanilife.npc.spawn

import io.papermc.paper.math.Position
import net.azisaba.vanilife.npc.Npc
import net.azisaba.vanilife.npc.NpcType
import net.azisaba.vanilife.npc.getNearbyNPCsByType
import net.azisaba.vanilife.npc.spawn
import org.bukkit.Location
import org.bukkit.World

internal class NpcNaturalSpawner {
    fun canSpawn(
        world: World,
        type: NpcType,
        position: Position,
        nearbyXZRange: Double = DEFAULT_NEARBY_XZ_RANGE,
        nearbyYRange: Double = DEFAULT_NEARBY_Y_RANGE,
        nearbyLimit: Int = DEFAULT_NEARBY_LIMIT,
    ): Boolean = world.getNearbyNPCsByType(
        type = type,
        position = position,
        xzRadius = nearbyXZRange,
        yRadius = nearbyYRange,
    ).size < nearbyLimit

    fun spawnIfPossible(
        world: World,
        type: NpcType,
        position: Position,
        nearbyXZRange: Double = DEFAULT_NEARBY_XZ_RANGE,
        nearbyYRange: Double = DEFAULT_NEARBY_Y_RANGE,
        nearbyLimit: Int = DEFAULT_NEARBY_LIMIT,
    ): Npc? {
        if (!canSpawn(world, type, position, nearbyXZRange, nearbyYRange, nearbyLimit)) {
            return null
        }

        return world.spawn(position, type)
    }

    fun spawnIfPossible(
        location: Location,
        type: NpcType,
        nearbyXZRange: Double = DEFAULT_NEARBY_XZ_RANGE,
        nearbyYRange: Double = DEFAULT_NEARBY_Y_RANGE,
        nearbyLimit: Int = DEFAULT_NEARBY_LIMIT,
    ): Npc? = spawnIfPossible(
        world = location.world,
        type = type,
        position = Position.fine(location.x, location.y, location.z),
        nearbyXZRange = nearbyXZRange,
        nearbyYRange = nearbyYRange,
        nearbyLimit = nearbyLimit,
    )

    fun countNearby(
        world: World,
        type: NpcType,
        position: Position,
        nearbyXZRange: Double = DEFAULT_NEARBY_XZ_RANGE,
        nearbyYRange: Double = DEFAULT_NEARBY_Y_RANGE,
    ): Int = world.getNearbyNPCsByType(
        type = type,
        position = position,
        xzRadius = nearbyXZRange,
        yRadius = nearbyYRange,
    ).size

    private companion object {
        const val DEFAULT_NEARBY_LIMIT: Int = 4
        const val DEFAULT_NEARBY_XZ_RANGE: Double = 48.0
        const val DEFAULT_NEARBY_Y_RANGE: Double = 24.0
    }
}
