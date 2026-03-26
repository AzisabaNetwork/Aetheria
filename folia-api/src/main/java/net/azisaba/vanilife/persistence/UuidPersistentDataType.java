package net.azisaba.vanilife.persistence;

import java.nio.ByteBuffer;
import java.util.UUID;
import net.azisaba.vanilife.annotations.VanilifoliaApi;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@VanilifoliaApi
public final class UuidPersistentDataType implements PersistentDataType<byte[], UUID> {
    @ApiStatus.Internal
    public UuidPersistentDataType() {
    }

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(final @NotNull UUID complex, final @NotNull PersistentDataAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
        buffer.putLong(complex.getMostSignificantBits());
        buffer.putLong(complex.getLeastSignificantBits());
        return buffer.array();
    }

    @Override
    public @NotNull UUID fromPrimitive(final byte @NotNull [] primitive, final @NotNull PersistentDataAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final long mostSignificantBits = buffer.getLong();
        final long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
