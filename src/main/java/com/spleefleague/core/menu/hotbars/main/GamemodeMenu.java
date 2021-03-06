package com.spleefleague.core.menu.hotbars.main;

import com.spleefleague.core.menu.InventoryMenuAPI;
import com.spleefleague.core.menu.InventoryMenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * @author NickM13
 * @since 4/18/2020
 */
public class GamemodeMenu {

    private static InventoryMenuItem menuItem = null;

    public static void init() {
        menuItem = InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.BLUE + "" + ChatColor.BOLD + "Games")
                .setDescription("Browse SpleefLeague's catalog of custom created games.")
                .setDisplayItem(Material.NETHER_STAR, 2)
                .setSelectedItem(Material.NETHER_STAR, 3)
                .createLinkedContainer("Games");
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
