/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.core.chat;

import com.spleefleague.core.Core;
import com.spleefleague.core.player.CorePlayer;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.spleefleague.core.request.RequestManager;
import com.spleefleague.coreapi.chat.ChatColor;
import com.spleefleague.coreapi.chat.ChatEmoticons;
import com.spleefleague.coreapi.utils.packet.spigot.chat.PacketSpigotChatConsole;
import com.spleefleague.coreapi.utils.packet.spigot.chat.PacketSpigotChatFriend;
import com.spleefleague.coreapi.utils.packet.spigot.chat.PacketSpigotChatPlayer;
import com.spleefleague.coreapi.utils.packet.spigot.chat.PacketSpigotChatTell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

/**
 * @author NickM13
 * jack_limestone wuz here
 * Vidiot wuz here two
 */
public class Chat {

    private static final HashMap<String, ChatColor> chatColors = new HashMap<>();
    public static String UNDO = ChatColor.UNDO + "",
            DEFAULT = ChatColor.GRAY + "",
            WHISPER = ChatColor.WHITE + "" + ChatColor.ITALIC + "",
            SUCCESS = ChatColor.GREEN + "",
            INFO = ChatColor.YELLOW + "",
            ERROR = ChatColor.RED + "",
            BROADCAST = ChatColor.LIGHT_PURPLE + "",
            BRACKET = ChatColor.GRAY + "",
            TAG_BRACE = ChatColor.DARK_GRAY + "",
            RANK = ChatColor.GRAY + "",
            GAMEMODE = ChatColor.GREEN + "",
            GAMEMAP = ChatColor.GREEN + "",
            MENU_NAME = ChatColor.WHITE + "" + ChatColor.BOLD,
            DESCRIPTION = ChatColor.GRAY + "",
            STAT = ChatColor.RED + "",
            TIME = ChatColor.RED + "",
            SCORE = ChatColor.GOLD + "",
            ELO = ChatColor.AQUA + "",
            PLAYER_NAME = ChatColor.YELLOW + "",
            PLAYER_CHAT = ChatColor.WHITE + "",
            TAG = ChatColor.GOLD + "",
            TICKET_PREFIX = ChatColor.GOLD + "",
            TICKET_ISSUE = ChatColor.GREEN + "",
            SCOREBOARD_DEFAULT = ChatColor.WHITE + "";

    public static ChatColor getColor(String color) {
        return chatColors.get(color);
    }

    public static void init() {
        chatColors.put("DEFAULT", ChatColor.GRAY);
        chatColors.put("SUCCESS", ChatColor.GREEN);
        chatColors.put("INFO", ChatColor.YELLOW);
        chatColors.put("ERROR", ChatColor.RED);
    }

    private static class FormattedPlayerMessage {

        Component component;
        boolean containsUrl;

        public FormattedPlayerMessage(Component component, boolean containsUrl) {
            this.component = component;
            this.containsUrl = containsUrl;
        }

    }

    public static FormattedPlayerMessage formatPlayerMessage(String message, Component baseFormat) {
        Pattern pattern = Pattern.compile(":(.*?):|(https?://[\\w.-]+(?:\\.[\\w\\-]+)+[/\\w\\-.,@?^=%&:/~+#]*[/?#][\\w\\-.,@?^=%&:/~+#])");
        Matcher matcher = pattern.matcher(message);
        Component result = Component.empty().append(baseFormat);

        int lastEnd = 0;
        boolean url = false;
        while (matcher.find()) {
            String beforeMatch = message.substring(lastEnd, matcher.start());
            String matchedEmote = matcher.group(1);
            String matchedUrl = matcher.group(2);

            result = result.append(Component.text(beforeMatch));

            if (matchedEmote != null) {
                String emote = ChatEmoticons.getEmoticons().get(matchedEmote);

                if (emote != null) {
                    Component hoverComponent = Component.text(matchedEmote);
                    HoverEvent<Component> hoverEvent = HoverEvent.showText(hoverComponent);
                    Component customComponent = Component.text(emote).hoverEvent(hoverEvent);

                    result = result.append(customComponent);
                } else {
                    result = result.append(Component.text(":" + matchedEmote + ":"));
                }
            } else if (matchedUrl != null) {
                try {
                    URL uri = new URL(matchedUrl);
                    ClickEvent clickEvent = ClickEvent.openUrl(uri);
                    Component urlComponent = Component.text(matchedUrl).clickEvent(clickEvent);
                    result = result.append(urlComponent);
                    url = true;
                } catch (MalformedURLException e) {
                    result = result.append(Component.text(matchedUrl));
                }
            }

            lastEnd = matcher.end();
        }
        result = result.append(Component.text(message.substring(lastEnd)));

        return new FormattedPlayerMessage(result, url);
    }

    /**
     * Replaces all &# with their associated colors and \n with newlines
     *
     * @param msg String to Colorize
     * @return Colorized String
     */
    public static String colorize(String msg) {
        StringBuilder newmsg = new StringBuilder();
        int i;
        Stack<com.spleefleague.coreapi.chat.ChatColor> colorStack = new Stack<>();
        for (i = 0; i < msg.length() - 1; i++) {
            if (msg.charAt(i) == '&' || msg.charAt(i) == '§') {
                if (i >= msg.length() - 1) continue;
                switch (msg.charAt(i + 1)) {
                    case 'b':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.AQUA));
                        break;
                    case '0':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.BLACK));
                        break;
                    case '9':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.BLUE));
                        break;
                    case '3':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_AQUA));
                        break;
                    case '1':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_BLUE));
                        break;
                    case '8':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_GRAY));
                        break;
                    case '2':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_GREEN));
                        break;
                    case '5':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_PURPLE));
                        break;
                    case '4':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.DARK_RED));
                        break;
                    case '6':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.GOLD));
                        break;
                    case '7':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.GRAY));
                        break;
                    case 'a':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.GREEN));
                        break;
                    case 'd':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.LIGHT_PURPLE));
                        break;
                    case 'c':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.RED));
                        break;
                    case 'f':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.WHITE));
                        break;
                    case 'e':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.YELLOW));
                        break;
                    case 'l':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.BOLD));
                        break;
                    case 'i':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.ITALIC));
                        break;
                    case 'r':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.RESET));
                        break;
                    case 'n':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.UNDERLINE));
                        break;
                    case 'm':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.STRIKETHROUGH));
                        break;
                    case 'k':
                        newmsg.append(colorStack.push(com.spleefleague.coreapi.chat.ChatColor.MAGIC));
                        break;
                    case 'u':
                        if (colorStack.size() <= 1) {
                            newmsg.append(com.spleefleague.coreapi.chat.ChatColor.RESET);
                        } else {
                            colorStack.pop();
                            newmsg.append(colorStack.peek());
                        }
                        break;
                    default:
                        newmsg = new StringBuilder(newmsg.toString().concat(Character.toString(msg.charAt(i))).concat(Character.toString(msg.charAt(i + 1))));
                        break;
                }
                i++;
            } else if (msg.charAt(i) == '\\') {
                if (msg.charAt(i + 1) == 'n') {
                    newmsg.append("\n");
                } else if (msg.charAt(i + 1) == '\\') {
                    newmsg.append("\\");
                } else {
                    newmsg = new StringBuilder(newmsg.toString().concat(Character.toString(msg.charAt(i))).concat(Character.toString(msg.charAt(i + 1))));
                }
                i++;
            } else {
                newmsg = new StringBuilder(newmsg.toString().concat(Character.toString(msg.charAt(i))));
            }
        }
        if (i <= msg.length() - 1) {
            newmsg = new StringBuilder(newmsg.toString().concat(Character.toString(msg.charAt(msg.length() - 1))));
        }
        return newmsg.toString();
    }

    public static void sendFakeMessage(CorePlayer sender, ChatChannel channel, String message) {
        if (channel == null) channel = sender.getChatChannel();

        if (!channel.isAvailable(sender)) {
            Core.getInstance().sendMessage(sender, "You have " + channel.getName() + " muted!");
            Core.getInstance().sendMessage(sender, "To unmute, go to Menu->Options->Chat Channels");
            return;
        }

        FormattedPlayerMessage playerMessage = formatPlayerMessage(message, channel.getPlayerMessageBase());

        if (playerMessage.containsUrl) {
            if (!sender.canSendUrl()) {
                Core.getInstance().sendMessage(sender, "Please ask for permission to send a URL");
                return;
            } else {
                //sender.disallowUrl();
            }
        }

        Component component = Component.empty()
                .append(channel.getTagComponent())
                .append(channel.isShowingTag() ? sender.getChatName() : sender.getChatNameRanked())
                .append(Component.text(ChatColor.GRAY + ": "))
                .append(playerMessage.component);

        sender.sendMessage(component);
    }

    public static void sendMessage(CorePlayer sender, String message) {
        sendMessage(sender, sender.getChatChannel(), message);
    }

    public static void sendMessage(CorePlayer sender, ChatChannel channel, String message) {
        if (channel == null) channel = sender.getChatChannel();

        if (!channel.isAvailable(sender)) {
            Core.getInstance().sendMessage(sender, "You have " + channel.getName() + " muted!");
            Core.getInstance().sendMessage(sender, "To unmute, go to Menu->Options->Chat Channels");
            return;
        }

        FormattedPlayerMessage playerMessage = formatPlayerMessage(message, channel.getPlayerMessageBase());

        if (playerMessage.containsUrl) {
            if (!sender.canSendUrl()) {
                Core.getInstance().sendMessage(sender, "Please ask for permission to send a URL");
                return;
            } else {
                //sender.disallowUrl();
            }
        }

        if (channel.isGlobal()) {
            Core.getInstance().sendPacket(new PacketSpigotChatPlayer(sender.getUniqueId(), channel.name(), message));
        } else {
            Component component = Component.empty()
                    .append(channel.getTagComponent())
                    .append(channel.isShowingTag() ? sender.getChatName() : sender.getChatNameRanked())
                    .append(Component.text(ChatColor.GRAY + ": "))
                    .append(playerMessage.component);

            for (CorePlayer cp : channel.getPlayerFunc().apply(sender)) {
                if (channel.isActive(cp)) {
                    cp.sendMessage(component);
                }
            }
        }
    }

    private static final String LINEBREAK = ChatColor.GOLD + "" + ChatColor.BOLD + "- - - - - - - - - - - - - - - - - - - - - - - - - - -";

    private static Component createNpcMessage(String profile, String name, String message) {
        Component component = Component.empty()
                .append(Component.text(LINEBREAK))
                .append(Component.text("\n" + profile, NamedTextColor.WHITE, TextDecoration.ITALIC))
                .append(Component.text(" " + name.replaceAll("_", " "), NamedTextColor.GOLD, TextDecoration.BOLD));
        int i = 0;
        for (String str : message.split("\\\\n")) {
            component = component.append(Component.text("\n亖 " + str, NamedTextColor.GREEN, TextDecoration.ITALIC));
            i++;
        }
        for (; i <= 4; i++) {
            component = component.appendNewline();
        }
        component = Component.empty().append(Component.text(LINEBREAK));
        return component;
    }

    public static void sendNpcMessage(CorePlayer receiver, String profile, String name, String message) {
        Component component = createNpcMessage(profile, name, message);
        receiver.sendMessage(component);
    }

    public static void sendNpcMessage(String profile, String name, String message) {
        Component component = createNpcMessage(profile, name, message);
        Chat.sendMessageLocal(component);
    }

    private static void sendMessageLocal(Component component) {
        for (CorePlayer cp : Core.getInstance().getPlayers().getAllLocal()) {
            cp.sendMessage(component);
        }
    }

    public static void sendNpcMessage(CorePlayer receiver, NpcMessage message) {
        Component component = createNpcMessage(message.getProfile(), message.getName(), String.join("\n", message.getMessages()));
        receiver.sendMessage(component);
    }

    ///npc 倗 Barmaid_Melissa What'll it be honey?\nOur Tree Stump Ales are made from the Valley's own\ntrees. You won't find a better Ale anywhere!

    public static void sendMessage(ChatChannel channel, Component text) {
        Core.getInstance().sendPacket(new PacketSpigotChatConsole(channel.name(), text.toString(), false));
    }

    public static void sendMessage(ChatChannel channel, Component text, Set<UUID> blacklist) {
        Core.getInstance().sendPacket(new PacketSpigotChatConsole(channel.name(), text.toString(), blacklist, false));
    }

    public static void sendMessageFriends(ChatChannel channel, Component text, Set<UUID> targets) {
        Core.getInstance().sendPacket(new PacketSpigotChatFriend(channel.name(), text.toString(), targets));
    }

    public static void sendTitle(ChatChannel channel, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (CorePlayer cp : Core.getInstance().getPlayers().getAllLocal()) {
            if (cp.getOptions().getBoolean("Chat:" + channel.getName())
                    && channel.isAvailable(cp)) {
                Component titleComponent = Component.text(title);
                Component subtitleComponent = Component.text(subtitle);
                Duration fadeInDuration = Duration.ofMillis(fadeIn);
                Duration stayDuration = Duration.ofMillis(stay);
                Duration fadeOutDuration = Duration.ofMillis(fadeOut);

                Title.Times times = Title.Times.times(fadeInDuration, stayDuration, fadeOutDuration);
                Title titleObj = Title.title(titleComponent, subtitleComponent, times);

                cp.getPlayer().showTitle(titleObj);
            }
        }
    }

    public static void sendTitle(CorePlayer cp, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (cp != null && cp.getPlayer() != null)
            cp.getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendMessageToPlayer(CorePlayer cp, String message) {
        cp.sendMessage(Chat.DEFAULT + message);
    }

    public static void sendMessageToPlayer(CorePlayer cp, Component text) {
        if (cp != null && cp.getPlayer() != null) {
            Component recolored = Component.empty().append(text).color(NamedTextColor.GRAY);
            cp.sendMessage(recolored);
        }
    }

    public static void sendMessageToPlayerSuccess(CorePlayer cp, Component text) {
        if (cp != null && cp.getPlayer() != null) {
            Component recolored = Component.empty().append(text).color(NamedTextColor.GREEN);
            cp.sendMessage(recolored);
        }
    }

    public static void sendMessageToPlayerError(CorePlayer cp, Component text) {
        if (cp != null && cp.getPlayer() != null) {
            Component recolored = Component.empty().append(text).color(NamedTextColor.RED);
            cp.sendMessage(recolored);
        }
    }

    public static void sendMessageToPlayerInfo(CorePlayer cp, Component text) {
        if (cp != null && cp.getPlayer() != null) {
            Component recolored = Component.empty().append(text).color(NamedTextColor.YELLOW);
            cp.sendMessage(recolored);
        }
    }

    public static void sendRequest(CorePlayer receiver, CorePlayer sender, BiConsumer<CorePlayer, CorePlayer> action, String message) {
        RequestManager.sendPlayerRequest(Core.getInstance().getChatPrefix(), receiver, sender, action, Component.text(message));
    }

    public static void sendRequest(CorePlayer receiver, String requestType, BiConsumer<CorePlayer, String> action, String message) {
        RequestManager.sendConsoleRequest(Core.getInstance().getChatPrefix(), receiver, requestType, action, Component.text(message));
    }

    public static void sendRequest(CorePlayer receiver, CorePlayer sender, BiConsumer<CorePlayer, CorePlayer> action, Component message) {
        RequestManager.sendPlayerRequest(Core.getInstance().getChatPrefix(), receiver, sender, action, message);
    }

    public static void sendRequest(CorePlayer receiver, String requestType, BiConsumer<CorePlayer, String> action, Component message) {
        RequestManager.sendConsoleRequest(Core.getInstance().getChatPrefix(), receiver, requestType, action, message);
    }

    public static void sendConfirmationButtons(CorePlayer receiver, String acceptCmd, String declineCmd) {
        Component accept = Component.text(Chat.TAG_BRACE + "[" + Chat.SUCCESS + "Accept" + Chat.TAG_BRACE + "]")
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept")))
                .clickEvent(ClickEvent.runCommand(acceptCmd));
        Component decline = Component.text(Chat.TAG_BRACE + "[" + Chat.ERROR + "Decline" + Chat.TAG_BRACE + "]")
                .hoverEvent(HoverEvent.showText(Component.text("Click to decline")))
                .clickEvent(ClickEvent.runCommand(declineCmd));

        Core.getInstance().sendMessage(receiver, Component.empty().append(accept).appendSpace().append(decline));
    }

    /**
     * Send a message from one player to another
     *
     * @param sender CorePlayer
     * @param target CorePlayer
     * @param msg    Message
     */
    public static void sendTell(CorePlayer sender, CorePlayer target, String msg) {
        Core.getInstance().sendPacket(new PacketSpigotChatTell(sender.getUniqueId(), target.getUniqueId(), msg));
    }

}
