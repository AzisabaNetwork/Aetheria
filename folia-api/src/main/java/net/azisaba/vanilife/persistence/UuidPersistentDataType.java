package net.azisaba.vanilife.persistence;

import java.nio.ByteBuffer;
import java.util.UUID;
import net.azisaba.vanilife.annotations.VanilifoliaApi;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

@VanilifoliaApi
public final class UuidPersistentDataType implements PersistentDataType<byte[], UUID> {
    @ApiStatus.Internal
    public UuidPersistentDataType() {
    }

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte[] toPrimitive(final UUID complex, final PersistentDataAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
        buffer.putLong(complex.getMostSignificantBits());
        buffer.putLong(complex.getLeastSignificantBits());
        return buffer.array();
    }

    @Override
    public UUID fromPrimitive(final byte[] primitive, final PersistentDataAdapterContext context) {
        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final long mostSignificantBits = buffer.getLong();
        final long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
