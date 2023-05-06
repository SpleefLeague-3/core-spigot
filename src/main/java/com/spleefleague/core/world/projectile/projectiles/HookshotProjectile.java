package com.spleefleague.core.world.projectile.projectiles;

import com.spleefleague.core.logger.CoreLogger;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.EntityRaycastResult;
import com.spleefleague.core.world.projectile.game.GameUtils;
import com.spleefleague.core.world.projectile.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * @author NickM13
 * @since 10/24/2022
 */
public class HookshotProjectile extends Projectile {

    private BlockRaycastResult hookedBlock = null;
    private EntityRaycastResult hookedEntity = null;
    private int hookLife = 0;
    private int maxHookLife = -1;

    private Particle.DustOptions dustOptions = null;

    public HookshotProjectile(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(projectileWorld, shooter, location, projectileStats, charge);
    }

    public HookshotProjectile setMaxHookLife(double hookSeconds) {
        this.maxHookLife = (int) (hookSeconds * 20);
        return this;
    }

    public HookshotProjectile setDust(Particle.DustOptions dustOptions) {
        this.dustOptions = dustOptions;
        return this;
    }

    public Entity getHookedEntity() {
        if (hookedEntity != null) {
            return hookedEntity.getEntity();
        }
        return null;
    }

    public boolean isHooked() {
        return hookedBlock == null && hookedEntity == null;
    }

    public Vector getHookPos() {
        if (hookedBlock != null) {
            switch (hookedBlock.getFace()) {
                case DOWN:
                    return getBukkitEntity().getLocation().toVector().subtract(new Vector(0, 1.8, 0));
                case NORTH:
                    return getBukkitEntity().getLocation().toVector().add(new Vector(0, 0, -.3));
                case EAST:
                    return getBukkitEntity().getLocation().toVector().add(new Vector(.3, 0, 0));
                case SOUTH:
                    return getBukkitEntity().getLocation().toVector().add(new Vector(0, 0, .3));
                case WEST:
                    return getBukkitEntity().getLocation().toVector().add(new Vector(-.3, 0, 0));
                case UP:
                    return getBukkitEntity().getLocation().toVector();
                default:
                    CoreLogger.logError("Block face not recognized: " + hookedBlock.getFace().toString());
                    return getBukkitEntity().getLocation().toVector();
            }
        } else if (hookedEntity != null) {
            return getBukkitEntity().getLocation().toVector().subtract(new Vector(0, 1.8, 0));
        }
        return null;
    }

    @Override
    protected void onEntityHit(EntityRaycastResult entityRaycastResult) {
        if (isHooked()) {
            getBukkitEntity().setGravity(false);
            getBukkitEntity().setVelocity(new Vector(0, 0, 0));
            getBukkitEntity().teleport(entityRaycastResult.getIntersection().toLocation(getBukkitEntity().getWorld()));
            hookedEntity = entityRaycastResult;
            hookLife = getBukkitEntity().getTicksLived() + maxHookLife;
        }
    }

    @Override
    protected boolean onBlockHit(BlockRaycastResult blockRaycastResult, Vector intersection) {
        if (isHooked()) {
            getBukkitEntity().setGravity(false);
            getBukkitEntity().setVelocity(new Vector(0, 0, 0));
            getBukkitEntity().teleport(blockRaycastResult.getIntersection().toLocation(getBukkitEntity().getWorld()));
            hookedBlock = blockRaycastResult;
            hookLife = getBukkitEntity().getTicksLived() + maxHookLife;
        }
        return false;
    }

    @Override
    public void ao() {
        if (isHooked()) {
            super.ao();
        } else if (hookLife < getBukkitEntity().getTicksLived()) {
            getBukkitEntity().remove();
        }
        if (!getBukkitEntity().isDead()) {
            if (hookedBlock != null && !projectileWorld.isReallySolid(hookedBlock.getBlockPos())) {
                getBukkitEntity().remove();
                return;
            }
            if (hookedEntity != null) {
                getBukkitEntity().teleport(hookedEntity.getEntity().getLocation().clone().add(hookedEntity.getOffset()));
            }
            if (dustOptions != null) {
                Vector vec = cpShooter.getPlayer().getEyeLocation().toVector();
                Vector dif = getBukkitEntity().getLocation().toVector().subtract(cpShooter.getPlayer().getEyeLocation().toVector());
                for (double i = 1; i < dif.length(); i += 0.5) {
                    GameUtils.spawnParticles(projectileWorld, dif.clone().normalize().multiply(i + Math.random() / 4).add(vec), dustOptions, 1, 0);
                }
            }
        }
    }
}
