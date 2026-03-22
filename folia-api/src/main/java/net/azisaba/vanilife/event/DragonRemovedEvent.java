package net.azisaba.vanilife.event;

import java.time.Instant;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jetbrains.annotations.ApiStatus;

@NullMarked
public class DragonRemovedEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final String islandId;
    private final String reason;
    private final Instant time;

    @ApiStatus.Internal
    public DragonRemovedEvent(String islandId, String reason, Instant time) {
        this.islandId = islandId;
        this.reason = reason;
        this.time = time;
    }

    public String getIslandId() {
        return islandId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
