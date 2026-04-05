package net.azisaba.vanilife.server.world.height;

import net.azisaba.vanilife.server.world.resource.ResourceLayer;
import net.azisaba.vanilife.server.world.resource.ResourceLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
public abstract class HeightContext implements HeightmapContext {
    private final HeightmapSet heightmap;

    public HeightContext(HeightmapSet heightmapSet) {
        this.heightmap = heightmapSet;
    }

    public abstract int seaLevel();

    public abstract int y(final int y1);

    public abstract int localY(final int y);

    public abstract int minY();

    public abstract int maxY();

    public boolean containsY(final int y) {
        return y >= this.minY() && y <= this.maxY();
    }

    public boolean containsY(final int y, final int margin) {
        return y >= this.minY() - margin && y <= this.maxY() + margin;
    }

    public boolean intersects(final BoundingBox box) {
        return this.intersects(box, 0);
    }

    public boolean intersects(final BoundingBox box, final int margin) {
        return box.maxY() >= this.minY() - margin && box.minY() <= this.maxY() + margin;
    }

    public BlockPos pos(final int x, final int y, final int z) {
        return new BlockPos(x, this.y(y), z);
    }

    public Heightmap.Types map(final Heightmap.Types type) {
        return switch (type) {
            case WORLD_SURFACE_WG -> this.worldSurfaceWg();
            case WORLD_SURFACE -> this.worldSurface();
            case OCEAN_FLOOR_WG -> this.oceanFloorWg();
            case OCEAN_FLOOR -> this.oceanFloor();
            case MOTION_BLOCKING -> this.motionBlocking();
            case MOTION_BLOCKING_NO_LEAVES -> this.motionBlockingNoLeaves();
            default -> type;
        };
    }

    @Override
    public Heightmap.Types worldSurfaceWg() {
        return this.heightmap.worldSurfaceWg();
    }

    @Override
    public Heightmap.Types worldSurface() {
        return this.heightmap.worldSurface();
    }

    @Override
    public Heightmap.Types oceanFloorWg() {
        return this.heightmap.oceanFloorWg();
    }

    @Override
    public Heightmap.Types oceanFloor() {
        return this.heightmap.oceanFloor();
    }

    @Override
    public Heightmap.Types motionBlocking() {
        return this.heightmap.motionBlocking();
    }

    @Override
    public Heightmap.Types motionBlockingNoLeaves() {
        return this.heightmap.motionBlockingNoLeaves();
    }

    public static class Vanilla extends HeightContext {
        private final WorldGenLevel worldGenRegion;

        public Vanilla(final WorldGenLevel level) {
            super(HeightmapSet.VANILLA);
            this.worldGenRegion = level;
        }

        @Override
        public int seaLevel() {
            return this.worldGenRegion.getSeaLevel();
        }

        @Override
        public int y(final int y1) {
            return y1;
        }

        @Override
        public int localY(final int y) {
            return y;
        }

        @Override
        public int minY() {
            return this.worldGenRegion.getMinY();
        }

        @Override
        public int maxY() {
            return this.worldGenRegion.getMaxY();
        }
    }

    public static class Layered extends HeightContext {
        private final ResourceLayout layout;
        private final ResourceLayer.Type layerType;

        public Layered(final ResourceLayout layout, final ResourceLayer.Type layerType) {
            super(layerType.heightmapSet());
            this.layout = layout;
            this.layerType = layerType;
        }

        @Override
        public int seaLevel() {
            return this.layerType.generator().getSeaLevel();
        }

        @Override
        public int y(int y1) {
            return this.layout.toBlockY(this.layerType, y1);
        }

        @Override
        public int localY(final int y) {
            return this.layout.toLayerY(this.layerType, y);
        }

        @Override
        public int minY() {
            return this.layout.getMinYOf(this.layerType);
        }

        @Override
        public int maxY() {
            return this.layout.getMaxYOf(this.layerType);
        }
    }

    public static class Identity extends HeightContext {
        private final int minY;
        private final int maxY;
        private final int seaLevel;

        public Identity(final int minY, final int maxY, final int seaLevel) {
            super(HeightmapSet.VANILLA);
            this.minY = minY;
            this.maxY = maxY;
            this.seaLevel = seaLevel;
        }

        @Override
        public int seaLevel() {
            return this.seaLevel;
        }

        @Override
        public int y(final int y1) {
            return y1;
        }

        @Override
        public int localY(final int y) {
            return y;
        }

        @Override
        public int minY() {
            return this.minY;
        }

        @Override
        public int maxY() {
            return this.maxY;
        }
    }
}
