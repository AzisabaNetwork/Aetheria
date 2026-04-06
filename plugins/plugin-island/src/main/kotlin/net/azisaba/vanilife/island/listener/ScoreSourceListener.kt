package net.azisaba.vanilife.island.listener

import com.github.shynixn.mccoroutine.folia.launch
import net.azisaba.vanilife.island.IslandsAccessor
import net.azisaba.vanilife.island.leveling.score.ScoreSource
import net.azisaba.vanilife.world.IslandPosition
import net.azisaba.vanilife.world.IslandsWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.player.PlayerHarvestBlockEvent
import org.bukkit.plugin.Plugin

internal class ScoreSourceListener(private val islands: IslandsAccessor, private val plugin: Plugin) : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.world !is IslandsWorld) return

        val blockState = event.block.state
        val position = IslandPosition.fromPosition(blockState.location)

        plugin.launch {
            val island = islands.byPosition(position) ?: return@launch

            ScoreSource.byLevel(island.level)
                .filterIsInstance<ScoreSource.BreakBlock>()
                .filter { it.containsBlock(blockState) }
                .forEach { source ->
                    island.updateScore(source)
                }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.world !is IslandsWorld) return

        val blockState = event.block.state
        val position = IslandPosition.fromPosition(blockState.location)

        plugin.launch {
            val island = islands.byPosition(position) ?: return@launch

            ScoreSource.byLevel(island.level)
                .filterIsInstance<ScoreSource.PlaceBlock>()
                .filter { it.containsBlock(blockState) }
                .forEach { source ->
                    island.updateScore(source)
                }
        }
    }

    @EventHandler
    fun onEntityBreed(event: EntityBreedEvent) {
        if (event.entity.world !is IslandsWorld) return

        val position = IslandPosition.fromPosition(event.entity.location)

        plugin.launch {
            val island = islands.byPosition(position) ?: return@launch

            ScoreSource.byLevel(island.level)
                .filterIsInstance<ScoreSource.Breed>()
                .filter { it.containsEntity(event.entity) }
                .forEach { source ->
                    island.updateScore(source)
                }
        }
    }

    @EventHandler
    fun onPlayerHarvestBlock(event: PlayerHarvestBlockEvent) {
        if (event.harvestedBlock.world !is IslandsWorld) return

        val blockState = event.harvestedBlock.state
        val position = IslandPosition.fromPosition(blockState.location)

        plugin.launch {
            val island = islands.byPosition(position) ?: return@launch

            ScoreSource.byLevel(island.level)
                .filterIsInstance<ScoreSource.Harvest>()
                .filter { it.containsCrop(blockState) }
                .forEach { source ->
                    island.updateScore(source)
                }
        }
    }
}
