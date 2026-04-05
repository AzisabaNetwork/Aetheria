package net.azisaba.vanilife.server.world.islands.river;

import io.papermc.paper.math.BlockPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.azisaba.vanilife.world.IslandPosition;
import net.minecraft.util.Mth;
final class RiverNetworkPlanner {
    private static final double PORTAL_RIVER_SAFE_RADIUS = 42.0;
    private static final double PORTAL_RIVER_DETOUR_DISTANCE = 28.0;
    private static final double SPAWN_RIVER_SAFE_RADIUS = 24.0;
    private static final double SPAWN_RIVER_DETOUR_DISTANCE = 18.0;
    private static final double MIN_MOUTH_INWARD_DOT = 0.72;

    private record Obstacle(double x, double z, double safeRadius, double detourDistance) {}

    private final double halfWidth;
    private final double halfHeight;

    RiverNetworkPlanner(final double halfWidth, final double halfHeight) {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
    }

    RiverNetwork create(final long levelSeed, final IslandPosition islandPos) {
        final long islandSeed = islandPos.computeSeed(levelSeed);
        final Random random = new Random(islandSeed ^ 0x24A31D7BE19C54D3L);
        final BlockPosition portalBlock = islandPos.defaultPortalPosition(levelSeed);
        final BlockPosition spawnBlock = islandPos.defaultSpawnPosition(levelSeed);

        final Obstacle[] obstacles = {
            new Obstacle(portalBlock.blockX() - islandPos.centerBlockX(), portalBlock.blockZ() - islandPos.centerBlockZ(), PORTAL_RIVER_SAFE_RADIUS, PORTAL_RIVER_DETOUR_DISTANCE),
            new Obstacle(spawnBlock.blockX() - islandPos.centerBlockX(), spawnBlock.blockZ() - islandPos.centerBlockZ(), SPAWN_RIVER_SAFE_RADIUS, SPAWN_RIVER_DETOUR_DISTANCE)
        };

        final int mouthCount = 3 + random.nextInt(2);
        final RiverNetwork.Mouth[] mouths = new RiverNetwork.Mouth[mouthCount];
        final double baseAngle = random.nextDouble() * Math.PI * 2.0;

        final double[] confluence = this.pushPointAwayFromObstacles(
            (random.nextDouble() - 0.5) * this.halfWidth * 0.18,
            (random.nextDouble() - 0.5) * this.halfHeight * 0.18,
            obstacles,
            16.0
        );
        final double confluenceX = confluence[0];
        final double confluenceZ = confluence[1];

        final double sideSign = random.nextBoolean() ? 1.0 : -1.0;
        final double[] trunkFinalTarget = this.pushPointAwayFromObstacles(
            (random.nextDouble() - 0.5) * this.halfWidth * 0.08,
            (random.nextDouble() - 0.5) * this.halfHeight * 0.08,
            obstacles,
            16.0
        );
        final List<double[]> trunkWaypoints = this.planPathAroundObstacles(
            confluenceX,
            confluenceZ,
            trunkFinalTarget[0],
            trunkFinalTarget[1],
            sideSign,
            obstacles
        );
        final double lastTrunkX = trunkWaypoints.get(trunkWaypoints.size() - 1)[0];
        final double lastTrunkZ = trunkWaypoints.get(trunkWaypoints.size() - 1)[1];

        final double[] trunkWaypointsX = new double[trunkWaypoints.size()];
        final double[] trunkWaypointsZ = new double[trunkWaypoints.size()];
        for (int j = 0; j < trunkWaypoints.size(); j++) {
            trunkWaypointsX[j] = trunkWaypoints.get(j)[0];
            trunkWaypointsZ[j] = trunkWaypoints.get(j)[1];
        }

        for (int i = 0; i < mouthCount; i++) {
            final double angle = baseAngle + (Math.PI * 2.0 * i) / mouthCount + (random.nextDouble() - 0.5) * 0.45;
            final double edgeX = Math.cos(angle) * (this.halfWidth + 6.0);
            final double edgeZ = Math.sin(angle) * (this.halfHeight + 6.0);

            final double[] mouthFinalTarget = this.pushPointAwayFromObstacles(
                confluenceX + (random.nextDouble() - 0.5) * this.halfWidth * 0.12,
                confluenceZ + (random.nextDouble() - 0.5) * this.halfHeight * 0.12,
                obstacles,
                16.0
            );

            final List<double[]> mouthWaypoints = this.planPathAroundObstacles(
                edgeX,
                edgeZ,
                mouthFinalTarget[0],
                mouthFinalTarget[1],
                -sideSign,
                obstacles
            );

            final double[] waypointsX = new double[mouthWaypoints.size()];
            final double[] waypointsZ = new double[mouthWaypoints.size()];
            for (int j = 0; j < mouthWaypoints.size(); j++) {
                waypointsX[j] = mouthWaypoints.get(j)[0];
                waypointsZ[j] = mouthWaypoints.get(j)[1];
            }

            final double lastX = waypointsX[waypointsX.length - 1];
            final double lastZ = waypointsZ[waypointsZ.length - 1];
            final double[] constrainedLast = this.constrainMouthTarget(edgeX, edgeZ, lastX, lastZ);
            waypointsX[waypointsX.length - 1] = constrainedLast[0];
            waypointsZ[waypointsZ.length - 1] = constrainedLast[1];

            double totalLength = 0;
            double currX = edgeX;
            double currZ = edgeZ;
            for (int j = 0; j < waypointsX.length; j++) {
                totalLength += Math.sqrt(Math.pow(waypointsX[j] - currX, 2) + Math.pow(waypointsZ[j] - currZ, 2));
                currX = waypointsX[j];
                currZ = waypointsZ[j];
            }

            mouths[i] = new RiverNetwork.Mouth(
                edgeX,
                edgeZ,
                waypointsX,
                waypointsZ,
                totalLength + 14.0 + random.nextDouble() * 10.0,
                4.4 + random.nextDouble() * 1.4,
                8.0 + random.nextDouble() * 5.0,
                random.nextDouble() * Math.PI * 2.0
            );
        }

        double trunkTotalLength = 0;
        double currTrunkX = confluenceX;
        double currTrunkZ = confluenceZ;
        for (int j = 0; j < trunkWaypointsX.length; j++) {
            trunkTotalLength += Math.sqrt(Math.pow(trunkWaypointsX[j] - currTrunkX, 2) + Math.pow(trunkWaypointsZ[j] - currTrunkZ, 2));
            currTrunkX = trunkWaypointsX[j];
            currTrunkZ = trunkWaypointsZ[j];
        }

        final RiverNetwork.Trunk trunk = new RiverNetwork.Trunk(
            confluenceX,
            confluenceZ,
            trunkWaypointsX,
            trunkWaypointsZ,
            Math.min(Math.min(this.halfWidth, this.halfHeight) * 0.32, trunkTotalLength + 18.0 + random.nextDouble() * 8.0),
            3.8 + random.nextDouble() * 1.0,
            5.0 + random.nextDouble() * 2.5,
            random.nextDouble() * Math.PI * 2.0
        );

        final RiverNetwork.Obstacle[] riverObstacles = new RiverNetwork.Obstacle[obstacles.length];
        for (int i = 0; i < obstacles.length; i++) {
            riverObstacles[i] = new RiverNetwork.Obstacle(obstacles[i].x, obstacles[i].z, obstacles[i].safeRadius);
        }

        return new RiverNetwork(mouths, trunk, riverObstacles);
    }

    private List<double[]> planPathAroundObstacles(
        final double startX,
        final double startZ,
        final double targetX,
        final double targetZ,
        final double sideSign,
        final Obstacle[] obstacles
    ) {
        final List<double[]> waypoints = new ArrayList<>();
        this.resolvePath(startX, startZ, targetX, targetZ, sideSign, obstacles, waypoints, 0);
        waypoints.add(new double[]{targetX, targetZ});
        return waypoints;
    }

    private void resolvePath(
        final double startX,
        final double startZ,
        final double targetX,
        final double targetZ,
        final double sideSign,
        final Obstacle[] obstacles,
        final List<double[]> waypoints,
        final int depth
    ) {
        if (depth >= 8) {
            return;
        }

        Obstacle firstCollision = null;
        for (final Obstacle obstacle : obstacles) {
            if (this.segmentPassesNearPortal(startX, startZ, targetX, targetZ, obstacle.x, obstacle.z, obstacle.safeRadius)) {
                firstCollision = obstacle;
                break;
            }
        }

        if (firstCollision == null) {
            return;
        }

        final double extraRadius = firstCollision.safeRadius + 14.0 + depth * 4.0;
        final double extraDetour = firstCollision.detourDistance + 10.0 + depth * 6.0;

        final double[] detour = this.detourAroundPortal(targetX, targetZ, sideSign, firstCollision.x, firstCollision.z, extraRadius, extraDetour);
        final double detourX = detour[0];
        final double detourZ = detour[1];

        this.resolvePath(startX, startZ, detourX, detourZ, sideSign, obstacles, waypoints, depth + 1);
        waypoints.add(new double[]{detourX, detourZ});
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

    private double[] pushPointAwayFromObstacles(
        double x,
        double z,
        final Obstacle[] obstacles,
        final double extraRadius
    ) {
        for (final Obstacle obstacle : obstacles) {
            final double dx = x - obstacle.x;
            final double dz = z - obstacle.z;
            final double distance = Math.sqrt(dx * dx + dz * dz);
            final double safeRadius = obstacle.safeRadius + extraRadius;
            if (distance < safeRadius) {
                final double nx;
                final double nz;
                if (distance < 1.0E-4) {
                    nx = obstacle.x >= 0.0 ? 1.0 : -1.0;
                    nz = 0.0;
                } else {
                    nx = dx / distance;
                    nz = dz / distance;
                }
                x = obstacle.x + nx * safeRadius;
                z = obstacle.z + nz * safeRadius;
            }
        }
        return new double[]{x, z};
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
