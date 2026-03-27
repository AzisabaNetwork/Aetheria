package net.azisaba.vanilife.event;

import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jetbrains.annotations.ApiStatus;

@NullMarked
public class DragonCustomizationChangedEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final String islandId;
    private final UUID changedBy;
    private final String field;
    private final String presetKey;

    @ApiStatus.Internal
    public DragonCustomizationChangedEvent(String islandId, UUID changedBy, String field, String presetKey) {
        this.islandId = islandId;
        this.changedBy = changedBy;
        this.field = field;
        this.presetKey = presetKey;
    }

    public String getIslandId() {
        return islandId;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public String getField() {
        return field;
    }

    public String getPresetKey() {
        return presetKey;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
