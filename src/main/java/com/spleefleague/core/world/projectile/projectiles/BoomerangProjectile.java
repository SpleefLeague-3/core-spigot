package com.spleefleague.core.world.projectile.projectiles;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.world.FakeBlock;
import com.spleefleague.core.world.projectile.Projectile;
import com.spleefleague.core.world.projectile.ProjectileStats;
import com.spleefleague.core.world.projectile.ProjectileWorld;
import com.spleefleague.core.world.projectile.ProjectileWorldPlayer;
import com.spleefleague.core.world.projectile.game.GameUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @author NickM13
 * @since 10/25/2022
 */
public class BoomerangProjectile extends Projectile {

    Vector targetDir = null;
    int distanceTravelled;

    public BoomerangProjectile(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(projectileWorld, shooter, location, projectileStats, charge);
    }

    @Override
    protected void blockChange(BlockRaycastResult blockRaycastResult) {
        FakeBlock fakeBlock;
        if (getBukkitEntity().getTicksLived() > 2) {
            fakeBlock = projectileWorld.getFakeBlock(blockRaycastResult.getBlockPos());
            if (fakeBlock != null) {
                projectileWorld.breakBlock(blockRaycastResult.getBlockPos(), cpShooter);
            }
            fakeBlock = projectileWorld.getFakeBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, -2, 0)));
            if (fakeBlock != null) {
                projectileWorld.breakBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, -2, 0)), cpShooter);
            }
            fakeBlock = projectileWorld.getFakeBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, -1, 0)));
            if (fakeBlock != null) {
                projectileWorld.breakBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, -1, 0)), cpShooter);
            }
            fakeBlock = projectileWorld.getFakeBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, 1, 0)));
            if (fakeBlock != null) {
                projectileWorld.breakBlock(blockRaycastResult.getBlockPos().add(new BlockPosition(0, 1, 0)), cpShooter);
            }
        }
    }

    @Override
    protected boolean blockBounce(BlockRaycastResult blockRaycastResult, Vector intersection) {
        return false;
    }

    @Override
    protected boolean onBlockHit(BlockRaycastResult blockRaycastResult, Vector intersection) {
        return false;
    }

    @Override
    public void ao() {
        if (getBukkitEntity().getTicksLived() > 5 && getBukkitEntity().getLocation().distance(cpShooter.getLocation()) < 2) {
            getBukkitEntity().remove();
        } else {
            super.ao();
            if (getBukkitEntity().getTicksLived() >= 0) {
                getBukkitEntity().setVelocity(getBukkitEntity().getVelocity().add(cpShooter.getPlayer().getEyeLocation().toVector().subtract(new Vector(0, 0.5, 0))
                        .subtract(getBukkitEntity().getLocation().toVector()).normalize().multiply(0.045)));
            }
            if (dustOptions != null) {
                GameUtils.spawnParticles(projectileWorld, getBukkitEntity().getLocation().toVector(), dustOptions, 2, 0.1);
            }
            // Type.OFFENSIVE.getDustSmall()
        }
    }
}
