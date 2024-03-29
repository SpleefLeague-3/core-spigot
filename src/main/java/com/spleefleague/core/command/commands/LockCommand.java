package com.spleefleague.core.command.commands;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.command.CoreCommand;
import com.spleefleague.core.command.annotation.CommandAnnotation;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.rank.CoreRank;
import com.spleefleague.core.world.projectile.global.lock.GlobalLock;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;

/**
 * @author NickM13
 * @since 5/12/2020
 */
public class LockCommand extends CoreCommand {

    public LockCommand() {
        super("lock", CoreRank.DEVELOPER);
    }

    @CommandAnnotation
    public void lock(CorePlayer sender) {
        Block block = sender.getPlayer().getTargetBlockExact(10, FluidCollisionMode.NEVER);
        if (block == null || block.getType().isAir()) {
            error(sender, "Can't lock that!");
        } else {
            BlockPosition pos = new BlockPosition(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            if (GlobalLock.lock(pos)) {
                success(sender, "Locked block at " + pos);
            } else {
                error(sender, "Block at " + pos + " is already locked!");
            }
        }
    }

}
