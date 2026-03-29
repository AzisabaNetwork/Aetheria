package net.azisaba.vanilife.island

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.island.waves.WaveAccessor
import net.azisaba.vanilife.island.wrack.WrackAccessor
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.GlobalContext
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

class Island internal constructor(
    val position: IslandPosition,
    val owner: UUID,
    private val database: Database,
    private val players: MutableSet<Player> = CopyOnWriteArraySet(),
) : ForwardingAudience,
    PrimaryDataAccessor by PrimaryDataAccessor.fromDatabase(position, database),
    EnchantmentAccessor by EnchantmentAccessor.fromDatabase(position, database),
    WaveAccessor by WaveAccessor.create(position),
    WrackAccessor by WrackAccessor.create(position, Vanilife.getIslandsWorld(), GlobalContext.get().get()) {
    val primaryData: PrimaryDataAccessor
        get() = this

    private val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    internal suspend fun tick(time: Long) {
        waveTick(time)
        wrackTick(time)
    }

    suspend fun addPlayer(player: Player, withTeleport: Boolean = true) = withContext(dispatcher) {
        if (withTeleport && !player.teleportAsync(primaryData.spawnPoint(position, Vanilife.getIslandsWorld())).await())
            return@withContext

        if (players.add(player)) {
            addWaveViewer(player.uniqueId)
            addWrackViewer(player)
        }
    }

    suspend fun removePlayer(player: Player) = withContext(dispatcher) {
        if (players.remove(player)) {
            removeWaveViewer(player.uniqueId)
            removeWrackViewer(player)
        }
    }

    override fun audiences(): Iterable<Audience> = players.toSet()

    suspend fun isActive(): Boolean = onIslandDispatcher {
        players.isNotEmpty()
    }

    private suspend fun <T> onIslandDispatcher(block: suspend () -> T): T = withContext(dispatcher) {
        block()
    }
}
