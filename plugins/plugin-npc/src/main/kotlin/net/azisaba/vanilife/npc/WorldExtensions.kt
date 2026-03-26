package net.azisaba.vanilife.npc

import io.papermc.paper.math.Position
import org.bukkit.NamespacedKey
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Chicken
import org.bukkit.persistence.PersistentDataType
import org.koin.core.context.GlobalContext

fun World.spawn(position: Position, npcType: NpcType): Npc {
    val location = Location(this, position.x(), position.y(), position.z())
    val chicken = spawn(location, Chicken::class.java) { spawned ->
        spawned.isSilent = true
        spawned.isPersistent = false
        spawned.persistentDataContainer.set(NpcPersistentKeys.NPC_MARKER, PersistentDataType.BYTE, 1)
    }
    return Npc(npcType, chicken).apply(npcContainer()::put)
}

fun World.getNearbyNPCs(
    position: Position,
    xzRadius: Double,
    yRadius: Double,
    predicate: (Npc) -> Boolean = { true },
): Collection<Npc> = getNearbyNPCs(position, xzRadius, yRadius, xzRadius, predicate)

fun World.getNearbyNPCs(
    position: Position,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    predicate: (Npc) -> Boolean = { true },
): Collection<Npc> = getNearbyEntitiesByType(
    Chicken::class.java,
    Location(this, position.x(), position.y(), position.z()),
    xRadius, yRadius, zRadius,
).mapNotNull(npcContainer()::getByDelegate).filter(predicate)

fun World.getNearbyNPCsByType(
    type: NpcType,
    position: Position,
    xzRadius: Double,
    yRadius: Double,
    predicate: (Npc) -> Boolean = { true },
): Collection<Npc> = getNearbyNPCsByType(type, position, xzRadius, yRadius, xzRadius, predicate)

fun World.getNearbyNPCsByType(
    type: NpcType,
    position: Position,
    xRadius: Double,
    yRadius: Double,
    zRadius: Double,
    predicate: (Npc) -> Boolean = { true },
): Collection<Npc> = getNearbyNPCs(position, xRadius, yRadius, zRadius) { npc ->
    npc.npcType == type && predicate(npc)
}

fun World.collectNPCs(): Collection<Npc> = getEntitiesByClass(Chicken::class.java)
    .mapNotNull(npcContainer()::getByDelegate)

fun Chunk.collectNPCs(): Collection<Npc> = entities.filterIsInstance<Chicken>()
    .mapNotNull(npcContainer()::getByDelegate)

private fun npcContainer(): NpcContainer = GlobalContext.get().get<NpcContainer>()

internal object NpcPersistentKeys {
    val NPC_MARKER: NamespacedKey = NamespacedKey("vanilife", "npc")
}
