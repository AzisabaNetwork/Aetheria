package net.azisaba.vanilife.server.world.islands.noise;

import net.azisaba.vanilife.server.world.islands.IslandsGeneratorSettings;
import net.azisaba.vanilife.world.IslandPosition;
import net.azisaba.vanilife.world.IslandsWorld;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import io.papermc.paper.math.BlockPosition;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class IslandNoise {
    private static final double SPAWN_BEACH_HEIGHT_RADIUS = 14.0;
    private static final double SPAWN_BEACH_STABILITY_RADIUS = 22.0;
    private static final double SPAWN_TARGET_SIGNED_DISTANCE = -2.5;

    public static IslandNoise createDefault(final long levelSeed, final IslandPosition islandPos) {
        return new IslandNoise(
            islandPos,
            levelSeed,
            IslandsWorld.ISLAND_SIZE_X_BLOCKS,
            IslandsWorld.ISLAND_SIZE_Z_BLOCKS,
            IslandNoiseSettings.createDefault()
        );
    }

    private final double halfWidth;
    private final double halfHeight;
    private final double cliffDirectionX;
    private final double cliffDirectionZ;
    private final int spawnBlockOffsetX;
    private final int spawnBlockOffsetZ;

    private final IslandNoiseSettings settings;

    private final NormalNoise coastNoise;
    private final NormalNoise cornerNoise;
    private final NormalNoise mountainMassNoise;
    private final NormalNoise ridgeNoise;
    private final NormalNoise cliffNoise;
    private final NormalNoise landDetailNoise;

    public IslandNoise(final IslandPosition position, final long levelSeed, final int width, final int height, final IslandNoiseSettings noiseSettings) {
        final long seed = position.computeSeed(levelSeed);
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;
        final double cliffDirectionAngle = ((seed >>> 8) & 1023L) / 1024.0 * Math.PI * 2.0;
        this.cliffDirectionX = Math.cos(cliffDirectionAngle);
        this.cliffDirectionZ = Math.sin(cliffDirectionAngle);
        final BlockPosition spawnBlock = position.spawnBlock(levelSeed);
        this.spawnBlockOffsetX = spawnBlock.blockX() - position.centerBlockX();
        this.spawnBlockOffsetZ = spawnBlock.blockZ() - position.centerBlockZ();
        this.settings = noiseSettings;
        this.coastNoise = createNoise(seed, -2, 1.0, 0.5, 0.25, 0.125);
        this.cornerNoise = createNoise(seed ^ 0x9E3779B97F4A7C15L, -1, 1.0, 0.55, 0.3);
        this.mountainMassNoise = createNoise(seed ^ 0x6EED0E9DA4D94A4FL, -3, 1.0, 0.55, 0.3);
        this.ridgeNoise = createNoise(seed ^ 0xC7E4D513A5B37611L, -2, 1.0, 0.58, 0.34, 0.2);
        this.cliffNoise = createNoise(seed ^ 0x51F2D4B79AC3E671L, -1, 1.0, 0.6, 0.36);
        this.landDetailNoise = createNoise(seed ^ 0xA5A1CE1B29D83CF1L, 0, 1.0, 0.6, 0.36);
    }

    public double computeSignedDistance(final double x, final double z) {
        final double cornerInfluence = this.cornerInfluence(x, z);
        final double cornerNoiseSample = sample2d(this.cornerNoise, x, z, 26.0);
        final double cornerRadius = this.settings.cornerRadius() + cornerNoiseSample * this.settings.cornerRadiusNoiseAmplitude() * cornerInfluence;

        final double baseSignedDistance = this.sdfRoundedRect(x, z, cornerRadius);

        final double coastInfluence = Mth.clamp(1.0 - Math.abs(baseSignedDistance) / this.settings.coastlineNoiseBand(), 0.0, 1.0);
        final double coastNoiseSample = sample2d(this.coastNoise, x, z, 48.0);
        final double coastlineOffset = coastNoiseSample * this.settings.coastlineNoiseAmplitude() * coastInfluence;

        final double signedDistance = baseSignedDistance + coastlineOffset;
        return Mth.lerp(this.spawnStability(x, z, SPAWN_BEACH_STABILITY_RADIUS), signedDistance, SPAWN_TARGET_SIGNED_DISTANCE);
    }

    public double cornerInfluence(final double x, final double z) {
        return this.cornerMask(x, z);
    }

    public double computeBeachTransitionNoise(final double x, final double z) {
        final double coastSample = sample2d(this.coastNoise, x - 37.0, z + 29.0, 14.0);
        final double detailSample = sample2d(this.landDetailNoise, x + 13.0, z - 17.0, 10.0);
        return Mth.lerp(
            this.spawnStability(x, z, SPAWN_BEACH_STABILITY_RADIUS),
            Mth.clamp((coastSample * 0.65) + (detailSample * 0.35), -1.0, 1.0),
            0.0
        );
    }

    public double computeBeachBlendNoise(final double x, final double z) {
        final double macro = sample2d(this.coastNoise, x + 19.0, z - 23.0, 8.0);
        final double micro = sample2d(this.landDetailNoise, x - 11.0, z + 7.0, 4.5);
        return Mth.lerp(
            this.spawnStability(x, z, SPAWN_BEACH_STABILITY_RADIUS),
            Mth.clamp((macro * 0.45) + (micro * 0.55), -1.0, 1.0),
            0.0
        );
    }

    public int computeBaseHighestY(
            final double x,
            final double z,
            final double signedDistance,
            final IslandsGeneratorSettings generatorSettings
    ) {
        if (signedDistance > 0.0) {
            final int shoreDepthStepBlocks = Math.max(1, this.settings.offshoreDepthStepDistanceBlocks());
            final int maxDepth = Math.max(0, generatorSettings.seaLevel() - (generatorSettings.minY() + 1));
            final int gradualDepth = Math.min(maxDepth, (int) Math.floor(signedDistance / shoreDepthStepBlocks));
            return generatorSettings.seaLevel() - gradualDepth;
        }

        final double shorelineShelf = Math.max(1.0, generatorSettings.beachWidth() - 1.0) + this.cornerInfluence(x, z) * 1.5;
        final double insideDistance = Math.max(0.0, -signedDistance - shorelineShelf);
        final double inlandSpan = Math.max(1.0, Math.min(this.halfWidth, this.halfHeight) - shorelineShelf);
        final double progress = Mth.clamp(insideDistance / inlandSpan, 0.0, 1.0);
        final double shorelineTransition = Mth.clamp((-signedDistance) / (shorelineShelf + 6.0), 0.0, 1.0);
        final double normalizedX = Math.abs(x) / Math.max(1.0, this.halfWidth - shorelineShelf);
        final double normalizedZ = Math.abs(z) / Math.max(1.0, this.halfHeight - shorelineShelf);
        final double superellipseDistance = Math.pow(
            Math.pow(normalizedX, 4.0) + Math.pow(normalizedZ, 4.0),
            0.25
        );
        final double radialCore = 1.0 - Mth.clamp(superellipseDistance, 0.0, 1.0);
        final double shoreLift = Mth.smoothstep(progress);
        final double edgeLift = Mth.smoothstep(shorelineTransition) * 0.1;
        final double mountainProgress = Math.pow(radialCore, this.settings.inlandRiseExponent()) * shoreLift;
        final double domeProgress = Math.sqrt(radialCore) * shoreLift;

        final double massNoise = sample2d(this.mountainMassNoise, x, z, 88.0);
        final double foothillNoise = sample2d(this.mountainMassNoise, x + 57.0, z - 41.0, 88.0);
        final double ridgeNoise = 1.0 - Math.abs(sample2d(this.ridgeNoise, x, z, 42.0));
        final double cliffNoise = sample2d(this.cliffNoise, x, z, 30.0);
        final double detailNoise = sample2d(this.landDetailNoise, x, z, 24.0);
        final double directionX = x / Math.max(1.0, this.halfWidth);
        final double directionZ = z / Math.max(1.0, this.halfHeight);
        final double directionalAlignment = Mth.clamp(
            (directionX * this.cliffDirectionX + directionZ * this.cliffDirectionZ + 1.0) * 0.5,
            0.0,
            1.0
        );
        final double cliffBandDistance = Math.abs(progress - this.settings.cliffBandCenter());
        final double cliffBand = Mth.clamp(1.0 - (cliffBandDistance / this.settings.cliffBandWidth()), 0.0, 1.0);
        final double cliffMask = Mth.smoothstep(cliffBand);
        final double cliffStep = Mth.smoothstep(Mth.clamp((cliffNoise + 1.0) * 0.5, 0.0, 1.0));
        final double cliffLift = cliffMask * cliffStep * directionalAlignment * this.settings.cliffStrength();
        final double peakDetailFade = 1.0 - domeProgress * 0.85;
        final double foothillBand = Mth.clamp(1.0 - (Math.abs(progress - 0.55) / 0.28), 0.0, 1.0);
        final double foothillLift = Mth.smoothstep(foothillBand)
            * Mth.clamp((foothillNoise + 1.0) * 0.5, 0.0, 1.0)
            * this.settings.foothillAmplitude();

        final double noisyProgress = Mth.clamp(
            edgeLift
                + domeProgress
                + foothillLift
                + massNoise * this.settings.mountainMassNoiseAmplitude() * (0.18 + mountainProgress * 0.36)
                + ((ridgeNoise * 2.0) - 1.0) * this.settings.ridgeNoiseAmplitude() * mountainProgress * (1.0 - domeProgress * 0.55)
                + cliffLift
                + detailNoise * this.settings.surfaceDetailNoiseAmplitude() * peakDetailFade * (0.12 + mountainProgress * 0.42),
            0.0,
            1.0
        );

        final int coastY = generatorSettings.seaLevel() + 1;
        final int centerY = Math.max(generatorSettings.landTopY() - 1, coastY + 1);
        final int span = centerY - coastY;
        final int highestY = coastY + (int) Math.round(noisyProgress * span);
        final double spawnBeachHeightStability = this.spawnStability(x, z, SPAWN_BEACH_HEIGHT_RADIUS);
        return (int) Math.round(Mth.lerp(spawnBeachHeightStability, highestY, coastY));
    }

    private double cornerMask(final double x, final double z) {
        final double ax = Math.abs(x);
        final double az = Math.abs(z);

        final double sx = this.halfWidth - this.settings.cornerRadius();
        final double sz = this.halfHeight - this.settings.cornerRadius();

        final double tx = Mth.clamp((ax - sx) / this.settings.cornerRadius(), 0.0, 1.0);
        final double tz = Mth.clamp((az - sz) / this.settings.cornerRadius(), 0.0, 1.0);

        return Mth.smoothstep(tx) * Mth.smoothstep(tz);
    }

    private double sdfRoundedRect(final double x, final double z, final double r) {
        final double qx = Math.abs(x) - (this.halfWidth - r);
        final double qz = Math.abs(z) - (this.halfHeight - r);

        final double ox = Math.max(qx, 0);
        final double oz = Math.max(qz, 0);

        final double outside = Math.sqrt(ox * ox + oz * oz);
        final double inside = Math.min(Math.max(qx, qz), 0);

        return outside + inside - r;
    }

    private double spawnStability(final double x, final double z, final double radius) {
        final double dx = x - this.spawnBlockOffsetX;
        final double dz = z - this.spawnBlockOffsetZ;
        final double distance = Math.sqrt(dx * dx + dz * dz);
        final double progress = 1.0 - Mth.clamp(distance / radius, 0.0, 1.0);
        return Mth.smoothstep(progress);
    }

    private static NormalNoise createNoise(final long seed, final int firstOctave, final double amplitude, final double... otherAmplitudes) {
        final double[] amplitudes = new double[otherAmplitudes.length + 1];
        amplitudes[0] = amplitude;
        System.arraycopy(otherAmplitudes, 0, amplitudes, 1, otherAmplitudes.length);
        return NormalNoise.create(new LegacyRandomSource(seed), firstOctave, amplitudes);
    }

    public double sample2d(final double x, final double z, final double scale) {
        return sample2d(this.landDetailNoise, x, z, scale);
    }

    private static double sample2d(final NormalNoise noise, final double x, final double z, final double scale) {
        return noise.getValue(x / scale, 0.0, z / scale);
    }
}
