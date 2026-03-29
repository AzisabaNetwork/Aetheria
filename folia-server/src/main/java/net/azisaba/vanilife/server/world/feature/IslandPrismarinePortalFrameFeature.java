package net.azisaba.vanilife.server.world.feature;

import com.mojang.serialization.Codec;
import net.azisaba.vanilife.world.IslandPosition;
import net.azisaba.vanilife.world.IslandsWorld;
import net.azisaba.vanilife.server.world.islands.IslandPortalLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

public final class IslandPrismarinePortalFrameFeature extends Feature<NoneFeatureConfiguration> {
    public IslandPrismarinePortalFrameFeature(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final WorldGenLevel level = context.level();
        final ChunkPos currentChunk = new ChunkPos(context.origin());
        for (final IslandPosition islandPos : candidateIslands(currentChunk)) {
            final IslandPortalLayout layout = IslandPortalLayout.create(islandPos.computeSeed(level.getSeed()));
            final BlockPos anchor = new BlockPos(
                islandPos.centerBlockX() + layout.blockOffsetX(),
                IslandsWorld.SEA_LEVEL,
                islandPos.centerBlockZ() + layout.blockOffsetZ()
            );
            if (!isSameChunk(anchor, currentChunk) || !fitsChunk(anchor, layout.axisX())) {
                continue;
            }
            final BlockPos base = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, anchor);
            return placeFrame(level, base, layout.axisX());
        }
        return false;
    }

    private Set<IslandPosition> candidateIslands(final ChunkPos chunkPos) {
        final int minX = chunkPos.getMinBlockX();
        final int minZ = chunkPos.getMinBlockZ();
        final int maxX = minX + 15;
        final int maxZ = minZ + 15;
        final int centerX = minX + 8;
        final int centerZ = minZ + 8;
        final LinkedHashSet<IslandPosition> islands = new LinkedHashSet<>();
        islands.add(IslandPosition.fromBlockPosition(minX, minZ));
        islands.add(IslandPosition.fromBlockPosition(minX, maxZ));
        islands.add(IslandPosition.fromBlockPosition(maxX, minZ));
        islands.add(IslandPosition.fromBlockPosition(maxX, maxZ));
        islands.add(IslandPosition.fromBlockPosition(centerX, centerZ));
        return islands;
    }

    private boolean placeFrame(final WorldGenLevel level, final BlockPos base, final boolean axisX) {
        final int frameWidth = 4;
        final int frameHeight = 5;
        final int lateralX = axisX ? 1 : 0;
        final int lateralZ = axisX ? 0 : 1;
        final int normalX = axisX ? 0 : 1;
        final int normalZ = axisX ? 1 : 0;
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int w = 0; w < frameWidth; w++) {
            final BlockPos footing = base.offset(lateralX * w, -1, lateralZ * w);
            final BlockPos currentGround = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, footing).below();
            if (currentGround.getY() < footing.getY()) {
                fillColumn(level, currentGround.above(), footing.getY() - currentGround.getY(), Blocks.DIRT.defaultBlockState());
            } else if (currentGround.getY() > footing.getY()) {
                clearColumn(level, footing.above(), currentGround.getY() - footing.getY());
            }
            level.setBlock(footing, Blocks.DIRT.defaultBlockState(), 2);
        }

        for (int w = -1; w <= frameWidth; w++) {
            for (int d = -1; d <= 1; d++) {
                final BlockPos column = base.offset(lateralX * w + normalX * d, 0, lateralZ * w + normalZ * d);
                for (int y = 0; y < frameHeight; y++) {
                    final BlockPos airPos = column.above(y);
                    if (!airPos.equals(base.offset(lateralX * w, y, lateralZ * w)) || w < 0 || w >= frameWidth) {
                        if (!level.getBlockState(airPos).isAir()) {
                            level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        for (int y = 0; y < frameHeight; y++) {
            for (int w = 0; w < frameWidth; w++) {
                cursor.set(base.getX() + lateralX * w, base.getY() + y, base.getZ() + lateralZ * w);
                final boolean border = y == 0 || y == frameHeight - 1 || w == 0 || w == frameWidth - 1;
                level.setBlock(cursor, border ? Blocks.PRISMARINE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }
        }

        return true;
    }

    private boolean isSameChunk(final BlockPos pos, final ChunkPos chunkPos) {
        return SectionPos.blockToSectionCoord(pos.getX()) == chunkPos.x && SectionPos.blockToSectionCoord(pos.getZ()) == chunkPos.z;
    }

    private boolean fitsChunk(final BlockPos pos, final boolean axisX) {
        final int localX = pos.getX() & 15;
        final int localZ = pos.getZ() & 15;
        if (axisX) {
            return localX >= 1 && localX <= 12 && localZ >= 1 && localZ <= 14;
        }
        return localX >= 1 && localX <= 14 && localZ >= 1 && localZ <= 12;
    }

    private void fillColumn(final WorldGenLevel level, final BlockPos start, final int height, final BlockState state) {
        for (int i = 0; i < height; i++) {
            level.setBlock(start.above(i), state, 2);
        }
    }

    private void clearColumn(final WorldGenLevel level, final BlockPos start, final int height) {
        for (int i = 0; i < height; i++) {
            level.setBlock(start.above(i), Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
