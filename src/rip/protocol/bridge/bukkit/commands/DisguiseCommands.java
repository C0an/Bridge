package rip.protocol.bridge.bukkit.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.utils.menu.disguise.DisguiseRankMenu;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;
import rip.protocol.plib.pLib;

import java.util.regex.Pattern;

public class DisguiseCommands {

    @Command(names = {"disguise", "nick", "skin"}, permission = "bridge.disguise", description = "Disguise yourself and hide your identity.")
    public static void disguise(Player player, @Param(name = "username", defaultValue = "N/A") String name) {
        pLib.getInstance().getSignGUI().open(player, new String[] { "", "^^^^^^^^^^^^^^", "Type a name", "you wish to use" }, (player1, lines) -> {
            startDisguiseProcess(player, lines[0]);
        });
    }

    @Command(names = {"undisguise", "nick clear", "nick off", "skin reset"}, permission = "bridge.disguise", description = "Reveal yourself once again!")
    public static void undisguise(Player player, @Param(name = "player", defaultValue = "self", extraData = "get") Profile target) {
        if(target.getDisguise() == null) {
            player.sendMessage(ChatColor.RED + target.getUsername() + " is not disguised!");
            return;
        }
        BukkitAPI.setupDisguise(target, true);
        player.sendMessage(ChatColor.GREEN + "You have cleared your disguise!");
    }

    @Command(names = {"checkdisguise", "realnick", "realname", "isdisguised", "whois"}, permission = "bridge.disguise.check", description = "Check who the person is!")
    public static void check(Player player, @Param(name = "player", defaultValue = "self", extraData = "get") Profile target) {
        if(target.getDisguise() == null) {
            player.sendMessage(ChatColor.RED + target.getUsername() + " is not disguised!");
            return;
        }
        player.sendMessage(target.getColor() + target.getDisguise().getDisguisedName() + ChatColor.YELLOW + "'s real name is: " + ChatColor.WHITE + target.getDisguise().getRealName() + ChatColor.YELLOW + ".");
    }

    public static void startDisguiseProcess(Player player, String name) {
        if(player == null) return;
        if(name.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Disguise Process was cancelled due to not typing a name.");
            return;
        }
        if(!Pattern.compile("^\\w{1,16}$").matcher(name).matches()) {
            player.sendMessage(ChatColor.RED + "That is not a valid username.");
            return;
        }
        if(Bukkit.getPlayer(name) != null) {
            player.sendMessage(ChatColor.RED + "You cannot use this name as they are already online.");
            return;
        }
        if(BukkitAPI.getProfile(name) != null && BukkitAPI.getPlayerRank(BukkitAPI.getProfile(name)).isStaff()) {
            player.sendMessage(ChatColor.RED + "You cannot disguise as a staff member.");
            return;
        }
        new DisguiseRankMenu(name).openMenu(player);

    }

}
