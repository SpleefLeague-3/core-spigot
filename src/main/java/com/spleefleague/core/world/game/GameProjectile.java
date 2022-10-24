/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.world.game;

import com.spleefleague.core.util.variable.BlockRaycastResult;
import com.spleefleague.core.util.variable.Point;

import java.util.ArrayList;
import java.util.List;

import com.spleefleague.core.world.game.projectile.ProjectileStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author NickM13
 */
public class GameProjectile {

    Point lastLoc = null;
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
        if (lastLoc != null) {
            Vector pos = new Vector(entity.getX(), entity.getY(), entity.getZ());
            Vector direction = pos.subtract(lastLoc.toVector());
            setLastLoc();
            return lastLoc.castBlocks(direction, direction.length());
        } else {
            setLastLoc();
            return new ArrayList<>();
        }
    }

    public void setLastLoc() {
        AABB bb = entity.getBoundingBox();
        lastLoc = new Point(
                (bb.maxX - bb.minX) / 2D + bb.minX,
                (bb.maxY - bb.minY) / 2D + bb.minY,
                (bb.maxZ - bb.minZ) / 2D + bb.minZ);
    }

}
