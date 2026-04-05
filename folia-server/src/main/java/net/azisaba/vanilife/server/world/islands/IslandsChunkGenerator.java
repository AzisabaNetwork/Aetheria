package net.azisaba.vanilife.server.world.islands;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.azisaba.vanilife.server.world.height.HeightContext;
import net.azisaba.vanilife.server.world.islands.IslandTerrainSampler.TerrainSample;
import net.azisaba.vanilife.world.IslandsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
public final class IslandsChunkGenerator extends ChunkGenerator {
    public static final MapCodec<IslandsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
                IslandsGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings),
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            )
            .apply(instance, IslandsChunkGenerator::new)
    );

    private final IslandsGeneratorSettings settings;
    private final IslandTerrainSampler terrainSampler;
    private final IslandSurfaceBlender surfaceBlender;
    private final IslandReefDecorator reefDecorator;

    public IslandsChunkGenerator(final IslandsGeneratorSettings settings, final BiomeSource biomeSource) {
        super(biomeSource);
        this.settings = settings;
        this.terrainSampler = new IslandTerrainSampler(settings);
        this.surfaceBlender = new IslandSurfaceBlender(settings);
        this.reefDecorator = new IslandReefDecorator(settings, this.terrainSampler::sample);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getMinY() {
        return this.settings.minY();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.seaLevel();
    }

    @Override
    public int getGenDepth() {
        return DimensionDefaults.OVERWORLD_GENERATION_HEIGHT;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
        final Blender blender,
        final RandomState randomState,
        final StructureManager structureManager,
        final ChunkAccess chunk,
        final HeightContext heightContext
    ) {
        final long levelSeed = structureManager.level.getMinecraftWorld().getSeed();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = 0; dx < 16; dx++) {
            final int blockX = chunk.getPos().getMinBlockX() + dx;
            for (int dz = 0; dz < 16; dz++) {
                final int blockZ = chunk.getPos().getMinBlockZ() + dz;
                this.fillTerrainColumn(chunk, pos, levelSeed, blockX, blockZ);
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(
        final WorldGenRegion region,
        final StructureManager structureManager,
        final RandomState random,
        final ChunkAccess chunk,
        final HeightContext heightContext
    ) {
        this.reefDecorator.buildSurface(region, chunk.getPos());
    }

    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level, final RandomState random) {
        final TerrainSample sample = this.terrainSampler.sample(0L, x, z);
        final int highestY = sample.highestY();
        if (highestY > this.settings.seaLevel()) {
            return highestY + 1;
        }
        return switch (type) {
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> highestY + 1;
            default -> this.settings.seaLevel() + 1;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final RandomState random) {
        final TerrainSample sample = this.terrainSampler.sample(0L, x, z);
        final int minY = height.getMinY();
        final int maxY = height.getMaxY();
        final BlockState[] column = new BlockState[height.getHeight()];

        for (int y = minY; y < maxY; y++) {
            column[y - minY] = this.blockStateAtY(sample, y, minY);
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
    }

    @Override
    public void createStructures(
        final RegistryAccess registryAccess,
        final ChunkGeneratorStructureState structureState,
        final StructureManager structureManager,
        final ChunkAccess chunk,
        final StructureTemplateManager structureTemplateManager,
        final ResourceKey<Level> level,
        final HeightContext heightContext
    ) {
    }

    @Override
    public void spawnOriginalMobs(final WorldGenRegion region) {
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }

    private void fillTerrainColumn(
        final ChunkAccess chunk, final BlockPos.MutableBlockPos pos, final long levelSeed, final int blockX, final int blockZ
    ) {
        final TerrainSample sample = this.terrainSampler.sample(levelSeed, blockX, blockZ);
        final int minY = chunk.getMinY();
        pos.set(blockX, minY, blockZ);
        for (int y = minY; y < this.settings.airTopY(); y++) {
            pos.setY(y);
            chunk.setBlockState(pos, this.blockStateAtY(sample, y, minY), Block.UPDATE_NONE);
        }
    }

    private BlockState blockStateAtY(final TerrainSample sample, final int y, final int minY) {
        if (y == minY) {
            return Blocks.BEDROCK.defaultBlockState();
        }
        if (this.isRiverWater(sample, y)) {
            return Blocks.WATER.defaultBlockState();
        }
        return sample.highestY() > this.settings.seaLevel() ? this.surfaceBlender.landBlockStateAtY(
            sample.signedDistance(),
            sample.cornerInfluence(),
            sample.beachTransitionNoise(),
            sample.beachBlendNoise(),
            sample.highestY(),
            sample.riverStrength(),
            IslandTerrainSampler.RIVER_BANK_THRESHOLD,
            sample.spawnPointDistance(),
            y
        ) : this.surfaceBlender.oceanBlockStateAtY(sample.highestY(), y);
    }

    private boolean isRiverWater(final TerrainSample sample, final int y) {
        return sample.riverStrength() > 0.0 && y > sample.highestY() && y <= sample.riverWaterY();
    }
}
