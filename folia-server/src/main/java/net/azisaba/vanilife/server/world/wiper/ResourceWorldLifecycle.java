package net.azisaba.vanilife.server.world.wiper;

import io.papermc.paper.threadedregions.RegionizedServer;
import io.papermc.paper.world.PaperWorldLoader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import net.azisaba.vanilife.Vanilife;
import net.azisaba.vanilife.server.VanilifeLevelStems;
import net.azisaba.vanilife.world.ResourceWorld;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * NMS-level helper for resource world hot-swap on Folia.
 *
 * <p>Because Folia's {@code CraftServer.unloadWorld()} throws
 * {@link UnsupportedOperationException}, this class interacts directly with
 * {@link MinecraftServer}, {@link RegionizedServer}, and the Moonrise chunk
 * system to close, delete, and recreate the resource dimension at runtime.</p>
 *
 * <p>All public methods document their threading requirements.</p>
 */
@NullMarked
public final class ResourceWorldLifecycle {
    private static final Logger LOGGER = Logger.getLogger("Vanilife/ResourceWorldLifecycle");

    /** World name used by PaperWorldLoader for the resource dimension. */
    public static final String WORLD_NAME = "world_vanilife_resource";

    private static final Field REGIONIZED_WORLDS_FIELD;
    private static final Field CRAFT_WORLDS_FIELD;

    static {
        try {
            REGIONIZED_WORLDS_FIELD = RegionizedServer.class.getDeclaredField("worlds");
            REGIONIZED_WORLDS_FIELD.setAccessible(true);
            CRAFT_WORLDS_FIELD = CraftServer.class.getDeclaredField("worlds");
            CRAFT_WORLDS_FIELD.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ResourceWorldLifecycle() {
    }

    /**
     * Teleports every player currently in the resource world to the overworld spawn.
     *
     * <p>Must be called from a Folia region thread (global or owned).
     * Returns a future that completes when all teleports have resolved.</p>
     *
     * @return future completing when all players have left
     */
    public static CompletableFuture<Void> drainPlayers() {
        final ResourceWorld resourceWorld = Vanilife.getResourceWorldOrNull();
        if (resourceWorld == null) {
            return CompletableFuture.completedFuture(null);
        }

        final World overworld = Bukkit.getWorlds().getFirst();
        final Location spawn = overworld.getSpawnLocation();
        final List<Player> players = ((World) resourceWorld).getPlayers();

        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        @SuppressWarnings("unchecked")
        final CompletableFuture<Boolean>[] futures = players.stream()
            .map(p -> p.teleportAsync(spawn))
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * Shuts down the resource world's chunk system, removes it from all
     * registries, and closes its storage.
     *
     * <p>Must be called from a {@link ca.spottedleaf.moonrise.common.util.TickThread}
     * (the caller in {@link WorldWiper} creates a dedicated thread).</p>
     *
     * @param serverLevel the resource ServerLevel to close
     * @throws IOException if storage close fails
     */
    public static void closeWorld(final ServerLevel serverLevel) throws IOException {
        Objects.requireNonNull(serverLevel, "serverLevel");
        final MinecraftServer server = serverLevel.getServer();

        LOGGER.info("Removing resource world from tick loop");
        removeFromRegionizedServer(serverLevel);

        LOGGER.info("Halting chunk system (soft)");
        serverLevel.moonrise$getChunkTaskScheduler().halt(false, 0L);

        LOGGER.info("Halting chunk system (hard + save)");
        serverLevel.moonrise$getChunkTaskScheduler().chunkHolderManager.close(
            true,  // save
            true,  // halt
            true,  // first
            true,  // last
            false  // checkRegions
        );

        LOGGER.info("Saving level data");
        serverLevel.saveLevelData(true);

        LOGGER.info("Removing from MinecraftServer.levels");
        server.removeLevel(serverLevel);

        LOGGER.info("Removing from CraftServer.worlds");
        removeFromCraftServer((CraftServer) server.server);

        LOGGER.info("Closing storage access");
        serverLevel.levelStorageAccess.close();

        LOGGER.info("Resource world closed successfully");
    }

    /**
     * Deletes the resource world folder from disk.
     *
     * <p>Safe to call from any thread. The folder is at
     * {@code <world-container>/dimensions/vanilife/resource}.</p>
     *
     * @throws IOException if deletion fails
     */
    public static void deleteWorldFolder() throws IOException {
        final Path worldContainer = MinecraftServer.getServer().server
            .getWorldContainer().toPath();
        final Path worldFolder = worldContainer.resolve("dimensions")
            .resolve("vanilife").resolve("resource");

        if (!Files.exists(worldFolder)) {
            LOGGER.info("World folder does not exist, nothing to delete: " + worldFolder);
            return;
        }

        LOGGER.info("Deleting world folder: " + worldFolder);
        Files.walkFileTree(worldFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        LOGGER.info("World folder deleted");
    }

    /**
     * Creates a fresh resource world using the same sequence as
     * {@link PaperWorldLoader#loadInitialWorlds()}.
     *
     * <p>Must be called on the server main thread (or a thread safe for NMS
     * world creation). After this call the world is fully loaded, registered
     * in all maps, and the {@code WorldLoadEvent} has fired.</p>
     *
     * @return the newly created ServerLevel
     * @throws IOException if storage access fails
     */
    public static ServerLevel createFreshWorld() throws IOException, ContentValidationException {
        final MinecraftServer server = MinecraftServer.getServer();
        final DedicatedServer dedicatedServer = (DedicatedServer) server;

        final Registry<LevelStem> stemRegistry = server.worldLoaderContext
            .datapackDimensions()
            .lookupOrThrow(Registries.LEVEL_STEM);
        final LevelStem stem = stemRegistry.getValueOrThrow(VanilifeLevelStems.RESOURCE);

        final Path worldContainer = server.server.getWorldContainer().toPath();
        final LevelStorageSource storageSource = LevelStorageSource.createDefault(worldContainer);
        final LevelStorageSource.LevelStorageAccess access =
            storageSource.validateAndCreateAccess(WORLD_NAME, VanilifeLevelStems.RESOURCE);

        final PrimaryLevelData levelData = (PrimaryLevelData) Main.createNewWorldData(
            dedicatedServer.settings,
            server.worldLoaderContext,
            stemRegistry,
            false, // demo
            false  // bonusChest
        ).cookie();

        final PaperWorldLoader.WorldLoadingInfo loadingInfo = new PaperWorldLoader.WorldLoadingInfo(
            -999,                         // dimension (custom)
            WORLD_NAME,                   // name
            "vanilife_resource",           // worldType
            VanilifeLevelStems.RESOURCE,   // stemKey
            true                          // enabled
        );

        LOGGER.info("Creating fresh resource ServerLevel");
        server.createLevel(stem, loadingInfo, access, levelData);

        final ServerLevel newLevel = server.getLevel(
            ResourceKey.create(
                Registries.DIMENSION,
                VanilifeLevelStems.RESOURCE.identifier()
            )
        );
        Objects.requireNonNull(newLevel, "ServerLevel was not registered after createLevel");

        LOGGER.info("Preparing level (initial chunks + RegionizedServer registration)");
        server.prepareLevel(newLevel);

        LOGGER.info("Resource world created and ready: " + WORLD_NAME);
        return newLevel;
    }

    @SuppressWarnings("unchecked")
    private static void removeFromRegionizedServer(final ServerLevel level) {
        try {
            final CopyOnWriteArrayList<ServerLevel> worlds =
                (CopyOnWriteArrayList<ServerLevel>) REGIONIZED_WORLDS_FIELD.get(RegionizedServer.getInstance());
            worlds.remove(level);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed to access RegionizedServer.worlds", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void removeFromCraftServer(final CraftServer craftServer) {
        try {
            final Map<String, World> worlds =
                (Map<String, World>) CRAFT_WORLDS_FIELD.get(craftServer);
            worlds.remove(WORLD_NAME.toLowerCase(Locale.ROOT));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed to access CraftServer.worlds", e);
        }
    }
}
