package rip.protocol.bridge;

import lombok.Getter;
import lombok.Setter;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import rip.protocol.bridge.bungee.BridgeBungee;
import rip.protocol.bridge.bungee.BridgeBungeeRef;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.disguise.Skin;
import rip.protocol.bridge.shared.managers.ProfileManager;
import rip.protocol.bridge.shared.managers.PunishmentManager;
import rip.protocol.bridge.shared.managers.RankManager;
import rip.protocol.bridge.shared.packet.PacketHandler;
import rip.protocol.bridge.shared.packet.listener.ChristianPacketListener;
import rip.protocol.bridge.shared.packets.*;
import rip.protocol.bridge.shared.status.StatusHandler;
import rip.protocol.bridge.shared.status.StatusThread;
import rip.protocol.bridge.shared.updater.UpdaterManager;
import rip.protocol.bridge.shared.utils.SystemType;

import java.io.File;

public class BridgeShared {

    @Getter @Setter public static SystemType systemType = SystemType.UNKNOWN;

    @Getter private static boolean mongoAuth, redisAuth, kickIfPunished;

    @Getter private static int mongoPort, redisPort, redisDB;
    @Getter private static String mongoHost, mongoUsername, mongoPassword, mongoDatabase, redisHost, redisPassword, redisChannel, serverName, serverDisplayName, serverWebsite, pluginUpdateDir, groupName;

    @Getter private static MongoManager mongoManager;
    @Getter private static RankManager rankManager;
    @Getter private static ProfileManager profileManager;
    @Getter private static PunishmentManager punishmentManager;
    @Getter private static PacketHandler packetHandler;
    @Getter private static UpdaterManager updaterManager;

    public static StatusThread statusThread;

    public BridgeShared() {
        new BridgeShared("", "", "", "", 27017, false, "", "", "", 0, 6379, false, true);
    }

    public BridgeShared(String mongoHost, String mongoUsername, String mongoPassword, String mongoDatabase, int mongoPort, boolean mongoAuth, String redisHost, String redisPassword, String redisChannel, int redisDB, int redisPort, boolean redisAuth, boolean getFromConfig) {
        setSystemType(currentSystemType());
        sendLog("We have detected that Bridge is running on System Type: " + getSystemType().name());

        if(getFromConfig) {
            setupConfigValues();
        }else {
            BridgeShared.mongoAuth = mongoAuth;
            BridgeShared.mongoPort = mongoPort;
            BridgeShared.mongoHost = mongoHost;
            BridgeShared.mongoUsername = mongoUsername;
            BridgeShared.mongoPassword = mongoPassword;
            BridgeShared.mongoDatabase = mongoDatabase;
            BridgeShared.redisAuth = redisAuth;
            BridgeShared.redisHost = redisHost;
            BridgeShared.redisPort = redisPort;
            BridgeShared.redisPassword = redisPassword;
            BridgeShared.redisChannel = redisChannel;
            BridgeShared.redisDB = redisDB;
        }
        sendLog("Loading Mongo backend...");
        mongoManager = new MongoManager();

        sendLog("Loading Ranks");
        rankManager = new RankManager();
        rankManager.init();

        profileManager = new ProfileManager();
        profileManager.init();
        sendLog("Successfully loaded the Profile Manager");

        new Thread(() -> {
            punishmentManager = new PunishmentManager();
            punishmentManager.init();
            sendLog("Successfully loaded the Punishment Manager");
        }).start();

        sendLog("Setting up Packet System (Redis)");
        packetHandler = new PacketHandler(redisChannel);

        sendLog("Registering all packets");
        getPacketHandler().registerPackets(RankCreatePacket.class, RankDeletePacket.class, RankUpdatePacket.class, GrantCreatePacket.class, PunishmentPacket.class);

        sendLog("Registering Packet Listener");
        getPacketHandler().registerListener(new ChristianPacketListener());

        sendLog("Setting up status handler");
        StatusHandler.init();

        new Skin();

        if(pluginUpdateDir != null && !pluginUpdateDir.isEmpty()) {
            sendLog("Setting up the update handler");
            updaterManager = new UpdaterManager();
        }

        sendLog("Completed Bridge Backend - loaded " + getSystemType() + " system type.");

    }

    private void setupConfigValues() {
        mongoAuth = Boolean.parseBoolean(getFromConfig("mongo.auth.enabled"));
        mongoPort = Integer.parseInt(getFromConfig("mongo.port"));
        mongoHost = getFromConfig("mongo.host");
        mongoUsername = getFromConfig("mongo.auth.username");
        mongoPassword = getFromConfig("mongo.auth.password");
        mongoDatabase = getFromConfig("mongo.database");

        redisAuth = !getFromConfig("redis.password").equals("");
        redisHost = getFromConfig("redis.host");
        redisPort = Integer.parseInt(getFromConfig("redis.port"));
        redisPassword = getFromConfig("redis.password");
        redisChannel = getFromConfig("redis.channel");
        redisDB = Integer.parseInt(getFromConfig("redis.db"));
        serverName = getFromConfig("servername");
        kickIfPunished = Boolean.parseBoolean(getFromConfig("kickIfPunished"));
        serverDisplayName = getFromConfig("serverDisplayName");
        serverWebsite = getFromConfig("serverWebsite");
        pluginUpdateDir = getFromConfig("pluginUpdateDirectory");
        groupName = getFromConfig("servergroup");
    }

    public static void shutdown() {
        StatusHandler.shutdown();
        getProfileManager().saveDisable();
        getRankManager().saveDisable();
        getMongoManager().getMongoClient().close();
    }

    private static SystemType currentSystemType() {
        try {
            Class.forName("net.md_5.bungee.BungeeCord");
            return SystemType.BUNGEE;
        } catch (ClassNotFoundException ignored) {}
        try {
            Class.forName("org.bukkit.Bukkit");
            return SystemType.BUKKIT;
        } catch (ClassNotFoundException ignored) {}
        return SystemType.UNKNOWN;
    }

    public static String getSystemName() {
        switch (getSystemType()) {
            case BUNGEE:
            case BUKKIT: {
                return serverName;
            }
            default: {
                return "Custom Java Applet";
            }
        }
    }

    public static void sendLog(String msg, boolean packetIncoming) {
        String logMessage = "%prefix% §r" + msg;
        switch (systemType) {
            case BUNGEE:
                BridgeBungeeRef.logMessages(msg.replace("%prefix%", "[Bridge]"));
                return;
            case BUKKIT:
                BridgeBukkitRef.logMessages(msg.replace("%prefix%", "[Bridge]"), packetIncoming);
                return;
            default:
                System.out.println(logMessage.replace("%prefix%", "[Bridge]").replace("§", ""));
        }
    }

    public static void sendLog(String msg) {
        sendLog(msg, false);
    }


    public static String getFromConfig(String path) {
        switch(systemType) {
            case BUNGEE: {
                return BridgeBungee.getConfig().getString(path);
            }
            case UNKNOWN: {
                return null;
            }
            case BUKKIT: {
                return Bridge.getInstance().getConfig().getString(path);
            }
        }
        return null;
    }



}
