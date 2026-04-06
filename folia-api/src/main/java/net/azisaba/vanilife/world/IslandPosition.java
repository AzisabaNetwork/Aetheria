package net.azisaba.vanilife.world;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import java.util.Random;

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

    default BlockPosition defaultSpawnPosition(final long seed) {
        final long islandSeed = this.computeSeed(seed);
        final Random random = new Random(islandSeed ^ 0x63A7B4F5D91EC24AL);
        final int coastSide = random.nextInt(4);
        final int maxOffsetX = (IslandsWorld.ISLAND_SIZE_X_BLOCKS / 2) - 18;
        final int maxOffsetZ = (IslandsWorld.ISLAND_SIZE_Z_BLOCKS / 2) - 18;
        final int alongX = random.nextInt(-maxOffsetX, maxOffsetX + 1);
        final int alongZ = random.nextInt(-maxOffsetZ, maxOffsetZ + 1);
        final int shoreX = (IslandsWorld.ISLAND_SIZE_X_BLOCKS / 2) - 8;
        final int shoreZ = (IslandsWorld.ISLAND_SIZE_Z_BLOCKS / 2) - 8;

        final int offsetX;
        final int offsetZ;
        switch (coastSide) {
            case 0 -> {
                offsetX = alongX;
                offsetZ = -shoreZ;
            }
            case 1 -> {
                offsetX = shoreX;
                offsetZ = alongZ;
            }
            case 2 -> {
                offsetX = alongX;
                offsetZ = shoreZ;
            }
            default -> {
                offsetX = -shoreX;
                offsetZ = alongZ;
            }
        }

        return Position.block(this.centerBlockX() + offsetX, IslandsWorld.SEA_LEVEL + 2, this.centerBlockZ() + offsetZ);
    }

    default BlockPosition defaultPortalPosition(final long seed) {
        final long islandSeed = this.computeSeed(seed);
        final Random random = new Random(islandSeed ^ 0x2F7A46D1B0C8E51AL);
        final boolean axisX = random.nextBoolean();
        final int chunkOffsetX = random.nextBoolean() ? -1 : 0;
        final int chunkOffsetZ = random.nextBoolean() ? -1 : 0;
        final int localX = axisX ? random.nextInt(12) + 1 : random.nextInt(14) + 1;
        final int localZ = axisX ? random.nextInt(14) + 1 : random.nextInt(12) + 1;
        final int offsetX = chunkOffsetX * 16 + localX;
        final int offsetZ = chunkOffsetZ * 16 + localZ;
        return Position.block(this.centerBlockX() + offsetX, IslandsWorld.SEA_LEVEL, this.centerBlockZ() + offsetZ);
    }

    default boolean defaultPortalAxisX(final long seed) {
        final long islandSeed = this.computeSeed(seed);
        final Random random = new Random(islandSeed ^ 0x2F7A46D1B0C8E51AL);
        return random.nextBoolean();
    }

    default long computeSeed(final long salt) {
        long s = (((long) this.x() << 32) ^ ((long) this.z() & 0xffffffffL)) ^ salt;
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
