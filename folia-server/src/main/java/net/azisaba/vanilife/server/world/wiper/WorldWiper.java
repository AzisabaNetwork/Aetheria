package net.azisaba.vanilife.server.world.wiper;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.azisaba.vanilife.Season;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Orchestrates resource world hot-swap when the season changes.
 *
 * <p>Polls once per hour. When the current {@link Season} differs from the
 * cached value, drives the full lifecycle:
 * drain players → close world → delete files → create fresh world.</p>
 *
 * <p>Thread model:
 * <ul>
 *   <li>Season check runs on a daemon scheduler thread.</li>
 *   <li>Player drain runs on Folia's global region.</li>
 *   <li>World close runs on a dedicated {@link TickThread}.</li>
 *   <li>Folder deletion runs on the daemon scheduler.</li>
 *   <li>World creation runs on Folia's global region.</li>
 * </ul></p>
 *
 * @see ResourceWorldLifecycle
 * @see WorldLifecycleState
 */
@NullMarked
public final class WorldWiper {
    private static final Logger LOGGER = Logger.getLogger("Vanilife/WorldWiper");

    private static final ScheduledExecutorService SCHEDULER =
        Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "WorldWiper-Scheduler");
            t.setDaemon(true);
            return t;
        });

    private static final AtomicReference<WorldLifecycleState> STATE =
        new AtomicReference<>(WorldLifecycleState.ACTIVE);

    private static volatile @Nullable Season currentSeason = null;

    private WorldWiper() {
    }

    public static WorldLifecycleState getState() {
        return STATE.get();
    }

    /**
     * Registers the hourly season-check task. Safe to call from server
     * bootstrap code (e.g. after {@code ServerLoadEvent}).
     *
     * <p>Uses a daemon {@link ScheduledExecutorService} for polling so no
     * {@link Plugin} instance is required at registration time.</p>
     */
    public static void registerTask() {
        currentSeason = Season.now();
        LOGGER.info("Season check task registered — current season: " + currentSeason);

        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                final Season now = Season.now();
                if (now != currentSeason) {
                    executeSwap(now);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Error in season check", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Force-triggers a resource world swap to the given season.
     *
     * <p>Useful for admin commands. Returns immediately if a swap is
     * already in progress. Safe to call from any thread.</p>
     *
     * @param targetSeason season to swap to
     * @return {@code true} if the swap was initiated, {@code false} if one
     *         is already in progress
     */
    public static boolean forceSwap(final Season targetSeason) {
        return executeSwap(targetSeason);
    }

    private static Plugin requirePlugin() {
        final Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        if (plugins.length == 0) {
            throw new IllegalStateException("No plugins loaded — cannot schedule Folia tasks");
        }
        return plugins[0];
    }

    private static boolean executeSwap(final Season targetSeason) {
        if (!STATE.compareAndSet(WorldLifecycleState.ACTIVE, WorldLifecycleState.DRAINING)) {
            LOGGER.warning("Swap requested but state is " + STATE.get() + " — ignoring");
            return false;
        }

        final Season oldSeason = currentSeason;
        currentSeason = targetSeason;
        LOGGER.info("Season change: " + oldSeason + " → " + targetSeason);

        Bukkit.getPluginManager().callEvent(new PreSeasonChangeEvent(oldSeason, targetSeason));

        final Plugin plugin = requirePlugin();
        Bukkit.getGlobalRegionScheduler().execute(plugin, () ->
            drainAndClose(plugin, oldSeason, targetSeason)
        );
        return true;
    }

    private static void drainAndClose(
        final Plugin plugin,
        final @Nullable Season oldSeason,
        final Season newSeason
    ) {
        LOGGER.info("Draining players from resource world");
        ResourceWorldLifecycle.drainPlayers().whenComplete((ignored, drainErr) -> {
            if (drainErr != null) {
                LOGGER.log(Level.SEVERE, "Failed to drain players — aborting swap", drainErr);
                STATE.set(WorldLifecycleState.ACTIVE);
                return;
            }

            if (!STATE.compareAndSet(WorldLifecycleState.DRAINING, WorldLifecycleState.CLOSING)) {
                LOGGER.warning("Unexpected state after drain: " + STATE.get());
                return;
            }

            final ServerLevel level = resolveResourceLevel();
            if (level == null) {
                LOGGER.warning("No resource ServerLevel found — skipping close, proceeding to create");
                STATE.set(WorldLifecycleState.DELETED);
                deleteAndRecreate(plugin, oldSeason, newSeason);
                return;
            }

            closeOnTickThread(plugin, level, oldSeason, newSeason);
        });
    }

    private static void closeOnTickThread(
        final Plugin plugin,
        final ServerLevel level,
        final @Nullable Season oldSeason,
        final Season newSeason
    ) {
        final Thread closeThread = new TickThread("ResourceWorldClose") {
            @Override
            public void run() {
                try {
                    ResourceWorldLifecycle.closeWorld(level);
                    STATE.set(WorldLifecycleState.DELETED);
                    deleteAndRecreate(plugin, oldSeason, newSeason);
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to close resource world", e);
                    STATE.set(WorldLifecycleState.ACTIVE);
                }
            }
        };
        closeThread.setDaemon(true);
        closeThread.start();
    }

    private static void deleteAndRecreate(
        final Plugin plugin,
        final @Nullable Season oldSeason,
        final Season newSeason
    ) {
        SCHEDULER.schedule(() -> {
            try {
                ResourceWorldLifecycle.deleteWorldFolder();
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to delete world folder", e);
                STATE.set(WorldLifecycleState.ACTIVE);
                return;
            }

            if (!STATE.compareAndSet(WorldLifecycleState.DELETED, WorldLifecycleState.CREATING)) {
                LOGGER.warning("Unexpected state after delete: " + STATE.get());
                return;
            }

            Bukkit.getGlobalRegionScheduler().execute(plugin, () ->
                createWorld(oldSeason, newSeason)
            );
        }, 1, TimeUnit.SECONDS);
    }

    private static void createWorld(
        final @Nullable Season oldSeason,
        final Season newSeason
    ) {
        try {
            ResourceWorldLifecycle.createFreshWorld();
            STATE.set(WorldLifecycleState.ACTIVE);

            LOGGER.info("Resource world swap complete — new season: " + newSeason);
            Bukkit.getPluginManager().callEvent(new PostSeasonChangeEvent(oldSeason, newSeason));
        } catch (final IOException | ContentValidationException e) {
            LOGGER.log(Level.SEVERE, "Failed to create fresh resource world", e);
            STATE.set(WorldLifecycleState.ACTIVE);
        }
    }

    private static @Nullable ServerLevel resolveResourceLevel() {
        for (final ServerLevel level : net.minecraft.server.MinecraftServer.getServer().getAllLevels()) {
            if (net.azisaba.vanilife.server.VanilifeLevelStems.isResourceLevel(level.dimension())) {
                return level;
            }
        }
        return null;
    }
}
