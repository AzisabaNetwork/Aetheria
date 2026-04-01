package net.azisaba.vanilife.server.world.islands.river;

import net.azisaba.vanilife.server.world.islands.IslandsGeneratorSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class RiverPathSampler {
    private static final double RIVER_BANK_WIDTH_BLOCKS = 6.0;

    private final double halfWidth;
    private final double halfHeight;
    private final NormalNoise riverNoise;

    RiverPathSampler(final double halfWidth, final double halfHeight, final NormalNoise riverNoise) {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.riverNoise = riverNoise;
    }

    IslandRiverLayout.RiverSample sample(
        final RiverNetwork riverNetwork,
        final double x,
        final double z,
        final double signedDistance,
        final IslandsGeneratorSettings generatorSettings
    ) {
        if (signedDistance > 28.0) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double shorelineShelf = Math.max(1.0, generatorSettings.beachWidth() - 1.0);
        final double insideDistance = Math.max(0.0, -signedDistance - shorelineShelf);
        final double inlandSpan = Math.max(1.0, Math.min(this.halfWidth, this.halfHeight) - shorelineShelf);
        final double progress = Mth.clamp(insideDistance / inlandSpan, 0.0, 1.0);
        final double inlandChannelBand = Mth.clamp(1.0 - Math.max(0.0, progress - 0.82) / 0.18, 0.0, 1.0);
        final double offshoreChannelBand = Mth.clamp(1.0 - Math.max(0.0, signedDistance) / 28.0, 0.0, 1.0);
        final double channelBand = Math.max(inlandChannelBand, offshoreChannelBand);
        if (channelBand <= 0.0) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        IslandRiverLayout.RiverSample best = this.computeRiverTrunkSample(riverNetwork, riverNetwork.trunk(), x, z, progress, channelBand);
        for (final RiverNetwork.Mouth mouth : riverNetwork.mouths()) {
            final IslandRiverLayout.RiverSample sample = this.computeRiverMouthSample(riverNetwork, mouth, x, z, channelBand);
            if (sample.relevance() > best.relevance()) {
                best = sample;
            }
        }
        return best;
    }

    private IslandRiverLayout.RiverSample computeRiverMouthSample(
        final RiverNetwork riverNetwork,
        final RiverNetwork.Mouth mouth,
        final double x,
        final double z,
        final double channelBand
    ) {
        final double localX = x - mouth.startX();
        final double localZ = z - mouth.startZ();

        double minDistanceSq = Double.MAX_VALUE;
        double closestAlong = 0;
        double closestAcross = 0;
        double totalAlong = 0;
        double prevX = mouth.startX();
        double prevZ = mouth.startZ();

        for (int i = 0; i < mouth.waypointsX().length; i++) {
            final double currX = mouth.waypointsX()[i];
            final double currZ = mouth.waypointsZ()[i];

            final double segmentVecX = currX - prevX;
            final double segmentVecZ = currZ - prevZ;
            final double segmentLenSq = segmentVecX * segmentVecX + segmentVecZ * segmentVecZ;
            final double segmentLen = Math.sqrt(segmentLenSq);

            final double pointVecX = x - prevX;
            final double pointVecZ = z - prevZ;

            double t = segmentLenSq < 1e-4 ? 0 : (pointVecX * segmentVecX + pointVecZ * segmentVecZ) / segmentLenSq;
            t = Mth.clamp(t, 0.0, 1.0);

            final double closestX = prevX + t * segmentVecX;
            final double closestZ = prevZ + t * segmentVecZ;
            final double distSq = Math.pow(x - closestX, 2) + Math.pow(z - closestZ, 2);

            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                closestAlong = totalAlong + t * segmentLen;
                closestAcross = (z - closestZ) * (segmentVecX / segmentLen) - (x - closestX) * (segmentVecZ / segmentLen);
            }

            totalAlong += segmentLen;
            prevX = currX;
            prevZ = currZ;
        }

        final double along = closestAlong;
        if (along < -28.0 || along > mouth.length()) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double alongProgress = Mth.clamp(along / mouth.length(), 0.0, 1.0);
        final double seaConnection = Mth.smoothstep(Mth.clamp((along + 28.0) / 28.0, 0.0, 1.0));
        final double inlandFade = Mth.smoothstep(Mth.clamp(1.0 - Math.max(0.0, alongProgress - 0.68) / 0.32, 0.0, 1.0));
        final double alongMask = seaConnection * inlandFade;
        if (alongMask <= 0.0) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double across = closestAcross;
        final double endpointTaper = computeEndpointTaper(alongProgress, 0.18);
        final double centerlineOffset = Math.sin(along / 18.0 + mouth.phase()) * mouth.meanderAmplitude()
                + sample2d(this.riverNoise, x + mouth.phase() * 7.0, z - mouth.phase() * 5.0, 30.0) * 3.0;
        final double centerline = centerlineOffset * endpointTaper * this.computeObstacleMeanderReduction(riverNetwork, x, z);
        final double mouthFlare = 1.25 + Mth.smoothstep(Mth.clamp(1.0 - Math.max(-16.0, along + 16.0) / 30.0, 0.0, 1.0)) * 2.2;
        final double inlandWidthTaper = 0.35 + inlandFade * 0.65;
        final double confluenceApproach = Mth.smoothstep(Mth.clamp((alongProgress - 0.55) / 0.45, 0.0, 1.0));
        final double confluenceWidthTaper = 1.0 + confluenceApproach * 0.45;
        final double riverWidth = mouth.width()
            * (0.9 + channelBand * 0.45)
            * mouthFlare
            * inlandWidthTaper
            * confluenceWidthTaper;
        return this.createRiverSample(Math.abs(across - centerline), riverWidth, alongMask * Mth.smoothstep(channelBand));
    }

    private IslandRiverLayout.RiverSample computeRiverTrunkSample(
        final RiverNetwork riverNetwork,
        final RiverNetwork.Trunk trunk,
        final double x,
        final double z,
        final double progress,
        final double channelBand
    ) {
        final double localX = x - trunk.startX();
        final double localZ = z - trunk.startZ();

        double minDistanceSq = Double.MAX_VALUE;
        double closestAlong = 0;
        double closestAcross = 0;
        double totalAlong = 0;
        double prevX = trunk.startX();
        double prevZ = trunk.startZ();

        for (int i = 0; i < trunk.waypointsX().length; i++) {
            final double currX = trunk.waypointsX()[i];
            final double currZ = trunk.waypointsZ()[i];

            final double segmentVecX = currX - prevX;
            final double segmentVecZ = currZ - prevZ;
            final double segmentLenSq = segmentVecX * segmentVecX + segmentVecZ * segmentVecZ;
            final double segmentLen = Math.sqrt(segmentLenSq);

            final double pointVecX = x - prevX;
            final double pointVecZ = z - prevZ;

            double t = segmentLenSq < 1e-4 ? 0 : (pointVecX * segmentVecX + pointVecZ * segmentVecZ) / segmentLenSq;
            t = Mth.clamp(t, 0.0, 1.0);

            final double closestX = prevX + t * segmentVecX;
            final double closestZ = prevZ + t * segmentVecZ;
            final double distSq = Math.pow(x - closestX, 2) + Math.pow(z - closestZ, 2);

            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                closestAlong = totalAlong + t * segmentLen;
                closestAcross = (z - closestZ) * (segmentVecX / segmentLen) - (x - closestX) * (segmentVecZ / segmentLen);
            }

            totalAlong += segmentLen;
            prevX = currX;
            prevZ = currZ;
        }

        final double along = closestAlong;
        if (along < -6.0 || along > trunk.length()) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double alongProgress = Mth.clamp(along / trunk.length(), 0.0, 1.0);
        final double startFade = Mth.smoothstep(Mth.clamp((along + 6.0) / 6.0, 0.0, 1.0));
        final double endFade = Mth.smoothstep(Mth.clamp(1.0 - Math.max(0.0, alongProgress - 0.72) / 0.28, 0.0, 1.0));
        final double alongMask = startFade * endFade;
        if (alongMask <= 0.0) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double across = closestAcross;
        final double endpointTaper = computeEndpointTaper(alongProgress, 0.22);
        final double centerlineOffset = Math.sin(along / 16.0 + trunk.phase()) * trunk.meanderAmplitude()
                + sample2d(this.riverNoise, x - trunk.phase() * 4.0, z + trunk.phase() * 6.0, 30.0) * 2.5;
        final double centerline = centerlineOffset * endpointTaper * this.computeObstacleMeanderReduction(riverNetwork, x, z);
        final double confluenceFlare = 1.25 + Mth.smoothstep(Mth.clamp(1.0 - Math.max(0.0, along) / 20.0, 0.0, 1.0)) * 0.55;
        final double widthTaper = 0.8 + endFade * 0.2;
        final double riverWidth = trunk.width() * (0.95 + channelBand * 0.3) * widthTaper * confluenceFlare;
        final double progressMask = Mth.smoothstep(Mth.clamp((progress - 0.16) / 0.22, 0.0, 1.0));
        return this.createRiverSample(Math.abs(across - centerline), riverWidth, alongMask * progressMask);
    }

    private IslandRiverLayout.RiverSample createRiverSample(final double distanceToRiver, final double riverWidth, final double mask) {
        if (mask <= 0.0) {
            return IslandRiverLayout.RiverSample.NONE;
        }

        final double corridor = Mth.clamp(1.0 - distanceToRiver / riverWidth, 0.0, 1.0);
        final double strength = Mth.smoothstep(corridor) * mask;
        final double bankInfluence = Mth.clamp(
            1.0 - Math.max(0.0, distanceToRiver - riverWidth) / RIVER_BANK_WIDTH_BLOCKS,
            0.0,
            1.0
        ) * mask;
        final double coreInfluence = corridor * mask;
        return new IslandRiverLayout.RiverSample(strength, bankInfluence, coreInfluence);
    }

    private static double computeEndpointTaper(final double alongProgress, final double taperPortion) {
        final double startTaper = Mth.smoothstep(Mth.clamp(alongProgress / taperPortion, 0.0, 1.0));
        final double endTaper = Mth.smoothstep(Mth.clamp((1.0 - alongProgress) / taperPortion, 0.0, 1.0));
        return startTaper * endTaper;
    }

    private double computeObstacleMeanderReduction(final RiverNetwork riverNetwork, final double x, final double z) {
        double reduction = 1.0;
        for (final RiverNetwork.Obstacle obstacle : riverNetwork.obstacles()) {
            final double dx = x - obstacle.x();
            final double dz = z - obstacle.z();
            final double distanceSq = dx * dx + dz * dz;
            final double influenceRadius = obstacle.safeRadius() + 16.0;
            if (distanceSq < influenceRadius * influenceRadius) {
                final double distance = Math.sqrt(distanceSq);
                reduction = Math.min(reduction, Mth.smoothstep(Mth.clamp((distance - obstacle.safeRadius() + 4.0) / 20.0, 0.0, 1.0)));
            }
        }
        return reduction;
    }

    private static double sample2d(final NormalNoise noise, final double x, final double z, final double scale) {
        return noise.getValue(x / scale, 0.0, z / scale);
    }
}
