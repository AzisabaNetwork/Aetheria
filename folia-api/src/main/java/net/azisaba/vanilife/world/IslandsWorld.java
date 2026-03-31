package net.azisaba.vanilife.world;

import net.azisaba.vanilife.Vanilife;
import net.kyori.adventure.key.Key;
import org.bukkit.World;

public interface IslandsWorld extends World {
    int MIN_Y = 0;
    int HEIGHT = 256;
    float CLOUD_HEIGHT = 122.33F;
    int SEA_LEVEL = 12;

    int ISLAND_SIZE_X_BLOCKS = 144;
    int ISLAND_SIZE_Z_BLOCKS = 144;
    int SPACING_BLOCKS = 1008;

    Key WORLD_KEY = Key.key(Vanilife.NAMESPACE, "islands");
}
