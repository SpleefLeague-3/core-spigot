/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.command.commands;

import com.spleefleague.core.command.annotation.CommandAnnotation;
import com.spleefleague.core.command.annotation.LiteralArg;
import com.spleefleague.core.command.annotation.NumberArg;
import com.spleefleague.core.command.CoreCommand;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.rank.CoreRank;

/**
 * @author NickM13
 */
public class FlySpeedCommand extends CoreCommand {

    public FlySpeedCommand() {
        super("fspeed", CoreRank.TEMP_MOD);
        addAlias("flyspeed");
        setUsage("/fspeed [player] <-1 to 1>");
        setDescription("Set flying speed");
    }

    @CommandAnnotation
    public void fspeed(CorePlayer sender, @NumberArg(minValue = -50, maxValue = 50) Double f) {
        sender.setFlySpeed(f.floatValue());
        success(sender, "Fly speed set to " + f);
    }

    @CommandAnnotation(minRank = "SENIOR_MODERATOR")
    public void fspeed(CorePlayer sender, CorePlayer target, @NumberArg(minValue = -50, maxValue = 50) Double f) {
        target.setFlySpeed(f.floatValue());
        success(target, "Fly speed set to " + f);
        success(sender, "Fly speed of " + target.getDisplayName() + " set to " + f);
    }

    @CommandAnnotation(minRank = "SENIOR_MODERATOR")
    public void fspeed(CorePlayer sender, CorePlayer cp, @LiteralArg(value = "reset") String l) {
        fspeed(sender, cp, 0.2);
    }

}
