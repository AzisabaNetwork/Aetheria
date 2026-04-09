package net.azisaba.vanilife.island.owner

import net.azisaba.vanilife.island.IslandsTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.java.javaUUID
import java.util.UUID

internal abstract class OwnersTable<T : Any>(name: String) : Table(name) {
    abstract val owner: Column<T>
}

internal object PlayerIslandOwnersTable : OwnersTable<UUID>(name = "player_island_owners") {
    val position: Column<EntityID<Long>> = reference(IslandsTable.id.name, foreign = IslandsTable)

    override val owner: Column<UUID> = javaUUID("owner")
}
