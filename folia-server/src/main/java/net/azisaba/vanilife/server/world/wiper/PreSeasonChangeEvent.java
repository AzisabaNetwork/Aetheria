package net.azisaba.vanilife.server.world.wiper;

import net.azisaba.vanilife.Season;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * This event will call on before season change
 */
@NullMarked
public class PreSeasonChangeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Nullable
    public final Season oldSeason;
    public final Season newSeason;

    @ApiStatus.Internal
    public PreSeasonChangeEvent(@Nullable Season oldSeason, Season newSeason) {
        super();
        this.oldSeason = oldSeason;
        this.newSeason = newSeason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}