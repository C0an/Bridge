package rip.protocol.bridge.bungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;

public class BridgeBungeeRef {

    public static void logMessages(String msg) {
        BungeeCord.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&', msg));
    }

}
