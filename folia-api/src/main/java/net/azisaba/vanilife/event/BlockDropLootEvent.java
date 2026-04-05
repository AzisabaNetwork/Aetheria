package net.azisaba.vanilife.event;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.azisaba.vanilife.annotations.VanilifoliaApi;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@VanilifoliaApi
public class BlockDropLootEvent extends BlockEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final BlockState blockState;

    private final Entity entity;
    private final ItemStack tool;

    private final List<ItemStack> drops;

    @ApiStatus.Internal
    public BlockDropLootEvent(
        final Block block,
        final BlockState blockState,
        final @Nullable Entity entity,
        final @Nullable ItemStack tool,
        final List<ItemStack> drops
    ) {
        super(block);
        this.blockState = blockState;
        this.entity = entity;
        this.tool = tool;
        this.drops = drops;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public @Nullable Entity getEntity() {
        return this.entity;
    }

    public @Nullable ItemStack getTool() {
        return this.tool;
    }

    public List<ItemStack> getDrops() {
        return this.drops.stream().map(ItemStack::clone).toList();
    }

    public void setDrops(final List<ItemStack> drops) {
        this.drops.clear();
        this.drops.addAll(drops);
    }

    public void mapDrops(final UnaryOperator<ItemStack> mapper) {
        this.setDrops(this.getDrops().stream().map(mapper).toList());
    }

    public void filterDrops(final Predicate<ItemStack> predicate) {
        this.setDrops(this.getDrops().stream().filter(predicate).toList());
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
