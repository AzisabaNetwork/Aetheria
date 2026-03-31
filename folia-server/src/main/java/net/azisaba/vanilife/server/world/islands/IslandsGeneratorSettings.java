package net.azisaba.vanilife.server.world.islands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IslandsGeneratorSettings(int seaLevel, int seaDepth, int landTopY, int airTopY, int beachWidth) {
    public static final Codec<IslandsGeneratorSettings> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.INT.fieldOf("sea_level").forGetter(IslandsGeneratorSettings::seaLevel),
                Codec.INT.fieldOf("sea_depth").forGetter(IslandsGeneratorSettings::seaDepth),
                Codec.INT.fieldOf("land_top_y").forGetter(IslandsGeneratorSettings::landTopY),
                Codec.INT.fieldOf("air_top_y").forGetter(IslandsGeneratorSettings::airTopY),
                Codec.INT.fieldOf("beach_width").forGetter(IslandsGeneratorSettings::beachWidth)
            )
            .apply(instance, IslandsGeneratorSettings::new)
    );
}
