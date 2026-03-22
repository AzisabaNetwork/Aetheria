package net.azisaba.vanilife.event;

import java.time.Instant;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jetbrains.annotations.ApiStatus;

@NullMarked
public class DragonInstalledEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final String islandId;
    private final UUID owner;
    private final Instant time;

    @ApiStatus.Internal
    public DragonInstalledEvent(String islandId, UUID owner, Instant time) {
        this.islandId = islandId;
        this.owner = owner;
        this.time = time;
    }

    public String getIslandId() {
        return islandId;
    }

    public UUID getOwner() {
        return owner;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
