package com.spleefleague.core.infraction;

import com.spleefleague.core.Core;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.coreapi.infraction.Infraction;
import com.spleefleague.coreapi.infraction.InfractionType;
import com.spleefleague.coreapi.utils.TimeUtils;
import com.spleefleague.coreapi.utils.packet.spigot.player.PacketSpigotPlayerInfraction;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;

/**
 * @author NickM13
 */
public class Infractions {

    /**
     * Secretly mute a player
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param millis Time in milliseconds
     * @param reason Reason
     */
    public static void muteSecret(CorePlayer sender, OfflinePlayer target, long millis, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.MUTE_SECRET)
                .setDuration(millis)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have secretly muted " + target.getName() + " for " + TimeUtils.timeToString(millis)));
        }
    }

    /**
     * Publicly mute a player
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param millis Time in milliseconds
     * @param reason Reason
     */
    public static void mutePublic(CorePlayer sender, OfflinePlayer target, long millis, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.MUTE_PUBLIC)
                .setDuration(millis)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have muted " + target.getName() + " for " + TimeUtils.timeToString(millis)));
        }
    }

    /**
     * Unmute a player
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param reason Reason
     */
    public static void unmute(CorePlayer sender, OfflinePlayer target, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.MUTE_SECRET)
                .setDuration(0L)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have unmuted " + target.getName()));
        }
    }

    /**
     * Temporarily ban a player from the server
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param millis Time in milliseconds
     * @param reason Reason
     */
    public static void tempban(CorePlayer sender, OfflinePlayer target, long millis, String reason) {
        if (target == null) return;
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.TEMPBAN)
                .setDuration(millis)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have kicked " + target.getName()));
        }
    }

    /**
     * Ban a player from the server
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param reason Reason
     */
    public static void ban(CorePlayer sender, OfflinePlayer target, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.BAN)
                .setDuration(0L)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have banned " + target.getName() + " for " + reason));
        }
    }

    /**
     * Unban a player from the server
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param reason Reason
     */
    public static void unban(CorePlayer sender, OfflinePlayer target, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.UNBAN)
                .setDuration(0L)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have unbanned " + target.getName()));
        }
    }

    /**
     * Kick a player from the server
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param reason Reason
     */
    public static void kick(CorePlayer sender, OfflinePlayer target, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.KICK)
                .setDuration(0L)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have kicked " + target.getName()));
        }
    }

    /**
     * Send a warning to a player
     * Also used for post-ban information
     *
     * @param sender Name of punisher
     * @param target OfflinePlayer
     * @param reason Reason
     */
    public static void warn(CorePlayer sender, OfflinePlayer target, String reason) {
        Infraction infraction = new Infraction();
        infraction.setTarget(target.getUniqueId())
                .setPunisher(sender == null ? "" : sender.getName())
                .setType(InfractionType.WARNING)
                .setDuration(0L)
                .setReason(reason);
        Core.getInstance().sendPacket(new PacketSpigotPlayerInfraction(infraction));

        if (sender != null) {
            Core.getInstance().sendMessage(sender, Component.text("You have warned " + target.getName() + " for " + reason));
        }
    }

}
