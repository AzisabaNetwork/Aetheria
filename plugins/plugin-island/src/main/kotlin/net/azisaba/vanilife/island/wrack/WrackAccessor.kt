package net.azisaba.vanilife.island.wrack

import kotlinx.coroutines.channels.Channel
import net.azisaba.serialization.IntProvider
import net.azisaba.vanilife.ConfigurationHolder
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.CoastSide
import net.azisaba.vanilife.island.enchantment.EnchantmentAccessor
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.random.Random

interface WrackAccessor {
    fun addWrackViewer(player: Player)

    fun removeWrackViewer(player: Player)

    fun spawnWrack(wrackType: WrackType)

    suspend fun wrackTick(time: Long, level: Int, enchantments: EnchantmentAccessor)

    companion object {
        fun create(
            position: IslandPosition,
            spawnLimit: ConfigurationHolder<Int>,
            spawnIntervalTicks: ConfigurationHolder<IntProvider>,
            plugin: Plugin,
        ): WrackAccessor = WrackAccessorImpl(position, spawnLimit, spawnIntervalTicks, plugin)
    }
}

private class WrackAccessorImpl(
    private val position: IslandPosition,
    private val spawnLimit: ConfigurationHolder<Int>,
    private val spawnIntervalTicks: ConfigurationHolder<IntProvider>,
    private val plugin: Plugin,
) : WrackAccessor {
    private val viewers: MutableSet<Player> = mutableSetOf()
    private val wrackEntities: MutableList<WrackEntity> = mutableListOf()
    private val tickingWrackEntities: MutableList<WrackEntity> = mutableListOf()
    private val random = Random(position.computeSeed(Vanilife.getIslandsWorld().seed))

    private val channel: Channel<Action> = Channel(Channel.BUFFERED)

    private var nextSpawnTime: Long = 0L

    override fun addWrackViewer(player: Player) = enqueueAction(Action.AddViewer(player))

    override fun removeWrackViewer(player: Player) = enqueueAction(Action.RemoveViewer(player))

    override fun spawnWrack(wrackType: WrackType) =
        enqueueAction(Action.SpawnWrack(wrackType, CoastSide.entries.random()))

    fun enqueueAction(action: Action) {
        val result = channel.trySend(action)
        if (result.isFailure) {
            plugin.componentLogger.warn("Failed to enqueue action: $action (${result.exceptionOrNull()})")
        }
    }

    override suspend fun wrackTick(time: Long, level: Int, enchantments: EnchantmentAccessor) {
        tickingWrackEntities.removeIf { !it.tick(time) }

        if (time >= nextSpawnTime && wrackEntities.size <= spawnLimit.value()) {
            spawnTick(time, level, enchantments)
        }

        while (true) {
            val action = channel.tryReceive().getOrNull() ?: break
            when (action) {
                is Action.AddViewer -> addViewerAction(action)
                is Action.RemoveViewer -> removeViewerAction(action)
                is Action.SpawnWrack -> spawnWrackAction(action, time)
            }
        }
    }

    private fun spawnTick(time: Long, level: Int, enchantments: EnchantmentAccessor) {
        viewers.removeIf { !it.isValid }
        if (viewers.isNotEmpty()) {
            WrackType.roll(random, level, enchantments)
                ?.takeIf { rolled ->
                    rolled !is WrackType.Enchantment || wrackEntities.none { wrackEntity ->
                        val type = wrackEntity.wrackType
                        type !is WrackType.Enchantment || type.enchantment != rolled
                    }
                }
                ?.let(::spawnWrack)
        }

        nextSpawnTime = time + spawnIntervalTicks.value().sample(random)
    }

    private fun addViewerAction(action: Action.AddViewer) {
        val player = action.viewer
        viewers.add(player)
        wrackEntities.forEach { it.addViewer(player) }
    }

    private fun removeViewerAction(action: Action.RemoveViewer) {
        val player = action.viewer
        viewers.remove(player)
        wrackEntities.forEach { it.removeViewer(player) }
    }

    private suspend fun spawnWrackAction(action: Action.SpawnWrack, time: Long) {
        val driftPath = DriftPath.random(position, action.coastSide, Vanilife.getIslandsWorld(), plugin)
        val wrackEntity = WrackEntity(action.wrackType, position, Vanilife.getIslandsWorld(), driftPath, time) {
            wrackEntities.remove(it)
            tickingWrackEntities.remove(it)
        }
        viewers.forEach(wrackEntity::addViewer)
        tickingWrackEntities.add(wrackEntity)
        wrackEntities.add(wrackEntity)
    }

    sealed interface Action {
        data class AddViewer(val viewer: Player) : Action

        data class RemoveViewer(val viewer: Player) : Action

        data class SpawnWrack(val wrackType: WrackType, val coastSide: CoastSide) : Action
    }
}
