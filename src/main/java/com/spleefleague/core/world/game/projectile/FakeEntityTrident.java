package com.spleefleague.core.world.game.projectile;

import com.spleefleague.core.player.CoreOfflinePlayer;
import com.spleefleague.core.util.variable.Point;
import com.spleefleague.core.world.game.GameWorld;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * @author NickM13
 * @since 5/19/2020
 */
public class FakeEntityTrident extends ThrownTrident {

    private final GameWorld gameWorld;
    private final ProjectileStats projectileStats;
    private Point lastLoc = null;
    private int bounces;

    public FakeEntityTrident(GameWorld gameWorld, CoreOfflinePlayer shooter, ProjectileStats projectileStats) {
        super(EntityType.TRIDENT, ((CraftWorld) gameWorld.getWorld()).getHandle());

        this.gameWorld = gameWorld;
        this.projectileStats = projectileStats;

        Location handLocation = shooter.getPlayer().getEyeLocation().clone()
                .add(shooter.getPlayer().getLocation().getDirection()
                        .crossProduct(new Vector(0, 1, 0)).normalize()
                        .multiply(0.15).add(new Vector(0, -0.15, 0)));
        setPos(handLocation.getX(), handLocation.getY(), handLocation.getZ());
        setRot(handLocation.getPitch(), handLocation.getYaw());

        Random rand = new Random();
        Location lookLoc = shooter.getPlayer().getLocation().clone();
        if (projectileStats.hSpread > 0) {
            lookLoc.setYaw(lookLoc.getYaw() + rand.nextInt(projectileStats.hSpread) - (projectileStats.hSpread / 2.f));
        }
        if (projectileStats.vSpread > 0) {
            lookLoc.setPitch(lookLoc.getPitch() + rand.nextInt(projectileStats.vSpread) - (projectileStats.vSpread / 2.f));
        }
        Vector direction = lookLoc.getDirection().normalize().multiply(projectileStats.fireRange * 0.25);
        setDeltaMovement(new Vec3(direction.getX(), direction.getY(), direction.getZ()));

        setNoGravity(!projectileStats.gravity);
        this.bounces = projectileStats.bounces;
        this.noPhysics = projectileStats.noClip;
        this.inGround = true;
    }

    @Override
    public void tick() {
        super.tick();
        lastLoc = new Point(getX(), getY(), getZ());
    }


    @Override
    protected void onHit(HitResult movingobjectposition) {
        if (!noPhysics) {
            super.onHit(movingobjectposition);
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player);
    }

}
