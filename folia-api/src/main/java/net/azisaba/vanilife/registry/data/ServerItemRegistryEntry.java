package net.azisaba.vanilife.registry.data;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.DataComponentView;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryBuilder;
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import java.util.Set;
import net.azisaba.vanilife.Season;
import net.azisaba.vanilife.item.ServerItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.JukeboxSong;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

@ApiStatus.NonExtendable
public interface ServerItemRegistryEntry extends DataComponentView, Translatable {
    @Contract(pure = true)
    boolean described();

    @Contract(pure = true)
    @NotNull ServerItemCategory category();

    @Contract(pure = true)
    @NotNull Set<Season.@NotNull Sub> peakSeason();

    @Contract(pure = true)
    @NotNull ServerItemLoreStyle loreStyle();

    @Contract(mutates = "param1")
    void applyData(final @NotNull ItemStack itemStack);

    @Contract(mutates = "param1")
    void applyItemName(final @NotNull ItemStack itemStack);

    @Contract(mutates = "param1")
    void applyItemLore(final @NotNull ItemStack itemStack);

    default boolean hasPeakSeason() {
        return !peakSeason().isEmpty();
    }

    @ApiStatus.NonExtendable
    interface Builder extends RegistryBuilder<ServerItem> {
        @Contract(value = "_ -> this", mutates = "this")
        @NotNull Builder translationKey(final @NotNull String translationKey);

        @Contract(value = "-> this", mutates = "this")
        @NotNull Builder describe();

        @Contract(value = "_ -> this", mutates = "this")
        @NotNull Builder category(final @NotNull ServerItemCategory category);

        @Contract(value = "_ -> this", mutates = "this")
        @NotNull Builder peakSeason(final Season.@NotNull Sub... peakSeason);

        @Contract(value = "_ -> this", mutates = "this")
        @NotNull Builder loreStyle(final @NotNull ServerItemLoreStyle loreStyle);

        @Contract(value = "_, _ -> this", mutates = "this")
        @NotNull <T> Builder withComponent(final DataComponentType.@NotNull Valued<T> type, @NotNull T value);

        @Contract(value = "_ -> this", mutates = "this")
        Builder withComponent(final DataComponentType.@NotNull NonValued type);

        @Contract(value = "_ -> this", mutates = "this")
        default Builder itemModel(final @NotNull Key itemModel) {
            return this.withComponent(DataComponentTypes.ITEM_MODEL, itemModel);
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder food(final @NotNull FoodProperties food) {
            return this.food(
                    food,
                    Consumable.consumable()
                            .consumeSeconds(1.6F)
                            .animation(ItemUseAnimation.EAT)
                            .sound(SoundEventKeys.ENTITY_GENERIC_EAT)
                            .hasConsumeParticles(true)
                            .build()
            );
        }

        @Contract(value = "_, _ -> this", mutates = "this")
        default Builder food(final @NotNull FoodProperties food, final @NotNull Consumable consumable) {
            return this.withComponent(DataComponentTypes.FOOD, food).withComponent(DataComponentTypes.CONSUMABLE, consumable);
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder usingConvertsTo(final @NotNull ItemStack usingConvertsTo) {
            return this.withComponent(DataComponentTypes.USE_REMAINDER, UseRemainder.useRemainder(usingConvertsTo));
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder useCooldown(final float useCooldown) {
            return this.withComponent(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(useCooldown).build());
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder stacksTo(final int maxStackSize) {
            return this.withComponent(DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder durability(final int maxDamage) {
            this.withComponent(DataComponentTypes.MAX_DAMAGE, maxDamage);
            this.withComponent(DataComponentTypes.MAX_STACK_SIZE, 1);
            this.withComponent(DataComponentTypes.DAMAGE, 0);
            return this;
        }

        @Contract(value = "-> this", mutates = "this")
        default Builder fireResistant() {
            return this.withComponent(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(DamageTypeTagKeys.IS_FIRE));
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder jukeboxPlayable(final @NotNull JukeboxSong song) {
            return this.withComponent(DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayable.jukeboxPlayable(song).build());
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder enchantable(final int enchantmentValue) {
            return this.withComponent(DataComponentTypes.ENCHANTABLE, Enchantable.enchantable(enchantmentValue));
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder equippable(final @NotNull EquipmentSlot slot) {
            return this.withComponent(DataComponentTypes.EQUIPPABLE, Equippable.equippable(slot).build());
        }

        @Contract(value = "_ -> this", mutates = "this")
        default Builder equippableUnswappable(final @NotNull EquipmentSlot slot) {
            return this.withComponent(DataComponentTypes.EQUIPPABLE, Equippable.equippable(slot).swappable(false).build());
        }
    }
}
