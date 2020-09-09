package rip.protocol.bridge.bukkit.commands.punishment;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AltsCommand {

    @Command(names = {"alts", "dupeip"}, permission = "bridge.command.alts", async = true)
    public static void alts(CommandSender sender, @Param(name = "profile", extraData = "get") Profile profile) {

        BridgeShared.getMongoManager().getProfiles(profile.getIP(), callback -> {
            if(callback == null || callback.isEmpty() || callback.size() == 1) {
                sender.sendMessage(ChatColor.RED + profile.getUsername() + " does not have any alt accounts!");
                return;
            }

            sender.sendMessage(BukkitAPI.getColor(profile) + profile.getUsername() + ChatColor.GREEN + " has " + ChatColor.WHITE +  callback.size() + " linked accounts" + ChatColor.GREEN + ".");
            List<String> formattedName = new ArrayList<>();
            callback.forEach(pr -> {
                if(Bukkit.getOfflinePlayer(pr.getUuid()).isOnline()) formattedName.add(ChatColor.GREEN + pr.getUsername());
                else if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BLACKLIST) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BLACKLIST)) formattedName.add(ChatColor.DARK_RED + pr.getUsername());
                else if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BAN)) formattedName.add(ChatColor.RED + pr.getUsername());
                else formattedName.add(ChatColor.WHITE + pr.getUsername());
            });
            sender.sendMessage(StringUtils.join(formattedName, ChatColor.WHITE + ", "));

        }, false);



    }

}
