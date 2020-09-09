package rip.protocol.bridge.shared.status.impltype;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import rip.protocol.bridge.shared.status.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FoxtrotHandler {

    public static boolean isDeathbanned(String server, UUID p) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement deathban = ServerInfo.getData(server).getAsJsonObject("deathban").get(p.toString());
        return deathban != null && Boolean.parseBoolean(deathban.getAsJsonObject().get("deathbanned").toString());
    }

    public static long getDeathbanTime(String server, UUID p) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement deathban = ServerInfo.getData(server).getAsJsonObject("deathban").get(p.toString());
        return (deathban == null ? null : (Boolean.parseBoolean(deathban.getAsJsonObject().get("deathbanned").toString()) ? (Long.parseLong(deathban.getAsJsonObject().get("duration").toString()) - System.currentTimeMillis()) / 1000 : 0));
    }

    public static int getFriendLives(String server, UUID p) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement livesElement = ServerInfo.getData(server).getAsJsonObject("friendLives").get(p.toString());
        return Integer.parseInt(livesElement == null ? "0" : livesElement.toString());
    }

    public static int getSoulboundLives(String server, UUID p) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement livesElement = ServerInfo.getData(server).getAsJsonObject("soulboundLives").get(p.toString());
        return Integer.parseInt(livesElement == null ? "0" : livesElement.toString());
    }

    public static List<UUID> getDeathbannedPlayers(String server) {
        JsonObject obj = ServerInfo.getData(server);
        return ServerInfo.getData(server).getAsJsonObject("deathban").keySet().stream().filter(u -> isDeathbanned(server, UUID.fromString(u))).map(UUID::fromString).collect(Collectors.toList());
    }

    public static List<UUID> getSoulboundLives(String server) {
        JsonObject obj = ServerInfo.getData(server);
        return ServerInfo.getData(server).getAsJsonObject("soulboundLives").keySet().stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public static List<UUID> getFriendLives(String server) {
        JsonObject obj = ServerInfo.getData(server);
        return ServerInfo.getData(server).getAsJsonObject("friendLives").keySet().stream().map(UUID::fromString).collect(Collectors.toList());
    }



    public static boolean isKitmap(String server) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement serverElement = obj.getAsJsonObject("serverSettings").get("kitmap");
        return (serverElement != null && Boolean.parseBoolean(serverElement.toString()));
    }

    public static String getServerSetting(String server, String setting) {
        JsonObject obj = ServerInfo.getData(server);
        JsonElement serverElement = obj.getAsJsonObject("serverSettings").get(setting);
        return serverElement != null ? serverElement.toString() : null;
    }

}
