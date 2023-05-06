/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.menu.hotbars.main;

import com.spleefleague.core.Core;
import com.spleefleague.core.menu.InventoryMenuAPI;
import com.spleefleague.core.menu.InventoryMenuItem;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.settings.Settings;
import com.spleefleague.coreapi.chat.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;


/**
 * @author NickM13
 */
public class SocialMediaMenu {

    private static InventoryMenuItem menuItem = null;

    public static void init() {
        menuItem = InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Social Media")
                .setDisplayItem(Material.MELON_SLICE, 1)
                .setSelectedItem(Material.MELON_SLICE, 2)
                .setDescription("Check us out on various media platforms!")
                .createLinkedContainer("Social Media");

        menuItem.getLinkedChest().setPageBoundaries(1, 3, 1, 7);

        menuItem.getLinkedChest().addStaticItem(InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Website")
                .setDisplayItem(Material.MELON_SLICE, 1)
                .setDescription(ChatColor.YELLOW + " ▹ Click for link ◃"), 6, 4)
                .setAction(SocialMediaMenu::sendWebsite);

        menuItem.getLinkedChest().addStaticItem(InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Online Store")
                .setDisplayItem(Material.EMERALD, 1)
                .setDescription(ChatColor.YELLOW + " ▹ Click for link ◃"), 5, 4)
                .setAction(SocialMediaMenu::sendStore);

        menuItem.getLinkedChest().addStaticItem(InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Discord")
                .setDisplayItem(Material.WRITABLE_BOOK, 4)
                .setDescription(ChatColor.YELLOW + " ▹ Click for link ◃"), 4, 4)
                .setAction(SocialMediaMenu::sendDiscord);

        menuItem.getLinkedChest().addStaticItem(InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Twitter")
                .setDisplayItem(Material.COOKED_CHICKEN, 3)
                .setDescription(ChatColor.YELLOW + " ▹ Click for link ◃"), 3, 4)
                .setAction(SocialMediaMenu::sendTwitter);
    }

    private static Component toUrl(String url) {
        return Component.empty()
                .color(NamedTextColor.BLUE)
                .append(Component.text(url))
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text(ChatColor.WHITE + "Click to open")));
    }

    private static void sendWebsite(CorePlayer corePlayer) {
        Core.getInstance().sendMessage(corePlayer, ChatColor.WHITE + "" + ChatColor.BOLD + "Visit our Website:");
        Core.getInstance().sendMessage(corePlayer, toUrl("https://spleefleague.com/"));
    }

    private static void sendStore(CorePlayer corePlayer) {
        Core.getInstance().sendMessage(corePlayer, ChatColor.WHITE + "" + ChatColor.BOLD + "Support us at our store:");
        Core.getInstance().sendMessage(corePlayer, toUrl("https://store.spleef.gg/"));
    }

    private static void sendDiscord(CorePlayer corePlayer) {
        Core.getInstance().sendMessage(corePlayer, ChatColor.WHITE + "" + ChatColor.BOLD + "Join us on Discord:");
        Core.getInstance().sendMessage(corePlayer, toUrl(Settings.getDiscord().getUrl()));
    }

    private static void sendTwitter(CorePlayer corePlayer) {
        Core.getInstance().sendMessage(corePlayer, ChatColor.WHITE + "" + ChatColor.BOLD + "Follow us on Twitter:");
        Core.getInstance().sendMessage(corePlayer, toUrl("https://twitter.com/SpleefLeague/"));
    }

    /**
     * Gets the menu item for this menu, if it doesn't exist
     * already then initialize it
     *
     * @return Inventory Menu Item
     */
    public static InventoryMenuItem getItem() {
        if (menuItem == null) init();
        return menuItem;
    }

}
