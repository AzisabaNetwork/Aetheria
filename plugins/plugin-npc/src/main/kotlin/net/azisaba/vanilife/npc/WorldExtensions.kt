package net.azisaba.vanilife.npc

import io.papermc.paper.math.Position
import org.bukkit.NamespacedKey
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Chicken
import org.bukkit.persistence.PersistentDataType
import org.koin.core.context.GlobalContext

fun World.spawn(position: Position, npcType: NpcType): NpcWrapper {
    val location = Location(this, position.x(), position.y(), position.z())
    val chicken = spawn(location, Chicken::class.java) { spawned ->
        spawned.isSilent = true
        spawned.isPersistent = false
        spawned.persistentDataContainer.set(NpcPersistentKeys.NPC_MARKER, PersistentDataType.BYTE, 1)
    }
    return NpcWrapper(npcType, chicken).apply(npcContainer()::put)
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
).mapNotNull(npcContainer()::getByDelegate).filter(predicate)

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
    .mapNotNull(npcContainer()::getByDelegate)

fun Chunk.collectNPCs(): Collection<NpcWrapper> = entities.filterIsInstance<Chicken>()
    .mapNotNull(npcContainer()::getByDelegate)

private fun npcContainer(): NpcContainer = GlobalContext.get().get<NpcContainer>()

internal object NpcPersistentKeys {
    val NPC_MARKER: NamespacedKey = NamespacedKey("vanilife", "npc")
}
