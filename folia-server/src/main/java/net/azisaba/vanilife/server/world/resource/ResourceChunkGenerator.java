package net.azisaba.vanilife.server.world.resource;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.azisaba.vanilife.server.world.height.HeightContext;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ResourceChunkGenerator extends ChunkGenerator {
    public static final MapCodec<ResourceChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
                ResourceLayout.CODEC.fieldOf("layout").forGetter(generator -> generator.layout)
            )
            .apply(instance, ResourceChunkGenerator::new)
    );

    private static BoundingBox getWritableArea(final ChunkAccess chunk) {
        final ChunkPos pos = chunk.getPos();
        final int minBlockX = pos.getMinBlockX();
        final int minBlockZ = pos.getMinBlockZ();
        final LevelHeightAccessor heightAccessorForGeneration = chunk.getHeightAccessorForGeneration();
        final int minY = heightAccessorForGeneration.getMinY() + 1;
        final int maxY = heightAccessorForGeneration.getMaxY();
        return new BoundingBox(minBlockX, minY, minBlockZ, minBlockX + 15, maxY, minBlockZ + 15);
    }

    public final ResourceLayout layout;

    private final ResourceRandomStateProvider randomStateSource = new ResourceRandomStateProvider();
    private final Map<ChunkGenerator, List<FeatureSorter.StepFeatureData>> featuresPerStepCache = new ConcurrentHashMap<>();

    public ResourceChunkGenerator(final ResourceLayout layout) {
        super(new ResourceBiomeSource(layout));
        this.layout = layout;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getMinY() {
        return this.layout.minY();
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getGenDepth() {
        return this.layout.height();
    }

    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level, final RandomState random) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final RandomState random) {
        final int minY = height.getMinY();
        final int maxY = height.getMaxY();

        final BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 0; i < column.length; i++) {
            column[i] = Blocks.AIR.defaultBlockState();
        }

        for (final ResourceLayer.Type layerType : this.layout) {
            final LevelHeightAccessor layerHeight = layerType.createHeightAccessor();
            final NoiseColumn layerColumn = layerType.generator().getBaseColumn(x, z, layerHeight, random);

            for (int layerY = layerType.minY(); layerY <= layerType.maxY(); layerY++) {
                final int blockY = this.layout.toBlockY(layerType, layerY);
                if (blockY < minY || blockY > maxY) {
                    continue;
                }
                column[blockY - minY] = layerColumn.getBlock(layerY);
            }
        }

        return new NoiseColumn(minY, column);
    }

    @Override
    public void applyCarvers(
        final WorldGenRegion region,
        final long seed,
        final RandomState random,
        final BiomeManager biomeManager,
        final StructureManager structureManager,
        final ChunkAccess chunk,
        final HeightContext heightContext
    ) {
        final HolderGetter<NormalNoise.NoiseParameters> noiseParametersGetter = region.registryAccess().lookupOrThrow(Registries.NOISE);

        for (final ResourceLayer.Type layerType : this.layout) {
            final ChunkGenerator layerGenerator = layerType.generator();
            final ResourceLayer layerChunk = ResourceLayer.copy(chunk, region.getMinecraftWorld(), this.layout, layerType);
            final RandomState layerRandomState = Objects.requireNonNullElse(
                this.randomStateSource.getOrCreate(seed, layerType, noiseParametersGetter),
                random
            );
            final HeightContext layerHeightContext = this.layout.createHeightContext(layerType);
            layerGenerator.applyCarvers(
                region,
                seed,
                layerRandomState,
                biomeManager.withLayeredSource(this.layout, layerType),
                structureManager,
                layerChunk,
                layerHeightContext
            );
            layerChunk.mergeInto(chunk, false);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        for (final ResourceLayer.Type layerType : this.layout) {
            final ChunkGenerator layerGenerator = layerType.generator();
            final HeightContext heightContext = this.layout.createHeightContext(layerType);
            this.applyLayerBiomeDecoration(level, chunk, structureManager, layerGenerator, heightContext);
        }
    }

    @Override
    public void createStructures(
        final RegistryAccess registryAccess,
        final ChunkGeneratorStructureState structureState,
        final StructureManager structureManager,
        final ChunkAccess chunk,
        final StructureTemplateManager structureTemplateManager,
        final net.minecraft.resources.ResourceKey<Level> level,
        final HeightContext heightContext
    ) {
        for (final ResourceLayer.Type layerType : this.layout) {
            layerType.generator().createStructures(
                registryAccess,
                structureState,
                structureManager,
                chunk,
                structureTemplateManager,
                level,
                this.layout.createHeightContext(layerType)
            );
        }
    }

    @Override
    public boolean canCreateStructure(
        final net.minecraft.world.level.levelgen.structure.Structure structure,
        final RegistryAccess registryAccess,
        final StructureTemplateManager structureTemplateManager,
        final RandomState randomState,
        final ChunkPos chunkPos,
        final LevelHeightAccessor heightAccessor,
        final long seed,
        final HeightContext heightContext
    ) {
        for (final ResourceLayer.Type layerType : this.layout) {
            if (layerType.generator()
                .canCreateStructure(
                    structure,
                    registryAccess,
                    structureTemplateManager,
                    randomState,
                    chunkPos,
                    heightAccessor,
                    seed,
                    this.layout.createHeightContext(layerType)
                )) {
                return true;
            }
        }

        return false;
    }

    @Override
    public HeightContext getStructurePlacementHeightContext(final WorldGenLevel level, final net.minecraft.world.level.levelgen.structure.StructureStart structureStart) {
        final BlockPos center = structureStart.getBoundingBox().getCenter();
        final ResourceLayer.Type layerType = this.layout.getLayerTypeAt(center.getY());
        if (layerType != null) {
            return this.layout.createHeightContext(layerType);
        }

        return new HeightContext.Vanilla(level);
    }

    @Override
    public void buildSurface(
        final WorldGenRegion region,
        final StructureManager structureManager,
        final RandomState random,
        final ChunkAccess chunk,
        final HeightContext heightContext
    ) {
        final Registry<NormalNoise.NoiseParameters> noiseParameters = region.registryAccess().lookupOrThrow(Registries.NOISE);

        for (final ResourceLayer.Type layerType : this.layout) {
            final ChunkGenerator layerGenerator = layerType.generator();

            final ResourceLayer layerChunk = ResourceLayer.copy(chunk, region.getMinecraftWorld(), this.layout, layerType);
            final RandomState layerRandomState = Objects.requireNonNullElse(
                this.randomStateSource.getOrCreate(region.getSeed(), layerType, noiseParameters),
                random
            );
            final HeightContext layerHeightContext = this.layout.createHeightContext(layerType);
            layerGenerator.buildSurface(region, structureManager, layerRandomState, layerChunk, layerHeightContext);
            layerChunk.mergeInto(chunk, true);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
        final Blender blender,
        final RandomState randomState,
        final StructureManager structureManager,
        final ChunkAccess chunk,
        final HeightContext heightContext
    ) {
        return CompletableFuture.supplyAsync(() -> {
            final ServerLevel level = structureManager.level.getMinecraftWorld();
            final long seed = level.getMinecraftWorld().getSeed();
            final HolderGetter<NormalNoise.NoiseParameters> noiseParametersGetter = structureManager.registryAccess().lookupOrThrow(Registries.NOISE);

            for (final ResourceLayer.Type layerType : this.layout) {
                final ChunkGenerator layerGenerator = layerType.generator();
                final ResourceLayer layerChunk = ResourceLayer.empty(chunk.getPos(), level, this.layout, layerType);
                final HeightContext layerHeightContext = this.layout.createHeightContext(layerType);
                layerGenerator.fillFromNoise(
                    blender,
                    Objects.requireNonNullElse(
                        this.randomStateSource.getOrCreate(seed, layerType, noiseParametersGetter),
                        randomState
                    ),
                    structureManager,
                    layerChunk,
                    layerHeightContext
                ).join();
                layerChunk.mergeInto(chunk, false);
            }
            return chunk;
        });
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            final ChunkPos pos = chunk.getPos();
            final int quartMinX = QuartPos.fromBlock(pos.getMinBlockX());
            final int quartMinY = QuartPos.fromBlock(chunk.getMinY());
            final int quartMinZ = QuartPos.fromBlock(pos.getMinBlockZ());
            final int quartHeight = QuartPos.fromBlock(chunk.getHeight());

            for (int qx = 0; qx < 4; qx++) {
                for (int qz = 0; qz < 4; qz++) {
                    final int quartX = quartMinX + qx;
                    final int quartZ = quartMinZ + qz;
                    for (int qyOffset = 0; qyOffset < quartHeight; qyOffset++) {
                        final int quartY = quartMinY + qyOffset;
                        final int blockY = QuartPos.toBlock(quartY);

                        final ResourceLayer.Type layerType = this.layout.getLayerTypeAt(blockY);
                        if (layerType == null) {
                            continue;
                        }

                        final int layerY = this.layout.toLayerY(layerType, blockY);
                        final int layerQuartY = QuartPos.fromBlock(layerY);
                        final RandomState layerRandomState = Objects.requireNonNullElse(
                            this.randomStateSource.getOrCreate(structureManager.level.getMinecraftWorld().getSeed(), layerType, structureManager.registryAccess().lookupOrThrow(Registries.NOISE)),
                            randomState
                        );
                        final Holder<Biome> biome = layerType.generator().getBiomeSource().getNoiseBiome(quartX, layerQuartY, quartZ, layerRandomState.sampler());
                        chunk.setBiome(quartX, quartY, quartZ, biome);
                    }
                }
            }

            return chunk;
        }, Runnable::run);
    }

    @Override
    public void spawnOriginalMobs(final WorldGenRegion region) {
        for (final ResourceLayer.Type layerType : this.layout) {
            final ChunkGenerator layerGenerator = layerType.generator();
            layerGenerator.spawnOriginalMobs(region);
        }
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }

    private void applyLayerBiomeDecoration(
        final WorldGenLevel level,
        final ChunkAccess chunk,
        final StructureManager structureManager,
        final ChunkGenerator layerGenerator,
        final HeightContext heightContext
    ) {
        this.addLayerVanillaDecorations(level, chunk, structureManager, layerGenerator, heightContext);

        final org.bukkit.World world = level.getMinecraftWorld().getWorld();
        if (!world.getPopulators().isEmpty()) {
            final org.bukkit.craftbukkit.generator.CraftLimitedRegion limitedRegion = new org.bukkit.craftbukkit.generator.CraftLimitedRegion(level, chunk.getPos());
            final int x = chunk.getPos().x;
            final int z = chunk.getPos().z;
            for (final org.bukkit.generator.BlockPopulator populator : world.getPopulators()) {
                final WorldgenRandom seededRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
                seededRandom.setDecorationSeed(level.getSeed(), x, z);
                populator.populate(world, new org.bukkit.craftbukkit.util.RandomSourceWrapper.RandomWrapper(seededRandom), x, z, limitedRegion);
            }
            limitedRegion.saveEntities();
            limitedRegion.breakLink();
        }
    }

    private void addLayerVanillaDecorations(final WorldGenLevel level, final ChunkAccess chunk, final StructureManager structureManager, final ChunkGenerator layerGenerator, final HeightContext heightContext) {
        final ChunkPos pos = chunk.getPos();
        if (SharedConstants.debugVoidTerrain(pos)) {
            return;
        }

        final SectionPos sectionPos = SectionPos.of(pos, level.getMinSectionY());
        final int decorationY = Mth.clamp(heightContext.y(layerGenerator.getSeaLevel()), heightContext.minY(), heightContext.maxY());
        final BlockPos blockPos = new BlockPos(pos.getMinBlockX(), decorationY, pos.getMinBlockZ());
        final Registry<Structure> structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        final Map<Integer, List<Structure>> structuresByStep = structureRegistry.stream()
            .collect(Collectors.groupingBy(structure -> structure.step().ordinal()));
        final List<FeatureSorter.StepFeatureData> featuresPerStep = this.featuresPerStepCache.computeIfAbsent(
            layerGenerator,
            generator -> FeatureSorter.buildFeaturesPerStep(
                List.copyOf(generator.getBiomeSource().possibleBiomes()),
                biome -> generator.generationSettingsGetter.apply(biome).features(),
                true
            )
        );
        final WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        final long decorationSeed = worldgenRandom.setDecorationSeed(level.getSeed(), blockPos.getX(), blockPos.getZ());
        final Set<Holder<Biome>> biomes = new ObjectArraySet<>();

        ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPos -> {
            final ChunkAccess neighbourChunk = level.getChunk(chunkPos.x, chunkPos.z);
            for (final LevelChunkSection section : neighbourChunk.getSections()) {
                section.getBiomes().getAll(biomes::add);
            }
        });
        biomes.retainAll(layerGenerator.getBiomeSource().possibleBiomes());

        try {
            final Registry<PlacedFeature> placedFeatureRegistry = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            final int max = Math.max(GenerationStep.Decoration.values().length, featuresPerStep.size());

            for (int step = 0; step < max; step++) {
                int structureIndex = 0;
                if (structureManager.shouldGenerateStructures()) {
                    for (final Structure structure : structuresByStep.getOrDefault(step, Collections.emptyList())) {
                        worldgenRandom.setFeatureSeed(decorationSeed, structureIndex, step);
                        final Supplier<String> description = () -> structureRegistry.getResourceKey(structure)
                            .map(Object::toString)
                            .orElseGet(structure::toString);

                        try {
                            level.setCurrentlyGenerating(description);
                            structureManager.startsForStructure(sectionPos, structure)
                                .forEach(
                                    structureStart -> structureStart.placeInChunk(
                                        level,
                                        structureManager,
                                        layerGenerator,
                                        worldgenRandom,
                                        getWritableArea(chunk),
                                        pos
                                    )
                                );
                        } catch (final Exception exception) {
                            final CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", description::get);
                            throw new ReportedException(crashReport);
                        }

                        structureIndex++;
                    }
                }

                if (step >= featuresPerStep.size()) {
                    continue;
                }

                final IntSet featureIndices = new IntArraySet();
                for (final Holder<Biome> biome : biomes) {
                    final List<HolderSet<PlacedFeature>> features = layerGenerator.generationSettingsGetter.apply(biome).features();
                    if (step < features.size()) {
                        final HolderSet<PlacedFeature> holderSet = features.get(step);
                        final FeatureSorter.StepFeatureData stepFeatureData = featuresPerStep.get(step);
                        holderSet.stream().map(Holder::value).forEach(feature -> featureIndices.add(stepFeatureData.indexMapping().applyAsInt(feature)));
                    }
                }

                final int[] sortedFeatureIndices = featureIndices.toIntArray();
                Arrays.sort(sortedFeatureIndices);
                final FeatureSorter.StepFeatureData stepFeatureData = featuresPerStep.get(step);

                for (final int featureIndex : sortedFeatureIndices) {
                    final PlacedFeature placedFeature = stepFeatureData.features().get(featureIndex);
                    final Supplier<String> description = () -> placedFeatureRegistry.getResourceKey(placedFeature)
                        .map(Object::toString)
                        .orElseGet(placedFeature::toString);
                    long featurePopulationSeed = decorationSeed;
                    final long configuredFeatureSeed = level.getMinecraftWorld().paperConfig().featureSeeds.features.getLong(placedFeature.feature());
                    if (configuredFeatureSeed != -1) {
                        featurePopulationSeed = worldgenRandom.setDecorationSeed(configuredFeatureSeed, blockPos.getX(), blockPos.getZ());
                    }
                    worldgenRandom.setFeatureSeed(featurePopulationSeed, featureIndex, step);

                    try {
                        level.setCurrentlyGenerating(description);
                        placedFeature.placeWithBiomeCheck(level, layerGenerator, worldgenRandom, blockPos, heightContext);
                    } catch (final Exception exception) {
                        final CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                        crashReport.addCategory("Feature").setDetail("Description", description::get);
                        throw new ReportedException(crashReport);
                    }
                }
            }

            level.setCurrentlyGenerating(null);
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
                net.minecraft.world.level.levelgen.feature.FeatureCountTracker.chunkDecorated(level.getLevel());
            }
        } catch (final Exception exception) {
            final CrashReport crashReport = CrashReport.forThrowable(exception, "Biome decoration");
            crashReport.addCategory("Generation")
                .setDetail("CenterX", pos.x)
                .setDetail("CenterZ", pos.z)
                .setDetail("Decoration Seed", decorationSeed);
            throw new ReportedException(crashReport);
        }
    }
}
