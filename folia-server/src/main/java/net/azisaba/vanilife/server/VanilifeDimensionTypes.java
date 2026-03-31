package net.azisaba.vanilife.server;

import net.azisaba.vanilife.Vanilife;
import net.azisaba.vanilife.world.IslandsWorld;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.*;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class VanilifeDimensionTypes {
    public static final ResourceKey<DimensionType> RESOURCE = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "resource"));
    public static final ResourceKey<DimensionType> ISLANDS = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "islands"));

    private VanilifeDimensionTypes() {
    }

    public static void bootstrap(final WritableRegistry<DimensionType> writable, final RegistryOps.RegistryInfoLookup lookup) {
        final HolderGetter<Timeline> timelines = lookup.lookup(Registries.TIMELINE).orElseThrow().getter();
        writable.register(RESOURCE, resource(timelines), RegistrationInfo.BUILT_IN);
        writable.register(ISLANDS, islands(timelines), RegistrationInfo.BUILT_IN);
    }

    private static DimensionType resource(final HolderGetter<Timeline> timelines) {
        return new DimensionType(
            false,
            true,
            false,
            1.0,
            DimensionDefaults.OVERWORLD_MIN_Y - DimensionDefaults.NETHER_GENERATION_HEIGHT,
            DimensionDefaults.NETHER_GENERATION_HEIGHT + DimensionDefaults.OVERWORLD_GENERATION_HEIGHT + DimensionDefaults.END_GENERATION_HEIGHT,
            DimensionDefaults.NETHER_GENERATION_HEIGHT + DimensionDefaults.OVERWORLD_GENERATION_HEIGHT + DimensionDefaults.END_GENERATION_HEIGHT,
            BlockTags.INFINIBURN_OVERWORLD,
            0.0F,
            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
            DimensionType.Skybox.OVERWORLD,
            DimensionType.CardinalLightType.DEFAULT,
            resourceEnvironment(),
            overworldTimeline(timelines)
        );
    }

    private static DimensionType islands(final HolderGetter<Timeline> timelines) {
        return new DimensionType(
            false,
            true,
            false,
            1.0,
            IslandsWorld.MIN_Y,
            IslandsWorld.HEIGHT,
            IslandsWorld.HEIGHT,
            BlockTags.INFINIBURN_OVERWORLD,
            0.0F,
            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
            DimensionType.Skybox.OVERWORLD,
            DimensionType.CardinalLightType.DEFAULT,
            islandsEnvironment(),
            overworldTimeline(timelines)
        );
    }

    private static EnvironmentAttributeMap resourceEnvironment() {
        return EnvironmentAttributeMap.builder()
            .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
            .set(EnvironmentAttributes.CLOUD_HEIGHT, IslandsWorld.CLOUD_HEIGHT)
            .set(EnvironmentAttributes.FOG_COLOR, -4138753)
            .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
            .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
            .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
            .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
            .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
            .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
            .build();
    }

    private static EnvironmentAttributeMap islandsEnvironment() {
        return EnvironmentAttributeMap.builder()
            .set(EnvironmentAttributes.CLOUD_COLOR, -1508609)
            .set(EnvironmentAttributes.CLOUD_HEIGHT, 108.0F)
            .set(EnvironmentAttributes.FOG_COLOR, 12835832)
            .set(EnvironmentAttributes.FOG_END_DISTANCE, 256f)
            .set(EnvironmentAttributes.FOG_START_DISTANCE, 125f)
            .set(EnvironmentAttributes.SKY_COLOR, 7905002)
            .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
            .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
            .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
            .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
            .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
            .build();
    }

    private static net.minecraft.core.HolderSet<Timeline> overworldTimeline(final HolderGetter<Timeline> timelines) {
        return timelines.getOrThrow(TimelineTags.IN_OVERWORLD);
    }
}
