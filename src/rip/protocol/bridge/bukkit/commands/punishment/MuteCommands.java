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

import java.util.concurrent.TimeUnit;

public class MuteCommands {

    @Command(names = {"mute"}, permission = "bridge.command.mute", async = true)
    public static void muteCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently mute the player") boolean silent, @Param(name = "target") Profile target, @Param(name = "time") Long length, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.MUTE) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already muted.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        if (!s.hasPermission("bridge.punishment.create.mute.permanent") && TimeUnit.DAYS.toMillis(31L) < length) {
            s.sendMessage(ChatColor.RED + "You don't have permission to create a mute this long. Maximum time allowed: 30 days.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.MUTE, false, silent, false, length);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"muteip", "ipmute"}, permission = "bridge.command.muteip", async = true)
    public static void muteIPCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently mute the player") boolean silent, @Param(name = "target") Profile target, @Param(name = "time") Long length, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.MUTE) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already muted.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        if (!s.hasPermission("bridge.punishment.create.mute.permanent") && TimeUnit.DAYS.toMillis(31L) < length) {
            s.sendMessage(ChatColor.RED + "You don't have permission to create a mute this long. Maximum time allowed: 30 days.");
            return;
        }

        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.MUTE, true, silent, false, length);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }



    @Command(names = {"pmute", "perm"}, permission = "bridge.command.permmute", async = true)
    public static void pMuteCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently mute the player") boolean silent, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.MUTE) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already muted.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.MUTE, false, silent, false, Long.MAX_VALUE);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"pmuteip", "permmuteip"}, permission = "bridge.command.permmuteip", async = true)
    public static void pMuteIPCmd(CommandSender s, @Flag(value = { "s", "silent" }, description = "Silently mute the player") boolean silent, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.MUTE) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is already muted.");
            return;
        }
        if(BukkitAPI.getPlayerRank(s).getPriority() < BukkitAPI.getPlayerRank(target).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot punish this player.");
            return;
        }
        Punishment punishment = new Punishment(target, pf, BridgeShared.getSystemName(), reason, PunishmentType.MUTE, true, silent, false, Long.MAX_VALUE);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }

    @Command(names = {"unmute"}, permission = "bridge.command.unmute", async = true)
    public static void unmuteCmd(CommandSender s, @Param(name = "target") Profile target, @Param(name = "reason", wildcard = true) String reason) {
        Profile pf = BukkitAPI.getProfile(s);
        if(!BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(target.getUuid(), PunishmentType.MUTE) && !BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE)) {
            s.sendMessage(ChatColor.RED + target.getUsername() + " is not currently muted.");
            return;
        }
        if(BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE) && !s.hasPermission("bridge.command.unmuteip")) {
            s.sendMessage(ChatColor.RED + "You cannot unban " + target.getUsername() + " because they are ip-muted.");
            return;
        }
        Punishment punishment = (BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(target.getIP(), PunishmentType.MUTE) ? (Punishment) BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(target.getIP(), PunishmentType.MUTE).toArray()[0] :  (Punishment) BridgeShared.getPunishmentManager().getActivePunishmentsByTypes(target.getUuid(), PunishmentType.MUTE).toArray()[0]);
        if(BukkitAPI.getPlayerRank(punishment.getExecutor()).getPriority() < BukkitAPI.getPlayerRank(s).getPriority()) {
            s.sendMessage(ChatColor.RED + "You cannot undo this punishment.");
            return;
        }
        punishment.pardon(pf, BridgeShared.getSystemName(), reason);
        punishment.save();
        BridgeShared.getPacketHandler().sendPacket(new PunishmentPacket(punishment.toString()));
    }


}
