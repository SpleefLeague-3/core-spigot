/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.game.manager;

import com.spleefleague.core.Core;
import com.spleefleague.core.game.BattleMode;
import com.spleefleague.core.game.battle.Battle;
import com.spleefleague.core.player.CorePlayer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BattleManager contains a queue system, list of ongoing battles
 *
 * @author NickM13
 */
public abstract class BattleManager {

    /**
     * Returns a new battle manager based on the team style of the passed ArenaMode
     *
     * @param mode Arena Mode
     * @return New Battle Manager
     */
    public static BattleManager createManager(BattleMode mode) {
        BattleManager bm = switch (mode.getTeamStyle()) {
            case SOLO -> new BattleManagerSolo(mode);
            case TEAM -> new BattleManagerTeam(mode);
            case DYNAMIC -> new BattleManagerDynamic(mode);
            case VERSUS -> new BattleManagerVersus(mode);
            case BONANZA -> new BattleManagerBonanza(mode);
        };
        bm.init();
        return bm;
    }

    protected final String name;
    protected final String displayName;
    protected final BattleMode mode;
    protected Class<? extends Battle<?>> battleClass;

    protected final List<Battle<?>> battles;

    protected BattleManager(BattleMode mode) {
        this.name = mode.getName();
        this.displayName = mode.getDisplayName();
        this.mode = mode;
        this.battleClass = mode.getBattleClass();

        this.battles = new ArrayList<>();
    }

    /**
     * Initializes task timers to update battles, removing ones
     * that are marked for removal, updating scores and countdown,
     * and updating the field and experience bar for timers
     */
    public void init() {
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> battles.forEach(Battle::ping), 0L, 30 * 20L);
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            Iterator<? extends Battle<?>> bit = battles.iterator();
            Battle<?> b;
            while (bit.hasNext()) {
                b = bit.next();
                if (b != null) {
                    if (b.isDestroyed()) {
                        bit.remove();
                    } else {
                        b.doCountdown();
                        b.updateGhosts();
                    }
                }
            }
        }, 0L, 20L);
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            Iterator<? extends Battle<?>> bit = battles.iterator();
            Battle<?> b;
            while (bit.hasNext()) {
                b = bit.next();
                if (b != null) {
                    if (!b.isWaiting()) {
                        if (b.isOngoing()) {
                            b.updateScoreboard();
                            b.updateExperience();
                        }
                    } else {
                        b.checkWaiting();
                    }
                }
            }
        }, 0L, 2L);
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            Iterator<? extends Battle<?>> bit = battles.iterator();
            Battle<?> b;
            while (bit.hasNext()) {
                b = bit.next();
                if (b != null && b.isOngoing()) {
                    b.updateField();
                }
            }
        }, 0L, 1L);
    }

    /**
     * Terminates battles
     */
    public void close() {
        for (Battle<?> battle : battles) {
            battle.destroy();
        }
        battles.clear();
    }

    /**
     * Returns the number of battles currently contained by this manager
     *
     * @return Battle Count
     */
    public int getOngoingBattles() {
        return battles.size();
    }

    /**
     * Returns the number of players currently in all of the battles in
     * this manager including spectators
     *
     * @return Player Count
     */
    public int getIngamePlayers() {
        int players = 0;
        for (Battle<?> battle : battles) {
            players += battle.getBattlers().size();
        }
        return players;
    }

    public abstract void startMatch(List<CorePlayer> corePlayers, String name);

    public void startMatch(Battle<?> battle) {
        battles.add(battle);
    }

    public void endMatch(Battle<?> battle) {
        battles.remove(battle);
    }

}
