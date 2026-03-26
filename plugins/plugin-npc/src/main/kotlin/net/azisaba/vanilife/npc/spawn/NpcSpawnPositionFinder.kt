package net.azisaba.vanilife.npc.spawn

import io.papermc.paper.math.Position
import kotlinx.serialization.Serializable
import org.bukkit.HeightMap
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

internal class NpcSpawnPositionFinder(private val config: Configuration) {
    fun find(world: World, xzPosition: Position, spawnRule: NpcSpawnRule): Position? {
        val x = xzPosition.blockX()
        val z = xzPosition.blockZ()
        val surfaceY = world.getHighestBlockYAt(x, z, HeightMap.RESOURCE_OVERWORLD_WORLD_SURFACE) + 1

        for (y in candidateYs(world, surfaceY)) {
            if (isPossibleBiomeAt(world, x, y, z, spawnRule) && isSafeSpawnSpaceAt(world, x, y, z)) {
                return Position.fine(xzPosition.x(), y.toDouble(), xzPosition.z())
            }
        }
        return null
    }

    private fun candidateYs(world: World, surfaceY: Int): IntProgression {
        val startY = (surfaceY + config.surfaceScanUp).coerceAtMost(world.maxHeight - 2)
        val endY = (surfaceY - config.surfaceScanDown).coerceAtLeast(world.minHeight + 1)
        return startY downTo endY
    }

    private fun isPossibleBiomeAt(world: World, x: Int, y: Int, z: Int, spawnRule: NpcSpawnRule): Boolean {
        val biome = world.getBiome(x, y, z)
        return spawnRule.supportsBiome(biome)
    }

    private fun isSafeSpawnSpaceAt(world: World, x: Int, y: Int, z: Int): Boolean {
        if (y <= world.minHeight || y + 1 >= world.maxHeight) return false

        val block = world.getBlockAt(x, y, z)
        val aboveBlock = block.getRelative(BlockFace.UP)
        val belowBlock = block.getRelative(BlockFace.DOWN)

        if (!block.isPassable || !aboveBlock.isPassable) return false
        if (!belowBlock.isSolid) return false
        if (belowBlock.isLiquid || block.isLiquid || aboveBlock.isLiquid) return false
        if (belowBlock.type in DANGEROUS_FLOOR_MATERIALS) return false
        if (block.type in DANGEROUS_BODY_MATERIALS || aboveBlock.type in DANGEROUS_BODY_MATERIALS) return false

        return true
    }

    private companion object {
        val DANGEROUS_BODY_MATERIALS: Set<Material> = setOf(
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
        )

        val DANGEROUS_FLOOR_MATERIALS: Set<Material> = setOf(
            Material.LAVA,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.FIRE,
            Material.SOUL_FIRE,
        )
    }

    @Serializable
    data class Configuration(val surfaceScanUp: Int = 8, val surfaceScanDown: Int = 96)
}
