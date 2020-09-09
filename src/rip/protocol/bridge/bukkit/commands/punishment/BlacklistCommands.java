package rip.protocol.bridge.bukkit.commands.punishment;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.packets.PunishmentPacket;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Flag;
import rip.protocol.plib.command.Param;

public class BlacklistCommands {

    @Command(names = {"blacklist"}, permission = "bridge.command.blacklist", async = true)
    public static void blacklistCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently blacklist the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BLACKLIST)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already blacklisted.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.BLACKLIST, false, silent, clear, Long.MAX_VALUE);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"unblacklist"}, permission = "bridge.command.unblacklist", async = true)
    public static void unblacklistCmd(CommandSender s, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(!BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BLACKLIST)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is not currently blacklisted.");
            return;
        }
        Punishment punishment = (Punishment) BridgeShared.getPunishmentManager().getActivePunishmentsByTypes(target.getUuid(), PunishmentType.BLACKLIST).toArray()[0];
        if(BukkitAPI.getPlayerRank(punishment.getExecutor()).getPriority() < BukkitAPI.getPlayerRank(s).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot undo this punishment.");
            return;
        }
        punishment.pardon(pf, BridgeShared.getSystemName(), reason);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

}
