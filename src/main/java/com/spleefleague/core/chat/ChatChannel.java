/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.chat;

import com.spleefleague.core.Core;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.rank.CoreRank;
import com.spleefleague.coreapi.chat.Chat;
import com.spleefleague.coreapi.chat.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author NickM13
 */
public enum ChatChannel {

    ADMIN("Admin",
            null,
            ChatColor.RED,
            cp -> cp.getRank().hasPermission(CoreRank.ADMIN),
            null),
    GAMES("Games",
            null,
            ChatColor.AQUA,
            null,
            null),
    GLOBAL("Global",
            (corePlayer -> {
                if (corePlayer.isInBattle()) {
                    return corePlayer.getBattle().getPlayers();
                } else {
                    return Core.getInstance().getPlayers().getAllLocal();
                }
            }),
            null,
            null,
            null),
    LOCAL("Local",
            (corePlayer -> {
                if (corePlayer.isInBattle()) {
                    return corePlayer.getBattle().getPlayers();
                } else {
                    return Core.getInstance().getPlayers().getAllLocal();
                }
            }),
            ChatColor.GRAY,
            cp -> cp.getRank().hasPermission(CoreRank.ADMIN),
            null),
    LOGIN("Login",
            null,
            ChatColor.GRAY,
            cp -> cp.getRank().hasPermission(CoreRank.ADMIN),
            null),
    PARTY("Party",
            null,
            ChatColor.AQUA,
            cp -> cp.getParty() != null,
            null),
    STAFF("Staff",
            null,
            ChatColor.LIGHT_PURPLE,
            cp -> cp.getRank().hasPermission(CoreRank.TEMP_MOD),
            null),
    TICKET("Ticket",
            null,
            ChatColor.GOLD,
            cp -> cp.getRank().hasPermission(CoreRank.TEMP_MOD),
            null),
    VIP("VIP",
            null,
            ChatColor.DARK_PURPLE,
            cp -> cp.getRank().hasPermission(CoreRank.VIP),
            null);

    private final String name;
    private final Function<CorePlayer, Collection<CorePlayer>> playerFunc;
    private final ChatColor tagColor;
    private final Function<CorePlayer, Boolean> available;

    private final Component tagComponent;
    private final Component playerMessageComponent;

    ChatChannel(String name,
                Function<CorePlayer, Collection<CorePlayer>> playerFunc,
                ChatColor tagColor,
                Function<CorePlayer, Boolean> available,
                String playerChatColor) {
        this.name = name;
        this.playerFunc = playerFunc;
        this.tagColor = tagColor;
        this.available = available;
        String playerChatColor1 = playerChatColor == null ? Chat.PLAYER_CHAT : playerChatColor;

        if (tagColor != null) {
            tagComponent = Component.text(com.spleefleague.coreapi.chat.Chat.TAG_BRACE + "[" + tagColor + name + Chat.TAG_BRACE + "] ");
        } else {
            tagComponent = Component.empty();
        }

        Component playerMessageComponent1 = Component.empty();
        for (com.spleefleague.coreapi.chat.ChatColor chatColor : com.spleefleague.coreapi.chat.ChatColor.getChatColors(playerChatColor1)) {
            switch (chatColor) {
                case RESET:
                    break;
                case STRIKETHROUGH:
                    playerMessageComponent1 = playerMessageComponent1.decorate(TextDecoration.STRIKETHROUGH);
                    break;
                case BOLD:
                    playerMessageComponent1 = playerMessageComponent1.decorate(TextDecoration.BOLD);
                    break;
                case UNDERLINE:
                    playerMessageComponent1 = playerMessageComponent1.decorate(TextDecoration.UNDERLINED);
                    break;
                case MAGIC:
                    playerMessageComponent1 = playerMessageComponent1.decorate(TextDecoration.OBFUSCATED);
                    break;
                case ITALIC:
                    playerMessageComponent1 = playerMessageComponent1.decorate(TextDecoration.ITALIC);
                    break;
                default:
                    playerMessageComponent1 = playerMessageComponent1.color(TextColor.color(chatColor.ordinal()));
            }
        }
        playerMessageComponent = playerMessageComponent1;
    }

    public String getName() {
        return name;
    }

    public boolean isGlobal() {
        return playerFunc == null;
    }

    public Function<CorePlayer, Collection<CorePlayer>> getPlayerFunc() {
        return playerFunc;
    }

    public ChatColor getTagColor() {
        return tagColor;
    }

    public boolean isShowingTag() {
        return tagColor != null;
    }

    public Component getTagComponent() {
        return tagComponent;
    }

    public boolean isAvailable(CorePlayer cp) {
        return available == null || available.apply(cp);
    }

    public boolean isActive(CorePlayer cp) {
        return isAvailable(cp) && cp.getOptions().getBoolean("Chat:" + name());
    }

    public Component getPlayerMessageBase() {
        return playerMessageComponent;
    }

}
