package com.spleefleague.core.world.game.projectile;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.Core;
import com.spleefleague.core.menu.InventoryMenuUtils;
import com.spleefleague.core.player.BattleState;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.CoreUtils;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.EntityRaycastResult;
import com.spleefleague.core.util.variable.Point;
import com.spleefleague.core.world.FakeBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
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
public class FakeEntitySnowball extends net.minecraft.world.entity.projectile.Snowball implements FakeEntity {

    protected final ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld;
    protected final ProjectileStats projectileStats;
    protected Point lastLoc;
    protected int bounces;
    protected final CorePlayer cpShooter;
    protected org.bukkit.entity.Entity lastHit = null;
    protected BlockPosition stuck = null;
    protected int lifeTicks;
    private final BlockPosition lastBlock = null;
    private final Vector size;
    private int breakAfter;

    public FakeEntitySnowball(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats) {
        this(projectileWorld, shooter, location, projectileStats, 1.);
    }

    public FakeEntitySnowball(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(EntityType.SNOWBALL, ((CraftWorld) projectileWorld.getWorld()).getHandle());
        this.cpShooter = shooter;

        this.projectileWorld = projectileWorld;
        this.projectileStats = projectileStats;
        this.size = new Vector(this.projectileStats.size, this.projectileStats.size, this.projectileStats.size);

        if (projectileStats.customModelDatas.isEmpty()) {
            ((Snowball) getBukkitEntity()).setItem(InventoryMenuUtils.createCustomItem(Material.SNOWBALL, 0));
        } else {
            ((Snowball) getBukkitEntity()).setItem(InventoryMenuUtils.createCustomItem(Material.SNOWBALL, projectileStats.customModelDatas.get(new Random().nextInt(projectileStats.customModelDatas.size()))));
        }

        Location handLocation = location.clone()
                .add(location.getDirection()
                        .crossProduct(new Vector(0, 1, 0)).normalize()
                        .multiply(0.15).add(new Vector(0, -0.15, 0)));
        setPos(handLocation.getX(), handLocation.getY(), handLocation.getZ());
        setRot(getXRot(), getYRot());

        Random rand = new Random();
        Location lookLoc = location.clone();
        Vector lookDir;
        if (projectileStats.shape == ProjectileStats.Shape.DEFAULT ||
                projectileStats.shape == ProjectileStats.Shape.CONE) {
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
        } else {
            lookDir = lookLoc.getDirection();
        }
        Vector direction = lookDir.normalize().multiply(projectileStats.fireRange * 0.25 * charge);
        setDeltaMovement(new Vec3(direction.getX(), direction.getY(), direction.getZ()));

        lastLoc = new Point(position());

        setNoGravity(!projectileStats.gravity);
        this.bounces = projectileStats.bounces;
        this.noPhysics = projectileStats.noClip;
        this.lifeTicks = projectileStats.lifeTicks;
        this.breakAfter = projectileStats.breakAfterBounces;
    }

    public CorePlayer getCpShooter() {
        return cpShooter;
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
            setNoGravity(true);
            getBukkitEntity().setVelocity(new Vector(0, 0, 0));
        } else {
            setNoGravity(false);
        }
    }

    private void checkBlockHit(CraftEntity craftEntity, Point center, Vector direction) {
        List<BlockRaycastResult> results = center.castBlocks(direction, size, direction.length());
        for (BlockRaycastResult blockResult : results) {
            FakeBlock fb = projectileWorld.getFakeBlock(blockResult.getBlockPos());
            Material mat;
            if (!blockResult.getBlockPos().equals(lastBlock)) {
                blockChange(craftEntity, blockResult);
                if (projectileWorld.checkProjectileBlock(this, craftEntity, blockResult)) {
                    return;
                }
            }
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
                if (onBlockHit(craftEntity, blockResult, new Vector(
                        lastLoc.getX() + distanced.getX(),
                        lastLoc.getY() + distanced.getY(),
                        lastLoc.getZ() + distanced.getZ()))) {
                    return;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        CraftEntity craftEntity = getBukkitEntity();
        if (craftEntity.getTicksLived() > lifeTicks) {
            kill();
            return;
        }
        craftEntity.setVelocity(craftEntity.getVelocity().multiply(projectileStats.drag));
        if (lastLoc != null) {
            Vector pos = new Vector(getX(), getY(), getZ());
            Vector direction = pos.subtract(lastLoc.toVector());
            if (stuck != null) {
                FakeBlock fb = projectileWorld.getFakeBlock(stuck);
                if ((fb == null || fb.blockData().getMaterial().isAir()) &&
                        projectileWorld.getWorld().getBlockAt(stuck.getX(), stuck.getY(), stuck.getZ()).getType().isAir()) {
                    setStuck(null);
                }
            }
            Point center = new Point(this.getX(), this.getY(), this.getZ());
            if (projectileStats.collidable) {
                boolean hit = false;
                List<org.bukkit.entity.Entity> entities = new ArrayList<>();
                for (ProjectileWorldPlayer pwp : projectileWorld.getPlayerMap().values()) {
                    if (pwp.getCorePlayer().getBattleState() == BattleState.BATTLER &&
                            (!pwp.getCorePlayer().equals(cpShooter) || craftEntity.getTicksLived() > 10)) {
                        entities.add(pwp.getPlayer());
                    }
                }
                for (EntityRaycastResult entityResult : lastLoc.castEntities(direction, size, direction.length(), entities)) {
                    org.bukkit.entity.Entity hitEntity = entityResult.getEntity();
                    if (!hitEntity.equals(lastHit)) {
                        onEntityHit(craftEntity, entityResult);
                        lastHit = hitEntity;
                        hit = true;
                        break;
                    }
                }
                if (!hit && stuck == null) {
                    checkBlockHit(craftEntity, center, direction);
                }
            } else {
                checkBlockHit(craftEntity, center, direction);
            }
        }

        lastLoc = new Point(getX(), getY(), getZ());
    }

    @Override
    protected void preOnHit(HitResult var0) {
        if (!noPhysics) {
            onHit(var0);
        }
    }

    protected void blockChange(CraftEntity craftEntity, BlockRaycastResult blockRaycastResult) {

    }

    protected boolean blockBounce(CraftEntity craftEntity, BlockRaycastResult blockRaycastResult, Vector intersection) {
        if (projectileStats.bounciness > 0) {
            switch (blockRaycastResult.getAxis()) {
                case 1 -> {
                    craftEntity.setVelocity(new Vector(
                            getDeltaMovement().x * -projectileStats.bounciness,
                            getDeltaMovement().y * projectileStats.bounciness,
                            getDeltaMovement().z * projectileStats.bounciness));
                    craftEntity.teleport(new Location(projectileWorld.getWorld(),
                            intersection.getX() + (craftEntity.getVelocity().getX() < 0 ? -0.01 : 0.01),
                            intersection.getY(),
                            intersection.getZ()));
                }
                case 2 -> {
                    craftEntity.setVelocity(new Vector(
                            getDeltaMovement().x * projectileStats.bounciness,
                            getDeltaMovement().y * -projectileStats.bounciness,
                            getDeltaMovement().z * projectileStats.bounciness));
                    if (craftEntity.getVelocity().getY() >= 0 && craftEntity.getVelocity().getY() < 0.03 && craftEntity.getVelocity().setY(0).length() < 0.005) {
                        setStuck(blockRaycastResult.getBlockPos());
                        craftEntity.teleport(new Location(projectileWorld.getWorld(),
                                intersection.getX(),
                                intersection.getY(),
                                intersection.getZ()));
                    } else {
                        craftEntity.teleport(new Location(projectileWorld.getWorld(),
                                intersection.getX(),
                                intersection.getY() + (getDeltaMovement().y < 0 ? -0.01 : 0.01),
                                intersection.getZ()));
                    }
                }
                case 3 -> {
                    craftEntity.setVelocity(new Vector(
                            getDeltaMovement().x * projectileStats.bounciness,
                            getDeltaMovement().y * projectileStats.bounciness,
                            getDeltaMovement().z * -projectileStats.bounciness));
                    craftEntity.teleport(new Location(projectileWorld.getWorld(),
                            intersection.getX(),
                            intersection.getY(),
                            intersection.getZ() + (craftEntity.getVelocity().getZ() < 0 ? -0.01 : 0.01)));
                }
                default -> {
                    craftEntity.teleport(new Location(projectileWorld.getWorld(),
                            intersection.getX(),
                            intersection.getY(),
                            intersection.getZ()));
                    craftEntity.setVelocity(craftEntity.getVelocity().multiply(-projectileStats.bounciness));
                }
            }
            return true;
        } else {
            craftEntity.setVelocity(craftEntity.getVelocity().multiply(-projectileStats.bounciness));
            if (projectileStats.bounciness < 0.0001 ||
                    projectileStats.bounciness > -0.0001) {
                craftEntity.teleport(new Location(projectileWorld.getWorld(),
                        blockRaycastResult.getIntersection().getX(),
                        blockRaycastResult.getIntersection().getY(),
                        blockRaycastResult.getIntersection().getZ()));
                setStuck(blockRaycastResult.getBlockPos());
            }
            return false;
        }
    }

    /**
     * Called when the entity collides with a solid block
     *
     * @param craftEntity        Self Craft Entity
     * @param blockRaycastResult Raycast Result
     * @return Should ignore next raycast results
     */
    protected boolean onBlockHit(CraftEntity craftEntity, BlockRaycastResult blockRaycastResult, Vector intersection) {
        if (breakAfter <= 0) {
            projectileWorld.onProjectileBlockHit(cpShooter, blockRaycastResult, projectileStats);
        } else {
            breakAfter--;
        }

        bounces--;
        if (bounces < 0) {
            kill();
            return true;
        } else {
            return blockBounce(craftEntity, blockRaycastResult, intersection);
        }
    }

    protected void entityBounce(CraftEntity craftEntity, EntityRaycastResult entityRaycastResult) {
        craftEntity.teleport(new Location(projectileWorld.getWorld(),
                entityRaycastResult.getIntersection().getX(),
                entityRaycastResult.getIntersection().getY(),
                entityRaycastResult.getIntersection().getZ()));
        craftEntity.setVelocity(craftEntity.getVelocity().multiply(-projectileStats.bounciness));
    }

    protected void onEntityHit(CraftEntity craftEntity, EntityRaycastResult entityRaycastResult) {
        bounces--;
        if (projectileStats.hitKnockback > 0) {
            CoreUtils.knockbackEntity(entityRaycastResult.getEntity(), craftEntity.getVelocity(), projectileStats.hitKnockback);
        }
        if (bounces < 0) {
            kill();
        } else {
            entityBounce(craftEntity, entityRaycastResult);
        }
        if (entityRaycastResult.getEntity() instanceof Player) {
            CorePlayer target = Core.getInstance().getPlayers().get(entityRaycastResult.getEntity().getUniqueId());
            if (target.isInBattle() && target.getBattleState() == BattleState.BATTLER) {
                target.getBattle().onPlayerHit(cpShooter, target);
            }
        }
    }

    /**
     * Make entity never underwater to not slow down
     *
     * @return In Water
     */
    @Override
    public boolean updateInWaterStateAndDoFluidPushing() {
        if (projectileStats.noClip) {
            this.wasTouchingWater = false;
            return false;
        } else {
            return super.updateInWaterStateAndDoFluidPushing();
        }
    }

    @Override
    public Entity getEntity() {
        return super.getBukkitEntity().getHandle();
    }

    @Override
    public boolean isAlive() {
        return super.isAlive();
    }

}
