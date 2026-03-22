package net.azisaba.vanilife.server;

import com.mojang.serialization.MapCodec;
import net.azisaba.vanilife.Vanilife;
import net.azisaba.vanilife.server.world.islands.IslandsChunkGenerator;
import net.azisaba.vanilife.server.world.resource.ResourceChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class VanilifeChunkGenerators {
    public static MapCodec<? extends ChunkGenerator> bootstrap(final Registry<MapCodec<? extends ChunkGenerator>> registry) {
        Registry.register(registry, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "resource"), ResourceChunkGenerator.CODEC);
        return Registry.register(registry, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "islands"), IslandsChunkGenerator.CODEC);
    }

    private VanilifeChunkGenerators() {
    }
}
