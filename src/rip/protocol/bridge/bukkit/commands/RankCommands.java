package rip.protocol.bridge.bukkit.commands;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.packets.RankCreatePacket;
import rip.protocol.bridge.shared.packets.RankDeletePacket;
import rip.protocol.bridge.shared.packets.RankUpdatePacket;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RankCommands {

    @Command(names = {"bridge rank list", "perms rank list"}, permission = "bridge.command")
    public static void permsRankListCmd(CommandSender s) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6§lBridge §7❘ §fRanks §7(" + BridgeShared.getRankManager().getRanks().size() + ")");
        s.sendMessage(BukkitAPI.LINE);
        ArrayList<Rank> rankList = new ArrayList<>(BridgeShared.getRankManager().getRanks());
        rankList.sort((o1, o2) -> o2.getPriority() - o1.getPriority());

        rankList.forEach(rank -> {
            if(s instanceof Player) {

                ComponentBuilder cp = new ComponentBuilder(rank.getColor() + rank.getDisplayName() + " §7❘ §f" + rank.getName() + (rank.isHidden() ? " §7[HIDDEN]" : "")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                        "§6Prefix: §f" + rank.getPrefix() + " §7(" + ChatColor.stripColor(rank.getPrefix().replaceAll("§", "&")) + ")" + "\n" +
                                "§6Suffix: §f" + rank.getSuffix() + " §7(" + ChatColor.stripColor(rank.getSuffix().replaceAll("§", "&")) + ")" + "\n" +
                                "§6Priority: §f" + rank.getPriority() + "\n" +
                                "§6Staff: §f" + rank.isStaff() + "\n" +
                                "§6Default: §f" + rank.isDefaultRank() + "\n\n" +
                                "§7§oClick for more information"
                ))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/perms rank info " + rank.getName()));
                ((Player)s).spigot().sendMessage(cp.create());
            }else {
                s.sendMessage(rank.getColor() + rank.getDisplayName() + " §7❘ §f" + rank.getName() + (rank.isHidden() ? " §7[HIDDEN]" : ""));
                s.sendMessage("§6Prefix: §f" + rank.getPrefix() + " §7(" + ChatColor.stripColor(rank.getPrefix().replaceAll("§", "&")) + ")");
                s.sendMessage("§6Suffix: §f" + rank.getSuffix() + " §7(" + ChatColor.stripColor(rank.getSuffix().replaceAll("§", "&")) + ")");
                s.sendMessage("§6Priority: §f" + rank.getPriority());
                s.sendMessage("§6Staff: §f" + rank.isStaff());
                s.sendMessage("§6Default: §f" + rank.isDefaultRank());
                s.sendMessage("");
            }
        });
        s.sendMessage("");
        s.sendMessage(s instanceof Player ? "§7§oHover over the ranks for more information." : "§7§oType /perms rank info <rank> for more information.");
        s.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = {"bridge rank info", "perms rank info"}, permission = "bridge.command")
    public static void permsRankInfoCmd(CommandSender s, @Param(name = "rank") Rank r) {
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage(r.getColor() + r.getDisplayName() + " Rank §7❘ §fInformation");
        s.sendMessage(BukkitAPI.LINE);
        s.sendMessage("§6Prefix: §f" + r.getPrefix() + " §7(" + ChatColor.stripColor(r.getPrefix().replaceAll("§", "&")) + ")");
        s.sendMessage("§6Suffix: §f" + r.getSuffix() + " §7(" + ChatColor.stripColor(r.getSuffix().replaceAll("§", "&")) + ")");
        s.sendMessage("§6Priority: §f" + r.getPriority());
        s.sendMessage("§6Staff: §f" + r.isStaff());
        s.sendMessage("§6Default: §f" + r.isDefaultRank());
        s.sendMessage("");
        s.sendMessage("§6Permissions: §f" + (r.getPermissions().isEmpty() ? "§7§oNone..." : StringUtils.join(r.getPermissions(), ", ")));
        s.sendMessage("§6Inherits: " + (r.getInherits().isEmpty() ? "§7§oNone..." : ""));
        if(!r.getInherits().isEmpty())r.getInherits().forEach(rank -> s.sendMessage(" §7* §f" + rank.getColor() + rank.getDisplayName()));

        s.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = {"bridge rank create", "perms rank create"}, permission = "bridge.command")
    public static void permsRankCreateCmd(CommandSender s, @Param(name = "rank") String name) {
        Rank r = BukkitAPI.getRank(name);
        if(r != null) {
            s.sendMessage("§cThere is already a rank with the name \"" + r.getName() + "\".");
            return;
        }
        r = BukkitAPI.createRank(name);
        BridgeShared.getPacketHandler().sendPacket(new RankCreatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
        s.sendMessage("§aSuccessfully created the rank " + r.getColor() + r.getName() + "§a!");
    }

    @Command(names = {"bridge rank delete", "perms rank delete"}, permission = "bridge.command")
    public static void permsRankDeleteCmd(CommandSender s, @Param(name = "rank") Rank r) {
        s.sendMessage("§aSuccessfully deleted the rank " + r.getColor() + r.getName() + "§a!");
        BridgeShared.getMongoManager().removeRank(r.getUuid(), callback -> {}, true);
        BridgeShared.getPacketHandler().sendPacket(new RankDeletePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank rename", "perms rank rename"}, permission = "bridge.command")
    public static void permsRankRenameCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "name") String name) {
        String original = r.getName();
        r.setName(name);
        r.saveRank();
        s.sendMessage("§aSuccessfully renamed the rank from " + original + " to " + name + "!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setdisplayname", "perms rank setdisplayname"}, permission = "bridge.command")
    public static void permsRankSetDisplayNameCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "displayName", wildcard = true) String displayName) {
        r.setDisplayName(displayName);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the display name to " + displayName + "!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setprefix", "perms rank setprefix"}, permission = "bridge.command")
    public static void permsRankSetPrefixCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "prefix", wildcard = true) String prfx) {
        String prefix = ChatColor.translateAlternateColorCodes('&', prfx);
        r.setPrefix(prefix);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the prefix to " + prefix + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setsuffix", "perms rank setsuffix"}, permission = "bridge.command")
    public static void permsRankSetSuffixCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "suffix", wildcard = true) String sfx) {
        String suffix = ChatColor.translateAlternateColorCodes('&', sfx);
        r.setSuffix(suffix);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the suffix to " + sfx + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setcolor", "perms rank setcolor", "bridge rank setcolour", "perms rank setcolour"}, permission = "bridge.command")
    public static void permsRankSetColorCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "color") String col) {
        String color = ChatColor.translateAlternateColorCodes('&', col);
        r.setColor(color);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the color to " + color + r.getName() + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setpriority", "perms rank setpriority", "bridge rank setweight", "perms rank setweight"}, permission = "bridge.command")
    public static void permsRankSetPriorityCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "priority") int priority) {
        r.setPriority(priority);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the priority to " + priority + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setstaff", "perms rank setstaff"}, permission = "bridge.command")
    public static void permsRankSetStaffCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "staff") boolean staff) {
        r.setStaff(staff);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the staff status to " + staff + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank sethidden", "perms rank sethidden"}, permission = "bridge.command")
    public static void permsRankSetHiddenCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "hidden") boolean hidden) {
        r.setHidden(hidden);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the hidden status to " + hidden + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setgrantable", "perms rank setgrantable"}, permission = "bridge.command")
    public static void permsRankSetGrantableCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "grantable") boolean grantable) {
        r.setGrantable(grantable);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the grantable status to " + grantable + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank setdefault", "perms rank setdefault"}, permission = "bridge.command")
    public static void permsRankSetDefaultCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "default") boolean b) {
        r.setDefaultRank(b);
        r.saveRank();
        s.sendMessage("§aSuccessfully changed the default status to " + b + "§a!");
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank permission", "perms rank permission", "bridge rank perm", "perms rank perm"}, permission = "bridge.command")
    public static void permsRankPermissionCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "permission") String perm) {
        boolean b = r.togglePerm(perm);
        r.saveRank();
        s.sendMessage("§aSuccessfully " + (b ? "added" : "removed") + " the permission " + perm);
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }

    @Command(names = {"bridge rank inherit", "perms rank inherit"}, permission = "bridge.command")
    public static void permsRankPermissionCmd(CommandSender s, @Param(name = "rank") Rank r, @Param(name = "inherit") Rank inhr) {
        boolean b = r.toggleInherit(inhr);
        r.saveRank();
        s.sendMessage("§aSuccessfully " + (b ? "added" : "removed") + " the inherit of " + inhr.getColor() + inhr.getDisplayName());
        BridgeShared.getPacketHandler().sendPacket(new RankUpdatePacket(r.getUuid(), (s instanceof Player ? ((Player) s).getDisplayName() + s.getName() : "§4§lCONSOLE"), BridgeShared.getSystemName()));
    }




}
