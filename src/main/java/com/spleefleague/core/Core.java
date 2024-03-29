package com.spleefleague.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.Lists;
import com.spleefleague.core.chat.Chat;
import com.spleefleague.core.chat.ticket.Tickets;
import com.spleefleague.core.command.CommandManager;
import com.spleefleague.core.command.CoreCommand;
import com.spleefleague.core.crate.CrateManager;
import com.spleefleague.core.game.BattleSessionManager;
import com.spleefleague.core.game.arena.Arenas;
import com.spleefleague.core.game.battle.team.TeamInfo;
import com.spleefleague.core.game.history.GameHistoryManager;
import com.spleefleague.core.game.leaderboard.LeaderboardManager;
import com.spleefleague.core.infraction.CoreInfractionManager;
import com.spleefleague.core.listener.*;
import com.spleefleague.core.logger.CoreLogger;
import com.spleefleague.core.menu.InventoryMenuSkullManager;
import com.spleefleague.core.menu.hotbars.AfkHotbar;
import com.spleefleague.core.menu.hotbars.SLMainHotbar;
import com.spleefleague.core.menu.hotbars.main.socialmedia.Credits;
import com.spleefleague.core.menu.overlays.SLMainOverlay;
import com.spleefleague.core.music.NoteBlockMusic;
import com.spleefleague.core.packet.PacketManager;
import com.spleefleague.core.player.CoreOfflinePlayer;
import com.spleefleague.core.player.CorePlayer;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.player.collectible.Collectible;
import com.spleefleague.core.player.party.CorePartyManager;
import com.spleefleague.core.player.rank.CoreRankManager;
import com.spleefleague.core.player.scoreboard.PersonalScoreboard;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.queue.PlayerQueue;
import com.spleefleague.core.queue.QueueManager;
import com.spleefleague.core.request.RequestManager;
import com.spleefleague.core.settings.Settings;
import com.spleefleague.core.util.variable.Warp;
import com.spleefleague.core.vendor.Artisans;
import com.spleefleague.core.world.FakeWorld;
import com.spleefleague.core.world.build.BuildWorld;
import com.spleefleague.coreapi.database.variable.DBPlayer;
import com.spleefleague.coreapi.utils.packet.bungee.refresh.PacketBungeeRefreshServerList;
import com.spleefleague.coreapi.utils.packet.spigot.PacketSpigot;
import com.spleefleague.coreapi.utils.packet.spigot.server.PacketSpigotServerHub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * SpleefLeague's Core Plugin
 *
 * @author NickM13
 */
public class Core extends CorePlugin {

    private static Core instance;
    public static World OVERWORLD;
    public static World LEVIATHON;
    private QueueManager queueManager;
    private final LeaderboardManager leaderboards = new LeaderboardManager();
    private final CommandManager commandManager = new CommandManager();

    // For packet managing
    private static ProtocolManager protocolManager;

    private final Set<String> lobbyServers = new HashSet<>();
    private final Set<String> minigameServers = new HashSet<>();
    private final Set<String> servers = new HashSet<>();

    private final CorePartyManager partyManager = new CorePartyManager();
    private final CoreRankManager rankManager = new CoreRankManager();
    private final CrateManager crateManager = new CrateManager();
    private final PacketManager packetManager = new PacketManager();
    private final BattleSessionManager battleSessionManager = new BattleSessionManager();
    private PlayerManager<CorePlayer, CoreOfflinePlayer> playerManager;
    private final CoreInfractionManager coreInfractionManager = new CoreInfractionManager();

    /**
     * Called when the plugin is enabling
     */
    @Override
    public void init() {
        instance = this;

        OVERWORLD = Bukkit.getWorlds().get(0);
        System.out.println(Bukkit.getWorlds());
        LEVIATHON = Bukkit.getWorld("world_the_end");
        protocolManager = ProtocolLibrary.getProtocolManager();

        CorePlugin.initMongo();
        initConfig();
        setPluginDB("SpleefLeague");

        rankManager.init();
        Credits.init();
        Chat.init();
        Warp.init();
        Collectible.init();
        Tickets.init();
        FakeWorld.init();
        Arenas.init();
        NoteBlockMusic.init();
        PersonalScoreboard.init();
        Settings.init();
        InventoryMenuSkullManager.init();
        TeamInfo.init();
        GameHistoryManager.init();

        // Initialize manager
        coreInfractionManager.init();
        playerManager = new PlayerManager<>(this, CorePlayer.class, CoreOfflinePlayer.class, getPluginDB().getCollection("Players"));
        crateManager.init();
        packetManager.init();
        battleSessionManager.init();

        Artisans.init();

        // Initialize listeners
        initListeners();

        // Initialize various things
        initQueues();
        initCommands();
        initMenus();
        initTabList();

        leaderboards.init();

        // TODO: Move this?
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getPlayers().getAllLocal().stream().iterator().forEachRemaining(cp -> {
                cp.checkTempRanks();
                cp.checkAfk();
            });
            RequestManager.checkTimeouts();
        }, 0L, 100L);
    }

    /**
     * Called when the plugin is disabling
     */
    @Override
    public void close() {
        for (BukkitTask task : taskList) {
            task.cancel();
        }
        BuildWorld.close();
        Warp.close();
        Collectible.close();
        Artisans.close();
        Tickets.close();
        leaderboards.close();
        NoteBlockMusic.close();
        GameHistoryManager.close();
        coreInfractionManager.close();
        playerManager.close();
        packetManager.close();
        battleSessionManager.close();
        running = false;
        protocolManager.removePacketListeners(Core.getInstance());
        ProtocolLibrary.getPlugin().onDisable();
        CorePlugin.closeMongo();
    }

    public static Core getInstance() {
        return instance;
    }

    public CorePartyManager getPartyManager() {
        return partyManager;
    }

    public CoreRankManager getRankManager() {
        return rankManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public BattleSessionManager getBattleSessionManager() {
        return battleSessionManager;
    }

    protected void refresh(Set<UUID> players) {

    }

    public void updateServerList(PacketBungeeRefreshServerList packet) {
        servers.clear();
        lobbyServers.clear();
        minigameServers.clear();
        servers.addAll(packet.lobbyServers);
        lobbyServers.addAll(packet.lobbyServers);
        minigameServers.addAll(packet.minigameServers);
    }

    public Set<String> getServers() {
        return servers;
    }

    public Set<String> getLobbyServers() {
        return lobbyServers;
    }

    public Set<String> getMinigameServers() {
        return minigameServers;
    }

    private static final List<BukkitTask> taskList = new ArrayList<>();

    public void addTask(BukkitTask task) {
        taskList.add(task);
    }

    /**
     * Initialize Bukkit event listener objects
     */
    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new AfkListener(), this);
        Bukkit.getPluginManager().registerEvents(new BattleListener(), this);
        Bukkit.getPluginManager().registerEvents(new BuildListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new EnvironmentListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "slcore:spigot");

        getServer().getMessenger().registerIncomingPluginChannel(this, "slcore:bungee", new BungeePluginListener());
    }

    /**
     * Initialize commands, uses a reflection library in Core to copy
     * all commands from the commands package
     * <p>
     * Does not work with sub-plugins
     */
    private void initCommands() {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(true), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.spleefleague.core.command.commands"))));

        initCommands(reflections);
    }

    public void initCommands(Reflections reflections) {
        Set<Class<? extends CoreCommand>> subTypes = reflections.getSubTypesOf(CoreCommand.class);

        subTypes.forEach(st -> {
            try {
                addCommand(st.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException
                    | NoSuchMethodException | InvocationTargetException exception) {
                //CoreLogger.logError(exception);
            }
        });
    }

    public enum ServerType {
        LOBBY,
        MINIGAME
    }

    private ServerType serverType = ServerType.MINIGAME;

    /**
     * Parse plugin's config.yml file
     */
    public void initConfig() {
        FileConfiguration config = this.getConfig();
        if (config.getString("serverType") == null) {
            CoreLogger.logError("Null Server Type");
            config.set("serverType", serverType.name());
        } else {
            serverType = ServerType.valueOf(config.getString("serverType"));
        }
        config.options().copyDefaults(false);
        saveConfig();
    }

    public ServerType getServerType() {
        return serverType;
    }

    /**
     * Initialize menus
     */
    private void initMenus() {
        AfkHotbar.init();
        SLMainHotbar.init();

        SLMainOverlay.init();
    }

    @Override
    public void reloadCollectibles() {
        Collectible.reload();
    }

    @Override
    public void reloadSettings() {
        super.reloadSettings();
    }

    @Override
    public void reloadArenas() {
        super.reloadArenas();
    }

    /**
     * Hide/show players based on in-game state and
     * vanished state
     *
     * @param cp Core Player
     */
    public void applyVisibilities(CorePlayer cp) {
        if (cp == null || cp.getOnlineState() != DBPlayer.OnlineState.HERE) return;
        if (cp.isGhosting()) {
            cp.getPlayer().hidePlayer(Core.getInstance(), cp.getPlayer());
        } else {
            cp.getPlayer().showPlayer(Core.getInstance(), cp.getPlayer());
        }
        // See and become seen by all players outside of games
        // getOnline doesn't return vanished players
        for (CorePlayer cp2 : getPlayers().getAllLocal()) {
            if (!cp.equals(cp2)) {
                if (cp.getBattle() == cp2.getBattle() &&
                        cp.getBuildWorld() == cp2.getBuildWorld()) {
                    if (!cp2.isGhosting()) cp.getPlayer().showPlayer(this, cp2.getPlayer());
                    else cp.getPlayer().hidePlayer(this, cp2.getPlayer());
                    if (!cp.isGhosting()) cp2.getPlayer().showPlayer(this, cp.getPlayer());
                    else cp2.getPlayer().hidePlayer(this, cp.getPlayer());
                } else {
                    cp.getPlayer().hidePlayer(this, cp2.getPlayer());
                    cp2.getPlayer().hidePlayer(this, cp.getPlayer());
                }
            }
        }
    }

    public void returnToHub(CoreOfflinePlayer cp) {
        if (cp == null || !cp.isOnline()) return;
        Core.getInstance().sendPacket(new PacketSpigotServerHub(Lists.newArrayList(cp)));
    }

    /**
     * Initialize Tab List packet listener that prevents players from
     * being removed upon entering a game and becoming invisible, and
     * removes players from Tab List when they become vanished
     */
    public void initTabList() {
        /*
        addProtocolPacketAdapter(new PacketAdapter(Core.getInstance(), PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent pe) {
                if (pe.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
                    PacketContainer packet = pe.getPacket();
                    if (packet.getPlayerInfoAction().read(0) == EnumWrappers.PlayerInfoAction.REMOVE_PLAYER) {
                        List<PlayerInfoData> newData = new ArrayList<>();
                        for (PlayerInfoData playerInfoData : packet.getPlayerInfoDataLists().read(0)) {
                            CoreOfflinePlayer cp = Core.getInstance().getPlayers().get(playerInfoData.getProfile().getUUID());
                            if (cp == null || cp.getOnlineState() != DBPlayer.OnlineState.HERE) {
                                newData.add(playerInfoData);
                            }
                        }
                        if (newData.isEmpty()) {
                            pe.setCancelled(true);
                        } else {
                            packet.getPlayerInfoDataLists().write(0, newData);
                        }
                    }
                }
            }
        });
         */
    }

    public static void addProtocolPacketAdapter(PacketAdapter packetAdapter) {
        protocolManager.addPacketListener(packetAdapter);
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }
    /**
     * Player manager that contains all online players for this plugin
     *
     * @return Player Manager
     */
    public final PlayerManager<CorePlayer, CoreOfflinePlayer> getPlayers() {
        return playerManager;
    }

    public final CoreInfractionManager getInfractionManager() {
        return coreInfractionManager;
    }

    /**
     * Sends a packet to a single player
     *
     * @param p      Player
     * @param packet Packet Container
     */
    public static void sendPacket(@Nonnull Player p, PacketContainer packet) {
        if (packet == null) return;
        protocolManager.sendServerPacket(p, packet);
    }

    /**
     * Sends a packet to a single player
     *
     * @param cp     Core Player
     * @param packet Packet Container
     */
    public static void sendPacket(CorePlayer cp, PacketContainer packet) {
        if (packet == null) return;
        Bukkit.getScheduler().runTask(Core.getInstance(), () -> {
            if (cp.getPlayer() != null) protocolManager.sendServerPacket(cp.getPlayer(), packet);
        });
    }

    public static void sendPacket(CorePlayer cp, PacketContainer packet, long delay) {
        if (packet == null) return;
        Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
            if (cp.getPlayer() != null) protocolManager.sendServerPacket(cp.getPlayer(), packet);
        }, delay);
    }

    public static void sendPacketSilently(@Nonnull Player p, PacketContainer packet) {
        if (packet == null) return;
        protocolManager.sendServerPacket(p, packet, null, false);
    }

    public static void sendPacketSilently(@Nonnull Player p, PacketContainer packet, long delay) {
        if (packet == null) return;
        Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
            protocolManager.sendServerPacket(p, packet, null, false);
        }, delay);
    }

    /**
     * Sends a packet to all online players
     *
     * @param packet Packet Container
     */
    public static void sendPacketAll(PacketContainer packet) {
        if (packet == null) return;
        for (CorePlayer cp : Core.getInstance().getPlayers().getAllLocal()) {
            sendPacket(cp, packet);
        }
    }

    /**
     * Initialize queue runnable thread
     */
    private void initQueues() {
        queueManager = new QueueManager();
        queueManager.initialize();

        Bukkit.getScheduler().runTaskTimer(this, () -> queueManager.getQueues().forEach(PlayerQueue::checkQueue), 0L, 20L);
    }

    /**
     * Add a queue to the Queue Manager
     *
     * @param queue Player Queue
     */
    public void addQueue(PlayerQueue queue) {
        queueManager.addQueue(queue);
    }

    /**
     * Unqueue a player from all Queues
     *
     * @param cp Core Player
     * @return Success
     */
    public boolean unqueuePlayerGlobally(CorePlayer cp) {
        boolean unqueued = false;
        for (PlayerQueue pq : queueManager.getQueues()) {
            unqueued = unqueued || pq.unqueuePlayer(cp);
        }
        return unqueued;
    }

    /**
     * @return Queue Manager
     */
    public QueueManager getQueueManager() {
        return queueManager;
    }

    public LeaderboardManager getLeaderboards() {
        return leaderboards;
    }

    /**
     * Returns list of players that are less than maxDist
     * and further than minDist from location
     *
     * @param loc     Location
     * @param minDist Minimum Distance
     * @param maxDist Maximum Distance
     * @return Player List
     */
    public List<CorePlayer> getPlayersInRadius(Location loc, Double minDist, Double maxDist) {
        List<CorePlayer> cpList = new ArrayList<>();

        for (CorePlayer cp1 : playerManager.getAllLocal()) {
            if (loc.getWorld() != null
                    && loc.getWorld().equals(cp1.getLocation().getWorld())
                    && loc.distance(cp1.getLocation()) >= minDist
                    && loc.distance(cp1.getLocation()) <= maxDist) {
                boolean inserted = false;
                for (int i = 0; i < cpList.size(); i++) {
                    CorePlayer cp2 = cpList.get(i);
                    if (loc.distance(cp1.getLocation()) < loc.distance(cp2.getLocation())) {
                        cpList.add(i, cp1);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    cpList.add(cp1);
                }
            }
        }

        return cpList;
    }

    /**
     * Register a command to Command Manager
     *
     * @param command CommandTemplate
     */
    public void addCommand(CoreCommand command) {
        commandManager.addCommand(command);
    }

    /**
     * @return Chat Prefix
     */
    @Override
    public Component getChatPrefix() {
        return Component.text("仳 ", NamedTextColor.WHITE);
    }

    /**
     * Send a packet to all servers with 1 or more players
     */
    public void sendPacket(PacketSpigot packet) {
        packetManager.sendPacket(packet);
    }

}
