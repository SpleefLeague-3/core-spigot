/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.command.commands;

import com.spleefleague.core.command.CoreCommand;
import com.spleefleague.core.command.annotation.CommandAnnotation;
import com.spleefleague.core.command.annotation.HelperArg;
import com.spleefleague.core.command.annotation.LiteralArg;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.rank.CoreRank;
import com.spleefleague.core.settings.Settings;
import com.spleefleague.coreapi.chat.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * @author NickM13
 */
public class DiscordCommand extends CoreCommand {

    public DiscordCommand() {
        super("discord", CoreRank.DEFAULT);
        setUsage("/discord");
    }

    @CommandAnnotation
    public void discord(CorePlayer sender) {
        Component discordComponent = Component.text(Settings.getDiscord().getUrl())
                .color(NamedTextColor.BLUE)
                .clickEvent(ClickEvent.openUrl(Settings.getDiscord().getUrl()));
        Component component = Component.text("Join us on discord: ")
                .append(discordComponent);
        success(sender, component);
    }

    @CommandAnnotation(minRank = "DEVELOPER")
    public void discord(CorePlayer sender,
                        @LiteralArg("set") String l,
                        @HelperArg("<url>") String url) {
        Settings.setDiscord(url);
        success(sender, "Set discord link to " + url);
    }

}
