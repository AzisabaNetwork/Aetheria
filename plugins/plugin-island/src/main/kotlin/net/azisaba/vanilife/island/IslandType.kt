package net.azisaba.vanilife.island

import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.StaticContents
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.jetbrains.exposed.v1.jdbc.Database

sealed class IslandType<T : Island> : Keyed {
    open val ticker: IslandTicker<T>?
        get() = null

    internal abstract suspend fun instantiate(
        position: IslandPosition,
        database: Database,
        config: ConfigurationHolder<Configuration>,
    ): T

    companion object : StaticContents<IslandType<*>>() {
        var PLAYER: IslandType<PlayerIsland> = bind(Player.key(), Player)
    }

    private object Player : IslandType<PlayerIsland>() {
        override fun key(): Key = Key.key(Vanilife.NAMESPACE, "player")

        override val ticker: IslandTicker<PlayerIsland> = IslandTicker { island, time ->
            if (island.hasPlayers) {
                island.waveTick(time)
            }

            if (island.isOwnerInIsland) {
                island.wrackTick(time, island.level, enchantments = island)
            } else if (island.isOwnerOnline && time % 3L == 0L) {
                island.wrackTick(time, island.level, enchantments = island)
            } else if (time % 100L == 0L) {
                island.wrackTick(time, island.level, enchantments = island)
            }
        }

        override suspend fun instantiate(
            position: IslandPosition,
            database: Database,
            config: ConfigurationHolder<Configuration>
        ): PlayerIsland = PlayerIsland(this, position, database, config)
    }
}
