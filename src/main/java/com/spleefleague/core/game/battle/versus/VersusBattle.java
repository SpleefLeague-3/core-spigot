package com.spleefleague.core.game.battle.versus;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.spleefleague.core.Core;
import com.spleefleague.core.chat.Chat;
import com.spleefleague.core.chat.ChatUtils;
import com.spleefleague.core.game.Arena;
import com.spleefleague.core.game.BattleMode;
import com.spleefleague.core.game.BattleUtils;
import com.spleefleague.core.game.battle.BattlePlayer;
import com.spleefleague.core.game.battle.Battle;
import com.spleefleague.core.game.history.GameHistory;
import com.spleefleague.core.game.request.EndGameRequest;
import com.spleefleague.core.game.request.PauseRequest;
import com.spleefleague.core.game.request.PlayToRequest;
import com.spleefleague.core.game.request.ResetRequest;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.util.CoreUtils;
import com.spleefleague.core.util.variable.Position;
import com.spleefleague.core.world.FakeBlock;
import com.spleefleague.core.world.FakeUtil;
import com.spleefleague.core.world.build.BuildStructure;
import com.spleefleague.core.world.build.BuildStructures;
import com.spleefleague.coreapi.utils.packet.spigot.battle.PacketSpigotBattleEnd;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author NickM13
 * @since 4/24/2020
 */
public abstract class VersusBattle<BP extends BattlePlayer> extends Battle<BP> {

    protected int playToPoints = 5;

    public VersusBattle(CorePlugin plugin, UUID battleId, List<UUID> players, Arena arena, Class<BP> battlePlayerClass, BattleMode battleMode) {
        super(plugin, battleId, players, arena, battlePlayerClass, battleMode);
    }

    /**
     * Initialize battle requests (/request)
     */
    protected void setupBattleRequests() {
        addBattleRequest(new EndGameRequest(this));
        addBattleRequest(new ResetRequest(this));
        addBattleRequest(new PlayToRequest(this));
        addBattleRequest(new PauseRequest(this));
    }

    /**
     * Initialize the players, called in startBattle()
     */
    @Override
    protected void setupBattlers() {

    }

    /**
     * Called in startBattle()<br>
     * Initialize scoreboard
     */
    protected void setupScoreboard() {
        chatGroup.setScoreboardName(ChatColor.GOLD + "" + ChatColor.BOLD + getMode().getDisplayName());
        chatGroup.addTeam("time", "00:00:00:000");
        chatGroup.addTeam("arena", ChatColor.GREEN + "  " + arena.getName());
        for (int i = 0; i < sortedBattlers.size(); i++) {
            chatGroup.addTeam("p" + i, "  " + Chat.PLAYER_NAME + "" + ChatColor.BOLD + sortedBattlers.get(i).getCorePlayer().getName());
            chatGroup.addTeam("p" + i + "score", BattleUtils.toScoreSquares(sortedBattlers.get(i), playToPoints));
        }
        updateScoreboard();
    }

    /**
     * Send a message on the start of a battle
     */
    @Override
    protected void sendStartMessage() {
        Component component = Component.empty()
                .color(NamedTextColor.GRAY)
                .append(Component.text("Starting "))
                .append(Component.text(getMode().getDisplayName()))
                .append(Component.text(" match on "))
                .append(Component.text(Chat.GAMEMAP + arena.getName()))
                .append(Component.text(" between "))
                .append(Component.text(CoreUtils.mergePlayerNames(battlers.keySet())));
        sendNotification(component);
    }

    @Override
    protected void fillField() {

    }

    /**
     * Called when a battler joins mid-game (if available)
     *
     * @param cp Core Player
     */
    @Override
    public void joinBattler(CorePlayer cp) {

    }

    /**
     * Save the battlers stats
     * Called when a battler is being removed from the battle
     *
     * @param bp Battle Player
     */
    @Override
    protected void saveBattlerStats(BP bp) {
        //Core.getInstance().getPlayers().save(bp.getCorePlayer());
    }

    /**
     * Called every 0.1 second or on score updates
     * Updates the player scoreboards
     */
    @Override
    public void updateScoreboard() {
        chatGroup.setTeamDisplayName("time", Chat.DEFAULT + getRuntimeString());
        for (int i = 0; i < sortedBattlers.size(); i++) {
            chatGroup.setTeamDisplayName("p" + i + "score", BattleUtils.toScoreSquares(sortedBattlers.get(i), playToPoints));
        }
    }

    /**
     * Called every 1/10 second
     * Updates the field on occasion for events such as
     * auto-regenerating maps
     */
    @Override
    public void updateField() {

    }

    /**
     * Updates the experience bar of players in the game
     */
    @Override
    public void updateExperience() {

    }

    @Override
    public void updatePhysicalScoreboard() {
        for (Position scoreboard : scoreboards) {
            if (sortedBattlers.size() > 0) {
                int score = sortedBattlers.get(0).getRoundWins();
                BuildStructure score0 = BuildStructures.get("score" + (score > 9 ? "+" : score));
                if (score0 != null) {
                    Map<BlockPosition, FakeBlock> blocks = score0.getFakeBlocks();
                    blocks = FakeUtil.translateBlocks(FakeUtil.rotateBlocks(blocks, (int) scoreboard.getYaw()), scoreboard.toBlockPosition());
                    gameWorld.setBlocksForced(blocks);
                }
            }
            if (sortedBattlers.size() > 1) {
                int score = sortedBattlers.get(1).getRoundWins();
                BuildStructure score1 = BuildStructures.get("score" + (score > 9 ? "+" : score));
                if (score1 != null) {
                    Map<BlockPosition, FakeBlock> blocks = score1.getFakeBlocks();
                    blocks = FakeUtil.translateBlocks(FakeUtil.rotateBlocks(FakeUtil.translateBlocks(blocks, new BlockPosition(7, 0, 0)), (int) scoreboard.getYaw()), scoreboard.toBlockPosition());
                    gameWorld.setBlocksForced(blocks);
                }
            }
        }
    }

    protected void onScorePoint(BP winner) {
        Component component = winner.getCorePlayer().getChatName().append(Component.text(" has scored a point!"));
        chatGroup.sendMessage(component);
        updatePhysicalScoreboard();
    }

    /**
     * End a round with a determined winner
     *
     * @param winner Winner
     */
    @Override
    protected void endRound(BP winner) {
        matchPointing = false;
        if (winner == null) {
            chatGroup.sendMessage(Chat.ERROR + "No other player found!");
            startRound();
            return;
        }
        winner.addRoundWin();
        if (winner.getRoundWins() < playToPoints) {
            onScorePoint(winner);
            startRound();
            if (winner.getRoundWins() == playToPoints - 1) {
                chatGroup.sendTitle(ChatColor.GOLD + "Match Point", ChatColor.GOLD + winner.getCorePlayer().getName(), 5, 50, 5);
                matchPointing = true;
            }
        } else {
            gameHistory.setEndReason(GameHistory.EndReason.NORMAL);
            endBattle(winner);
        }
    }

    protected int applyEloChange(BP winner, BP loser) {
        if (forced) {
            Component component = Component.empty()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("You have defeated "))
                    .append(loser.getCorePlayer().getChatName())
                    .append(Component.text(loser.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                    .append(Component.text(" " + winner.getRoundWins() + "-" + loser.getRoundWins()));
            winner.getCorePlayer().sendMessage(component);

            component = Component.empty()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("You have been defeated by "))
                    .append(winner.getCorePlayer().getChatName())
                    .append(Component.text(winner.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                    .append(Component.text(" " + loser.getRoundWins() + "-" + winner.getRoundWins()));
            loser.getCorePlayer().sendMessage(component);
            return 0;
        }
        int avgRating = 0;
        int winnerRating = winner.getCorePlayer().getRatings().getElo(getMode().getName(), getMode().getSeason());

        for (CorePlayer cp : battlers.keySet()) {
            int rating = cp.getRatings().getElo(getMode().getName(), getMode().getSeason());
            avgRating += rating;
        }
        avgRating /= battlers.size();

        int ratingDiff = (avgRating - winnerRating) * battlers.size();
        ratingDiff = Math.min(Math.max(ratingDiff, -750), 750);

        int eloChange = (int) (0.00001f * Math.pow(ratingDiff, 2) + 0.014f * ratingDiff + 20.f);

        for (BattlePlayer bp : battlers.values()) {
            int initialElo = bp.getCorePlayer().getRatings().getElo(getMode().getName(), getMode().getSeason());
            int toChange = bp.equals(winner) ? eloChange : -eloChange;
            bp.getCorePlayer().getRatings().addRating(getMode().getName(), getMode().getSeason(), toChange);

            Component component = Component.empty()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("You have " + (toChange >= 0 ? "defeated " : "been defeated by ")))
                    .append(bp.equals(winner) ? loser.getCorePlayer().getChatName() : winner.getCorePlayer().getChatName())
                    .append(Component.text(bp.equals(winner) ? (" " + winner.getRoundWins() + "-" + loser.getRoundWins()) : (" " + loser.getRoundWins() + "-" + winner.getRoundWins())))
                    .append(Component.text(" and " + (toChange >= 0 ? "gained " : "lost ")))
                    .append(Component.text(eloChange).color(NamedTextColor.GREEN))
                    .append(Component.text(" Rating Points ("))
                    .append(Component.text(initialElo).color(NamedTextColor.RED))
                    .append(Component.text("->"))
                    .append(Component.text(initialElo + toChange).color(NamedTextColor.GREEN))
                    .append(Component.text(")"));

            bp.getCorePlayer().sendMessage(component);
        }

        applyRewards(winner, true);

        return eloChange;
    }

    /**
     * End a battle with a determined winner
     *
     * @param winner Winner
     */
    @Override
    public void endBattle(BP winner) {
        if (winner == null) {
            Component component = Component.empty()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("Battle between " + CoreUtils.mergePlayerNames(battlers.keySet()) + " was peacefully concluded."));
            sendNotification(component);
        } else {
            BP loser = null;
            for (CorePlayer cp : battlers.keySet()) {
                if (cp != null && !cp.equals(winner.getCorePlayer())) {
                    loser = battlers.get(cp);
                    break;
                }
            }
            if (loser == null) {
                loser = winner;
                Component component = Component.empty()
                        .color(NamedTextColor.GRAY)
                        .append(winner.getCorePlayer().getChatName())
                        .append(Component.text(winner.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                        .append(Component.text(" has " + BattleUtils.randomDefeatSynonym() + " "))
                        .append(loser.getCorePlayer().getChatName())
                        .append(Component.text(loser.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                        .append(Component.text(" ("))
                        .append(Component.text(Chat.SCORE + winner.getRoundWins()))
                        .append(Component.text("-"))
                        .append(Component.text(Chat.SCORE + loser.getRoundWins()))
                        .append(Component.text(")"));
                sendNotification(component);
                gameHistory.setEndReason(GameHistory.EndReason.CANCEL);
            } else {
                for (BattlePlayer bp : battlers.values()) {
                    bp.getCorePlayer().sendMessage(Chat.colorize("             &6&l" + getMode().getDisplayName()));
                    StringBuilder linebreak = new StringBuilder(Chat.colorize("             &8"));
                    for (int i = 0; i < ChatUtils.getPixelCount(ChatColor.BOLD + getMode().getDisplayName()) / (double) (ChatUtils.getPixelCount("-")); i++) {
                        linebreak.append("-");
                    }
                    bp.getCorePlayer().sendMessage(linebreak.toString());
                }
                applyEloChange(winner, loser);

                Component component = Component.empty()
                        .color(NamedTextColor.GRAY)
                        .append(winner.getCorePlayer().getChatName())
                        .append(Component.text(winner.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                        .append(Component.text(" has " + BattleUtils.randomDefeatSynonym() + " "))
                        .append(loser.getCorePlayer().getChatName())
                        .append(Component.text(loser.getCorePlayer().getRatings().getDisplayElo(getMode().getName(), getMode().getSeason())))
                        .append(Component.text(" ("))
                        .append(Component.text(Chat.SCORE + winner.getRoundWins()))
                        .append(Component.text("-"))
                        .append(Component.text(Chat.SCORE + loser.getRoundWins()))
                        .append(Component.text(")"));
                sendNotification(component);
                gameHistory.setPlayerStats(winner.getPlayer().getUniqueId(), 0, winner.getRoundWins());
                gameHistory.setPlayerStats(loser.getPlayer().getUniqueId(), 1, loser.getRoundWins());
            }
        }
        Core.getInstance().sendPacket(new PacketSpigotBattleEnd(battleId));
        sendRequeueMessage();
        destroy();
    }

    /**
     * Called when a battler leaves boundaries
     *
     * @param cp CorePlayer
     */
    @Override
    protected void failBattler(CorePlayer cp) {
        remainingPlayers.remove(battlers.get(cp));
        if (remainingPlayers.isEmpty()) {
            endRound(null);
        } else {
            endRound(remainingPlayers.iterator().next());
        }
    }

    /**
     * Called when a battler enters a goal area
     *
     * @param cp CorePlayer
     */
    @Override
    protected void winBattler(CorePlayer cp) {
        endRound(battlers.get(cp));
    }

    /**
     * Called when a player surrenders (/ff, /leave)
     *
     * @param cp CorePlayer
     */
    @Override
    public void surrender(CorePlayer cp) {
        leaveBattler(cp);
    }

    /**
     * Called when a Play To request passes
     *
     * @param playToPoints Play To Value
     */
    @Override
    public void setPlayTo(int playToPoints) {
        this.playToPoints = playToPoints;
    }

    /**
     * Called when a battler wants to leave (/leave, /ff)
     *
     * @param cp Battler CorePlayer
     */
    @Override
    protected void leaveBattler(CorePlayer cp) {
        remainingPlayers.remove(battlers.get(cp));
        gameHistory.setEndReason(GameHistory.EndReason.FORFEIT);
        if (remainingPlayers.isEmpty()) {
            endBattle(null);
        } else {
            endBattle(remainingPlayers.iterator().next());
        }
    }

}
