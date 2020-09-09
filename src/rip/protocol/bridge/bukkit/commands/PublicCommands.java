package rip.protocol.bridge.bukkit.commands;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.plib.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.protocol.plib.command.Param;

import java.util.*;
import java.util.stream.Collectors;

public class PublicCommands {

    @Command(names = { "list", "who", "players" }, permission = "", description = "See a list of online players", async = true)
    public static void list(CommandSender sender) {
        Map<Rank, List<String>> sorted = new TreeMap<>(Comparator.comparingInt(Rank::getPriority).reversed());
        int online = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            ++online;
            if (sender instanceof Player && !((Player)sender).canSee(player) && player.hasMetadata("invisible")) {
                continue;
            }
            Profile profile = BukkitAPI.getProfile(player.getUniqueId());
            Rank rank = BukkitAPI.getPlayerRank(profile);

            String displayName = player.getDisplayName();
            if (player.hasMetadata("invisible")) {
                displayName = ChatColor.GRAY + "*" + displayName;
            }
            sorted.putIfAbsent(rank, new LinkedList<>());
            sorted.get(rank).add(displayName);
        }
        List<String> merged = new LinkedList<>();
        for (List<String> part : sorted.values()) {
            part.sort(String.CASE_INSENSITIVE_ORDER);
            merged.addAll(part);
        }
        sender.sendMessage(getHeader());
        sender.sendMessage("(" + online + "/" + Bukkit.getMaxPlayers() + ") " + merged);
    }

    private static String getHeader() {
        StringBuilder builder = new StringBuilder();
        List<Rank> ranks = BridgeShared.getRankManager().getRanks().parallelStream().sorted(Comparator.comparingInt(Rank::getPriority).reversed()).collect(Collectors.toList());
        for (Rank rank : ranks) {
            boolean displayed = rank.getPriority() >= 0;
            if (displayed) {
                builder.append(rank.getColor()).append(rank.getDisplayName()).append(ChatColor.RESET).append(", ");
            }
        }
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }
        return builder.toString();
    }

    @Command(names = { "find" }, permission = "bridge.find", description = "See the server an user is currently playing on", async = true)
    public static void find(CommandSender sender, @Param(name = "player") Profile profile) {

        String server = ServerInfo.findPlayerServer(profile.getUuid());
        String proxy = ServerInfo.findPlayerProxy(profile.getUuid());
        if (server == null) {
            sender.sendMessage(ChatColor.RED + profile.getUsername() + " is currently not on the network.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + profile.getUsername() +  " is on " + ChatColor.GREEN + server + (proxy != null ? ChatColor.YELLOW + " (" + ChatColor.GREEN + proxy + " Proxy" + ChatColor.YELLOW + ")" : ""));
    }

}
