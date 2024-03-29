package com.spleefleague.core.menu.hotbars.main;

import com.spleefleague.core.Core;
import com.spleefleague.core.crate.Crate;
import com.spleefleague.core.crate.CrateLoot;
import com.spleefleague.core.menu.InventoryMenuAPI;
import com.spleefleague.core.menu.InventoryMenuItem;
import com.spleefleague.core.menu.InventoryMenuUtils;
import com.spleefleague.core.world.projectile.global.GlobalWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author NickM13
 * @since 2/2/2021
 */
public class CrateMenu {

    private static InventoryMenuItem menuItem = null;

    public static void init() {
        menuItem = InventoryMenuAPI.createItemDynamic()
                .setName(ChatColor.GREEN + "" + ChatColor.BOLD + "Crates")
                .setDisplayItem(Material.YELLOW_SHULKER_BOX, 3)
                .setSelectedItem(Material.YELLOW_SHULKER_BOX, 4)
                .setDescription("")
                .createLinkedContainer("Crates");

        menuItem.getLinkedChest()
                .addDeadSpace(0, 0)
                .addDeadSpace(0, 1)
                .addDeadSpace(0, 2)
                .addDeadSpace(0, 3)
                .addDeadSpace(0, 4)

                .addDeadSpace(3, 0)
                .addDeadSpace(3, 1)
                .addDeadSpace(3, 2)
                .addDeadSpace(3, 3)
                .addDeadSpace(3, 4)

                .addDeadSpace(4, 0)
                .addDeadSpace(4, 1)
                .addDeadSpace(4, 2)
                .addDeadSpace(4, 3)
                .addDeadSpace(4, 4);

        menuItem.getLinkedChest()
                .setRefreshAction((container, cp) -> {
                    container.clear();
                    for (Crate crate : Core.getInstance().getCrateManager().getSortedCrates()) {
                        int crateCount = cp.getCrates().getCrateCount(crate.getIdentifier());
                        if (crateCount <= 0 && crate.isHidden()) {
                            continue;
                        }

                        if (crateCount > 0) {
                            container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                    .setName("Open Crate")
                                    .setDisplayItem(InventoryMenuUtils.MenuIcon.ENABLED.getIconItem(crateCount))
                                    .setAction(cp2 -> {
                                        CrateLoot loot = cp.getCrates().openCrate(crate.getIdentifier());
                                        int delay = 0;
                                        GlobalWorld.getGlobalWorld(Core.OVERWORLD).addRotationItem(cp, crate.getOpened());
                                        for (CrateLoot.CrateLootItem item : loot.items) {
                                            if (item.lootType == CrateLoot.CrateLootItem.LootType.COLLECTIBLE) {
                                                container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                                        .setDisplayItem(item.collectible.getDisplayItem())
                                                        .setName(item.collectible.getDisplayName()));
                                                Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
                                                    if (item.replacement == null) {
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "You received the " + item.collectible.getDisplayName() + ChatColor.GRAY + "!");
                                                    } else {
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "You received another " + item.collectible.getDisplayName() + ChatColor.GRAY + "!");
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "Converted to a " + item.replacement.color + item.replacement.displayName + ChatColor.GRAY + "!");
                                                    }
                                                    double height = 0.05;
                                                    if (item.collectible.getParentType().equalsIgnoreCase("Shovel")) {
                                                        height += 0.2;
                                                    }
                                                    GlobalWorld.getGlobalWorld(Core.OVERWORLD).addRotationItem(cp, item.collectible.getDisplayItem(), height);
                                                }, delay += 20);
                                            } else if (item.lootType == CrateLoot.CrateLootItem.LootType.SKIN) {
                                                container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                                        .setDisplayItem(item.skin.getDisplayItem())
                                                        .setName(item.skin.getDisplayName()));
                                                Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
                                                    if (item.replacement == null) {
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "You received the " + item.skin.getFullDisplayName() + ChatColor.GRAY + "!");
                                                    } else {
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "You received another " + item.skin.getFullDisplayName() + ChatColor.GRAY + "!");
                                                        Core.getInstance().sendMessage(cp,
                                                                ChatColor.GRAY + "Converted to a " + item.replacement.color + item.replacement.displayName + ChatColor.GRAY + "!");
                                                    }
                                                    double height = 0.05;
                                                    if (item.skin.getParent().getParentType().equalsIgnoreCase("Shovel")) {
                                                        height += 0.2;
                                                    }
                                                    GlobalWorld.getGlobalWorld(Core.OVERWORLD).addRotationItem(cp, item.skin.getDisplayItem(), height);
                                                }, delay += 20);
                                            } else {
                                                ItemStack itemStack = item.currency.displayItem.clone();
                                                itemStack.setAmount(item.amount);
                                                container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                                        .setDisplayItem(itemStack)
                                                        .setName(item.currency.displayName));
                                                Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
                                                    Core.getInstance().sendMessage(cp,
                                                            ChatColor.GRAY + "You received " + item.currency.color + item.amount + " " +
                                                                    item.currency.displayName + (item.amount != 1 ? "s" : "") + ChatColor.GRAY + "!");
                                                    GlobalWorld.getGlobalWorld(Core.OVERWORLD).addRotationItem(cp, item.currency.displayItem, -0.15);
                                                }, delay += 20);
                                            }
                                        }
                                    })
                                    .setCloseOnAction(true));
                        } else {
                            container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                    .setName("0 Crates, buy more at the store!")
                                    .setDisplayItem(InventoryMenuUtils.MenuIcon.DISABLED.getIconItem())
                                    .setCloseOnAction(false));
                        }

                        container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                .setName(crate.getDisplayName())
                                .setDisplayItem(crate.getClosed())
                                .setDescription(crate.getDescription())
                                .setCloseOnAction(false));

                        /*
                        container.addMenuItem(InventoryMenuAPI.createItemStatic()
                                .setName("Crates Online Store")
                                .setDisplayItem(InventoryMenuUtils.MenuIcon.STORE.getIconItem())
                                .setCloseOnAction(false));
                        */
                    }
                });
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
