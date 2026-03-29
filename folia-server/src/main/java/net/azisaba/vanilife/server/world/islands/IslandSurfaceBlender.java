package net.azisaba.vanilife.server.world.islands;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class IslandSurfaceBlender {
    public static final int SURFACE_LAYER_THICKNESS = 3;

    private final IslandsGeneratorSettings settings;

    public IslandSurfaceBlender(final IslandsGeneratorSettings settings) {
        this.settings = settings;
    }

    public BlockState landBlockStateAtY(
        final double signedDistance,
        final double cornerInfluence,
        final double beachTransitionNoise,
        final double beachBlendNoise,
        final int highestY,
        final double riverStrength,
        final double riverBankThreshold,
        final int y
    ) {
        final double beachBand = Math.max(1.0, this.settings.beachWidth() - 1.0) + cornerInfluence * 1.5;
        final double pureBeachBand = Math.max(0.0, beachBand - 6.0);
        final double mixedBandWidth = Math.max(18.0, this.settings.beachWidth() * 3.2);
        final boolean riverBank = riverStrength > riverBankThreshold;
        final boolean beachCore = signedDistance > -pureBeachBand || highestY <= this.settings.seaLevel() || riverBank;
        final double beachMixProgress = this.computeBeachMixProgress(signedDistance, beachBand, mixedBandWidth);

        if (y < highestY - SURFACE_LAYER_THICKNESS) {
            return Blocks.STONE.defaultBlockState();
        }
        if (y < highestY) {
            if (beachCore || this.shouldPlaceSandBelowSurface(beachMixProgress, beachTransitionNoise, beachBlendNoise, riverBank)) {
                return Blocks.SAND.defaultBlockState();
            }
            return Blocks.DIRT.defaultBlockState();
        }
        if (y == highestY) {
            final boolean placeSand = beachCore
                || this.shouldPlaceSandOnSurface(beachMixProgress, beachTransitionNoise, beachBlendNoise, riverBank);
            if (placeSand) {
                return Blocks.SAND.defaultBlockState();
            }
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private double computeBeachMixProgress(final double signedDistance, final double beachBand, final double mixedBandWidth) {
        final double inlandDistance = Math.max(0.0, -signedDistance);
        final double bandStart = Math.max(0.0, beachBand - mixedBandWidth * 0.5);
        return Mth.smoothstep(Mth.clamp((inlandDistance - bandStart) / mixedBandWidth, 0.0, 1.0));
    }

    private boolean shouldPlaceSandOnSurface(
        final double beachMixProgress,
        final double transitionNoise,
        final double beachBlendNoise,
        final boolean riverBank
    ) {
        if (riverBank || beachMixProgress <= 0.0) {
            return true;
        }
        if (beachMixProgress >= 1.0) {
            return false;
        }

        final double coarse = Mth.clamp((transitionNoise + 1.0) * 0.5, 0.0, 1.0);
        final double fine = Mth.clamp((beachBlendNoise + 1.0) * 0.5, 0.0, 1.0);
        final double mixedNoise = Mth.clamp(coarse * 0.3 + fine * 0.7, 0.0, 1.0);
        final double sandChance = Mth.lerp(beachMixProgress, 0.58, 0.24);
        final double threshold = Mth.clamp(sandChance + (coarse - 0.5) * 0.04, 0.0, 1.0);
        return mixedNoise < threshold;
    }

    public BlockState oceanBlockStateAtY(final int highestY, final int y) {
        if (y <= highestY) {
            final int depthBelowSeaLevel = Math.max(0, this.settings.seaLevel() - highestY);
            final double stoneProgress = Mth.smoothstep(Mth.clamp((depthBelowSeaLevel - 2.0) / 10.0, 0.0, 1.0));
            if (y < highestY - SURFACE_LAYER_THICKNESS) {
                return this.selectOceanStoneState(stoneProgress, true);
            }
            return this.selectOceanFloorState(highestY, y, stoneProgress);
        }
        if (y <= this.settings.seaLevel()) {
            return Blocks.WATER.defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private boolean shouldPlaceSandBelowSurface(
        final double beachMixProgress,
        final double transitionNoise,
        final double beachBlendNoise,
        final boolean riverBank
    ) {
        if (riverBank || beachMixProgress <= 0.0) {
            return true;
        }
        if (beachMixProgress >= 1.0) {
            return false;
        }

        final double coarse = Mth.clamp((transitionNoise + 1.0) * 0.5, 0.0, 1.0);
        final double fine = Mth.clamp((beachBlendNoise + 1.0) * 0.5, 0.0, 1.0);
        final double mixedNoise = Mth.clamp(coarse * 0.25 + fine * 0.75, 0.0, 1.0);
        final double sandChance = Mth.lerp(beachMixProgress, 0.76, 0.32);
        final double threshold = Mth.clamp(sandChance + (coarse - 0.5) * 0.04, 0.0, 1.0);
        return mixedNoise < threshold;
    }

    private BlockState selectOceanFloorState(final int highestY, final int y, final double stoneProgress) {
        final double layerProgress = Mth.clamp((highestY - y) / (double) SURFACE_LAYER_THICKNESS, 0.0, 1.0);
        final double effectiveStoneProgress = Mth.clamp(stoneProgress * 0.8 + layerProgress * 0.2, 0.0, 1.0);
        if (effectiveStoneProgress < 0.28) {
            return Blocks.SAND.defaultBlockState();
        }
        if (effectiveStoneProgress < 0.52) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (effectiveStoneProgress < 0.78) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        return this.selectOceanStoneState(effectiveStoneProgress, false);
    }

    private BlockState selectOceanStoneState(final double stoneProgress, final boolean deepLayer) {
        if (stoneProgress < 0.72) {
            return Blocks.STONE.defaultBlockState();
        }
        if (stoneProgress < 0.9) {
            return deepLayer ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
        return Blocks.ANDESITE.defaultBlockState();
    }
}
