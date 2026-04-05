package net.azisaba.vanilife.server.world.islands.river;

import net.azisaba.vanilife.server.world.islands.IslandsGeneratorSettings;
import net.azisaba.vanilife.world.IslandPosition;
import net.azisaba.vanilife.world.IslandsWorld;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
public final class IslandRiverLayout {
    private static final long RIVER_NOISE_SALT = 0x71D1B54A6CE29EF3L;

    private final double halfWidth;
    private final double halfHeight;
    private final RiverNetwork riverNetwork;
    private final NormalNoise riverNoise;
    private final RiverPathSampler pathSampler;

    public static IslandRiverLayout createDefault(final long levelSeed, final IslandPosition islandPos) {
        return new IslandRiverLayout(levelSeed, islandPos, IslandsWorld.ISLAND_SIZE_X_BLOCKS, IslandsWorld.ISLAND_SIZE_Z_BLOCKS);
    }

    public IslandRiverLayout(final long levelSeed, final IslandPosition islandPos, final int width, final int height) {
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;
        final long islandSeed = islandPos.computeSeed(levelSeed);
        this.riverNetwork = new RiverNetworkPlanner(this.halfWidth, this.halfHeight).create(levelSeed, islandPos);
        this.riverNoise = NormalNoise.create(new LegacyRandomSource(islandSeed ^ RIVER_NOISE_SALT), -1, 1.0, 0.6);
        this.pathSampler = new RiverPathSampler(this.halfWidth, this.halfHeight, this.riverNoise);
    }

    public RiverSample sample(final double x, final double z, final double signedDistance, final IslandsGeneratorSettings generatorSettings) {
        return this.pathSampler.sample(this.riverNetwork, x, z, signedDistance, generatorSettings);
    }

    public RiverNetwork getRiverNetwork() {
        return this.riverNetwork;
    }

    public record RiverSample(double strength, double bankInfluence, double coreInfluence) {
        public static final RiverSample NONE = new RiverSample(0.0, 0.0, 0.0);

        public double relevance() {
            return Math.max(this.strength, this.bankInfluence);
        }
    }
}
