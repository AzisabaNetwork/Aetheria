package net.azisaba.vanilife.persistence;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import net.azisaba.vanilife.annotations.VanilifoliaApi;
import net.kyori.adventure.key.Key;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

@VanilifoliaApi
public final class KeyPersistentDataType implements PersistentDataType<byte[], Key> {
    @ApiStatus.Internal
    public KeyPersistentDataType() {
    }

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<Key> getComplexType() {
        return Key.class;
    }

    @Override
    public byte[] toPrimitive(final Key complex, final PersistentDataAdapterContext context) {
        final byte[] namespaceBytes = complex.namespace().getBytes(StandardCharsets.UTF_8);
        final byte[] valueBytes = complex.value().getBytes(StandardCharsets.UTF_8);

        if (namespaceBytes.length > 255) {
            throw new IllegalArgumentException("Namespace is too long to serialize: " + complex.asString());
        }

        return ByteBuffer.allocate(1 + namespaceBytes.length + valueBytes.length)
            .put((byte) namespaceBytes.length)
            .put(namespaceBytes)
            .put(valueBytes)
            .array();
    }

    @Override
    public Key fromPrimitive(final byte[] primitive, final PersistentDataAdapterContext context) {
        if (primitive.length < 2) {
            throw new IllegalArgumentException("Invalid serialized key length: " + primitive.length);
        }

        final ByteBuffer buffer = ByteBuffer.wrap(primitive);
        final int namespaceLength = Byte.toUnsignedInt(buffer.get());
        if (primitive.length < 1 + namespaceLength + 1) {
            throw new IllegalArgumentException("Invalid serialized key payload");
        }

        final byte[] namespaceBytes = new byte[namespaceLength];
        buffer.get(namespaceBytes);

        final byte[] valueBytes = new byte[buffer.remaining()];
        buffer.get(valueBytes);

        final String namespace = new String(namespaceBytes, StandardCharsets.UTF_8);
        final String value = new String(valueBytes, StandardCharsets.UTF_8);
        return Key.key(namespace, value);
    }
}
