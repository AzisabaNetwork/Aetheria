package net.azisaba.vanilife.server.world.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.azisaba.vanilife.server.world.height.HeightContext;
import org.jspecify.annotations.Nullable;

public record ResourceLayout(
    int minY,
    List<ResourceLayer.Type> layersTypes
) implements Iterable<ResourceLayer.Type> {
    public static final Codec<ResourceLayout> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.INT.fieldOf("min_y").forGetter(ResourceLayout::minY),
                ResourceLayer.Type.CODEC.listOf().fieldOf("layers").forGetter(ResourceLayout::layersTypes)
            )
            .apply(instance, ResourceLayout::new)
    );

    public int height() {
        return this.layersTypes.stream()
            .mapToInt(ResourceLayer.Type::height)
            .sum();
    }

    public ResourceLayer.Type bottomLayer() {
        return this.layersTypes.getFirst();
    }

    public ResourceLayer.Type topLayer() {
        return this.layersTypes.getLast();
    }

    public int getMinYOf(final ResourceLayer.Type type) {
        final int index = this.layersTypes.indexOf(type);
        if (index == -1) {
            throw new IllegalStateException("Layer type not found: " + type);
        }
        return this.minY + this.layersTypes.subList(0, index)
            .stream()
            .mapToInt(ResourceLayer.Type::height)
            .sum();
    }

    public int getMaxYOf(final ResourceLayer.Type type) {
        return this.getMinYOf(type) + type.height() - 1;
    }

    public ResourceLayer.@Nullable Type getLayerTypeAt(final int y) {
        return this.layersTypes.stream()
            .filter(t -> {
                final int minY = this.getMinYOf(t);
                final int maxY = this.getMaxYOf(t);
                return y >= minY && y <= maxY;
            })
            .findFirst()
            .orElse(null);
    }

    public int toBlockY(final ResourceLayer.Type type, final int layerY) {
        return this.getMinYOf(type) + layerY - type.minY();
    }

    public int toLayerY(final ResourceLayer.Type type, final int blockY) {
        return blockY - this.getMinYOf(type) + type.minY();
    }

    public HeightContext createHeightContext(final ResourceLayer.Type type) {
        return new HeightContext.Layered(this, type);
    }

    public Stream<ResourceLayer.Type> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<ResourceLayer.Type> iterator() {
        return this.layersTypes.iterator();
    }
}
