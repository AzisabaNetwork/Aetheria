package net.azisaba.vanilife.island

import kotlinx.coroutines.future.await
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

val Player.currentIsland: Island?
    get() = IslandsPlayerAccessor.byPlayer(uniqueId)

val Player.ownedIsland: Island
    get() = (this as OfflinePlayer).ownedIsland!!

val OfflinePlayer.ownedIsland: Island?
    get() {
        val islands = GlobalContext.get().get<IslandsAccessor>()
        return islands.byOwner(uniqueId)
    }

suspend fun Player.teleport(island: Island) {
    teleportAsync(island.spawnPoint).await()
    assignToIsland(island)
}

suspend fun Player.assignToIsland(island: Island) {
    IslandsPlayerAccessor.assign(this, island)
}

suspend fun Player.unassignFromIsland() {
    IslandsPlayerAccessor.unassign(this)
}
