package rip.protocol.bridge.bukkit.utils;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.spigotmc.SpigotConfig;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.util.PlayerUtils;
import rip.protocol.plib.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BridgeBukkitRef {

    public static void updatePermissions(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            Profile profile = BukkitAPI.getProfile(uuid);
            PermissionAttachment attachment = player.addAttachment(Bridge.getInstance());
            profile.updateColor();
            attachment.getPermissions().keySet().forEach(attachment::unsetPermission);
            ArrayList<String> perms = new ArrayList<>(profile.getPermissions());
            profile.getPermissions().forEach(permission->{
                if (permission.startsWith("-")) {
                    permission = permission.substring(1);
                    attachment.setPermission(permission, false);
                } else {
                    attachment.setPermission(permission, true);
                }
            });

            profile.getActiveGrants().forEach(grant->{
                perms.addAll(grant.getRank().getPermissions());
                grant.getRank().getPermissions().forEach(permission->{
                    if (permission.startsWith("-")) {
                        permission = permission.substring(1);
                        attachment.setPermission(permission, false);
                    } else {
                        attachment.setPermission(permission, true);
                    }
                });
                grant.getRank().getInherits().forEach(inherit->{
                    perms.addAll(inherit.getPermissions());
                    inherit.getPermissions().forEach(permission -> {
                        if (permission.startsWith("-")) {
                            permission = permission.substring(1);
                            attachment.setPermission(permission, false);
                        } else {
                            attachment.setPermission(permission, true);
                        }
                    });
                });
            });
            profile.setActivePermissions(perms);
        }
    }

    public static void updateColor(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.setDisplayName(BukkitAPI.getPlayerRank(player).getColor() + player.getName() + "§r");
            if(player.hasMetadata("RankPrefix")) player.removeMetadata("RankPrefix", Bridge.getInstance());
            player.setMetadata("RankPrefix", new FixedMetadataValue(Bridge.getInstance(), BukkitAPI.getPlayerRank(player).getPrefix()));
        }
    }

    public static String getServerStatus() {
        return (Bridge.getInstance().isBooted() ? (Bukkit.hasWhitelist() ? "WHITELISTED" : "ONLINE") : "BOOTING");
    }

    public static void refreshPlayersInRank(Rank rank) {
        BridgeShared.getProfileManager().getProfiles().stream().filter(profile-> Bukkit.getPlayer(profile.getUuid()) != null && profile.hasActiveGrantOf(rank)).forEach(Profile::refreshCurrentGrant);
    }

    public static void logMessages(String msg, boolean packetIncoming) {
        if(packetIncoming) Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(player -> player.sendMessage(msg));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        if(BridgeShared.getProfileManager() != null && !BridgeShared.getProfileManager().getProfiles().isEmpty()) BridgeShared.getProfileManager().getProfiles().stream().filter(profile -> Bukkit.getPlayer(profile.getUuid()) != null && profile.isDebug()).forEach(profile -> Bukkit.getPlayer(profile.getUuid()).sendMessage(msg));
    }

    public static void broadcastMessage(String msg, String permission) {
        String m = ChatColor.translateAlternateColorCodes('&', msg);
        Bukkit.getConsoleSender().sendMessage(m);
        if(permission.equals("")) Bukkit.broadcastMessage(m);
        else Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(permission)).forEach(p -> p.sendMessage(m));
    }

    public static void broadcastMessage(String msg) {
        broadcastMessage(msg, "");
    }

    public static void broadcastMessage(BaseComponent[] msg, String permission) {
        Bukkit.getConsoleSender().sendMessage(TextComponent.toPlainText(msg));
        if(permission.equals("")) {
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(msg));
        }else {
            Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(permission)).forEach(p -> p.spigot().sendMessage(msg));
        }
    }

    public static void sendMessage(BaseComponent[] msg, List<Player> players) {
        players.forEach(p -> p.spigot().sendMessage(msg));
    }

    public static void broadcastMessage(BaseComponent[] msg) {
        broadcastMessage(msg, "");
    }

    public static void callEvent(Object cls) {
        ((BaseEvent)cls).call();
    }

    private boolean isRunningOnBungee(){
        return SpigotConfig.bungee && (!(Bukkit.getServer().getOnlineMode()));
    }

    public static void handlePunishment(Punishment punishment, boolean pardon) {

        Bukkit.getOnlinePlayers().forEach(p -> {
            FancyMessage m = new FancyMessage(
                    BukkitAPI.getColor(punishment.getTarget()) + punishment.getTarget().getUsername() +
                            ChatColor.GREEN + " was " + (punishment.isSilent() ? ChatColor.YELLOW + "silently " + ChatColor.GREEN : "") + punishment.getDisplayString() +
                            " by " +
                            (pardon ? BukkitAPI.getColor(punishment.getPardonedBy()) +
                                    punishment.getPardonedBy().getUsername() :
                                    BukkitAPI.getColor(punishment.getExecutor()) +
                                            punishment.getExecutor().getUsername()) +
                            ChatColor.GREEN + ".");


            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + (pardon ? punishment.getPardonedReason() : punishment.getReason()));
            if(!pardon) lore.add(ChatColor.YELLOW + "Length: " + ChatColor.RED + punishment.getRemainingString());
            lore.add(ChatColor.YELLOW + "Server: " + ChatColor.RED + (pardon ? punishment.getPardonedServer() : punishment.getPunishedServer() ));


            boolean staff = BukkitAPI.getPlayerRank(p, true).isStaff();
            if(staff) m.tooltip(lore);
            if(punishment.isSilent()) {
                if(staff) m.send(p);
            }else {
                m.send(p);
            }
        });

        if(pardon) {
            Punishment p = BridgeShared.getPunishmentManager().getPunishmentByID(punishment.getUuid());
            p.setPardoned(pardon);
            p.setPardonedAt(punishment.getPardonedAt());
            p.setPardonedBy(punishment.getPardonedBy());
            p.setPardonedReason(punishment.getPardonedReason());
            p.setPardonedServer(punishment.getPardonedServer());
        }else {
            BridgeShared.getPunishmentManager().addPunishment(punishment);

            if(punishment.isIP()) {
                Bukkit.getOnlinePlayers().stream().filter(p -> BukkitAPI.getProfile(p).getIP().equalsIgnoreCase(punishment.getTarget().getIP())).forEach(trgt -> {
                    if(punishment.isClear()) PlayerUtils.resetInventory(trgt);
                    if(punishment.getPunishmentType() != PunishmentType.WARN && punishment.getPunishmentType() != PunishmentType.MUTE) trgt.kickPlayer(getPunishmentMessage(punishment));
                });
            }else {
                Player trgt = Bukkit.getPlayer(punishment.getTarget().getUuid());
                if(trgt != null) Bukkit.getScheduler().runTask(Bridge.getInstance(), () -> {
                    if(punishment.isClear()) PlayerUtils.resetInventory(trgt);
                    if(punishment.getPunishmentType() != PunishmentType.WARN && punishment.getPunishmentType() != PunishmentType.MUTE) {
                        trgt.kickPlayer(getPunishmentMessage(punishment));
                    }else if(punishment.getPunishmentType() == PunishmentType.WARN){
                        trgt.sendMessage("");
                        trgt.sendMessage("");
                        trgt.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "You have been warned: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + punishment.getReason());
                        trgt.sendMessage("");
                        trgt.sendMessage("");
                    }else {
                        trgt.sendMessage(ChatColor.RED + "You have been " + (punishment.isPermanent() ? "permanently silenced" : "silenced for " + TimeUtils.formatIntoDetailedString((int) (punishment.getDuration() / 1000))) + ".");
                    }
                });
            }



        }


        }

    public static String getPunishmentMessage(Punishment punishment, String IP) {
        String msg = "";
        switch(punishment.getPunishmentType()) {
            case BLACKLIST: {
                msg = ChatColor.RED + "You have been blacklisted from the " + BridgeShared.getServerDisplayName() + "\nThis punishment cannot be appealed!";
                break;
            }
            case BAN: {
                msg = ChatColor.RED + "You have been banned from the " + BridgeShared.getServerDisplayName() + (!punishment.isPermanent() ? "\nThis punishment expires in " + punishment.getRemainingString() : "");
                break;
            }
            case KICK: {
                msg = ChatColor.RED + "You have been kicked for: " + ChatColor.YELLOW + punishment.getReason();
            }
        }
        return msg + (punishment.isIP() && !IP.equals("") && punishment.getTarget().getIP().equalsIgnoreCase(IP) ? "\n" + ChatColor.RED + "This punishment is assiciated with " + punishment.getTarget().getUsername() : "");
    }

    public static String getPunishmentMessage(Punishment punishment) {
        return getPunishmentMessage(punishment, "");
    }






}
