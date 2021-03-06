/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.command.commands;

import com.google.common.collect.Lists;
import com.spleefleague.core.Core;
import com.spleefleague.core.chat.ChatUtils;
import com.spleefleague.core.command.annotation.CommandAnnotation;
import com.spleefleague.core.chat.Chat;
import com.spleefleague.core.command.CoreCommand;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.rank.CoreRank;

import java.util.Comparator;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

/**
 * @author NickM13
 */
public class SPingCommand extends CoreCommand {

    public SPingCommand() {
        super("sping", CoreRank.DEFAULT);
        setDescription("Get all player pings");
    }

    @CommandAnnotation
    public void sping(CorePlayer sender) {
        List<CorePlayer> players = Lists.newArrayList(Core.getInstance().getPlayers().getAllLocal());
        players.sort(Comparator.comparingInt(CorePlayer::getPing));

        sender.sendMessage(ChatUtils.centerChat(ChatColor.AQUA + "[" + ChatColor.GOLD + " Everyone's Ping " + ChatColor.AQUA + "]"));
        int maxDisplay = 8;
        for (CorePlayer cp : players) {
            sender.sendMessage(cp.getPingFormatted() + Chat.DEFAULT + " >> " + cp.getDisplayName());
            maxDisplay--;
            if (maxDisplay < 0) {
                sender.sendMessage(Chat.ERROR + "...");
                break;
            }
        }
    }

}
