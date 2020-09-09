package rip.protocol.bridge.shared.status;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ServerInfo {

    @Getter private static HashSet<String> bridgeServers = new HashSet<>();


    public static boolean serverExists(String server) {
        return StatusProvider.getServerDataTable().column(ServerProperty.ONLINE).keySet().stream().filter(s -> s.equalsIgnoreCase(server)).findFirst().orElse(null) != null;
    }

    public static String getProperty(String server, ServerProperty data) {
        if(!serverExists(server)) {
            return null;
        }else {

            return "" + StatusProvider.getServerDataTable().column(data).get(getProperName(server));
        }
    }

    public static String getProperName(String server) {
        if(!serverExists(server)) return null;
        else return StatusProvider.getServerDataTable().column(ServerProperty.ONLINE).keySet().stream().filter(s -> s.equalsIgnoreCase(server)).findFirst().orElse(null);
    }

    public static JsonObject getData(String server) {
        return new JsonParser().parse(getProperty(server, ServerProperty.DATA)).getAsJsonObject();
    }

    public static boolean doesGroupExist(String g) {
        return getServersInGroup(g) != null;
    }

    public static List<String> getServersInGroup(String g) {
        List<String> groups = StatusProvider.getServerDataTable().column(ServerProperty.GROUP).keySet().stream().filter(s -> StatusProvider.getServerDataTable().column(ServerProperty.GROUP).get(s).equalsIgnoreCase(g)).map(String::toString).collect(Collectors.toList());
        if(groups.isEmpty()) return null;
        return groups;
    }

    public static List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        StatusProvider.getServerDataTable().column(ServerProperty.GROUP).values().stream().filter(s -> !groups.contains(s) && getProperty(s, ServerProperty.GROUP) != null).forEach(groups::add);
        if(groups.isEmpty()) return null;
        return groups;
    }

    public static String findPlayerServer(UUID uuid) {
        AtomicReference<String> server = new AtomicReference<>();
        StatusProvider.getServerDataTable().column(ServerProperty.PLAYERS).forEach((s, s2) -> {
            if(ServerInfo.getProperty(s, ServerProperty.PLAYERS) == null || s.toLowerCase().contains("bungee")) return;
            List<String> online = new Gson().fromJson(s2, List.class);
            if(online == null || online.isEmpty()) return;
            if(online.stream().filter(Objects::nonNull).anyMatch(s1 -> s1.equals(uuid.toString()))) server.set(s);
        });
        return server.get();
    }

    public static String findPlayerProxy(UUID uuid) {
        AtomicReference<String> server = new AtomicReference<>();
        StatusProvider.getServerDataTable().column(ServerProperty.PLAYERS).forEach((s, s2) -> {
            if(ServerInfo.getProperty(s, ServerProperty.PLAYERS) == null || !s.toLowerCase().contains("bungee")) return;
            List<String> online = new Gson().fromJson(s2, List.class);
            if(online == null || online.isEmpty()) return;
            if(online.stream().filter(Objects::nonNull).anyMatch(s1 -> s1.equals(uuid.toString()))) server.set(s);
        });
        return server.get();
    }



    public static String formattedStatus(String server, boolean color) {
        if(!serverExists(server)) {
            return null;
        }
        String s = getProperty(server, ServerProperty.STATUS);
        switch(s.toLowerCase()) {
            case "online": {
                return (color ? "§a" : "") + "Online";
            }
            case "whitelisted": {
                return (color ? "§f" : "") + "Whitelisted";
            }
            case "offline": {
                return (color ? "§c" : "") + "Offline";
            }
            case "booting": {
                return (color ? "§6" : "" + "Booting");
            }
            default: {
                return (color ? "§9" : "") + WordUtils.capitalize(s.toLowerCase());
            }
        }
    }

}
