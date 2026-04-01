package net.azisaba.vanilife.island

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import kotlinx.coroutines.withContext
import net.azisaba.vanilife.Vanilife
import net.azisaba.vanilife.world.IslandPosition
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object IslandSpawnPointFinder : KoinComponent {
    private const val SEARCH_MARGIN_BLOCKS: Int = 4

    private val plugin: Plugin by inject()

    private val UNSAFE_GROUND_MATERIALS: Set<Material> = setOf(
        Material.CACTUS,
        Material.CAMPFIRE,
        Material.FIRE,
        Material.LAVA,
        Material.MAGMA_BLOCK,
        Material.POWDER_SNOW,
        Material.SWEET_BERRY_BUSH,
        Material.WATER,
    )

    suspend fun find(island: Island): Location? = find(island.position)

    suspend fun find(position: IslandPosition): Location? {
        val levelSeed = Vanilife.getIslandsWorld().seed
        val spawnBlock = position.spawnBlock(levelSeed)
        val spawnYaw = position.spawnYaw(levelSeed)
        val minBlockX = position.minBlockX() + SEARCH_MARGIN_BLOCKS
        val maxBlockX = position.maxBlockX() - SEARCH_MARGIN_BLOCKS
        val minBlockZ = position.minBlockZ() + SEARCH_MARGIN_BLOCKS
        val maxBlockZ = position.maxBlockZ() - SEARCH_MARGIN_BLOCKS
        val centerBlockX = spawnBlock.blockX().coerceIn(minBlockX, maxBlockX)
        val centerBlockZ = spawnBlock.blockZ().coerceIn(minBlockZ, maxBlockZ)
        val centerLocation = Location(Vanilife.getIslandsWorld(), centerBlockX + 0.5, 0.0, centerBlockZ + 0.5)

        return withContext(plugin.regionDispatcher(centerLocation)) {
            spiralOffsets(maxOf(maxBlockX - minBlockX, maxBlockZ - minBlockZ)).forEach { (offsetX, offsetZ) ->
                val blockX = centerBlockX + offsetX
                val blockZ = centerBlockZ + offsetZ
                if (blockX !in minBlockX..maxBlockX || blockZ !in minBlockZ..maxBlockZ) {
                    return@forEach
                }

                findAt(blockX, blockZ, spawnYaw)?.let { return@withContext it }
            }
            return@withContext null
        }
    }

    suspend fun isSafe(location: Location): Boolean {
        val world = location.world ?: return false
        val blockX = location.blockX
        val blockZ = location.blockZ
        val anchor = Location(world, location.x(), location.y(), location.z())

        return if (Bukkit.isOwnedByCurrentRegion(world, blockX shr 4, blockZ shr 4)) {
            isSafeAt(location)
        } else {
            withContext(plugin.regionDispatcher(anchor)) {
                isSafeAt(location)
            }
        }
    }

    private suspend fun findAt(blockX: Int, blockZ: Int, yaw: Float): Location? {
        val world = Vanilife.getIslandsWorld()
        val anchor = Location(world, blockX + 0.5, 0.0, blockZ + 0.5)
        val highestBlock = if (Bukkit.isOwnedByCurrentRegion(world, blockX shr 4, blockZ shr 4)) {
            world.getHighestBlockAt(blockX, blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES)
        } else {
            withContext(plugin.regionDispatcher(anchor)) {
                world.getHighestBlockAt(blockX, blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES)
            }
        }
        if (!isSafeGround(highestBlock)) {
            return null
        }

        val feetBlock = highestBlock.getRelative(0, 1, 0)
        val headBlock = feetBlock.getRelative(0, 1, 0)
        if (!feetBlock.isPassable || !headBlock.isPassable) {
            return null
        }

        return Location(
            world,
            blockX + 0.5,
            feetBlock.y.toDouble(),
            blockZ + 0.5,
            yaw,
            0f,
        )
    }

    private fun isSafeAt(location: Location): Boolean {
        val feetBlock = location.block
        val groundBlock = feetBlock.getRelative(0, -1, 0)
        val headBlock = feetBlock.getRelative(0, 1, 0)
        if (!isSafeGround(groundBlock)) {
            return false
        }
        if (!feetBlock.isPassable || !headBlock.isPassable) {
            return false
        }
        return true
    }

    private fun isSafeGround(block: Block): Boolean {
        if (!block.type.isSolid || block.isLiquid) {
            return false
        }
        return block.type !in UNSAFE_GROUND_MATERIALS
    }

    private fun spiralOffsets(maxRadius: Int): Sequence<Pair<Int, Int>> = sequence {
        yield(0 to 0)
        for (radius in 1..maxRadius) {
            for (x in -radius..radius) {
                yield(x to -radius)
                yield(x to radius)
            }
            for (z in (-radius + 1) until radius) {
                yield(-radius to z)
                yield(radius to z)
            }
        }
    }
}
