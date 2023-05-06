package com.spleefleague.core.world.projectile;

import com.spleefleague.core.player.CorePlayer;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * @author NickM13
 * @since 10/25/2022
 */
public class Projectile extends FakeEntitySnowball {

    protected Particle.DustOptions dustOptions = null;

    public Projectile(ProjectileWorld<? extends ProjectileWorldPlayer> projectileWorld, CorePlayer shooter, Location location, ProjectileStats projectileStats, Double charge) {
        super(projectileWorld, shooter, location, projectileStats);
    }

}
