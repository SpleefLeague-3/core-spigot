package com.spleefleague.core.world;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.EntityRaycastResult;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @author NickM13
 * @since 4/29/2023
 */
public class CollisionUtil {

    public static List<BlockRaycastResult> getBlocksPassedThrough(Vector center, Vector size, Vector direction) {
        List<BlockRaycastResult> results = new ArrayList<>();
        if (direction.getX() >= 0) {
            if (direction.getY() >= 0) {
                if (direction.getZ() >= 0) {
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                } else {
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                }
            } else {
                if (direction.getZ() >= 0) {
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                } else {
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                }
            }
        } else {
            if (direction.getY() >= 0) {
                if (direction.getZ() >= 0) {
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                } else {
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                }
            } else {
                if (direction.getZ() >= 0) {
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                } else {
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() + size.getY(), center.getZ() - size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() - size.getX(), center.getY() + size.getY(), center.getZ() + size.getZ(), direction));
                    results.addAll(castBlocks(center.getX() + size.getX(), center.getY() - size.getY(), center.getZ() + size.getZ(), direction));
                }
            }
        }
        Map<BlockPosition, BlockRaycastResult> uniqueResults = new HashMap<>();
        for (BlockRaycastResult rr1 : results) {
            if (!uniqueResults.containsKey(rr1.getBlockPos()) || rr1.getDistance() < uniqueResults.get(rr1.getBlockPos()).getDistance()) {
                uniqueResults.put(rr1.getBlockPos(), rr1);
            }
        }

        List<BlockRaycastResult> resultSorted = new ArrayList<>(uniqueResults.values());
        resultSorted.sort(Comparator.comparingDouble(BlockRaycastResult::getDistance));
        return resultSorted;
    }

    public static List<BlockRaycastResult> castBlocks(Vector center, Vector direction) {
        List<BlockRaycastResult> resultSorted = new ArrayList<>();

        boolean inserted;
        for (BlockRaycastResult rr1 : castBlocks(center.getX(), center.getY(), center.getZ(), direction)) {
            inserted = false;
            for (int i = 0; i < resultSorted.size(); i++) {
                BlockRaycastResult rr2 = resultSorted.get(i);
                if (rr1.getDistance() < rr2.getDistance()) {
                    resultSorted.add(i, rr1);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                resultSorted.add(rr1);
            }
        }

        return resultSorted;
    }

    private static List<BlockRaycastResult> castBlocks(double x, double y, double z, Vector direction) {
        direction = direction.normalize();
        double px = x < 0 ? x - Math.floor(x) : x;
        double py = y < 0 ? y - Math.floor(y) : y;
        double pz = z < 0 ? z - Math.floor(z) : z;

        double distX = direction.getX() > 0 ? (1 - (px % 1)) : (px % 1);
        double distY = direction.getY() > 0 ? (1 - (py % 1)) : (py % 1);
        double distZ = direction.getZ() > 0 ? (1 - (pz % 1)) : (pz % 1);

        double requiredX = Math.abs(direction.getX()) <= 0.00001D ? 100000 : 1D / Math.abs(direction.getX());
        double requiredY = Math.abs(direction.getY()) <= 0.00001D ? 100000 : 1D / Math.abs(direction.getY());
        double requiredZ = Math.abs(direction.getZ()) <= 0.00001D ? 100000 : 1D / Math.abs(direction.getZ());

        double remainX = distX * requiredX;
        double remainY = distY * requiredY;
        double remainZ = distZ * requiredZ;

        List<BlockRaycastResult> result = new ArrayList<>();

        BlockPosition cPos = new BlockPosition((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        double cDist = 0;
        int axis;
        BlockFace face = BlockFace.SELF;

        // Distance to faces for initial axis
        // Closer to 0 is closer to face
        double faceDistX = (((x % 1) + 1) % 1);
        double faceDistY = (((y % 1) + 1) % 1);
        double faceDistZ = (((z % 1) + 1) % 1);

        double fx = requiredX * faceDistX;
        double fy = requiredY * faceDistY;
        double fz = requiredZ * faceDistZ;
        if (fx < fy) {
            if (fx < fz) {
                axis = 1;
            } else {
                axis = 3;
            }
        } else {
            if (fy < fz) {
                axis = 2;
            } else {
                axis = 3;
            }
        }

        double minRemain;
        double distanceRemaining = direction.length();
        while (distanceRemaining > 0) {
            BlockRaycastResult rr = new BlockRaycastResult(cPos, cDist, (new Vector(x, y, z).add(direction.clone().multiply(cDist))), axis, face);
            result.add(rr);
            if (remainX < remainY) {
                if (remainX < remainZ) {
                    // Smallest remainder is X
                    minRemain = remainX;
                    remainX += requiredX;
                    cPos = cPos.add(new BlockPosition(direction.getX() > 0 ? 1 : -1, 0, 0));
                    axis = 1;
                    if (direction.getX() > 0)
                        face = BlockFace.WEST;
                    else
                        face = BlockFace.EAST;
                } else {
                    // Smallest remainder is Z
                    minRemain = remainZ;
                    remainZ += requiredZ;
                    cPos = cPos.add(new BlockPosition(0, 0, direction.getZ() > 0 ? 1 : -1));
                    axis = 3;
                    if (direction.getZ() > 0)
                        face = BlockFace.NORTH;
                    else
                        face = BlockFace.SOUTH;
                }
            } else if (remainY < remainZ) {
                // Smallest remainder is Y
                minRemain = remainY;
                remainY += requiredY;
                cPos = cPos.add(new BlockPosition(0, direction.getY() > 0 ? 1 : -1, 0));
                axis = 2;
                if (direction.getY() > 0)
                    face = BlockFace.DOWN;
                else
                    face = BlockFace.UP;
            } else {
                // Smallest remainder is Z
                minRemain = remainZ;
                remainZ += requiredZ;
                cPos = cPos.add(new BlockPosition(0, 0, direction.getZ() > 0 ? 1 : -1));
                axis = 3;
                if (direction.getZ() > 0)
                    face = BlockFace.NORTH;
                else
                    face = BlockFace.SOUTH;
            }
            distanceRemaining -= minRemain;
            cDist += minRemain;
            remainX -= minRemain;
            remainY -= minRemain;
            remainZ -= minRemain;
        }
        return result;
    }

    public static List<EntityRaycastResult> getEntitiesPassedThrough(Vector center, Vector size, Vector direction, List<Entity> entities) {
        List<EntityRaycastResult> results = new ArrayList<>();
        double distance = direction.length();
        if (distance <= 0.00001) return results;
        direction = direction.normalize();
        for (Entity entity : entities) {
            BoundingBox bb = entity.getBoundingBox().clone().expand(size);
            RayTraceResult rtr = bb.rayTrace(center, direction, distance);
            if (rtr != null) {
                results.add(new EntityRaycastResult(rtr.getHitPosition().subtract(center).length(), rtr.getHitPosition(), entity));
            }
        }
        results.sort((l, r) -> (int) (l.getDistance() * 1000 - r.getDistance() * 1000));
        return results;
    }

}
