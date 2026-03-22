package net.azisaba.vanilife.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.azisaba.vanilife.islands.DragonBuff;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jetbrains.annotations.ApiStatus;

@NullMarked
public class DragonBuffQueryEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final String islandId;
    private final Player player;
    private final List<DragonBuff> accumulator = new ArrayList<>();

    @ApiStatus.Internal
    public DragonBuffQueryEvent(String islandId, Player player) {
        this.islandId = islandId;
        this.player = player;
    }

    public String getIslandId() {
        return islandId;
    }

    public Player getPlayer() {
        return player;
    }

    public List<DragonBuff> getAccumulator() {
        return accumulator;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
