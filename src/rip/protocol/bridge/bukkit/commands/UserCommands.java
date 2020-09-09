package rip.protocol.bridge.bukkit.commands;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;
import rip.protocol.plib.nametag.FrozenNametagHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class UserCommands {

    @Command(names = {"bridge player info", "perms player info", "bridge player check", "perms player check"}, permission = "bridge.command", async = true)
    public static void permsUserInfoCmd(CommandSender s, @Param(name = "profile", extraData = "get") Profile pf) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage(BukkitAPI.getColor(pf) + pf.getUsername() + " §7❘ §fProfile Information");
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6UUID: §f" + pf.getUuid());
        s.sendMessage("§6Current Rank On Scope: §f" + pf.getCurrentGrant().getRank().getColor() + pf.getCurrentGrant().getRank().getDisplayName());
        s.sendMessage("§6Active Grants: §f" + pf.getActiveGrants().size());
        s.sendMessage("");
        s.sendMessage("§6Prefix: §f" + BukkitAPI.getPrefix(pf) + " §7(" + ChatColor.stripColor(BukkitAPI.getPrefix(pf).replace("§", "&")) + ")");
        s.sendMessage("§6Suffix: §f" + BukkitAPI.getSuffix(pf) + " §7(" + ChatColor.stripColor(BukkitAPI.getSuffix(pf).replace("§", "&")) + ")");
        s.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = {"bridge player delete", "perms player delete"}, permission = "bridge.command", async = true)
    public static void permsUserDeleteCmd(CommandSender s, @Param(name = "profile", extraData = "get") Profile pf) {
        BridgeShared.getMongoManager().removeProfile(pf.getUuid(), callback -> {
            if(callback) {
                if(Bukkit.getOfflinePlayer(pf.getUuid()).isOnline()) Bukkit.getPlayer(pf.getUuid()).kickPlayer("§cYour profile has been deleted - please reconnect.");
                BridgeShared.getProfileManager().getProfiles().remove(pf);
                s.sendMessage("§aSuccessfully deleted " + pf.getUsername() + "'s Profile.");
            }else {
                s.sendMessage("§cFailed to delete " + pf.getUsername() + "'s Profile.");
            }
        }, false);
    }

    @Command(names = {"bridge player setprefix", "perms player setprefix"}, permission = "bridge.command", async = true)
    public static void permsUserSetPrefixCmd(CommandSender s, @Param(name = "profile") Profile pf, @Param(name = "prefix", wildcard = true) String prfx) {
        String tag = ChatColor.translateAlternateColorCodes('&', prfx);
        if(prfx.equals("clear")) tag = "";
        pf.setPrefix(tag);
        pf.saveProfile();
        s.sendMessage("§aSuccessfully " + (tag.equals("") ? "cleared" : "set") + " the prefix of " + pf.getUsername() + (!tag.equals("") ? " to " + tag : ""));
    }

    @Command(names = {"bridge player setsuffix", "perms player setsuffix"}, permission = "bridge.command", async = true)
    public static void permsUserSetSuffixCmd(CommandSender s, @Param(name = "profile") Profile pf, @Param(name = "suffix", wildcard = true) String sfx) {
        String tag = ChatColor.translateAlternateColorCodes('&', sfx);
        if(sfx.equals("clear")) tag = "";
        pf.setSuffix(tag);
        pf.saveProfile();
        s.sendMessage("§aSuccessfully " + (tag.equals("") ? "cleared" : "set") + " the suffix of " + pf.getUsername() + (!tag.equals("") ? " to " + tag : ""));
    }

    @Command(names = {"bridge player setcolor", "perms player setcolor", "bridge player setcolour", "perms player setcolour"}, permission = "bridge.command", async = true)
    public static void permsUserSetColorCmd(CommandSender s, @Param(name = "profile") Profile pf, @Param(name = "color") String col) {
        String tag = ChatColor.translateAlternateColorCodes('&', col);
        if(col.equals("clear")) tag = "";
        pf.setColor(tag);
        pf.saveProfile();
        s.sendMessage("§aSuccessfully " + (tag.equals("") ? "cleared" : "set") + " the color of " + pf.getUsername() + (!tag.equals("") ? " to " + tag + pf.getUsername() : ""));
    }

    @Command(names = {"bridge player setdisguise", "perms player setdisguise", "bridge player disguise", "perms player disguise"}, permission = "bridge.command", async = true)
    public static void permsUserSetDisguiseCmd(CommandSender s, @Param(name = "profile") Profile pf, @Param(name = "disguise") String disguise) {
        Rank r = null;
        if (disguise.equals("clear")) {
            r = null;
        } else {
            r = BridgeShared.getRankManager().getRankByName(disguise);
        }
        pf.setDisguisedRank(r);
        pf.updateColor();
        pf.saveProfile();

        s.sendMessage("§aSuccessfully " + (r == null ? "cleared" : "set") + " the disguise of " + pf.getUsername() + (r != null ? " to " + r.getDisplayName() : ""));
    }


}
