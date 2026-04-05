package net.azisaba.vanilife.server.world.feature;

import com.mojang.serialization.Codec;
import net.azisaba.vanilife.world.IslandPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

public final class IslandSpawnWreckageFeature extends Feature<NoneFeatureConfiguration> {
    public IslandSpawnWreckageFeature(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final WorldGenLevel level = context.level();
        final ChunkPos currentChunk = new ChunkPos(context.origin());
        final long levelSeed = level.getSeed();
        final RandomSource random = context.random();

        boolean placed = false;
        for (final IslandPosition islandPos : candidateIslands(currentChunk)) {
            final int spawnX = islandPos.defaultSpawnPosition(levelSeed).blockX();
            final int spawnZ = islandPos.defaultSpawnPosition(levelSeed).blockZ();

            if (SectionPos.blockToSectionCoord(spawnX) != currentChunk.x || SectionPos.blockToSectionCoord(spawnZ) != currentChunk.z) {
                continue;
            }

            final BlockPos base = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(spawnX, 0, spawnZ));

            if (placeWreckage(level, base, random)) {
                placed = true;
            }
        }
        return placed;
    }

    private Set<IslandPosition> candidateIslands(final ChunkPos chunkPos) {
        final int minX = chunkPos.getMinBlockX();
        final int minZ = chunkPos.getMinBlockZ();
        final int maxX = minX + 15;
        final int maxZ = minZ + 15;
        final int centerX = minX + 8;
        final int centerZ = minZ + 8;
        final LinkedHashSet<IslandPosition> islands = new LinkedHashSet<>();
        islands.add(IslandPosition.fromBlockXZ(minX, minZ));
        islands.add(IslandPosition.fromBlockXZ(minX, maxZ));
        islands.add(IslandPosition.fromBlockXZ(maxX, minZ));
        islands.add(IslandPosition.fromBlockXZ(maxX, maxZ));
        islands.add(IslandPosition.fromBlockXZ(centerX, centerZ));
        return islands;
    }

    private boolean placeWreckage(final WorldGenLevel level, final BlockPos base, final RandomSource random) {
        // Place some fire and maybe some "debris" around the spawn point
        final int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                final double distanceSq = x * x + z * z;
                if (distanceSq > radius * radius) continue;

                final BlockPos pos = new BlockPos(base.getX() + x, 0, base.getZ() + z);
                if (SectionPos.blockToSectionCoord(pos.getX()) != SectionPos.blockToSectionCoord(base.getX()) || SectionPos.blockToSectionCoord(pos.getZ()) != SectionPos.blockToSectionCoord(base.getZ())) {
                    continue;
                }
                final BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);

                // Check if the surface is actually陸地 (not water)
                if (level.getBlockState(surfacePos.below()).is(Blocks.WATER)) continue;

                // Randomly place fire
                if (random.nextFloat() < 0.25f * (1.0 - Math.sqrt(distanceSq) / radius)) {
                    if (level.getBlockState(surfacePos).isAir() && !level.getBlockState(surfacePos.below()).isAir()) {
                        level.setBlock(surfacePos, Blocks.FIRE.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private boolean isSameChunk(final BlockPos pos, final ChunkPos chunkPos) {
        return SectionPos.blockToSectionCoord(pos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(pos.getZ()) == chunkPos.z;
    }
}
