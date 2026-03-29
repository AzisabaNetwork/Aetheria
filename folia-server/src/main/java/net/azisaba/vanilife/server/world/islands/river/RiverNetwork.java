package net.azisaba.vanilife.server.world.islands.river;

import org.jspecify.annotations.NullMarked;

@NullMarked
record RiverNetwork(RiverNetwork.Mouth[] mouths, RiverNetwork.Trunk trunk) {
    @NullMarked
    record Mouth(
        double x, double z,
        double directionX, double directionZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }

    @NullMarked
    record Trunk(
        double x, double z,
        double directionX, double directionZ,
        double length, double width,
        double meanderAmplitude,
        double phase
    ) {
    }
}
