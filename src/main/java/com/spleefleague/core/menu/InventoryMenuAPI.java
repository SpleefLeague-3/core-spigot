/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.menu;

import com.spleefleague.core.player.CorePlayer;

import java.util.Collection;
import java.util.function.Function;

import org.bukkit.inventory.ItemStack;

/**
 * @author NickM13
 */
public class InventoryMenuAPI {

    /**
     * Creates an InventoryMenuOverlay
     *
     * @return New InventoryMenuOverlay
     */
    public static InventoryMenuOverlay createOverlay() {
        return new InventoryMenuOverlay();
    }

    /**
     * Creates an InventoryMenuContainerChest
     *
     * @return New InventoryMenuContainerChest
     */
    public static InventoryMenuContainerChest createContainer() {
        return new InventoryMenuContainerChest();
    }

    /**
     * Creates an InventoryMenuEditor
     *
     * @return New InventoryMenuEditor
     */
    public static InventoryMenuEditor createEditor() {
        return new InventoryMenuEditor();
    }

    /**
     * Creates a base InventoryMenuItemDynamic
     *
     * @return New InventoryMenuItemDynamic
     */
    public static InventoryMenuItemDynamic createItemDynamic() {
        return new InventoryMenuItemDynamic();
    }

    /**
     * Create a base InventoryMenuItemStatic
     *
     * @return New InventoryMenuItemStatic
     */
    public static InventoryMenuItemStatic createItemStatic() {
        return new InventoryMenuItemStatic();
    }

    public static InventoryMenuItemEmpty createItemEmpty() {
        return new InventoryMenuItemEmpty();
    }

    public static InventoryMenuItemToggle createItemToggle() {
        return new InventoryMenuItemToggle();
    }

    public static InventoryMenuItemSearch createItemSearch() {
        return new InventoryMenuItemSearch();
    }

    /**
     * Creates an InventoryMenuContainerAnvil
     *
     * @return New InventoryMenuContainerAnvil
     */
    public static InventoryMenuContainerAnvil createAnvil() {
        return new InventoryMenuContainerAnvil();
    }

    /**
     * Creates and registers a new Hotbar item with a permanent slot TODO: should it be permanent?
     * and an identifier which is stored in the "hotbar" nbt
     * accessed by InventoryMenuItemHotbar::getHotbarTag
     *
     * @param slot      Slot Number
     * @param hotbarTag "hotbar" NBT String
     * @return New InventoryMenuItemHotbar
     */
    public static InventoryMenuItemHotbar createItemHotbar(int slot, String hotbarTag) {
        return new InventoryMenuItemHotbar(slot, hotbarTag);
    }

    @Deprecated
    public static InventoryMenuDialog createDialog() {
        return new InventoryMenuDialog();
    }

    public static InventoryMenuItemOption createItemOption(Function<CorePlayer, Integer> selectedFun) {
        return new InventoryMenuItemOption()
                .setSelected(selectedFun);
    }

    /**
     * Returns a collection of all hotbar items
     *
     * @return All Hotbar Items
     */
    public static Collection<InventoryMenuItemHotbar> getHotbarItems() {
        return InventoryMenuItemHotbar.getHotbarItems().values();
    }

    /**
     * @param item ItemStack
     * @return Whether item is a registered Hotbar Item
     */
    public static boolean isHotbarItem(ItemStack item) {
        if (item == null) return false;
        return InventoryMenuItemHotbar.isHotbarItem(item);
    }

    /**
     * Returns the Hotbar menu item by the hotbar nbt tag, or
     * null if it isn't a Hotbar item
     *
     * @param item ItemStack
     * @return InventoryMenuItemHotbar
     */
    public static InventoryMenuItemHotbar getHotbarItem(ItemStack item) {
        return InventoryMenuItemHotbar.getHotbarItem(item);
    }

}
