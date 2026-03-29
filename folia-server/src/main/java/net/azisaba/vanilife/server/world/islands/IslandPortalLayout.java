package net.azisaba.vanilife.server.world.islands;

import net.minecraft.util.RandomSource;

public record IslandPortalLayout(int blockOffsetX, int blockOffsetZ, boolean axisX) {
    private static final long PORTAL_SALT = 0x2F7A46D1B0C8E51AL;

    public static IslandPortalLayout create(final long islandSeed) {
        final RandomSource random = RandomSource.create(islandSeed ^ PORTAL_SALT);
        final boolean axisX = random.nextBoolean();
        final int chunkOffsetX = random.nextBoolean() ? -1 : 0;
        final int chunkOffsetZ = random.nextBoolean() ? -1 : 0;
        final int localX = axisX ? random.nextInt(1, 13) : random.nextInt(1, 15);
        final int localZ = axisX ? random.nextInt(1, 15) : random.nextInt(1, 13);
        return new IslandPortalLayout(chunkOffsetX * 16 + localX, chunkOffsetZ * 16 + localZ, axisX);
    }
}
