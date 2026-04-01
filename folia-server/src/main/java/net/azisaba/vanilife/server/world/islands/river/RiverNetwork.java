package net.azisaba.vanilife.server.world.islands.river;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record RiverNetwork(Mouth[] mouths, Trunk trunk, Obstacle[] obstacles) {
    public record Obstacle(double x, double z, double safeRadius) {}

    @NullMarked
    record Mouth(
        double startX, double startZ,
        double[] waypointsX, double[] waypointsZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }

    @NullMarked
    record Trunk(
        double startX, double startZ,
        double[] waypointsX, double[] waypointsZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }
}
