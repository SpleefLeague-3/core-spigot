package com.spleefleague.core.world.projectile.projectiles;

import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.world.projectile.Projectile;
import com.spleefleague.core.world.projectile.ProjectileStats;
import com.spleefleague.core.world.projectile.ProjectileWorld;
import com.spleefleague.core.world.projectile.ProjectileWorldPlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * @author NickM13
 * @since 10/25/2022
 */
public class BouncingProjectile extends Projectile {

    public BouncingProjectile(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(projectileWorld, shooter, location, projectileStats, charge);
    }

    @Override
    public void ao() {
        super.ao();
    }

    @Override
    protected boolean onBlockHit(BlockRaycastResult blockRaycastResult, Vector intersection) {
        if (super.onBlockHit(blockRaycastResult, intersection)) {
            if (dustOptions != null) {
                projectileWorld.spawnParticles(Particle.REDSTONE,
                        blockRaycastResult.getIntersection().getX(),
                        blockRaycastResult.getIntersection().getY(),
                        blockRaycastResult.getIntersection().getZ(),
                        40, 1, 1, 1, 0D, dustOptions);
                //Type.OFFENSIVE.getDustMedium()
            }
            projectileWorld.playSound(blockRaycastResult.getIntersection().toLocation(projectileWorld.getWorld()), Sound.ENTITY_LLAMA_SPIT, 1, 1.5f);
            return true;
        }
        return false;
    }

}
