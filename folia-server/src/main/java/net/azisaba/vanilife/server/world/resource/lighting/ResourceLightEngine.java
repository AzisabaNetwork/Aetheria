package net.azisaba.vanilife.server.world.resource.lighting;

import ca.spottedleaf.moonrise.patches.starlight.light.SWMRNibbleArray;
import ca.spottedleaf.moonrise.patches.starlight.light.SkyStarLightEngine;
import net.azisaba.vanilife.server.world.resource.ResourceChunkGenerator;
import net.azisaba.vanilife.server.world.resource.ResourceLayer;
import net.azisaba.vanilife.server.world.resource.ResourceLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Arrays;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public final class ResourceLightEngine extends SkyStarLightEngine {
    public static boolean shouldUse(final Level level) {
        return level instanceof ServerLevel serverLevel && serverLevel.chunkSource.getGenerator() instanceof ResourceChunkGenerator;
    }

    @Override
    protected void propagateBlockChanges(final LightChunkGetter lightAccess, final ChunkAccess atChunk, final Set<BlockPos> positions) {
        final ResourceLayout resourceLayout = this.tryGetResourceLayout();
        if (resourceLayout == null) {
            super.propagateBlockChanges(lightAccess, atChunk, positions);
            return;
        }

        this.rewriteNibbleCacheForSkylight(atChunk);
        Arrays.fill(this.nullPropagationCheckCache, false);

        final BlockGetter world = lightAccess.getLevel();
        final int chunkX = atChunk.getPos().x;
        final int chunkZ = atChunk.getPos().z;
        final int heightMapOffset = chunkX * -16 + (chunkZ * (-16 * 16));

        for (final BlockPos pos : positions) {
            final int index = pos.getX() + (pos.getZ() << 4) + heightMapOffset;
            final int curr = this.heightMapBlockChange[index];
            if (pos.getY() > curr) {
                this.heightMapBlockChange[index] = pos.getY();
            }
        }

        for (int index = 0; index < (16 * 16); ++index) {
            final int maxY = this.heightMapBlockChange[index];
            if (maxY == Integer.MIN_VALUE) {
                continue;
            }
            this.heightMapBlockChange[index] = Integer.MIN_VALUE;

            final int columnX = (index & 15) | (chunkX << 4);
            final int columnZ = (index >>> 4) | (chunkZ << 4);
            this.propagateLayeredSkylightSourcesForColumn(world, resourceLayout, columnX, columnZ, true, true);
        }

        this.processDelayedIncreases();
        this.processDelayedDecreases();

        for (final BlockPos pos : positions) {
            this.checkBlock(lightAccess, pos.getX(), pos.getY(), pos.getZ());
        }

        this.performLightDecrease(lightAccess);
    }

    @Override
    protected void lightChunk(final LightChunkGetter lightAccess, final ChunkAccess chunk, final boolean needsEdgeChecks) {
        final ResourceLayout resourceLayout = this.tryGetResourceLayout();
        if (resourceLayout == null) {
            super.lightChunk(lightAccess, chunk, needsEdgeChecks);
            return;
        }

        this.rewriteNibbleCacheForSkylight(chunk);
        Arrays.fill(this.nullPropagationCheckCache, false);

        final BlockGetter world = lightAccess.getLevel();
        final ChunkPos chunkPos = chunk.getPos();
        final int chunkX = chunkPos.x;
        final int chunkZ = chunkPos.z;

        final LevelChunkSection[] sections = chunk.getSections();

        int highestNonEmptySection = this.maxSection;
        while (highestNonEmptySection == (this.minSection - 1)
            || sections[highestNonEmptySection - this.minSection] == null
            || sections[highestNonEmptySection - this.minSection].hasOnlyAir()) {
            this.checkNullSection(chunkX, highestNonEmptySection, chunkZ, false);

            for (final AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
                final int neighbourX = chunkX + direction.x;
                final int neighbourZ = chunkZ + direction.z;
                final SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(neighbourX, highestNonEmptySection, neighbourZ);
                if (neighbourNibble == null) {
                    continue;
                }

                final int incX;
                final int incZ;
                final int startX;
                final int startZ;

                if (direction.x != 0) {
                    incX = 0;
                    incZ = 1;
                    startX = direction.x < 0 ? chunkX << 4 : chunkX << 4 | 15;
                    startZ = chunkZ << 4;
                } else {
                    incX = 1;
                    incZ = 0;
                    startZ = direction.z < 0 ? chunkZ << 4 : chunkZ << 4 | 15;
                    startX = chunkX << 4;
                }

                final int encodeOffset = this.coordinateOffset;
                final long propagateDirection = 1L << direction.ordinal();

                for (int currY = highestNonEmptySection << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (int i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        this.appendToIncreaseQueue(
                            ((currX + (currZ << 6) + (currY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                | (15L << (6 + 6 + 16))
                                | (propagateDirection << (6 + 6 + 16 + 4))
                        );
                    }
                }
            }

            if (highestNonEmptySection-- == (this.minSection - 1)) {
                break;
            }
        }

        if (highestNonEmptySection >= this.minSection) {
            final int minX = chunkPos.x << 4;
            final int maxX = chunkPos.x << 4 | 15;
            final int minZ = chunkPos.z << 4;
            final int maxZ = chunkPos.z << 4 | 15;
            for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                for (int currX = minX; currX <= maxX; ++currX) {
                    for (final ResourceLayer.Type layerType : resourceLayout) {
                        this.tryPropagateSkylight(
                            world,
                            currX,
                            resourceLayout.getMaxYOf(layerType),
                            currZ,
                            false,
                            false,
                            layerType != resourceLayout.topLayer(),
                            resourceLayout.getMinYOf(layerType)
                        );
                    }
                }
            }
        }

        if (needsEdgeChecks) {
            this.performLightIncrease(lightAccess);

            for (int y = highestNonEmptySection; y >= this.minLightSection; --y) {
                this.checkNullSection(chunkX, y, chunkZ, false);
            }
            super.checkChunkEdges(lightAccess, chunk, this.minLightSection, highestNonEmptySection);
        } else {
            for (int y = highestNonEmptySection; y >= this.minLightSection; --y) {
                this.checkNullSection(chunkX, y, chunkZ, false);
            }
            this.propagateNeighbourLevels(lightAccess, chunk, this.minLightSection, highestNonEmptySection);

            this.performLightIncrease(lightAccess);
        }
    }

    private void propagateLayeredSkylightSourcesForColumn(
        final BlockGetter world,
        final ResourceLayout resourceLayout,
        final int columnX,
        final int columnZ,
        final boolean extrudeInitialised,
        final boolean delayLightSet
    ) {
        for (final ResourceLayer.Type layerType : resourceLayout) {
            final int maxPropagationY = this.tryPropagateSkylight(
                world,
                columnX,
                resourceLayout.getMaxYOf(layerType),
                columnZ,
                extrudeInitialised,
                delayLightSet,
                layerType != resourceLayout.topLayer(),
                resourceLayout.getMinYOf(layerType)
            );
            this.clearSkylightSourcesInSegment(
                columnX,
                columnZ,
                maxPropagationY,
                resourceLayout.getMinYOf(layerType),
                extrudeInitialised
            );
        }
    }

    private @Nullable ResourceLayout tryGetResourceLayout() {
        if (!(this.world instanceof ServerLevel serverLevel)) {
            return null;
        }

        final ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        return chunkGenerator instanceof ResourceChunkGenerator resourceChunkGenerator ? resourceChunkGenerator.layout : null;
    }

    private void clearSkylightSourcesInSegment(
        final int worldX,
        final int worldZ,
        final int maxPropagationY,
        final int minPropagationY,
        final boolean extrudeInitialised
    ) {
        final long propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection;
        final int encodeOffset = this.coordinateOffset;

        if (this.getLightLevelExtruded(worldX, maxPropagationY, worldZ) != 15) {
            return;
        }

        this.checkNullSection(worldX >> 4, maxPropagationY >> 4, worldZ >> 4, extrudeInitialised);

        for (int currY = maxPropagationY; currY >= minPropagationY; --currY) {
            if ((currY & 15) == 15) {
                this.checkNullSection(worldX >> 4, currY >> 4, worldZ >> 4, extrudeInitialised);
            }

            final SWMRNibbleArray nibble = this.getNibbleFromCache(worldX >> 4, currY >> 4, worldZ >> 4);
            if (nibble == null) {
                currY &= ~15;
                continue;
            }

            if (nibble.getUpdating(worldX, currY, worldZ) != 15) {
                break;
            }

            this.appendToDecreaseQueue(
                ((worldX + (worldZ << 6) + (currY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                    | (15L << (6 + 6 + 16))
                    | (propagateDirection << (6 + 6 + 16 + 4))
            );
        }
    }

    private int tryPropagateSkylight(
        final BlockGetter world,
        final int worldX,
        int startY,
        final int worldZ,
        final boolean extrudeInitialised,
        final boolean delayLightSet,
        final boolean ignoreAboveLight,
        final int minPropagationY
    ) {
        final int encodeOffset = this.coordinateOffset;
        final long propagateDirection = AxisDirection.POSITIVE_Y.everythingButThisDirection;

        if (!ignoreAboveLight && this.getLightLevelExtruded(worldX, startY + 1, worldZ) != 15) {
            return startY;
        }

        this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);

        BlockState above = this.getBlockState(worldX, startY + 1, worldZ);

        for (; startY >= minPropagationY; --startY) {
            if ((startY & 15) == 15) {
                this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);
            }
            final BlockState current = this.getBlockState(worldX, startY, worldZ);

            final VoxelShape fromShape;
            if (((ca.spottedleaf.moonrise.patches.starlight.blockstate.StarlightAbstractBlockState) above)
                .starlight$isConditionallyFullOpaque()) {
                fromShape = above.getFaceOcclusionShape(AxisDirection.NEGATIVE_Y.nms);
                if (Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                    break;
                }
            } else {
                fromShape = Shapes.empty();
            }

            long flags = 0L;
            if (((ca.spottedleaf.moonrise.patches.starlight.blockstate.StarlightAbstractBlockState) current)
                .starlight$isConditionallyFullOpaque()) {
                final VoxelShape cullingFace = current.getFaceOcclusionShape(AxisDirection.POSITIVE_Y.nms);

                if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                    break;
                }
                flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
            }

            final int opacity = current.getLightBlock();
            if (opacity > 0) {
                break;
            }

            this.appendToIncreaseQueue(
                ((worldX + (worldZ << 6) + (startY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                    | (15L << (6 + 6 + 16))
                    | (propagateDirection << (6 + 6 + 16 + 4))
                    | flags
            );

            above = current;

            if (this.getNibbleFromCache(worldX >> 4, startY >> 4, worldZ >> 4) == null) {
                --this.increaseQueueInitialLength;
                startY &= ~15;
                above = AIR_BLOCK_STATE;
            } else if (!delayLightSet) {
                this.setLightLevel(worldX, startY, worldZ, 15);
            }
        }

        return startY;
    }
}
