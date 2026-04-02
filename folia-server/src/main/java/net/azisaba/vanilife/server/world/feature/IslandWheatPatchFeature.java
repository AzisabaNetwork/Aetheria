package net.azisaba.vanilife.server.world.feature;

import com.mojang.serialization.Codec;
import io.papermc.paper.math.BlockPosition;
import net.azisaba.vanilife.world.IslandPosition;
import net.azisaba.vanilife.world.IslandsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class IslandWheatPatchFeature extends Feature<NoneFeatureConfiguration> {
    public IslandWheatPatchFeature(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final WorldGenLevel level = context.level();
        final IslandPosition islandPos = IslandPosition.fromBlockXZ(context.origin().getX(), context.origin().getZ());
        final long seed = islandPos.computeSeed(level.getSeed()) ^ 0x51C2E6B4D9A3F17BL;
        final RandomSource random = RandomSource.create(seed);
        final ChunkPos currentChunk = new ChunkPos(context.origin());
        final BlockPosition spawnBlock = islandPos.defaultSpawnPosition(level.getSeed());
        boolean placed = false;

        final int wheatCount = 12 + random.nextInt(7);
        for (int i = 0; i < wheatCount; i++) {
            final BlockPos anchor = new BlockPos(
                islandPos.centerBlockX() + random.nextInt(-34, 35),
                IslandsWorld.SEA_LEVEL,
                islandPos.centerBlockZ() + random.nextInt(-34, 35)
            );
            if (!isSameChunk(anchor, currentChunk) || !hasChunkMargin(anchor, 0)) {
                continue;
            }

            final double spawnDistanceSq = Math.pow(anchor.getX() - spawnBlock.blockX(), 2) + Math.pow(anchor.getZ() - spawnBlock.blockZ(), 2);
            if (spawnDistanceSq < 15.0 * 15.0) {
                continue;
            }

            final BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, anchor);
            final BlockPos base = surface.below();
            placed |= placeSingleWheat(level, random, base);
        }

        return placed;
    }

    private boolean placeSingleWheat(final WorldGenLevel level, final RandomSource random, final BlockPos ground) {
        if (!canReplaceGround(level.getBlockState(ground))) {
            return false;
        }

        final BlockPos cropPos = ground.above();
        if (!level.getBlockState(cropPos).isAir()) {
            return false;
        }

        clearAbove(level, cropPos);
        level.setBlock(ground, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
        level.setBlock(cropPos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE), 2);

        if (random.nextFloat() < 0.25f) {
            final BlockPos accentPos = ground.below();
            if (level.getBlockState(accentPos).is(Blocks.DIRT) || level.getBlockState(accentPos).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(accentPos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
            }
        }
        return true;
    }

    private void clearAbove(final WorldGenLevel level, final BlockPos pos) {
        for (int dy = 0; dy <= 2; dy++) {
            final BlockPos target = pos.above(dy);
            if (!level.getBlockState(target).isAir()) {
                level.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private boolean canReplaceGround(final BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.DIRT)
            || state.is(Blocks.COARSE_DIRT)
            || state.is(Blocks.ROOTED_DIRT)
            || state.is(Blocks.MUD)
            || state.is(Blocks.GRAVEL);
    }

    private boolean isSameChunk(final BlockPos pos, final ChunkPos chunkPos) {
        return SectionPos.blockToSectionCoord(pos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(pos.getZ()) == chunkPos.z;
    }

    private boolean hasChunkMargin(final BlockPos pos, final int margin) {
        final int localX = pos.getX() & 15;
        final int localZ = pos.getZ() & 15;
        return localX >= margin && localX <= 15 - margin && localZ >= margin && localZ <= 15 - margin;
    }
}
