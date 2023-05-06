package com.spleefleague.core.world.projectile;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.Core;
import com.spleefleague.core.player.BattleState;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.CoreUtils;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.EntityRaycastResult;
import com.spleefleague.core.world.CollisionUtil;
import com.spleefleague.core.world.FakeBlock;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EntitySnowball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author NickM13
 * @since 5/19/2020
 */
public class FakeEntitySnowball extends EntitySnowball implements FakeEntity {

    protected final ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld;
    protected final ProjectileStats projectileStats;
    protected Location previousLocation = null;
    protected int bounces;
    protected final CorePlayer cpShooter;
    protected org.bukkit.entity.Entity lastHit = null;
    protected BlockPosition stuck = null;
    protected int lifeTicks;
    private BlockPosition lastBlock = null;
    private final Vector size;
    private int breakAfter;

    public FakeEntitySnowball(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats) {
        this(projectileWorld, shooter, location, projectileStats, 1.);
    }

    public FakeEntitySnowball(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(EntityTypes.aP, ((CraftWorld) projectileWorld.getWorld()).getHandle());
        this.cpShooter = shooter;

        // Calculate the position of the hand of the player throwing the snowball
        Location handLocation = location.clone().add(location.getDirection().crossProduct(new Vector(0, 1, 0)).normalize().multiply(0.15).add(new Vector(0, -0.15, 0)));

        // Modify the snowball as needed, e.g., set the shooter, velocity, or custom name
        ((Snowball) getBukkitEntity()).setItem(projectileStats.getItem());
        ((Snowball) getBukkitEntity()).setShooter(shooter.getPlayer());
        getBukkitEntity().setGravity(projectileStats.gravity);

        Random rand = new Random();
        Location lookLoc = location.clone();
        Vector lookDir;
        switch (projectileStats.shape) {
            case DEFAULT, CONE -> {
                if (projectileStats.vSpread > 0) {
                    lookLoc.setPitch(lookLoc.getPitch() + rand.nextInt(projectileStats.vSpread) - (projectileStats.vSpread / 2.f));
                }
                if (projectileStats.hSpread > 0) {
                    Location temp = lookLoc.clone();
                    temp.setPitch(lookLoc.getPitch() + 90);
                    lookDir = lookLoc.getDirection().rotateAroundNonUnitAxis(temp.getDirection(), Math.toRadians(rand.nextInt(projectileStats.hSpread) - (projectileStats.hSpread / 2.f)));
                } else {
                    lookDir = lookLoc.getDirection();
                }
            }
            default -> lookDir = lookLoc.getDirection();
        }
        Vector direction = lookDir.normalize().multiply(projectileStats.fireRange * 0.25 * charge);
        getBukkitEntity().setVelocity(direction);

        this.projectileWorld = projectileWorld;
        this.projectileStats = projectileStats;
        this.size = new Vector(this.projectileStats.size, this.projectileStats.size, this.projectileStats.size);

        getBukkitEntity().setGravity(projectileStats.gravity);
        //this.noPhysics = projectileStats.noClip;

        this.bounces = projectileStats.bounces;
        this.lifeTicks = projectileStats.lifeTicks;
        this.breakAfter = projectileStats.breakAfterBounces;
    }

    public CorePlayer getCpShooter() {
        return cpShooter;
    }

    @Override
    public net.minecraft.world.entity.Entity getEntity() {
        return this;
    }

    @Override
    public void reducedStats(FakeEntity fakeEntity) {
        this.lifeTicks = fakeEntity.getRemainingLife();
        this.bounces = fakeEntity.getRemainingBounces();
        this.breakAfter = fakeEntity.getRemainingBreakAfter();
    }

    public ProjectileStats getStats() {
        return projectileStats;
    }

    public int getRemainingLife() {
        return lifeTicks;
    }

    public int getRemainingBounces() {
        return bounces;
    }

    public int getRemainingBreakAfter() {
        return breakAfter;
    }

    protected void setStuck(BlockPosition pos) {
        stuck = pos;
        if (stuck != null) {
            getBukkitEntity().setGravity(false);
            getBukkitEntity().setVelocity(new Vector(0, 0, 0));
        } else {
            getBukkitEntity().setGravity(true);
        }
    }

    private boolean checkEntityHit(Vector direction) {
        boolean hit = false;
        List<org.bukkit.entity.Entity> entities = new ArrayList<>();
        for (ProjectileWorldPlayer pwp : projectileWorld.getPlayerMap().values()) {
            if (pwp.getCorePlayer().getBattleState() == BattleState.BATTLER &&
                    (!pwp.getCorePlayer().equals(cpShooter) || getBukkitEntity().getTicksLived() > 10)) {
                entities.add(pwp.getPlayer());
            }
        }
        for (EntityRaycastResult entityResult : CollisionUtil.getEntitiesPassedThrough(getBukkitEntity().getLocation().toVector(), size, direction, entities)) {
            org.bukkit.entity.Entity hitEntity = entityResult.getEntity();
            if (!hitEntity.equals(lastHit)) {
                onEntityHit(entityResult);
                lastHit = hitEntity;
                hit = true;
                break;
            }
        }
        return hit;
    }

    private void checkBlockHit(Vector direction) {
        List<BlockRaycastResult> blocks = CollisionUtil.getBlocksPassedThrough(getBukkitEntity().getLocation().toVector(), size, direction);

        List<BlockRaycastResult> results = CollisionUtil.castBlocks(direction, size);
        for (BlockRaycastResult blockResult : results) {
            FakeBlock fb = projectileWorld.getFakeBlock(blockResult.getBlockPos());
            if (!blockResult.getBlockPos().equals(lastBlock)) {
                lastBlock = blockResult.getBlockPos();
                blockChange(blockResult);
                if (projectileWorld.checkProjectileBlock(this, blockResult)) {
                    return;
                }
            }
            Material mat;
            if (fb != null) {
                mat = fb.blockData().getMaterial();
            } else {
                mat = projectileWorld.getWorld().getBlockAt(
                        blockResult.getBlockPos().getX(),
                        blockResult.getBlockPos().getY(),
                        blockResult.getBlockPos().getZ()).getType();
            }
            if (mat.isSolid()) {
                Vector distanced = direction.clone().normalize().multiply(blockResult.getDistance());
                if (onBlockHit(blockResult, new Vector(
                        previousLocation.getX() + distanced.getX(),
                        previousLocation.getY() + distanced.getY(),
                        previousLocation.getZ() + distanced.getZ()))) {
                    return;
                }
            }
        }
    }

    public void killEntity() {
        super.a(RemovalReason.b);
    }

    @Override
    public void ao() {
        if (getBukkitEntity().getTicksLived() > lifeTicks) {
            getBukkitEntity().remove();
            return;
        }
        Location currentLocation = getBukkitEntity().getLocation();
        Vector center = previousLocation.toVector();
        Vector direction = currentLocation.toVector().subtract(center);
        getBukkitEntity().setVelocity(direction.clone().multiply(projectileStats.drag));

        if (previousLocation != null) {
            if (stuck != null) {
                FakeBlock fb = projectileWorld.getFakeBlock(stuck);
                if ((fb == null || !fb.blockData().getMaterial().isSolid()) &&
                        !projectileWorld.getWorld().getBlockAt(stuck.getX(), stuck.getY(), stuck.getZ()).getType().isSolid()) {
                    setStuck(null);
                }
            }
            if (projectileStats.collidable) {
                boolean hit = checkEntityHit(direction);
                if (!hit && stuck == null) {
                    checkBlockHit(direction);
                }
            } else {
                checkBlockHit(direction);
            }
        }

        previousLocation = currentLocation;
    }

    protected void checkBlockCollision() {

    }


    protected void blockChange(BlockRaycastResult blockRaycastResult) {

    }

    protected boolean blockBounce(BlockRaycastResult blockRaycastResult, Vector intersection) {
        Vector velocity = getBukkitEntity().getVelocity();
        if (projectileStats.bounciness > 0) {
            switch (blockRaycastResult.getAxis()) {
                case 1 -> {
                    getBukkitEntity().setVelocity(new Vector(
                            velocity.getX() * -projectileStats.bounciness,
                            velocity.getY() * projectileStats.bounciness,
                            velocity.getZ() * projectileStats.bounciness));
                    teleport(intersection.getX() + (velocity.getX() < 0 ? -0.01 : 0.01),
                            intersection.getY(),
                            intersection.getZ());
                }
                case 2 -> {
                    getBukkitEntity().setVelocity(new Vector(
                            velocity.getX() * projectileStats.bounciness,
                            velocity.getY() * -projectileStats.bounciness,
                            velocity.getZ() * projectileStats.bounciness));
                    if (velocity.getY() >= 0 && velocity.getY() < 0.03 && velocity.setY(0).length() < 0.005) {
                        setStuck(blockRaycastResult.getBlockPos());
                        teleport(intersection.getX(),
                                intersection.getY(),
                                intersection.getZ());
                    } else {
                        teleport(intersection.getX(),
                                intersection.getY() + (velocity.getY() < 0 ? -0.01 : 0.01),
                                intersection.getZ());
                    }
                }
                case 3 -> {
                    getBukkitEntity().setVelocity(new Vector(
                            velocity.getX() * projectileStats.bounciness,
                            velocity.getY() * projectileStats.bounciness,
                            velocity.getZ() * -projectileStats.bounciness));
                    teleport(intersection.getX(),
                            intersection.getY(),
                            intersection.getZ() + (velocity.getZ() < 0 ? -0.01 : 0.01));
                }
                default -> {
                    teleport(intersection.getX(),
                            intersection.getY(),
                            intersection.getZ());
                    getBukkitEntity().setVelocity(velocity.clone().multiply(-projectileStats.bounciness));
                }
            }
            return true;
        } else {
            getBukkitEntity().setVelocity(velocity.clone().multiply(-projectileStats.bounciness));
            if (projectileStats.bounciness < 0.0001 ||
                    projectileStats.bounciness > -0.0001) {
                Vector blockIntersection = blockRaycastResult.getIntersection();
                teleport(blockIntersection.getX(),
                        blockIntersection.getY(),
                        blockIntersection.getZ());
                setStuck(blockRaycastResult.getBlockPos());
            }
            return false;
        }
    }

    /**
     * Called when the entity collides with a solid block
     *
     * @param blockRaycastResult Raycast Result
     * @return Should ignore next raycast results
     */
    protected boolean onBlockHit(BlockRaycastResult blockRaycastResult, Vector intersection) {
        if (breakAfter <= 0) {
            projectileWorld.onProjectileBlockHit(cpShooter, blockRaycastResult, projectileStats);
        } else {
            breakAfter--;
        }

        bounces--;
        if (bounces < 0) {
            getBukkitEntity().remove();
            return true;
        } else {
            return blockBounce(blockRaycastResult, intersection);
        }
    }

    public void teleport(double x, double y, double z) {
        teleportTo(((CraftWorld) projectileWorld.getWorld()).getHandle(), new Position(x, y, z));
    }

    protected void entityBounce(EntityRaycastResult entityRaycastResult) {
        Vector intersection = entityRaycastResult.getIntersection();
        teleport(intersection.getX(), intersection.getY(), intersection.getZ());
        getBukkitEntity().setVelocity(getBukkitEntity().getVelocity().multiply(-projectileStats.bounciness));
    }

    protected void onEntityHit(EntityRaycastResult entityRaycastResult) {
        bounces--;
        if (projectileStats.hitKnockback > 0) {
            CoreUtils.knockbackEntity(entityRaycastResult.getEntity(), getBukkitEntity().getVelocity(), projectileStats.hitKnockback);
        }
        if (bounces < 0) {
            getBukkitEntity().remove();
        } else {
            entityBounce(entityRaycastResult);
        }
        if (entityRaycastResult.getEntity() instanceof Player) {
            CorePlayer target = Core.getInstance().getPlayers().get(entityRaycastResult.getEntity().getUniqueId());
            if (target.isInBattle() && target.getBattleState() == BattleState.BATTLER) {
                target.getBattle().onPlayerHit(cpShooter, target);
            }
        }
    }
}
