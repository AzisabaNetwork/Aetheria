package net.azisaba.vanilife.server;

import com.mojang.serialization.MapCodec;
import net.azisaba.vanilife.Vanilife;
import net.azisaba.vanilife.server.world.resource.ResourceBiomeSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.BiomeSource;

public final class VanilifeBiomeSources {
    public static MapCodec<? extends BiomeSource> bootstrap(final Registry<MapCodec<? extends BiomeSource>> registry) {
        return Registry.register(registry, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "resource"), ResourceBiomeSource.CODEC);
    }

    private VanilifeBiomeSources() {
    }
}
