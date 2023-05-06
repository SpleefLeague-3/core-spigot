package com.spleefleague.core.world.projectile;

import com.spleefleague.core.player.CorePlayer;
import net.minecraft.world.entity.Entity;

/**
 * @author NickM13
 */
public interface FakeEntity {

    CorePlayer getCpShooter();

    Entity getEntity();

    void reducedStats(FakeEntity fakeEntity);

    ProjectileStats getStats();

    int getRemainingLife();

    int getRemainingBounces();

    int getRemainingBreakAfter();
}
