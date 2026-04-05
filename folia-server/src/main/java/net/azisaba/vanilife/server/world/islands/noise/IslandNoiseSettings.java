package net.azisaba.vanilife.server.world.islands.noise;

public record IslandNoiseSettings(
    double cornerRadius,
    double cornerRadiusNoiseAmplitude,
    double coastlineNoiseBand,
    double coastlineNoiseAmplitude,
    double inlandRiseExponent,
    double foothillAmplitude,
    double mountainMassNoiseAmplitude,
    double ridgeNoiseAmplitude,
    double cliffBandCenter,
    double cliffBandWidth,
    double cliffStrength,
    double surfaceDetailNoiseAmplitude,
    int offshoreDepthStepDistanceBlocks
) {
    public static IslandNoiseSettings createDefault() {
        return new IslandNoiseSettings(
            16.0,
            7.0,
            14.0,
            3.5,
            1.2,
            0.03,
            0.02,
            0.01,
            0.56,
            0.13,
            0.02,
            0.01,
            6
        );
    }
}
