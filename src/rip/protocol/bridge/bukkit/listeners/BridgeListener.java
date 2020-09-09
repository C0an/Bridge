package rip.protocol.bridge.bukkit.listeners;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.EncryptionHandler;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import rip.protocol.plib.nametag.FrozenNametagHandler;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BridgeListener implements Listener {

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        String ip = EncryptionHandler.encryptUsingKey(e.getAddress().getHostAddress());
        if(BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(ip, PunishmentType.BLACKLIST, PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(e.getUniqueId(), PunishmentType.BLACKLIST, PunishmentType.BAN)) {
            Punishment punishment;
            if(BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(ip, PunishmentType.BLACKLIST, PunishmentType.BAN).isEmpty()) {
                punishment = (Punishment) BridgeShared.getPunishmentManager().getActivePunishmentsByTypes(e.getUniqueId(), PunishmentType.BLACKLIST, PunishmentType.BAN).toArray()[0];
            }else {
                punishment = (Punishment) BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(ip, PunishmentType.BLACKLIST, PunishmentType.BAN).toArray()[0];
            }
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, (punishment.isIP() && punishment.getTarget().getIP().equalsIgnoreCase(ip) ? BridgeBukkitRef.getPunishmentMessage(punishment, ip) : BridgeBukkitRef.getPunishmentMessage(punishment)));
            return;
        }
        if (BridgeShared.getProfileManager().getProfileByUUID(uuid) == null) {
            BridgeShared.getMongoManager().loadProfile(uuid.toString(), callback -> {
                if (callback == null) {
                    BridgeShared.getProfileManager().addProfile(new Profile(e.getName(), uuid, false)).applyGrant(new Grant(BridgeShared.getRankManager().getDefaultRank(), Long.MAX_VALUE, Arrays.asList("GLOBAL"), "New Player", "N/A", BridgeShared.getSystemName()), null, false);
                } else {
                    BridgeShared.getProfileManager().addProfile(callback);

                }
            }, true, MongoManager.LoadType.UUID);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Profile profile = BridgeShared.getProfileManager().getProfileByUUID(e.getPlayer().getUniqueId());
        if (profile != null) {
            profile.refreshCurrentGrant();
            FrozenNametagHandler.reloadPlayer(p);
            String ip = EncryptionHandler.encryptUsingKey(p.getAddress().getAddress().getHostAddress());
            profile.setIP(ip == null ? "N/A" : ip);
            if(profile.getFirstJoined() == 0) profile.setFirstJoined(System.currentTimeMillis());
            profile.setLastJoined(System.currentTimeMillis());
            profile.setUsername(p.getName());
            profile.saveProfile();

            new Thread(() -> BridgeShared.getPunishmentManager().isBanEvading(profile, cbck -> {
                if(cbck) {
                    BridgeShared.getMongoManager().getProfiles(profile.getIP(), callback -> {
                        if (callback == null || callback.isEmpty() || callback.size() == 1) {
                            return;
                        }
                        List<String> formattedName = new ArrayList<>();
                        callback.forEach(pr -> {
                            if(pr.getUuid().toString().equals(profile.getUuid().toString())) return;
                            if(!BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BAN)) return;
                            if(Bukkit.getOfflinePlayer(pr.getUuid()).isOnline()) formattedName.add(ChatColor.GREEN + pr.getUsername());
                            else if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BLACKLIST) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BLACKLIST)) formattedName.add(ChatColor.DARK_RED + pr.getUsername());
                            else if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BAN)) formattedName.add(ChatColor.RED + pr.getUsername());
                            else formattedName.add(ChatColor.GRAY + pr.getUsername());
                        });
                        BridgeBukkitRef.broadcastMessage("&6&l[ALTS] " + BukkitAPI.getColor(profile) + p.getName() + " &eis possibly &cban evading &7(" + (callback.size() - 1) +  " accounts)" + "\n" + StringUtils.join(formattedName, ChatColor.WHITE + ", "), "bridge.alts.sendmessage");
                    }, true);
                }
            })).start();
        }

        if(p.hasPermission("bridge.updater") && BridgeShared.getUpdaterManager().getFilesForGroup(BridgeShared.getGroupName()).stream().anyMatch(file -> BridgeShared.getUpdaterManager().getStatus(file).isShouldUpdate())) {
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "There are " + (int) BridgeShared.getUpdaterManager().getFilesForGroup(BridgeShared.getGroupName()).stream().filter(file -> BridgeShared.getUpdaterManager().getStatus(file).isShouldUpdate()).count() + " plugins awaiting an update.");
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Type /updater update to update the plugins!");
        }

        if(profile.getDisguise() != null) BukkitAPI.setupDisguise(profile);

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Profile profile = BridgeShared.getProfileManager().getProfileByUUID(e.getPlayer().getUniqueId());
        if (profile == null) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYour profile has failed to load, please try relogging or message an owner.");
            return;
        }

        if(profile.isMuted()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "You are currently muted" + (profile.getMute().isPermanent() ? "" : " for " + profile.getMute().getRemainingString()) + ".");
            return;
        }

        Rank r = BukkitAPI.getPlayerRank(e.getPlayer());
        e.setMessage(r.isStaff() ? ChatColor.translateAlternateColorCodes('&', e.getMessage()) : e.getMessage());
        e.setFormat(ChatColor.translateAlternateColorCodes('&', BukkitAPI.getPrefix(e.getPlayer()) + BukkitAPI.getColor(e.getPlayer()) + e.getPlayer().getName() + BukkitAPI.getSuffix(e.getPlayer()) + "§r: %2$s"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Profile profile = BridgeShared.getProfileManager().getProfileByUUID(e.getPlayer().getUniqueId());
        if (profile != null) {
            profile.setLastQuit(System.currentTimeMillis());
            profile.setConnectedServer(BridgeShared.getSystemName());
            profile.saveProfile();
            BridgeShared.getProfileManager().getProfiles().remove(profile);
        }
    }

}
