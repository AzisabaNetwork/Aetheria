package net.azisaba.vanilife;

import java.util.Objects;
import net.azisaba.vanilife.world.IslandDefaults;
import net.azisaba.vanilife.world.IslandsWorld;
import net.azisaba.vanilife.world.ResourceWorld;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Vanilife {
    public static final String NAMESPACE = "vanilife";

    public static IslandsWorld getIslandsWorld() {
        return (IslandsWorld) Objects.requireNonNull(Bukkit.getWorld(IslandDefaults.WORLD_KEY));
    }

    /**
     * Returns the currently loaded resource world, or {@code null} if none is loaded.
     *
     * @return the current resource world, or null
     */
    public static @Nullable ResourceWorld getResourceWorldOrNull() {
        for (final World world : Bukkit.getWorlds()) {
            if (world instanceof ResourceWorld resourceWorld) {
                return resourceWorld;
            }
        }
        return null;
    }

    /**
     * Returns the currently loaded resource world.
     *
     * @return the current resource world
     * @throws IllegalStateException if no resource world is loaded
     */
    public static ResourceWorld getResourceWorld() {
        final ResourceWorld world = getResourceWorldOrNull();
        if (world == null) {
            throw new IllegalStateException("No resource world is currently loaded");
        }
        return world;
    }

    private Vanilife() {
    }
}
