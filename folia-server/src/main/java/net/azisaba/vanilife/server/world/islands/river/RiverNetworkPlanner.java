package net.azisaba.vanilife.server.world.islands.river;

import java.util.Random;
import net.azisaba.vanilife.server.world.islands.IslandPortalLayout;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class RiverNetworkPlanner {
    private static final double PORTAL_RIVER_SAFE_RADIUS = 42.0;
    private static final double PORTAL_RIVER_DETOUR_DISTANCE = 28.0;
    private static final double MIN_MOUTH_INWARD_DOT = 0.72;

    private final double halfWidth;
    private final double halfHeight;

    RiverNetworkPlanner(final double halfWidth, final double halfHeight) {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
    }

    RiverNetwork create(final long seed) {
        final Random random = new Random(seed ^ 0x24A31D7BE19C54D3L);
        final IslandPortalLayout portalLayout = IslandPortalLayout.create(seed);
        final double portalX = portalLayout.blockOffsetX();
        final double portalZ = portalLayout.blockOffsetZ();
        final int mouthCount = 3 + random.nextInt(2);
        final RiverNetwork.Mouth[] mouths = new RiverNetwork.Mouth[mouthCount];
        final double baseAngle = random.nextDouble() * Math.PI * 2.0;

        final double[] confluence = this.pushPointAwayFromPortal(
            (random.nextDouble() - 0.5) * this.halfWidth * 0.18,
            (random.nextDouble() - 0.5) * this.halfHeight * 0.18,
            portalX,
            portalZ,
            PORTAL_RIVER_SAFE_RADIUS + 8.0
        );
        final double confluenceX = confluence[0];
        final double confluenceZ = confluence[1];

        final double[] trunkTarget = this.resolveTargetAroundPortal(
            confluenceX,
            confluenceZ,
            (random.nextDouble() - 0.5) * this.halfWidth * 0.08,
            (random.nextDouble() - 0.5) * this.halfHeight * 0.08,
            random.nextBoolean() ? 1.0 : -1.0,
            portalX,
            portalZ,
            PORTAL_RIVER_SAFE_RADIUS,
            PORTAL_RIVER_DETOUR_DISTANCE
        );
        final double trunkVectorX = trunkTarget[0] - confluenceX;
        final double trunkVectorZ = trunkTarget[1] - confluenceZ;
        final double trunkVectorLength = Math.max(1.0, Math.sqrt(trunkVectorX * trunkVectorX + trunkVectorZ * trunkVectorZ));
        final double trunkDirectionX = trunkVectorX / trunkVectorLength;
        final double trunkDirectionZ = trunkVectorZ / trunkVectorLength;

        for (int i = 0; i < mouthCount; i++) {
            final double angle = baseAngle + (Math.PI * 2.0 * i) / mouthCount + (random.nextDouble() - 0.5) * 0.45;
            final double edgeX = Math.cos(angle) * (this.halfWidth + 6.0);
            final double edgeZ = Math.sin(angle) * (this.halfHeight + 6.0);
            final double[] divertedTarget = this.resolveTargetAroundPortal(
                edgeX,
                edgeZ,
                confluenceX + (random.nextDouble() - 0.5) * this.halfWidth * 0.12,
                confluenceZ + (random.nextDouble() - 0.5) * this.halfHeight * 0.12,
                random.nextBoolean() ? 1.0 : -1.0,
                portalX,
                portalZ,
                PORTAL_RIVER_SAFE_RADIUS,
                PORTAL_RIVER_DETOUR_DISTANCE + 8.0
            );
            final double[] constrainedTarget = this.constrainMouthTarget(edgeX, edgeZ, divertedTarget[0], divertedTarget[1]);
            final double targetX = constrainedTarget[0];
            final double targetZ = constrainedTarget[1];
            final double vectorX = targetX - edgeX;
            final double vectorZ = targetZ - edgeZ;
            final double vectorLength = Math.max(1.0, Math.sqrt(vectorX * vectorX + vectorZ * vectorZ));
            mouths[i] = new RiverNetwork.Mouth(
                edgeX,
                edgeZ,
                vectorX / vectorLength,
                vectorZ / vectorLength,
                vectorLength + 14.0 + random.nextDouble() * 10.0,
                4.4 + random.nextDouble() * 1.4,
                8.0 + random.nextDouble() * 5.0,
                random.nextDouble() * Math.PI * 2.0
            );
        }

        final RiverNetwork.Trunk trunk = new RiverNetwork.Trunk(
            confluenceX,
            confluenceZ,
            trunkDirectionX,
            trunkDirectionZ,
            Math.min(Math.min(this.halfWidth, this.halfHeight) * 0.32, trunkVectorLength + 18.0 + random.nextDouble() * 8.0),
            3.8 + random.nextDouble() * 1.0,
            5.0 + random.nextDouble() * 2.5,
            random.nextDouble() * Math.PI * 2.0
        );
        return new RiverNetwork(mouths, trunk);
    }

    private double[] resolveTargetAroundPortal(
        final double startX,
        final double startZ,
        final double targetX,
        final double targetZ,
        final double sideSign,
        final double portalX,
        final double portalZ,
        final double safeRadius,
        final double detourDistance
    ) {
        double[] target = this.pushPointAwayFromPortal(targetX, targetZ, portalX, portalZ, safeRadius);
        if (!this.segmentPassesNearPortal(startX, startZ, target[0], target[1], portalX, portalZ, safeRadius)) {
            return target;
        }

        for (int i = 0; i < 4; i++) {
            final double extraRadius = safeRadius + 10.0 + i * 8.0;
            final double extraDetour = detourDistance + i * 10.0;
            target = this.detourAroundPortal(target[0], target[1], sideSign, portalX, portalZ, extraRadius, extraDetour);
            if (!this.segmentPassesNearPortal(startX, startZ, target[0], target[1], portalX, portalZ, safeRadius)) {
                return target;
            }
        }

        return this.detourAroundPortal(target[0], target[1], sideSign, portalX, portalZ, safeRadius + 42.0, detourDistance + 36.0);
    }

    private double[] constrainMouthTarget(final double edgeX, final double edgeZ, final double targetX, final double targetZ) {
        final double inwardNormalLength = Math.max(1.0E-4, Math.sqrt(edgeX * edgeX + edgeZ * edgeZ));
        final double inwardX = -edgeX / inwardNormalLength;
        final double inwardZ = -edgeZ / inwardNormalLength;
        final double vectorX = targetX - edgeX;
        final double vectorZ = targetZ - edgeZ;
        final double vectorLength = Math.max(1.0E-4, Math.sqrt(vectorX * vectorX + vectorZ * vectorZ));
        double directionX = vectorX / vectorLength;
        double directionZ = vectorZ / vectorLength;
        final double inwardDot = directionX * inwardX + directionZ * inwardZ;
        if (inwardDot >= MIN_MOUTH_INWARD_DOT) {
            return new double[]{targetX, targetZ};
        }

        final double tangentX = -inwardZ;
        final double tangentZ = inwardX;
        double tangentDot = directionX * tangentX + directionZ * tangentZ;
        tangentDot = Mth.clamp(
            tangentDot,
            -Math.sqrt(1.0 - MIN_MOUTH_INWARD_DOT * MIN_MOUTH_INWARD_DOT),
            Math.sqrt(1.0 - MIN_MOUTH_INWARD_DOT * MIN_MOUTH_INWARD_DOT)
        );
        directionX = inwardX * MIN_MOUTH_INWARD_DOT + tangentX * tangentDot;
        directionZ = inwardZ * MIN_MOUTH_INWARD_DOT + tangentZ * tangentDot;
        final double adjustedLength = Math.max(vectorLength, Math.min(this.halfWidth, this.halfHeight) * 0.18);
        return new double[]{edgeX + directionX * adjustedLength, edgeZ + directionZ * adjustedLength};
    }

    private double[] pushPointAwayFromPortal(
        final double x,
        final double z,
        final double portalX,
        final double portalZ,
        final double safeRadius
    ) {
        final double dx = x - portalX;
        final double dz = z - portalZ;
        final double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance >= safeRadius) {
            return new double[]{x, z};
        }

        final double nx;
        final double nz;
        if (distance < 1.0E-4) {
            nx = portalX >= 0.0 ? 1.0 : -1.0;
            nz = 0.0;
        } else {
            nx = dx / distance;
            nz = dz / distance;
        }
        return new double[]{portalX + nx * safeRadius, portalZ + nz * safeRadius};
    }

    private double[] detourAroundPortal(
        final double targetX,
        final double targetZ,
        final double sideSign,
        final double portalX,
        final double portalZ,
        final double radialDistance,
        final double tangentialDistance
    ) {
        final double dirX = targetX - portalX;
        final double dirZ = targetZ - portalZ;
        final double dirLength = Math.max(1.0E-4, Math.sqrt(dirX * dirX + dirZ * dirZ));
        final double radialX = dirX / dirLength;
        final double radialZ = dirZ / dirLength;
        final double tangentX = -radialZ * sideSign;
        final double tangentZ = radialX * sideSign;
        return new double[]{
            portalX + radialX * radialDistance + tangentX * tangentialDistance,
            portalZ + radialZ * radialDistance + tangentZ * tangentialDistance
        };
    }

    private boolean segmentPassesNearPortal(
        final double startX,
        final double startZ,
        final double endX,
        final double endZ,
        final double portalX,
        final double portalZ,
        final double safeRadius
    ) {
        final double abX = endX - startX;
        final double abZ = endZ - startZ;
        final double abLengthSquared = abX * abX + abZ * abZ;
        if (abLengthSquared < 1.0E-4) {
            final double dx = startX - portalX;
            final double dz = startZ - portalZ;
            return dx * dx + dz * dz < safeRadius * safeRadius;
        }

        final double apX = portalX - startX;
        final double apZ = portalZ - startZ;
        final double t = Mth.clamp((apX * abX + apZ * abZ) / abLengthSquared, 0.0, 1.0);
        final double closestX = startX + abX * t;
        final double closestZ = startZ + abZ * t;
        final double dx = closestX - portalX;
        final double dz = closestZ - portalZ;
        return dx * dx + dz * dz < safeRadius * safeRadius;
    }
}
