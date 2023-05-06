/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.world.projectile.game;

import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.Point;

import java.util.ArrayList;
import java.util.List;

import com.spleefleague.core.world.projectile.ProjectileStats;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * @author NickM13
 */
public class GameProjectile {

    Point previousLocation = null;
    Entity entity;
    Player shooter;
    ProjectileStats type;
    int bounces = 1;
    double bouncePower = 0.3;
    double drag = 1;

    public GameProjectile(Entity entity, ProjectileStats type) {
        this.entity = entity;
        this.type = type;
        this.bounces = type.bounces;
        this.bouncePower = type.bounciness;
        this.drag = type.drag;
    }

    public double getDrag() {
        return drag;
    }

    public boolean doesBounce() {
        return bouncePower > 0;
    }

    public void bounce() {
        bounces--;
    }

    public boolean hasBounces() {
        return bounces >= 0;
    }

    public double getBouncePower() {
        return bouncePower;
    }

    public ProjectileStats getProjectile() {
        return type;
    }

    public void setShooter(Player shooter) {
        this.shooter = shooter;
    }

    public Entity getEntity() {
        return entity;
    }

    public List<BlockRaycastResult> cast() {
        if (previousLocation != null) {
            Vector position = entity.getBukkitEntity().getLocation().toVector();
            Vector direction = position.subtract(previousLocation.toVector());
            setLastLoc();
            return previousLocation.castBlocks(direction, direction.length());
        } else {
            setLastLoc();
            return new ArrayList<>();
        }
    }

    public void setLastLoc() {
        BoundingBox bb = entity.getBukkitEntity().getBoundingBox();
        previousLocation = new Point(bb.getCenterX(), bb.getCenterY(), bb.getCenterZ());
    }

}
