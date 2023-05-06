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
 * @since 10/24/2022
 */
public class EnderPearlProjectile extends Projectile {

    public EnderPearlProjectile(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(projectileWorld, shooter, location, projectileStats, charge);
    }

    public EnderPearlProjectile setDust(Particle.DustOptions dustOptions) {
        this.dustOptions = dustOptions;
        return this;
    }

    @Override
    protected boolean onBlockHit(BlockRaycastResult blockRaycastResult, Vector intersection) {
        cpShooter.teleport(blockRaycastResult.getRelative().toLocation(cpShooter.getPlayer().getWorld()).add(0.5, 0.5, 0.5));
        projectileWorld.playSound(blockRaycastResult.getIntersection().toLocation(projectileWorld.getWorld()), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.5f);
        projectileWorld.spawnParticles(Particle.REDSTONE,
                blockRaycastResult.getIntersection().getX(),
                blockRaycastResult.getIntersection().getY(),
                blockRaycastResult.getIntersection().getZ(),
                40, 1, 1, 1, 0D, dustOptions);
        return true;
    }

}
