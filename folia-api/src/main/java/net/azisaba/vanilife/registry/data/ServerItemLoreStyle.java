package net.azisaba.vanilife.registry.data;

import com.google.common.collect.ImmutableList;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.azisaba.vanilife.Season;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ServerItemLoreStyle(List<ConditionedPart> conditionedParts) {
    private static final ServerItemLoreStyle DEFAULT = loreStyle()
        .then(ServerItemRegistryEntry::described, Part.description())
        .then(Part.itemCategory())
        .then(ServerItemRegistryEntry::hasPeakSeason, Part.peakSeason())
        .build();

    private static final ServerItemLoreStyle EMPTY = new ServerItemLoreStyle(Collections.emptyList());

    private static final Style RESET_LORE_STYLE = Style.style()
        .color(NamedTextColor.GRAY)
        .decoration(TextDecoration.ITALIC, false)
        .build();

    @Contract(value = "-> new", pure = true)
    public static @NotNull Builder loreStyle() {
        return new Builder();
    }

    @Contract(pure = true)
    public static @NotNull ServerItemLoreStyle defaultStyle() {
        return DEFAULT;
    }

    @Contract(pure = true)
    public static @NotNull ServerItemLoreStyle emptyStyle() {
        return EMPTY;
    }

    public @NotNull List<@NotNull Part> parts(final ServerItemRegistryEntry item) {
        return this.conditionedParts.stream()
            .filter((conditioned) -> conditioned.predicate().test(item))
            .map(ConditionedPart::part)
            .toList();
    }

    public @NotNull ItemLore itemLore(final @NotNull ServerItemRegistryEntry item) {
        final List<Component> lines = this.buildLines(item).stream().map((line) -> line.applyFallbackStyle(RESET_LORE_STYLE)).toList();
        return ItemLore.lore(lines);
    }

    private @NotNull List<Component> buildLines(final @NotNull ServerItemRegistryEntry item) {
        final List<Part> parts = this.parts(item);
        final ImmutableList.Builder<Component> builder = ImmutableList.builder();
        for (int i = 0; i < parts.size(); i++) {
            if (0 < i) {
                builder.add(Component.empty());
            }
            final Part part = parts.get(i);
            part.append(item, builder);
        }
        return builder.build();
    }

    @FunctionalInterface
    public interface Part {
        static @NotNull Part description() {
            return (item, builder) -> builder.add(Component.translatable(item.translationKey() + ".description"));
        }

        static @NotNull Part itemCategory() {
            return (item, builder) -> builder.add(Component.translatable("item.vanilife.category"))
                .add(Component.translatable(item.category(), item.category().color()));
        }

        static @NotNull Part peakSeason() {
            return new Part() {
                @Override
                public void append(@NotNull ServerItemRegistryEntry item, ImmutableList.@NotNull Builder<@NotNull Component> builder) {
                    final List<Season.Sub> peakSeason = item.peakSeason().stream().sorted().toList();
                    final List<Range> ranges = this.buildRanges(peakSeason);

                    builder.add(Component.translatable("item.vanilife.peak_season"));

                    for (Range range : ranges) {
                        builder.add(range.toComponent());
                    }

                    builder.add(Component.translatable(item.translationKey() + ".season"));
                }

                private List<Range> buildRanges(final List<Season.Sub> sorted) {
                    final List<Range> result = new ArrayList<>();

                    Season.Sub start = sorted.getFirst();
                    Season.Sub prev = start;

                    for (int i = 1; i < sorted.size(); i++) {
                        final Season.Sub current = sorted.get(i);

                        if (!prev.next().equals(current)) {
                            result.add(new Range(start, prev));
                            start = current;
                        }

                        prev = current;
                    }

                    result.add(new Range(start, prev));
                    return Collections.unmodifiableList(result);
                }

                private record Range(Season.Sub start, Season.Sub end) {
                    public Component toComponent() {
                        if (this.start.equals(this.end)) {
                            return Component.translatable("item.vanilife.peak_season.range.single", Component.translatable(this.start, this.start.season().color()));
                        } else {
                            return Component.translatable("item.vanilife.peak_season.range.multiple", Component.translatable(this.start, this.start.season().color()), Component.translatable(this.end, this.end.season().color()));
                        }
                    }
                }
            };
        }

        void append(final @NotNull ServerItemRegistryEntry item, final ImmutableList.@NotNull Builder<@NotNull Component> builder);
    }

    public record ConditionedPart(@NotNull Predicate<@NotNull ServerItemRegistryEntry> predicate, @NotNull Part part) {
    }

    public static final class Builder {
        private final List<ConditionedPart> conditionedParts = new ArrayList<>();

        private Builder() {
        }

        @Contract(value = "_ -> this", mutates = "this")
        public Builder then(final @NotNull Part part) {
            return this.then((item) -> true, part);
        }

        @Contract(value = "_, _ -> this", mutates = "this")
        public @NotNull Builder then(final @NotNull Predicate<@NotNull ServerItemRegistryEntry> predicate, final @NotNull Part part) {
            conditionedParts.add(new ConditionedPart(predicate, part));
            return this;
        }

        @Contract(value = "-> new", pure = true)
        public @NotNull ServerItemLoreStyle build() {
            return new ServerItemLoreStyle(this.conditionedParts);
        }
    }
}
