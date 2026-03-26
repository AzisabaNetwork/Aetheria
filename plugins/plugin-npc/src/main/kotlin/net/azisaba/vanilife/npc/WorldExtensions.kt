package net.azisaba.vanilife.npc

import io.papermc.paper.math.Position
import net.azisaba.vanilife.npc.wrapper.NpcWrapper
import net.azisaba.vanilife.npc.wrapper.NpcWrapperMap
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Chicken
import org.koin.core.context.GlobalContext

fun World.spawn(position: Position, npcType: NpcType): NpcWrapper {
    val location = Location(this, position.x(), position.y(), position.z())
    val chicken = spawn(location, Chicken::class.java)
    return NpcWrapper.wrap(npcType, chicken).apply(npcWrapperMap()::register)
}

fun World.getNearbyNPCs(
    position: Position,
    xzRadius: Double,
    yRadius: Double,
    predicate: (NpcWrapper) -> Boolean = { true },
): Collection<NpcWrapper> = getNearbyNPCs(position, xzRadius, yRadius, xzRadius, predicate)

fun World.getNearbyNPCs(
    position: Position,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    predicate: (NpcWrapper) -> Boolean = { true },
): Collection<NpcWrapper> = getNearbyEntitiesByType(
    Chicken::class.java,
    Location(this, position.x(), position.y(), position.z()),
    xRadius, yRadius, zRadius,
).mapNotNull(npcWrapperMap()::byDelegate).filter(predicate)

fun World.getNearbyNPCsByType(
    type: NpcType,
    position: Position,
    xzRadius: Double,
    yRadius: Double,
    predicate: (NpcWrapper) -> Boolean = { true },
): Collection<NpcWrapper> = getNearbyNPCsByType(type, position, xzRadius, yRadius, xzRadius, predicate)

fun World.getNearbyNPCsByType(
    type: NpcType,
    position: Position,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    predicate: (NpcWrapper) -> Boolean = { true },
): Collection<NpcWrapper> = getNearbyNPCs(position, xRadius, yRadius, zRadius) { npc ->
    npc.npcType == type && predicate(npc)
}

fun World.collectNPCs(): Collection<NpcWrapper> = getEntitiesByClass(Chicken::class.java)
    .mapNotNull(npcWrapperMap()::byDelegate)

fun Chunk.collectNPCs(): Collection<NpcWrapper> = entities.filterIsInstance<Chicken>()
    .mapNotNull(npcWrapperMap()::byDelegate)

private fun npcWrapperMap(): NpcWrapperMap = GlobalContext.get().get<NpcWrapperMap>()

