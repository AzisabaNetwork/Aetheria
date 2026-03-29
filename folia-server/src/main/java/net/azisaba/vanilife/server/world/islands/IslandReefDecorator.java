package net.azisaba.vanilife.server.world.islands;

import net.azisaba.vanilife.server.world.islands.IslandTerrainSampler.TerrainSample;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class IslandReefDecorator {
    private static final long REEF_RANDOM_SALT = 0x50A1D4B71E2CL;
    private static final int SHALLOW_REEF_MIN_DEPTH = 1;
    private static final int SHALLOW_REEF_MAX_DEPTH = 4;
    private static final int FEATURE_CHUNK_MARGIN = 1;

    private final IslandsGeneratorSettings settings;
    private final Sampler sampler;

    IslandReefDecorator(final IslandsGeneratorSettings settings, final Sampler sampler) {
        this.settings = settings;
        this.sampler = sampler;
    }

    void buildSurface(final WorldGenLevel level, final ChunkPos chunkPos) {
        final RandomSource random = RandomSource.create(level.getSeed() ^ chunkPos.toLong() ^ REEF_RANDOM_SALT);
        this.placeShallowReefs(level, chunkPos, random);
    }

    private void placeShallowReefs(final WorldGenLevel level, final ChunkPos chunkPos, final RandomSource random) {
        final int attempts = 3 + random.nextInt(2);
        for (int i = 0; i < attempts; i++) {
            final int blockX = chunkPos.getMinBlockX() + FEATURE_CHUNK_MARGIN + random.nextInt(16 - FEATURE_CHUNK_MARGIN * 2);
            final int blockZ = chunkPos.getMinBlockZ() + FEATURE_CHUNK_MARGIN + random.nextInt(16 - FEATURE_CHUNK_MARGIN * 2);
            this.tryPlaceShallowReef(level, chunkPos, random, blockX, blockZ);
        }
    }

    private void tryPlaceShallowReef(
        final WorldGenLevel level,
        final ChunkPos chunkPos,
        final RandomSource random,
        final int blockX,
        final int blockZ
    ) {
        final TerrainSample sample = this.sampler.sample(level.getSeed(), blockX, blockZ);
        final int depth = this.settings.seaLevel() - sample.highestY();
        if (sample.highestY() >= this.settings.seaLevel()) {
            return;
        }
        if (depth < SHALLOW_REEF_MIN_DEPTH || depth > SHALLOW_REEF_MAX_DEPTH) {
            return;
        }
        if (sample.signedDistance() < 1.5 || sample.signedDistance() > this.settings.beachWidth() + 9.0) {
            return;
        }

        final int radius = 1 + random.nextInt(2);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if ((dx * dx) + (dz * dz) > radius * radius + 1) {
                    continue;
                }

                final int placeX = blockX + dx;
                final int placeZ = blockZ + dz;
                if (!this.isWithinChunkMargin(chunkPos, placeX, placeZ)) {
                    continue;
                }

                final TerrainSample localSample = this.sampler.sample(level.getSeed(), placeX, placeZ);
                final int columnTopY = this.computeReefColumnHeight(localSample.highestY(), random);
                for (int y = localSample.highestY(); y <= columnTopY; y++) {
                    final BlockPos placePos = new BlockPos(placeX, y, placeZ);
                    if (!this.isReefReplaceable(level.getBlockState(placePos))) {
                        continue;
                    }
                    level.setBlock(placePos, this.chooseReefState(random), Block.UPDATE_NONE);
                }
            }
        }
    }

    private BlockState chooseReefState(final RandomSource random) {
        final int roll = random.nextInt(10);
        if (roll < 5) {
            return Blocks.STONE.defaultBlockState();
        }
        if (roll < 8) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private boolean isReefReplaceable(final BlockState state) {
        return state.isAir()
            || state.is(Blocks.WATER)
            || state.is(Blocks.SAND)
            || state.is(Blocks.GRAVEL)
            || state.is(Blocks.CLAY)
            || state.is(Blocks.STONE);
    }

    private boolean isWithinChunkMargin(final ChunkPos chunkPos, final int blockX, final int blockZ) {
        final int minX = chunkPos.getMinBlockX() + FEATURE_CHUNK_MARGIN;
        final int maxX = chunkPos.getMaxBlockX() - FEATURE_CHUNK_MARGIN;
        final int minZ = chunkPos.getMinBlockZ() + FEATURE_CHUNK_MARGIN;
        final int maxZ = chunkPos.getMaxBlockZ() - FEATURE_CHUNK_MARGIN;
        return blockX >= minX && blockX <= maxX && blockZ >= minZ && blockZ <= maxZ;
    }

    private int computeReefColumnHeight(final int seafloorY, final RandomSource random) {
        return Math.min(this.settings.seaLevel(), seafloorY + 1 + random.nextInt(2));
    }

    @FunctionalInterface
    interface Sampler {
        TerrainSample sample(long levelSeed, int blockX, int blockZ);
    }
}
