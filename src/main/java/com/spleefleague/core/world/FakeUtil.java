package com.spleefleague.core.world;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.spleefleague.core.Core;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.MathUtils;
import com.spleefleague.core.util.variable.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author NickM13
 * @since 4/26/2020
 */
public class FakeUtil {

    public static boolean isInstantBreak(CorePlayer corePlayer, Material material) {
        return true;
    }

    public static boolean isOnGround(CorePlayer corePlayer) {
        Player player = corePlayer.getPlayer();
        // Only consider the player as standing if their Y velocity is less than or equal to 0
        if (player.getVelocity().getY() > 0) {
            return false;
        }

        Location playerLocation = player.getLocation().clone();
        playerLocation.setY(playerLocation.getY() - 1); // Check block below the player

        Location[] blockLocations = new Location[]{
                playerLocation.clone().add(0.3, 0, 0.3).getBlock().getLocation(),
                playerLocation.clone().add(-0.3, 0, 0.3).getBlock().getLocation(),
                playerLocation.clone().add(0.3, 0, -0.3).getBlock().getLocation(),
                playerLocation.clone().add(-0.3, 0, -0.3).getBlock().getLocation()
        };

        for (Location blockLocation : blockLocations) {
            Block block = blockLocation.getBlock();
            Material material = block.getType();
            if (material.isSolid()) {
                return true;
            }

            Iterator<FakeWorld<?>> fit = corePlayer.getFakeWorlds();
            while (fit.hasNext()) {
                FakeWorld<?> fakeWorld = fit.next();
                FakeBlock fakeBlock = fakeWorld.getFakeBlock(blockLocation);
                if (fakeBlock != null) {
                    if (fakeBlock.blockData().getMaterial().isSolid()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static BlockPosition getHighestFakeBlockBelow(CorePlayer cp) {
        BlockPosition pos = new BlockPosition(cp.getLocation().getBlockX(), cp.getLocation().getBlockY() - 1, cp.getLocation().getBlockZ());
        while (pos.getY() >= 0) {
            Iterator<FakeWorld<?>> fit = cp.getFakeWorlds();
            while (fit.hasNext()) {
                FakeWorld<?> fakeWorld = fit.next();
                FakeBlock fakeBlock = fakeWorld.getFakeBlock(pos);
                if (fakeBlock != null && !fakeBlock.blockData().getMaterial().isAir()) {
                    return pos;
                }
            }
            pos = pos.subtract(new BlockPosition(0, 1, 0));
        }
        return pos;
    }

    public static BlockPosition getHighestBlockBelow(CorePlayer cp) {
        BlockPosition pos = new BlockPosition(cp.getLocation().getBlockX(), cp.getLocation().getBlockY() - 1, cp.getLocation().getBlockZ());
        while (pos.getY() >= 0) {
            if (!cp.getPlayer().getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType().isAir()) {
                return pos;
            }
            Iterator<FakeWorld<?>> fit = cp.getFakeWorlds();
            while (fit.hasNext()) {
                FakeWorld<?> fakeWorld = fit.next();
                FakeBlock fb = fakeWorld.getFakeBlock(pos);
                if (fb != null && !fb.blockData().getMaterial().isAir()) {
                    return pos;
                }
            }
            pos = pos.subtract(new BlockPosition(0, 1, 0));
        }
        return pos;
    }

    public static Map<BlockPosition, FakeBlock> mergeBlocks(List<Map<BlockPosition, FakeBlock>> blockMaps) {
        Map<BlockPosition, FakeBlock> mergedBlocks = new HashMap<>();
        for (Map<BlockPosition, FakeBlock> blockMap : blockMaps) {
            mergedBlocks.putAll(blockMap);
        }
        return mergedBlocks;
    }

    public static Set<BlockPosition> translateBlocks(Set<BlockPosition> blocks, BlockPosition translation) {
        Set<BlockPosition> transformedBlocks = new HashSet<>();
        for (BlockPosition pos : blocks) {
            transformedBlocks.add(pos.add(translation));
        }
        return transformedBlocks;
    }

    public static Map<BlockPosition, FakeBlock> translateBlocks(Map<BlockPosition, FakeBlock> blocks, BlockPosition translation) {
        Map<BlockPosition, FakeBlock> transformedBlocks = new HashMap<>();
        for (Map.Entry<BlockPosition, FakeBlock> entry : blocks.entrySet()) {
            transformedBlocks.put(entry.getKey().add(translation), entry.getValue());
        }
        return transformedBlocks;
    }

    public static Map<BlockPosition, FakeBlock> rotateBlocks(Map<BlockPosition, FakeBlock> blocks, int degrees) {
        double radians = Math.toRadians(degrees);
        Map<BlockPosition, FakeBlock> transformedBlocks = new HashMap<>();
        for (Map.Entry<BlockPosition, FakeBlock> entry : blocks.entrySet()) {
            BlockPosition newPos = new BlockPosition(
                    (int) (entry.getKey().getX() * MathUtils.cos(radians, 4) - entry.getKey().getZ() * MathUtils.sin(radians, 4)),
                    entry.getKey().getY(),
                    (int) (entry.getKey().getX() * MathUtils.sin(radians, 4) + entry.getKey().getZ() * MathUtils.cos(radians, 4)));
            transformedBlocks.put(newPos, entry.getValue());
        }
        return transformedBlocks;
    }

    public static Map<BlockPosition, FakeBlock> transformBlocks(Map<BlockPosition, FakeBlock> blocks, Position transform) {
        return FakeUtil.translateBlocks(FakeUtil.rotateBlocks(blocks, (int) transform.getYaw()), transform.toBlockPosition());
    }

    public static Set<BlockPosition> createCylinderShell(double radius, int height) {
        Set<BlockPosition> blocks = new HashSet<>();
        int lastMove1 = -1;
        int lastMove2 = -1;
        int prevX = -1;
        int prevZ = -1;
        for (double d = 0; d < Math.PI / 2D; d += Math.PI / 90) {
            int x = (int) (Math.cos(d) * radius + 0.25);
            int z = (int) (Math.sin(d) * radius + 0.25);
            if (x == prevX && z == prevZ) continue;
            if ((x != prevX && (lastMove1 != 0 || lastMove2 == 0)) || (z != prevZ && (lastMove1 != 1 || lastMove2 == 1))) {
                for (int i = 0; i < 4; i++) {
                    Vector vec = new Vector(Math.cos(d + i * Math.PI / 2D), 0, Math.sin(d + i * Math.PI / 2D)).multiply(radius).add(new Vector(0.5, 0, 0.5));
                    for (int h = 0; h < height; h++) {
                        blocks.add(new BlockPosition(vec.getBlockX(), h, vec.getBlockZ()));
                    }
                }
            }
            lastMove2 = lastMove1;
            if (prevX != x) {
                lastMove1 = 0;
            } else {
                lastMove1 = 1;
            }
            prevX = x;
            prevZ = z;
        }
        return blocks;
    }

    public static Set<BlockPosition> createSphere(double radius) {
        Set<BlockPosition> blocks = new HashSet<>();
        double dx, dy, dz;
        for (int x = -(int) Math.ceil(radius); x <= (int) Math.ceil(radius); x++) {
            dx = ((double) x) / radius;
            for (int y = -(int) Math.ceil(radius); y <= (int) Math.ceil(radius); y++) {
                dy = ((double) y) / radius;
                for (int z = -(int) Math.ceil(radius); z <= (int) Math.ceil(radius); z++) {
                    dz = ((double) z) / radius;
                    if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 1) {
                        blocks.add(new BlockPosition(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }

    private static BoundingBox computeDiscAABB(Vector center, Vector normal, double radius) {
        Vector e1 = (new Vector(1., 1., 1.).subtract(normal.clone().multiply(normal.clone())));
        Vector e = new Vector(
                e1.getX() > 0.001 ? Math.sqrt(e1.getX()) : 0,
                e1.getY() > 0.001 ? Math.sqrt(e1.getY()) : 0,
                e1.getZ() > 0.001 ? Math.sqrt(e1.getZ()) : 0).multiply(radius);
        return new BoundingBox(
                center.getX() - e.getX(),
                center.getY() - e.getY(),
                center.getZ() - e.getZ(),
                center.getX() + e.getX(),
                center.getY() + e.getY(),
                center.getZ() + e.getZ());
    }

    public static Set<BlockPosition> createCone(Vector dir, double distance, double radius) {
        dir = dir.clone().normalize();
        Set<BlockPosition> blocks = new HashSet<>();

        BoundingBox discBoundingBox = computeDiscAABB(dir.clone().multiply(distance), dir.clone(), radius);
        BoundingBox coneBoundingBox = discBoundingBox.union(new Vector(0, 0, 0));

        for (int x = (int) Math.floor(coneBoundingBox.getMinX()); x <= (int) Math.ceil(coneBoundingBox.getMaxX()); x++) {
            for (int y = (int) Math.floor(coneBoundingBox.getMinY()); y <= (int) Math.ceil(coneBoundingBox.getMaxY()); y++) {
                for (int z = (int) Math.floor(coneBoundingBox.getMinZ()); z <= (int) Math.ceil(coneBoundingBox.getMaxZ()); z++) {
                    Vector p = new Vector(x, y, z);
                    double coneDist = p.clone().dot(dir);
                    if (coneDist >= 0 && coneDist <= distance) {
                        double orthoDist = (p.clone().subtract(dir.clone().multiply(coneDist))).length();
                        if (orthoDist < (coneDist / distance) * radius) {
                            blocks.add(new BlockPosition(x, y, z));
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public static Set<BlockPosition> createConeFurthest(Vector facing, double distance, double radius) {
        Set<BlockPosition> blocks = new HashSet<>();
        double dx, dy, dz;
        for (int x = -(int) Math.ceil(radius); x <= (int) Math.ceil(radius); x++) {
            dx = ((double) x) / radius;
            for (int y = -(int) Math.ceil(radius); y <= (int) Math.ceil(radius); y++) {
                dy = ((double) y) / radius;
                for (int z = -(int) Math.ceil(radius); z <= (int) Math.ceil(radius); z++) {
                    dz = ((double) z) / radius;
                    if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 1) {
                        Vector p = new Vector(x, y, z).normalize();
                        double dist = p.dot(facing);
                        if (dist >= (dist / distance) * radius) {
                            blocks.add(new BlockPosition(x, y, z));
                        }
                    }
                }
            }
        }
        return blocks;
    }

    private static final AtomicInteger nextFakeId = new AtomicInteger(500000000);

    public static int getNextId() {
        return nextFakeId.getAndIncrement();
    }

    private static final ProtocolManager protocolManager = Core.getProtocolManager();

    public static void sendArmorStandSpawn(Player player, int entityId, Location location, ItemStack itemStack) {
        // Create the spawn entity packet
        PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        UUID entityUUID = UUID.randomUUID();

        spawnPacket.getIntegers()
                .write(0, entityId)
                .write(1, (int) (location.getX() * 32))
                .write(2, (int) (location.getY() * 32))
                .write(3, (int) (location.getZ() * 32));

        spawnPacket.getUUIDs().write(0, entityUUID);
        spawnPacket.getIntegers().write(4, 0).write(5, 0).write(6, 0);
        spawnPacket.getIntegers().write(7, 78); // 78 is the entity type ID for ArmorStand
        spawnPacket.getIntegers().write(8, 0);

        // Create the entity metadata packet
        PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entityId);

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

        // Set ArmorStand to be invisible
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject invisibilityFlag = new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer);
        dataWatcher.setObject(invisibilityFlag, (byte) 0x20);

        // Set item on ArmorStand's head
        WrappedDataWatcher.WrappedDataWatcherObject itemOnHead = new WrappedDataWatcher.WrappedDataWatcherObject(6, WrappedDataWatcher.Registry.getItemStackSerializer(false));
        dataWatcher.setObject(itemOnHead, itemStack);

        metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        // Send the packets to the player
        protocolManager.sendServerPacket(player, spawnPacket);
        protocolManager.sendServerPacket(player, metadataPacket);
    }

    public static void sendEntityMove(Player player, int entityId, short offsetX, short offsetY, short offsetZ) {
        // Create the relative entity move packet
        PacketContainer movePacket = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE);

        // Set the entity ID
        movePacket.getIntegers().write(0, entityId);

        // Set the relative movement offsets
        movePacket.getShorts()
                .write(0, (short) (offsetX * 4096))
                .write(1, (short) (offsetY * 4096))
                .write(2, (short) (offsetZ * 4096));

        // Send the packet to the player
        protocolManager.sendServerPacket(player, movePacket);
    }

    public static void sendEntityMoveLook(Player player, int entityId, double offsetX, double offsetY, double offsetZ, float yaw, float pitch) {
        // Create the relative entity move and look packet
        PacketContainer movePacket = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

        // Set the entity ID
        movePacket.getIntegers().write(0, entityId);

        // Set the relative movement offsets
        movePacket.getShorts()
                .write(0, (short) (offsetX * 4096))
                .write(1, (short) (offsetY * 4096))
                .write(2, (short) (offsetZ * 4096));

        // Set the rotation (yaw and pitch)
        movePacket.getBytes()
                .write(0, (byte) (yaw * 256 / 360))
                .write(1, (byte) (pitch * 256 / 360));

        // Send the packet to the player
        protocolManager.sendServerPacket(player, movePacket);
    }

    public static void sendEntityDestroy(Player player, int... entityIds) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntegerArrays().write(0, entityIds);

        protocolManager.sendServerPacket(player, packet, null, false);
    }

}
