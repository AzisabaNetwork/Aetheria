package net.azisaba.vanilife.event;

import java.util.Random;
import net.azisaba.vanilife.annotations.VanilifoliaApi;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@VanilifoliaApi
public class BlockRandomTickEvent extends BlockEvent implements Cancellable {
    private static final @NotNull HandlerList HANDLER_LIST = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final @NotNull Random random;
    private boolean cancelled = false;

    @ApiStatus.Internal
    public BlockRandomTickEvent(final @NotNull Block block, final @NotNull Random random) {
        super(block);
        this.random = random;
    }

    public @NotNull Random getRandom() {
        return this.random;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
