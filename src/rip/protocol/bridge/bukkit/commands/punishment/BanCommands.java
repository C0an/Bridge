package rip.protocol.bridge.bukkit.commands.punishment;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.packets.PunishmentPacket;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Flag;
import rip.protocol.plib.command.Param;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BanCommands {

    @Command(names = {"ban"}, permission = "bridge.command.ban", async = true)
    public static void banCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently ban the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already banned.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.BAN, false, silent, clear, Long.MAX_VALUE);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"banip", "ipban"}, permission = "bridge.command.banip", async = true)
    public static void banIPCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently ban the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already banned.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.BAN, true, silent, clear, Long.MAX_VALUE);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }



    @Command(names = {"tban", "tempban"}, permission = "bridge.command.tempban", async = true)
    public static void tBanCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently ban the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") Profile target, @Param(name = "time") Long length, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already banned.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.BAN, false, silent, clear, length);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"tbanip", "tempbanip"}, permission = "bridge.command.tempbanip", async = true)
    public static void tBanIPCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently ban the player") boolean silent, @Flag(value = { "c", "clear" }, description = "Clear the player's inventory") boolean clear, @Param(name = "target") Profile target, @Param(name = "time") Long length, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already banned.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.BAN, true, silent, clear, length);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"unban"}, permission = "bridge.command.unban", async = true)
    public static void unbanCmd(CommandSender s, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(!BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.BAN) && !BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is not currently banned.");
            return;
        }
        if(BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN) && !s.hasPermission("bridge.command.unbanip")) {
            s.sendMessage(ChatColor.RED + "You cannot unban " + target.getUsername() + " because they are ip-banned.");
            return;
        }

        Punishment punishment = (BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN) ? (Punishment) BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(target.getIP(), PunishmentType.BAN).toArray()[0] :  (Punishment) BridgeShared.getPunishmentManager().getActivePunishmentsByTypes(target.getUuid(), PunishmentType.BAN).toArray()[0]);
        if(BukkitAPI.getPlayerRank(punishment.getExecutor()).getPriority() < BukkitAPI.getPlayerRank(s).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot undo this punishment.");
            return;
        }
        punishment.pardon(pf, BridgeShared.getSystemName(), reason);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"unbanip"}, permission = "bridge.command.unbanip", async = true)
    public static void unbanIPCmd(CommandSender s, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(!BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.BAN)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is not currently ip-banned.");
            return;
        }
        Punishment punishment = (Punishment) BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(target.getIP(), PunishmentType.BAN).toArray()[0];
        if(BukkitAPI.getPlayerRank(punishment.getExecutor()).getPriority() < BukkitAPI.getPlayerRank(s).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot undo this punishment.");
            return;
        }
        punishment.pardon(pf, BridgeShared.getSystemName(), reason);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }



}
