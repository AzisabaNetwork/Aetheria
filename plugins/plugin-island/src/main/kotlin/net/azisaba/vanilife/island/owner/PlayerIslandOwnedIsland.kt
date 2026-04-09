package net.azisaba.vanilife.island.owner

import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.coroutines.future.await
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

internal class PlayerIslandOwnedIsland(
    position: IslandPosition, private val database: Database,
) : OwnedIsland<PlayerProfile> {
    override val owner: PlayerProfile
        get() = requireLoaded()

    private val positionId: Long = position.toLong()

    private var cache: PlayerProfile? = null

    suspend fun bootstrap() {
        val ownerUuid = suspendTransaction(database) {
            PlayerIslandOwnersTable.select(PlayerIslandOwnersTable.owner)
                .where { PlayerIslandOwnersTable.position eq positionId }
                .single()[PlayerIslandOwnersTable.owner]
        }

        cache = Bukkit.createProfile(ownerUuid).update().await()
    }

    private fun requireLoaded(): PlayerProfile = cache ?: throw IllegalStateException("Owner has not yet been loaded")
}
