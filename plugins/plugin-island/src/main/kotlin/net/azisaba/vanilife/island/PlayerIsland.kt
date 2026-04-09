package net.azisaba.vanilife.island

import com.destroystokyo.paper.profile.PlayerProfile
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.island.address.Address
import net.azisaba.vanilife.island.enchantments.EnchantmentHolder
import net.azisaba.vanilife.island.enchantments.IslandEnchantmentHolder
import net.azisaba.vanilife.island.leveling.IslandLevellable
import net.azisaba.vanilife.island.leveling.Levellable
import net.azisaba.vanilife.island.owner.OwnedIsland
import net.azisaba.vanilife.island.owner.PlayerIslandOwnedIsland
import net.azisaba.vanilife.island.storage.IslandStorageHolder
import net.azisaba.vanilife.island.storage.StorageHolder
import net.azisaba.vanilife.island.util.PermissionSet
import net.azisaba.vanilife.island.waves.IslandWaveAccess
import net.azisaba.vanilife.island.waves.WaveAccess
import net.azisaba.vanilife.island.wrack.IslandWrackAccess
import net.azisaba.vanilife.island.wrack.WrackAccess
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.jdbc.Database

class PlayerIsland internal constructor(
    type: IslandType<*>,
    position: IslandPosition,
    database: Database,
    private val config: ConfigurationHolder<Configuration>,
    private val ownedIslandDelegate: PlayerIslandOwnedIsland = PlayerIslandOwnedIsland(position, database),
    private val levellableDelegate: IslandLevellable = IslandLevellable(position, database),
    private val enchantmentHolderDelegate: IslandEnchantmentHolder = IslandEnchantmentHolder(position, database),
    private val storageHolderDelegate: IslandStorageHolder = IslandStorageHolder(position, database),
    private val waveAccessDelegate: IslandWaveAccess = IslandWaveAccess(position),
    private val wrackAccessDelegate: IslandWrackAccess = IslandWrackAccess(position, config.map(Configuration::wrack)),
) : Island(type, position, database), OwnedIsland<PlayerProfile> by ownedIslandDelegate,
    Levellable by levellableDelegate,
    EnchantmentHolder by enchantmentHolderDelegate,
    StorageHolder by storageHolderDelegate,
    WaveAccess by waveAccessDelegate,
    WrackAccess by wrackAccessDelegate,
    IslandFeatureHolder {
    val isOwnerOnline: Boolean
        get() = owner.id?.let(Bukkit::getPlayer) != null

    val isOwnerInIsland: Boolean
        get() = playersMutable.any { it.uniqueId == owner.id }

    fun isOwner(player: OfflinePlayer): Boolean = player.uniqueId == owner.id

    fun isNotOwner(player: OfflinePlayer): Boolean = !isOwner(player)

    override fun getPermissionSet(player: Player): PermissionSet {
        if (isOwner(player)) return PermissionSet.FULL_ACCESS
        return super.getPermissionSet(player)
    }

    override fun addPlayer(player: Player, address: Address?) {
        super.addPlayer(player, address)
        addWaveViewer(player.uniqueId)
        addWrackViewer(player)
    }

    override fun removePlayer(player: Player) {
        super.removePlayer(player)
        removeWaveViewer(player.uniqueId)
        removeWrackViewer(player)
    }

    override suspend fun bootstrap() {
        super.bootstrap()
        ownedIslandDelegate.bootstrap()
        levellableDelegate.bootstrap()
        enchantmentHolderDelegate.bootstrap()
        storageHolderDelegate.bootstrap()
    }
}
