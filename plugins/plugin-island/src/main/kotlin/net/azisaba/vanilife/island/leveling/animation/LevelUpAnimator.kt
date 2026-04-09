package net.azisaba.vanilife.island.leveling.animation

import com.github.retrooper.packetevents.protocol.world.Location
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import io.papermc.paper.math.Position
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.tofaa.entitylib.container.EntityContainer
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.island.Island
import net.azisaba.vanilife.island.IslandFeature
import net.azisaba.vanilife.island.IslandFonts
import net.azisaba.vanilife.island.IslandSoundEvents
import net.azisaba.vanilife.island.IslandTranslations
import net.azisaba.vanilife.island.wrack.WrackType
import net.azisaba.vanilife.world.IslandPosition
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

internal object LevelUpAnimator {
    private const val EFFECT_RADIUS_BLOCKS: Int = 16
    private const val EFFECT_SPACING_BLOCKS: Int = 3
    private const val EFFECT_LIFETIME_TICKS: Int = 32

    suspend fun animate(island: Island, player: Player, newLevel: Int, oldLevel: Int, plugin: Plugin) {
        playSounds(player)
        sendMessage(player, newLevel, oldLevel)

        val effectArea = EffectArea(
            islandPosition = island.position,
            centerPosition = player.location,
        )

        val container = spawnEffects(effectArea, newLevel, plugin, viewer = player.uniqueId)

        playEffects(container)
    }

    private fun sendMessage(player: Player, newLevel: Int, oldLevel: Int) = player.sendMessage {
        val componentBuilder = Component.text().appendNewline()

        componentBuilder.append(Component.text(IslandFonts.IslandLevelIcons.levelOf(newLevel)).font(IslandFonts.ISLAND_LEVEL_ICONS))
            .append(Component.text(newLevel))
            .appendSpace()
            .append(Component.translatable(IslandTranslations.ISLAND_LEVEL_UP, NamedTextColor.YELLOW))

        for (newFeature in IslandFeature.entries.filter { it.requiredLevel == newLevel }) {
            componentBuilder.appendNewline()
                .append(
                    Component.translatable(
                        IslandTranslations.ISLAND_LEVEL_UP_UNLOCKED_FEATURE,
                        Component.translatable(newFeature, NamedTextColor.YELLOW),
                    )
                )
        }

        val oldWrackTypes = WrackType.byLevel(oldLevel)
        val newWrackTypes = WrackType.byLevel(newLevel)
        val unlockedWrackTypes = newWrackTypes - oldWrackTypes
        if (unlockedWrackTypes.isNotEmpty()) {
            componentBuilder.appendNewline()
                .append(
                    Component.translatable(
                        IslandTranslations.ISLAND_LEVEL_UP_UNLOCKED_WRACK_TYPES,
                        Component.text(unlockedWrackTypes.size),
                    )
                )
        }

        componentBuilder.appendNewline().build()
    }

    private fun playSounds(player: Player) {
        player.playSound(Sound.sound(IslandSoundEvents.ISLAND_LEVEL_UP, Sound.Source.PLAYER, 0.35f, 1f))
    }

    private suspend fun playEffects(entityContainer: EntityContainer) {
        try {
            repeat(EFFECT_LIFETIME_TICKS) { time ->
                entityContainer.forEach { entity ->
                    entity.tick(time.toLong())
                }
                delay(50L.milliseconds)
            }
        } finally {
            entityContainer.clearEntities(true)
        }
    }

    private suspend fun spawnEffects(
        effectArea: EffectArea,
        level: Int,
        plugin: Plugin,
        viewer: UUID
    ): EntityContainer {
        val entityContainer = EntityContainer.basic()
        val world = Vanilife.getIslandsWorld()

        withContext(plugin.regionDispatcher(world, effectArea.minBlockX shr 4, effectArea.minBlockZ shr 4)) {
            for (blockX in effectArea.minBlockX..effectArea.maxBlockX) {
                for (blockZ in effectArea.minBlockZ..effectArea.maxBlockZ) {
                    if (!effectArea.shouldSpawnEffectAt(blockX, blockZ)) continue

                    val highestY = if (Bukkit.isOwnedByCurrentRegion(world, blockX shr 4, blockZ shr 4)) {
                        world.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES)
                    } else withContext(plugin.regionDispatcher(world, blockX shr 4, blockZ shr 4)) {
                        world.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES)
                    }

                    val spawnX = blockX + 0.5
                    val spawnY = highestY + 1.0
                    val spawnZ = blockZ + 0.5

                    WrapperLevelUpEffect(level).apply {
                        addViewer(viewer)
                        spawn(Location(spawnX, spawnY, spawnZ, 0f, 0f), entityContainer)
                    }
                }
            }
        }

        return entityContainer
    }

    private data class EffectArea(val minBlockX: Int, val maxBlockX: Int, val minBlockZ: Int, val maxBlockZ: Int) {
        constructor(islandPosition: IslandPosition, centerPosition: Position) : this(
            minBlockX = max(islandPosition.minBlockX(), centerPosition.blockX() - EFFECT_RADIUS_BLOCKS),
            maxBlockX = min(islandPosition.maxBlockX(), centerPosition.blockX() + EFFECT_RADIUS_BLOCKS),
            minBlockZ = max(islandPosition.minBlockZ(), centerPosition.blockZ() - EFFECT_RADIUS_BLOCKS),
            maxBlockZ = min(islandPosition.maxBlockZ(), centerPosition.blockZ() + EFFECT_RADIUS_BLOCKS),
        )

        fun shouldSpawnEffectAt(blockX: Int, blockZ: Int): Boolean {
            if ((blockX - minBlockX) % EFFECT_SPACING_BLOCKS != 0) return false
            if ((blockZ - minBlockZ) % EFFECT_SPACING_BLOCKS != 0) return false
            return true
        }
    }
}
