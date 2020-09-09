package rip.protocol.bridge.bukkit.utils.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import rip.protocol.bridge.shared.status.StatusProvider;
import rip.protocol.bridge.shared.utils.JsonChain;

import java.util.List;
import java.util.stream.Collectors;

public class BukkitStatusImplementer extends StatusProvider {
    public BukkitStatusImplementer() {
        super("Bridge Bukkit Implementer", 1);
    }

    @Override
    public String serverName() {
        return BridgeShared.getServerName();
    }

    @Override
    public String serverStatus() {
        return BridgeBukkitRef.getServerStatus();
    }

    @Override
    public int online() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public int maximum() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public String motd() {
        return Bukkit.getMotd();
    }

    @Override
    public double tps() {
        return MinecraftServer.getServer().recentTps[0];
    }

    @Override
    public List<String> players() {
        return Bukkit.getOnlinePlayers().stream().map(player -> player.getUniqueId().toString()).collect(Collectors.toList());
    }

    @Override
    public JsonObject dataPassthrough() {
        return null;
    }
}
