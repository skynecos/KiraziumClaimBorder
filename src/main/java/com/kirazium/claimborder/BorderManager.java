package com.kirazium.claimborder;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.Types.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class BorderManager {

    private static final float CHUNK_SIZE = 16.0F;

    private final KiraziumClaimBorder plugin;
    private final SimpleClaimSystemAPI api;
    private final Map<UUID, List<UUID>> activeDisplays = new HashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

    BorderManager(KiraziumClaimBorder plugin, SimpleClaimSystemAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    void show(Player player) {
        clear(player.getUniqueId());

        final Chunk currentChunk = player.getLocation().getChunk();
        final Claim claim = api.getClaimAtChunk(currentChunk);

        final Set<Chunk> chunks = new HashSet<>();
        if (claim == null) {
            chunks.add(currentChunk);
        } else {
            for (Chunk chunk : claim.getChunks()) {
                if (chunk.getWorld().equals(player.getWorld())) {
                    chunks.add(chunk);
                }
            }
        }

        if (chunks.isEmpty()) {
            chunks.add(currentChunk);
        }

        final Material material = getWallMaterial();
        final float height = (float) Math.max(0.25D, plugin.getConfig().getDouble("wall.height", 4.0D));
        final float thickness = (float) Math.max(0.01D,
                Math.min(1.0D, plugin.getConfig().getDouble("wall.thickness", 0.04D)));
        final long durationTicks = Math.max(20L, plugin.getConfig().getLong("wall.duration-ticks", 100L));
        final double yOffset = plugin.getConfig().getDouble("wall.y-offset", -1.0D);
        final int brightness = clamp(plugin.getConfig().getInt("wall.brightness", 15), 0, 15);
        final double baseY = player.getLocation().getY() + yOffset;

        final Set<Long> chunkKeys = new HashSet<>();
        for (Chunk chunk : chunks) {
            chunkKeys.add(key(chunk.getX(), chunk.getZ()));
        }

        final List<UUID> displays = new ArrayList<>();

        for (Chunk chunk : chunks) {
            final int chunkX = chunk.getX();
            final int chunkZ = chunk.getZ();
            final int minX = chunkX << 4;
            final int minZ = chunkZ << 4;

            if (!chunkKeys.contains(key(chunkX, chunkZ - 1))) {
                displays.add(spawnWall(
                        player,
                        new Location(player.getWorld(), minX, baseY, minZ - (thickness / 2.0F)),
                        CHUNK_SIZE, height, thickness, material, brightness
                ));
            }

            if (!chunkKeys.contains(key(chunkX, chunkZ + 1))) {
                displays.add(spawnWall(
                        player,
                        new Location(player.getWorld(), minX, baseY, minZ + CHUNK_SIZE - (thickness / 2.0F)),
                        CHUNK_SIZE, height, thickness, material, brightness
                ));
            }

            if (!chunkKeys.contains(key(chunkX - 1, chunkZ))) {
                displays.add(spawnWall(
                        player,
                        new Location(player.getWorld(), minX - (thickness / 2.0F), baseY, minZ),
                        thickness, height, CHUNK_SIZE, material, brightness
                ));
            }

            if (!chunkKeys.contains(key(chunkX + 1, chunkZ))) {
                displays.add(spawnWall(
                        player,
                        new Location(player.getWorld(), minX + CHUNK_SIZE - (thickness / 2.0F), baseY, minZ),
                        thickness, height, CHUNK_SIZE, material, brightness
                ));
            }
        }

        activeDisplays.put(player.getUniqueId(), displays);

        final BukkitTask expiry = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> clear(player.getUniqueId()),
                durationTicks
        );
        expiryTasks.put(player.getUniqueId(), expiry);
    }

    private UUID spawnWall(
            Player viewer,
            Location location,
            float scaleX,
            float scaleY,
            float scaleZ,
            Material material,
            int brightness
    ) {
        final World world = location.getWorld();
        if (world == null) {
            throw new IllegalStateException("Cannot spawn claim border without a world.");
        }

        final BlockDisplay display = world.spawn(location, BlockDisplay.class, entity -> {
            entity.setBlock(material.createBlockData());
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setPersistent(false);
            entity.setVisibleByDefault(false);
            entity.setShadowRadius(0.0F);
            entity.setShadowStrength(0.0F);
            entity.setBrightness(new Display.Brightness(brightness, brightness));
            entity.setTransformation(new Transformation(
                    new Vector3f(0.0F, 0.0F, 0.0F),
                    new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F),
                    new Vector3f(scaleX, scaleY, scaleZ),
                    new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)
            ));
        });

        viewer.showEntity(plugin, display);
        return display.getUniqueId();
    }

    void clear(UUID playerId) {
        final BukkitTask task = expiryTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }

        final List<UUID> displays = activeDisplays.remove(playerId);
        if (displays == null) {
            return;
        }

        for (UUID entityId : displays) {
            final Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
    }

    void clearAll() {
        final Set<UUID> players = new HashSet<>(activeDisplays.keySet());
        for (UUID playerId : players) {
            clear(playerId);
        }

        for (BukkitTask task : expiryTasks.values()) {
            task.cancel();
        }
        expiryTasks.clear();
    }

    private Material getWallMaterial() {
        final String configured = plugin.getConfig().getString("wall.material", "PURPLE_STAINED_GLASS");
        final Material material = configured == null ? null : Material.matchMaterial(configured);

        if (material == null || !material.isBlock() || material.isAir()) {
            plugin.getLogger().warning(
                    "Invalid wall.material '" + configured + "'. Falling back to PURPLE_STAINED_GLASS."
            );
            return Material.PURPLE_STAINED_GLASS;
        }

        return material;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
