package com.spleefleague.core.world.projectile.personal;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.world.projectile.ProjectileWorld;
import org.bukkit.World;

/**
 * @author NickM13
 * @since 5/19/2020
 */
public class PersonalWorld extends ProjectileWorld<PersonalWorldPlayer> {

    public PersonalWorld(int priority, World world) {
        super(priority, world, PersonalWorldPlayer.class);
    }

    @Override
    protected boolean onBlockPunch(CorePlayer cp, BlockPosition pos) {
        return getFakeBlock(pos) != null;
    }

    /**
     * On player item use.
     *
     * @param cp            Core Player
     * @param pos Click Block
     * @param relative Placed Block
     * @return Cancel Event
     */
    @Override
    protected boolean onItemUse(CorePlayer cp, BlockPosition pos, BlockPosition relative) {
        return getFakeBlock(pos) != null;
    }

    @Override
    protected void applyVisibility(CorePlayer cp) {

    }

}
