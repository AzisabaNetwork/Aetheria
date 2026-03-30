package net.azisaba.vanilife.world;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface IslandPosition permits IslandPositionImpl {
    long SERIALIZED_WIDTH = 4096L;

    static IslandPosition of(final int x, final int z) {
        return new IslandPositionImpl(x, z);
    }

    static IslandPosition fromPosition(final Position position) {
        return fromBlockXZ(position.blockX(), position.blockZ());
    }

    static IslandPosition fromBlockXZ(final int blockX, final int blockZ) {
        return of(islandAxisOf(blockX), islandAxisOf(blockZ));
    }

    static IslandPosition fromLong(final long value) {
        Preconditions.checkArgument(value >= 1L, "Value must be >= 1: %s", value);

        final long id0 = value - 1L;
        final int x = (int) (id0 % SERIALIZED_WIDTH);
        final long zLong = id0 / SERIALIZED_WIDTH;
        Preconditions.checkArgument(zLong <= Integer.MAX_VALUE, "Z out of Int range: %s", zLong);

        return of(x, (int) zLong);
    }

    private static int islandAxisOf(final int levelCoord) {
        final int half = IslandsWorld.SPACING_BLOCKS / 2;
        return Math.floorDiv(levelCoord + half, IslandsWorld.SPACING_BLOCKS);
    }

    int x();

    int z();

    default int minBlockX() {
        return centerBlockX() - (IslandsWorld.ISLAND_SIZE_X_BLOCKS / 2);
    }

    default int maxBlockX() {
        return centerBlockX() + (IslandsWorld.ISLAND_SIZE_X_BLOCKS / 2);
    }

    default int minBlockZ() {
        return centerBlockZ() - (IslandsWorld.ISLAND_SIZE_Z_BLOCKS / 2);
    }

    default int maxBlockZ() {
        return centerBlockZ() + (IslandsWorld.ISLAND_SIZE_Z_BLOCKS / 2);
    }

    default int centerBlockX() {
        return this.x() * IslandsWorld.SPACING_BLOCKS;
    }

    default int centerBlockZ() {
        return this.z() * IslandsWorld.SPACING_BLOCKS;
    }

    default long computeSeed(final long levelSeed) {
        long s = (((long) this.x() << 32) ^ ((long) this.z() & 0xffffffffL)) ^ levelSeed;
        s ^= (s >>> 30);
        s *= 0xBF58476D1CE4E5B9L;
        s ^= (s >>> 27);
        s *= 0x94D049BB133111EBL;
        s ^= (s >>> 31);
        return s;
    }

    default long toLong() {
        final long x = this.x();
        final long z = this.z();
        Preconditions.checkArgument(x >= 0L && x < SERIALIZED_WIDTH, "X out of range: %s (expected 0..%s)", x, SERIALIZED_WIDTH - 1L);
        Preconditions.checkArgument(z >= 0L, "Z must be >= 0: %s", z);
        return z * SERIALIZED_WIDTH + x + 1L;
    }
}
