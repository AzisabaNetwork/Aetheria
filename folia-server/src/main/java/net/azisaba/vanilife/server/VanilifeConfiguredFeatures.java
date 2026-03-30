package net.azisaba.vanilife.server;

import net.azisaba.vanilife.Vanilife;
import net.azisaba.vanilife.server.world.feature.CaveIceClusterFeature;
import net.azisaba.vanilife.server.world.feature.CaveSnowCoverFeature;
import net.azisaba.vanilife.server.world.feature.CaveWallFrostFeature;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.PlaceOnGroundDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class VanilifeConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_ICE_CLUSTER = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "cave_ice_cluster"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_ICE_PILLAR = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "cave_ice_pillar"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_SNOW_COVER = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "cave_snow_cover"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_WALL_FROST = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "cave_wall_frost"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISLAND_BEACH_PATCH = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "island_beach_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISLAND_CONIFER_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "island_conifer_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISLAND_GROUND_PATCH = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "island_ground_patch"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISLAND_PRISMARINE_PORTAL_FRAME = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "island_prismarine_portal_frame"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISLAND_WHEAT_PATCH = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Vanilife.NAMESPACE, "island_wheat_patch"));

    private VanilifeConfiguredFeatures() {
    }

    public static void bootstrap(final WritableRegistry<ConfiguredFeature<?, ?>> writable, final RegistryOps.RegistryInfoLookup lookup) {
        final HolderGetter<Feature<?>> features = lookup.lookup(Registries.FEATURE)
            .orElseThrow()
            .getter();

        writable.register(CAVE_ICE_CLUSTER, caveIceCluster(features), RegistrationInfo.BUILT_IN);
        writable.register(CAVE_ICE_PILLAR, caveIcePillar(features), RegistrationInfo.BUILT_IN);
        writable.register(CAVE_SNOW_COVER, caveSnowCover(features), RegistrationInfo.BUILT_IN);
        writable.register(CAVE_WALL_FROST, caveWallFrost(features), RegistrationInfo.BUILT_IN);
        writable.register(ISLAND_BEACH_PATCH, islandBeachPatch(), RegistrationInfo.BUILT_IN);
        writable.register(ISLAND_CONIFER_TREE, islandConiferTree(), RegistrationInfo.BUILT_IN);
        writable.register(ISLAND_GROUND_PATCH, islandGroundPatch(), RegistrationInfo.BUILT_IN);
        writable.register(ISLAND_PRISMARINE_PORTAL_FRAME, islandPrismarinePortalFrame(features), RegistrationInfo.BUILT_IN);
        writable.register(ISLAND_WHEAT_PATCH, islandWheatPatch(features), RegistrationInfo.BUILT_IN);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> caveIceCluster(final HolderGetter<Feature<?>> features) {
        final Feature<CaveIceClusterFeature.Configuration> feature = (Feature<CaveIceClusterFeature.Configuration>) features.getOrThrow((ResourceKey) VanilifeFeatures.CAVE_ICE_CLUSTER).value();
        return new ConfiguredFeature<>(
            feature,
            new CaveIceClusterFeature.Configuration(
                new WeightedStateProvider(
                    WeightedList.<BlockState>builder()
                        .add(Blocks.PACKED_ICE.defaultBlockState(), 6)
                        .add(Blocks.ICE.defaultBlockState(), 2)
                ),
                new WeightedStateProvider(
                    WeightedList.<BlockState>builder()
                        .add(Blocks.BLUE_ICE.defaultBlockState(), 5)
                        .add(Blocks.PACKED_ICE.defaultBlockState(), 1)
                )
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> caveIcePillar(final HolderGetter<Feature<?>> features) {
        final Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) features.getOrThrow((ResourceKey) VanilifeFeatures.CAVE_ICE_PILLAR).value();
        return new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> caveSnowCover(final HolderGetter<Feature<?>> features) {
        final Feature<CaveSnowCoverFeature.Configuration> feature = (Feature<CaveSnowCoverFeature.Configuration>) features.getOrThrow((ResourceKey) VanilifeFeatures.CAVE_SNOW_COVER).value();
        return new ConfiguredFeature<>(
            feature,
            new CaveSnowCoverFeature.Configuration(
                8,
                8,
                3,
                0.125F,
                8.0F,
                2.0F,
                1.0F,
                2,
                new WeightedStateProvider(
                    WeightedList.<BlockState>builder()
                        .add(Blocks.PACKED_ICE.defaultBlockState(), 5)
                        .add(Blocks.ICE.defaultBlockState(), 2)
                        .add(Blocks.BLUE_ICE.defaultBlockState(), 1)
                )
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> caveWallFrost(final HolderGetter<Feature<?>> features) {
        final Feature<CaveWallFrostFeature.Configuration> feature = (Feature<CaveWallFrostFeature.Configuration>) features.getOrThrow((ResourceKey) VanilifeFeatures.CAVE_WALL_FROST).value();
        return new ConfiguredFeature<>(
            feature,
            new CaveWallFrostFeature.Configuration(
                new WeightedStateProvider(
                    WeightedList.<BlockState>builder()
                        .add(Blocks.PACKED_ICE.defaultBlockState(), 5)
                        .add(Blocks.SNOW_BLOCK.defaultBlockState(), 3)
                        .add(Blocks.ICE.defaultBlockState(), 2)
                ),
                new WeightedStateProvider(
                    WeightedList.<BlockState>builder()
                        .add(Blocks.BLUE_ICE.defaultBlockState(), 3)
                        .add(Blocks.PACKED_ICE.defaultBlockState(), 2)
                        .add(Blocks.SNOW_BLOCK.defaultBlockState(), 1)
                )
            )
        );
    }

    private static ConfiguredFeature<?, ?> islandBeachPatch() {
        return new ConfiguredFeature<>(
            Feature.DISK,
            new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(
                    new WeightedStateProvider(
                        WeightedList.<BlockState>builder()
                            .add(Blocks.GRAVEL.defaultBlockState(), 6)
                            .add(Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(), 2)
                            .add(Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2)
                            .add(Blocks.SANDSTONE.defaultBlockState(), 5)
                            .add(Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 3)
                            .add(Blocks.CLAY.defaultBlockState(), 2)
                    )
                ),
                BlockPredicate.matchesBlocks(
                    Blocks.SAND,
                    Blocks.GRAVEL,
                    Blocks.SUSPICIOUS_SAND,
                    Blocks.SUSPICIOUS_GRAVEL,
                    Blocks.SANDSTONE,
                    Blocks.SMOOTH_SANDSTONE,
                    Blocks.CLAY,
                    Blocks.RED_SANDSTONE
                ),
                UniformInt.of(1, 3),
                1
            )
        );
    }

    private static ConfiguredFeature<?, ?> islandConiferTree() {
        return new ConfiguredFeature<>(
            Feature.TREE,
            new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new StraightTrunkPlacer(17, 2, 2),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new MegaPineFoliagePlacer(
                    ConstantInt.of(1),
                    UniformInt.of(1, 3),
                    ClampedNormalInt.of(14.0F, 3.0F, 14, 17)
                ),
                new TwoLayersFeatureSize(0, 1, 0)
            )
                .dirt(BlockStateProvider.simple(Blocks.DIRT))
                .forceDirt()
                .ignoreVines()
                .decorators(
                    java.util.List.of(
                        new PlaceOnGroundDecorator(
                            256,
                            7,
                            3,
                            new WeightedStateProvider(
                                WeightedList.<BlockState>builder()
                                    .add(leafLitterState(Direction.NORTH, 1), 3)
                                    .add(leafLitterState(Direction.EAST, 1), 3)
                                    .add(leafLitterState(Direction.SOUTH, 1), 3)
                                    .add(leafLitterState(Direction.WEST, 1), 3)
                                    .add(leafLitterState(Direction.NORTH, 2), 3)
                                    .add(leafLitterState(Direction.EAST, 2), 3)
                                    .add(leafLitterState(Direction.SOUTH, 2), 3)
                                    .add(leafLitterState(Direction.WEST, 2), 3)
                                    .add(leafLitterState(Direction.NORTH, 3), 2)
                                    .add(leafLitterState(Direction.EAST, 3), 2)
                                    .add(leafLitterState(Direction.SOUTH, 3), 2)
                                    .add(leafLitterState(Direction.WEST, 3), 2)
                            )
                        )
                    )
                )
                .build()
        );
    }

    private static ConfiguredFeature<?, ?> islandGroundPatch() {
        return new ConfiguredFeature<>(
            Feature.DISK,
            new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(
                    new WeightedStateProvider(
                        WeightedList.<BlockState>builder()
                            .add(Blocks.COARSE_DIRT.defaultBlockState(), 7)
                            .add(Blocks.ROOTED_DIRT.defaultBlockState(), 4)
                            .add(Blocks.GRAVEL.defaultBlockState(), 4)
                            .add(Blocks.MUD.defaultBlockState(), 3)
                            .add(Blocks.CLAY.defaultBlockState(), 2)
                    )
                ),
                BlockPredicate.matchesBlocks(
                    Blocks.DIRT,
                    Blocks.GRASS_BLOCK,
                    Blocks.COARSE_DIRT,
                    Blocks.ROOTED_DIRT,
                    Blocks.MUD,
                    Blocks.GRAVEL,
                    Blocks.CLAY
                ),
                UniformInt.of(2, 5),
                1
            )
        );
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> islandPrismarinePortalFrame(final HolderGetter<Feature<?>> features) {
        final Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) features.getOrThrow((ResourceKey) VanilifeFeatures.ISLAND_PRISMARINE_PORTAL_FRAME).value();
        return new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    private static ConfiguredFeature<?, ?> islandWheatPatch(final HolderGetter<Feature<?>> features) {
        final Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) features.getOrThrow((ResourceKey) VanilifeFeatures.ISLAND_WHEAT_PATCH).value();
        return new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE);
    }

    private static BlockState leafLitterState(final Direction direction, final int segmentAmount) {
        return Blocks.LEAF_LITTER.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, direction)
            .setValue(BlockStateProperties.SEGMENT_AMOUNT, segmentAmount);
    }
}
