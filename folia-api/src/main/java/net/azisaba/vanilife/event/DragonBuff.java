package net.azisaba.vanilife.event;

import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DragonBuff {
    private final PotionEffectType type;
    private final int amplifier;
    private final int durationTicks;

    public DragonBuff(final PotionEffectType type, final int amplifier, final int durationTicks) {
        this.type = type;
        this.amplifier = amplifier;
        this.durationTicks = durationTicks;
    }

    public DragonBuff(final PotionEffectType type, final int amplifier) {
        this(type, amplifier, 200);
    }

    public PotionEffectType getType() {
        return this.type;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public int getDurationTicks() {
        return this.durationTicks;
    }
}
