package rip.protocol.bridge.bukkit.commands;

import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.utils.TPS;
import rip.protocol.bridge.bukkit.utils.impl.WhitelistPacket;
import rip.protocol.bridge.bukkit.utils.menu.data.ServerDataMenu;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.bridge.shared.status.ServerProperty;
import rip.protocol.plib.command.Command;
import org.bukkit.command.CommandSender;
import rip.protocol.plib.command.Param;
import rip.protocol.plib.util.TPSUtils;
import rip.protocol.plib.xpacket.FrozenXPacketHandler;

import java.util.UUID;

public class BridgeCommands {

    @Command(names = {"bridge", "perms"}, permission = "bridge.command", hidden = true)
    public static void permsCmd(CommandSender s) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6§lBridge §7❘ §fMain Page");
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6/perms rank §7❘ §fAll help regarding rank commands");
        s.sendMessage("§6/perms player §7❘ §fAll help regarding player commands");
        s.sendMessage(BukkitAPI.LINE);
    }



    @Command(names = {"bridge rank", "perms rank"}, permission = "bridge.command", hidden = true)
    public static void permsRankHelpCmd(CommandSender s) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6§lBridge §7❘ §fRank Help Page");
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6/perms rank list §7❘ §fList all created ranks");
        s.sendMessage("§6/perms rank info <rank> §7❘ §fGet information about a rank");
        s.sendMessage("§6/perms rank create <string> §7❘ §fCreate a rank");
        s.sendMessage("§6/perms rank delete <string> §7❘ §fDelete a rank");
        s.sendMessage("");
        s.sendMessage("§6/perms rank rename <rank> <string> §7❘ §fRename a rank");
        s.sendMessage("§6/perms rank setdisplayname <rank> <string...> §7❘ §fSet a ranks display name");
        s.sendMessage("§6/perms rank setprefix <rank> <string...> §7❘ §fSet a ranks prefix");
        s.sendMessage("§6/perms rank setsuffix <rank> <string...> §7❘ §fSet a ranks suffix");
        s.sendMessage("§6/perms rank setcolor <rank> <string> §7❘ §fSet a ranks color");
        s.sendMessage("");
        s.sendMessage("§6/perms rank setpriority <rank> <integer> §7❘ §fSet a ranks priority");
        s.sendMessage("");
        s.sendMessage("§6/perms rank setstaff <rank> <boolean> §7❘ §fSet a ranks staff status");
        s.sendMessage("§6/perms rank sethidden <rank> <boolean> §7❘ §fSet a ranks hidden status");
        s.sendMessage("§6/perms rank setgrantable <rank> <boolean> §7❘ §fSet a ranks grantable status");
        s.sendMessage("§6/perms rank setdefault <rank> <boolean> §7❘ §fSet a ranks default status");
        s.sendMessage("");
        s.sendMessage("§6/perms rank permission <rank> <string> §7❘ §fAdd/remove a permission");
        s.sendMessage("§6/perms rank inherit <rank> <rank> §7❘ §fAdd/remove a rank inherit");
        s.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = {"bridge player", "perms player"}, permission = "bridge.command", hidden = true)
    public static void permsUserHelpCmd(CommandSender s) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6§lBridge §7❘ §fUser Help Page");
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6/perms player info <player> §7❘ Get information about a players profile");
        s.sendMessage("§6/perms player delete <player> §7❘ Delete a players profile");
        s.sendMessage("");
        s.sendMessage("§6/perms player setprefix <player> <string...> §7❘ Set a players prefix");
        s.sendMessage("§6/perms player setsuffix <player> <string...> §7❘ Set a players suffix");
        s.sendMessage("§6/perms player setcolor <player> <string> §7❘ Set a players color");
        s.sendMessage("§6/perms player disguise <player> <rank> §7❘ Disguise a player as a specific rank");
        s.sendMessage("");
        s.sendMessage("§6/perms player permission <player> <permission> §7❘ Add/remove a players permission");
        s.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = {"group list"}, permission = "bridge.group.list", hidden = true)
    public static void groupList(CommandSender s) {
        if(ServerInfo.getGroups() == null) {
            s.sendMessage(ChatColor.RED + "There are no server groups...");
            return;
        }
        FancyMessage m = new FancyMessage(ChatColor.GREEN + "Available Groups: ");
        ServerInfo.getGroups().forEach(serv -> m.then(serv + " ").tooltip(ChatColor.BLUE + "Click to view servers in the group!").command("/group info " + serv));
        m.send(s);
    }

    @Command(names = {"group info"}, permission = "bridge.group.info", hidden = true)
    public static void groupList(CommandSender s, @Param(name = "group") String g) {
        if(ServerInfo.getServersInGroup(g) == null) {
            s.sendMessage(ChatColor.RED + "There is no such group with the name \"" + g + "\".");
            return;
        }
        s.sendMessage(StringUtils.repeat(ChatColor.BLUE.toString() + ChatColor.STRIKETHROUGH + "-", 35));
        s.sendMessage(ChatColor.RED + "Servers in the group " + g + ":");
        ServerInfo.getServersInGroup(g).forEach(serv -> new FancyMessage(ChatColor.RED + serv).tooltip(ChatColor.BLUE + "Click to view information about the server!").command("/serverinfo info " + serv).send(s));
        s.sendMessage(StringUtils.repeat(ChatColor.BLUE.toString() + ChatColor.STRIKETHROUGH + "-", 35));
    }

    @Command(names = {"serverinfo list"}, permission = "bridge.server.list", hidden = true)
    public static void serverList(CommandSender s) {
        if(ServerInfo.getBridgeServers().isEmpty()) {
            s.sendMessage(ChatColor.RED + "There are no servers found...");
            return;
        }
        FancyMessage m = new FancyMessage(ChatColor.GREEN + "Available Servers: ");
        ServerInfo.getBridgeServers().forEach(serv -> m.then(serv + " ").tooltip(ChatColor.BLUE + "Click to view information about the server!").command("/serverinfo info " + serv));
        m.send(s);
    }

    @Command(names = {"serverinfo info"}, permission = "bridge.server.info", hidden = true)
    public static void serverInfo(CommandSender s, @Param(name = "server")String server) {
        if(!ServerInfo.serverExists(server)) {
            s.sendMessage(ChatColor.RED + "There is no such server with the name \"" + server + "\".");
            return;
        }
        String serv = ServerInfo.getProperName(server);
        s.sendMessage(StringUtils.repeat(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-", 45));
        s.sendMessage(ChatColor.BLUE + serv + ChatColor.GRAY + " [" + ServerInfo.getProperty(serv, ServerProperty.ONLINE) + '/' + ServerInfo.getProperty(serv, ServerProperty.MAXIMUM) + "]");
        s.sendMessage(ChatColor.YELLOW + "Using Provider: " + ChatColor.RED + ServerInfo.getProperty(serv, ServerProperty.PROVIDERNAME));
        s.sendMessage(ChatColor.YELLOW + "Current Status: " + ServerInfo.formattedStatus(serv, true));
        s.sendMessage(ChatColor.YELLOW + "MOTD: " + ChatColor.RED + ServerInfo.getProperty(serv, ServerProperty.MOTD));
        s.sendMessage(ChatColor.YELLOW + "TPS: " + ChatColor.RED + TPSUtils.formatTPS(Double.parseDouble(ServerInfo.getProperty(serv, ServerProperty.TPS)), true));
        boolean wl = ServerInfo.getProperty(serv, ServerProperty.STATUS).equalsIgnoreCase("WHITELISTED");
        new FancyMessage(ChatColor.YELLOW + "Whitelisted: ").then(ChatColor.RED +  (wl ? "True" : "False")).tooltip(ChatColor.AQUA + "Click to change to change whitelist status.").command("/serverinfo whitelist " + serv).send(s);
        s.sendMessage(StringUtils.repeat(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-", 45));
    }

    @Command(names = {"serverinfo data"}, permission = "bridge.server.data", hidden = true)
    public static void serverData(Player p, @Param(name = "server")String server) {
        if(!ServerInfo.serverExists(server)) {
            p.sendMessage(ChatColor.RED + "There is no such server with the name \"" + server + "\".");
            return;
        }
        String serv = ServerInfo.getProperName(server);
        new ServerDataMenu(serv).openMenu(p);

    }

    @Command(names = {"serverinfo whitelist"}, permission = "bridge.server.whitelist", hidden = true)
    public static void serverWhitelist(CommandSender s, @Param(name = "server")String server) {
        if(!ServerInfo.serverExists(server)) {
            s.sendMessage(ChatColor.RED + "There is no such server with the name \"" + server + "\".");
            return;
        }
        String serv = ServerInfo.getProperName(server);
        boolean wl = ServerInfo.getProperty(serv, ServerProperty.STATUS).equalsIgnoreCase("WHITELISTED");
        FrozenXPacketHandler.sendToAll(new WhitelistPacket(serv, !wl));
        s.sendMessage(ChatColor.LIGHT_PURPLE + serv + ChatColor.YELLOW + " is " + (wl ? "no longer" : "now") + " whitelisted.");
    }


}
