package net.azisaba.vanilife.server.world.islands;

import io.papermc.paper.math.BlockPosition;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.azisaba.vanilife.server.world.islands.noise.IslandNoise;
import net.azisaba.vanilife.server.world.islands.river.IslandRiverLayout;
import net.azisaba.vanilife.server.world.islands.river.IslandRiverLayout.RiverSample;
import net.azisaba.vanilife.server.world.islands.river.RiverNetwork;
import net.azisaba.vanilife.world.IslandPosition;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

final class IslandTerrainSampler {
    static final double RIVER_BANK_THRESHOLD = 0.015;

    private static final long DEEP_OCEAN_NOISE_SALT = 0xD1342543DE82EF95L;
    private static final int DEEP_OCEAN_VARIATION = 3;
    private static final int RIVER_BANK_SLOPE_BLOCKS = 6;

    private final IslandsGeneratorSettings settings;
    private final Map<Long, IslandNoise> islandNoiseCache = new ConcurrentHashMap<>();
    private final Map<Long, IslandRiverLayout> islandRiverLayoutCache = new ConcurrentHashMap<>();
    private final Map<Long, NormalNoise> deepOceanNoiseCache = new ConcurrentHashMap<>();

    IslandTerrainSampler(final IslandsGeneratorSettings settings) {
        this.settings = settings;
    }

    TerrainSample sample(final long levelSeed, final int blockX, final int blockZ) {
        final IslandPosition islandPos = IslandPosition.fromBlockXZ(blockX, blockZ);
        final long islandSeed = islandPos.computeSeed(levelSeed);
        final IslandNoise islandNoise = this.getIslandNoise(levelSeed, islandPos);
        final IslandRiverLayout riverLayout = this.getIslandRiverLayout(levelSeed, islandPos);

        final double localX = blockX - islandPos.centerBlockX();
        final double localZ = blockZ - islandPos.centerBlockZ();
        final double signedDistance = islandNoise.computeSignedDistance(localX, localZ);
        final double cornerInfluence = islandNoise.cornerInfluence(localX, localZ);
        final double beachTransitionNoise = islandNoise.computeBeachTransitionNoise(localX, localZ);
        final double beachBlendNoise = islandNoise.computeBeachBlendNoise(localX, localZ);
        final int baseHighestY = islandNoise.computeBaseHighestY(localX, localZ, signedDistance, this.settings);
        int resolvedHighestY = this.resolveHighestY(levelSeed, blockX, blockZ, baseHighestY);
        final int riverWaterY = this.settings.seaLevel();
        final RiverSample baseRiverSample = riverLayout.sample(localX, localZ, signedDistance, this.settings);

        final RiverNetwork riverNetwork = riverLayout.getRiverNetwork();
        double obstacleReduction = 1.0;
        for (final RiverNetwork.Obstacle obstacle : riverNetwork.obstacles()) {
            final double dx = localX - obstacle.x();
            final double dz = localZ - obstacle.z();
            final double distanceSq = dx * dx + dz * dz;
            final double safeRadius = obstacle.safeRadius();
            if (distanceSq < safeRadius * safeRadius) {
                final double distance = Math.sqrt(distanceSq);
                obstacleReduction = Math.min(obstacleReduction, Mth.smoothstep(Mth.clamp((distance - (safeRadius - 16.0)) / 20.0, 0.0, 1.0)));
            }
        }

        final RiverSample riverSample = obstacleReduction < 1.0
            ? new RiverSample(baseRiverSample.strength() * obstacleReduction, baseRiverSample.bankInfluence() * obstacleReduction, baseRiverSample.coreInfluence() * obstacleReduction)
            : baseRiverSample;

        final BlockPosition spawnBlock = islandPos.defaultSpawnPosition(levelSeed);
        final double spawnPointDistance = Math.sqrt(Math.pow(blockX - spawnBlock.blockX(), 2) + Math.pow(blockZ - spawnBlock.blockZ(), 2));

        final double wastelandThreshold = 12.0 + (beachTransitionNoise + beachBlendNoise) * 2.0;
        if (spawnPointDistance < wastelandThreshold) {
            final double heightNoise = islandNoise.sample2d(blockX, blockZ, 4.0);
            if (heightNoise > 0.4) {
                resolvedHighestY++;
            } else if (heightNoise < -0.4) {
                resolvedHighestY--;
            }
        }

        if (riverSample.relevance() <= 0.0) {
            return this.createTerrainSampleWithoutRiver(
                signedDistance,
                cornerInfluence,
                beachTransitionNoise,
                beachBlendNoise,
                resolvedHighestY,
                riverWaterY,
                spawnPointDistance
            );
        }

        return this.createTerrainSampleWithRiver(
            signedDistance,
            cornerInfluence,
            beachTransitionNoise,
            beachBlendNoise,
            resolvedHighestY,
            riverSample,
            riverWaterY,
            spawnPointDistance
        );
    }

    private TerrainSample createTerrainSampleWithoutRiver(
        final double signedDistance,
        final double cornerInfluence,
        final double beachTransitionNoise,
        final double beachBlendNoise,
        final int resolvedHighestY,
        final int riverWaterY,
        final double spawnPointDistance
    ) {
        return new TerrainSample(
            signedDistance,
            cornerInfluence,
            beachTransitionNoise,
            beachBlendNoise,
            resolvedHighestY,
            0.0,
            0.0,
            0.0,
            riverWaterY,
            spawnPointDistance
        );
    }

    private TerrainSample createTerrainSampleWithRiver(
        final double signedDistance,
        final double cornerInfluence,
        final double beachTransitionNoise,
        final double beachBlendNoise,
        final int resolvedHighestY,
        final RiverSample riverSample,
        final int riverWaterY,
        final double spawnPointDistance
    ) {
        final int highestY = this.computeRiverAdjustedHighestY(signedDistance, resolvedHighestY, riverSample, riverWaterY);
        return new TerrainSample(
            signedDistance,
            cornerInfluence,
            beachTransitionNoise,
            beachBlendNoise,
            highestY,
            riverSample.strength(),
            riverSample.bankInfluence(),
            riverSample.coreInfluence(),
            riverWaterY,
            spawnPointDistance
        );
    }

    private int computeRiverAdjustedHighestY(
        final double signedDistance,
        final int resolvedHighestY,
        final RiverSample riverSample,
        final int riverWaterY
    ) {
        int highestY = resolvedHighestY;
        if (this.isInland(signedDistance)) {
            highestY = this.applyInlandRiverBankCorrection(highestY, riverWaterY, riverSample.bankInfluence());
        } else if (this.shouldForceOpenRiverMouth(signedDistance, riverSample.strength())) {
            highestY = Math.min(highestY, riverWaterY - 1);
        }

        return this.applyRiverCoreCorrection(highestY, riverWaterY, riverSample.coreInfluence());
    }

    private int applyInlandRiverBankCorrection(
        final int resolvedHighestY,
        final int riverWaterY,
        final double riverBankInfluence
    ) {
        if (riverBankInfluence <= RIVER_BANK_THRESHOLD || resolvedHighestY <= riverWaterY) {
            return resolvedHighestY;
        }

        final double normalizedInfluence = Mth.clamp(
            (riverBankInfluence - RIVER_BANK_THRESHOLD) / (1.0 - RIVER_BANK_THRESHOLD),
            0.0,
            1.0
        );
        final int bankHeight = riverWaterY + Math.round((1.0F - (float) normalizedInfluence) * RIVER_BANK_SLOPE_BLOCKS);
        return Math.min(resolvedHighestY, Math.max(riverWaterY, bankHeight));
    }

    private int applyRiverCoreCorrection(final int currentHighestY, final int riverWaterY, final double riverCoreInfluence) {
        if (riverCoreInfluence <= 0.0) {
            return currentHighestY;
        }

        final int maxDepth = 2;
        final int coreDepth = Math.max(1, Math.round((float) riverCoreInfluence * maxDepth));
        final int targetY = riverWaterY - coreDepth;
        return Math.min(currentHighestY, targetY);
    }

    private int resolveHighestY(final long levelSeed, final int blockX, final int blockZ, final int baseHighestY) {
        final int deepBaseFloorY = this.settings.minY() + 1;
        if (baseHighestY > deepBaseFloorY) {
            return baseHighestY;
        }

        final int bump = (int) Math.round(sample2d(this.getDeepOceanNoise(levelSeed), blockX, blockZ, 16.0) * DEEP_OCEAN_VARIATION);
        return Math.max(deepBaseFloorY, deepBaseFloorY + bump);
    }

    private boolean isInland(final double signedDistance) {
        return signedDistance < 0.0;
    }

    private boolean shouldForceOpenRiverMouth(final double signedDistance, final double riverStrength) {
        return riverStrength > 0.1 && signedDistance >= -this.settings.beachWidth() - 4.0;
    }

    private IslandNoise getIslandNoise(final long levelSeed, final IslandPosition islandPos) {
        return this.islandNoiseCache.computeIfAbsent(islandPos.computeSeed(levelSeed), seed -> IslandNoise.createDefault(levelSeed, islandPos));
    }

    private IslandRiverLayout getIslandRiverLayout(final long levelSeed, final IslandPosition islandPos) {
        return this.islandRiverLayoutCache.computeIfAbsent(islandPos.computeSeed(levelSeed), k -> IslandRiverLayout.createDefault(levelSeed, islandPos));
    }

    private NormalNoise getDeepOceanNoise(final long levelSeed) {
        return this.deepOceanNoiseCache.computeIfAbsent(
            levelSeed,
            seed -> NormalNoise.create(new LegacyRandomSource(seed ^ DEEP_OCEAN_NOISE_SALT), -2, 1.0, 0.55, 0.3)
        );
    }

    private static double sample2d(final NormalNoise noise, final double x, final double z, final double scale) {
        return noise.getValue(x / scale, 0.0, z / scale);
    }

    record TerrainSample(
        double signedDistance,
        double cornerInfluence,
        double beachTransitionNoise,
        double beachBlendNoise,
        int highestY,
        double riverStrength,
        double riverBankInfluence,
        double riverCoreInfluence,
        int riverWaterY,
        double spawnPointDistance
    ) {
    }
}
