/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.request;

import com.spleefleague.core.chat.Chat;
import com.spleefleague.core.player.CorePlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

/**
 * @author NickM13
 */
public class RequestManager {

    // Receiver, <Sender, Request>
    protected static Map<String, Map<UUID, Request>> requests = new HashMap<>();

    protected static void validatePlayer(CorePlayer receiver) {
        if (!requests.containsKey(receiver.getName())) {
            requests.put(receiver.getName(), new HashMap<>());
        }
    }

    public static void checkTimeouts() {
        for (Map.Entry<String, Map<UUID, Request>> r : requests.entrySet()) {
            Iterator<Map.Entry<UUID, Request>> sit = r.getValue().entrySet().iterator();
            while (sit.hasNext()) {
                Map.Entry<UUID, Request> sn = sit.next();
                if (sn.getValue().isExpired()) {
                    sn.getValue().timeout();
                    sit.remove();
                }
            }
        }
    }

    public static boolean acceptRequest(CorePlayer receiver, UUID uuid) {
        validatePlayer(receiver);
        if (requests.get(receiver.getName()).containsKey(uuid)) {
            requests.get(receiver.getName()).get(uuid).accept();
            requests.get(receiver.getName()).remove(uuid);
            return true;
        } else {
            //Core.sendMessageToPlayer(receiver, "Request no longer exists");
            return false;
        }
    }

    public static boolean declineRequest(CorePlayer receiver, UUID uuid) {
        validatePlayer(receiver);
        if (requests.get(receiver.getName()).containsKey(uuid)) {
            requests.get(receiver.getName()).get(uuid).decline();
            requests.get(receiver.getName()).remove(uuid);
            return true;
        } else {
            return false;
        }
    }

    public static void sendPlayerRequest(Component tag, CorePlayer receiver, CorePlayer target, BiConsumer<CorePlayer, CorePlayer> action, Component message) {
        PlayerRequest request = new PlayerRequest(action, receiver, tag, target);
        sendRequest(tag, receiver, target.getName(), request, message);
    }

    public static void sendConsoleRequest(Component tag, CorePlayer receiver, String target, BiConsumer<CorePlayer, String> action, Component message) {
        ConsoleRequest request = new ConsoleRequest(action, receiver, tag, target);
        sendRequest(tag, receiver, target, request, message);
    }

    public static void sendRequest(Component tag, CorePlayer receiver, String target, Request request, Component message) {
        validatePlayer(receiver);
        UUID uuid = UUID.randomUUID();
        requests.get(receiver.getName()).put(uuid, request);

        Component accept = Component.text(Chat.TAG_BRACE + "[" + Chat.SUCCESS + "Accept" + Chat.TAG_BRACE + "]")
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept")))
                .clickEvent(ClickEvent.runCommand("/request accept " + uuid));
        Component decline = Component.text(Chat.TAG_BRACE + "[" + Chat.ERROR + "Decline" + Chat.TAG_BRACE + "]")
                .hoverEvent(HoverEvent.showText(Component.text("Click to decline")))
                .clickEvent(ClickEvent.runCommand("/request decline " + uuid));

        receiver.sendMessage(Component.empty().append(tag).appendSpace().append(message));
        receiver.sendMessage(Component.empty().append(tag).append(accept).appendSpace().append(decline));
    }

}
