package net.azisaba.vanilife.server.world.islands.river;

public record RiverNetwork(Mouth[] mouths, Trunk trunk, Obstacle[] obstacles) {
    public record Obstacle(double x, double z, double safeRadius) {}

    record Mouth(
        double startX, double startZ,
        double[] waypointsX, double[] waypointsZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }

    record Trunk(
        double startX, double startZ,
        double[] waypointsX, double[] waypointsZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }
}
