package net.azisaba.vanilife.island

import net.azisaba.vanilife.island.address.Address
import net.azisaba.vanilife.island.address.AddressHolder
import net.azisaba.vanilife.island.address.IslandAddressHolder
import net.azisaba.vanilife.island.util.PermissionSet
import net.azisaba.vanilife.island.lookup.PlayerLookup
import net.azisaba.vanilife.island.visitors.IslandVisitorAccess
import net.azisaba.vanilife.island.visitors.VisitorAccess
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class Island internal constructor(
    val type: IslandType<*>,
    val position: IslandPosition,
    protected val database: Database,
    private val primaryDataAccessDelegate: IslandPrimaryDataAccess = IslandPrimaryDataAccess(position, database),
    private val addressHolderDelegate: IslandAddressHolder = IslandAddressHolder(position, database),
    private val visitorAccessDelegate: IslandVisitorAccess = IslandVisitorAccess(position, database),
) : PrimaryDataAccess by primaryDataAccessDelegate, AddressHolder by addressHolderDelegate,
    VisitorAccess by visitorAccessDelegate, ForwardingAudience {
    val players: List<Player>
        get() = playersMutable.toList()

    protected val playersMutable: MutableSet<Player> = ConcurrentHashMap.newKeySet()

    protected val addressByPlayer: ConcurrentHashMap<UUID, Address> = ConcurrentHashMap()

    val playerCount: Int
        get() = playersMutable.size

    val hasPlayers: Boolean
        get() = playersMutable.isNotEmpty()

    open fun getPermissionSet(player: Player): PermissionSet {
        val address = addressByPlayer[player.uniqueId]
        return address?.let(::getPermissionSet) ?: PermissionSet.NO_ACCESS
    }

    open fun addPlayer(player: Player, address: Address? = null) {
        PlayerLookup.bind(player, position)
        playersMutable.add(player)
        address?.let {
            addressByPlayer[player.uniqueId] = address
        }
    }

    open fun removePlayer(player: Player) {
        PlayerLookup.unbind(player)
        playersMutable.remove(player)
        addressByPlayer.remove(player.uniqueId)
    }

    open suspend fun bootstrap() {
        primaryDataAccessDelegate.bootstrap()
        addressHolderDelegate.bootstrap()
        visitorAccessDelegate.bootstrap()
    }

    override fun audiences(): Iterable<Player> = playersMutable.toList()
}
