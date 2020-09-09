package rip.protocol.bridge.shared.profile;

import lombok.Getter;
import lombok.Setter;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import rip.protocol.bridge.shared.SharedAPI;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.disguise.Disguise;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.bridge.shared.utils.SystemType;

import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class Profile {

    @Getter private static Profile consoleProfile = new Profile(UUID.fromString("00000000-0000-0000-0000-000000000000"), "Console", "§8[§4§lCONSOLE§8] §4", "§f", "§4§l");

    private UUID uuid, messagingPartner;
    private String username, prefix = "", suffix = "", color = "", connectedServer;
    private ArrayList<Grant> grants;
    private ArrayList<String> permissions, activePermissions;
    private String IP = "N/A";
    private boolean debug = false;
    private long firstJoined, lastJoined, lastQuit;
    private Disguise disguise = null;
    private Rank disguisedRank = null;

    public Profile(String username, UUID uuid, boolean load) {
        this.username = username;
        this.uuid = uuid;
        this.grants = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.activePermissions = new ArrayList<>();
        if (load) {
            BridgeShared.getMongoManager().loadProfile(this.uuid.toString(), profile -> {
                if (profile != null) {
                    this.importSettings(profile);
                }
            }, true, MongoManager.LoadType.UUID);
        }
    }

    public Profile(UUID uuid, String username, String prefix, String suffix, String color) {
        this.uuid = uuid;
        this.username = username;
        this.prefix = prefix;
        this.suffix = suffix;
        this.color = color;
    }

    public void importSettings(Profile profile) {
        this.grants = profile.getGrants();
        this.permissions = profile.getPermissions();
        this.IP = profile.getIP();
        this.disguisedRank = profile.getDisguisedRank();

        this.disguise = profile.getDisguise();

        this.firstJoined = profile.getFirstJoined();
        this.lastJoined = profile.getLastJoined();
        this.lastQuit = profile.getLastQuit();

        this.connectedServer = profile.getConnectedServer();

        refreshCurrentGrant();
    }

    public void saveProfile() {
        BridgeShared.getMongoManager().saveProfile(this, callback -> {
            if (callback) {
                BridgeShared.sendLog("§aSuccessfully saved §f" + getUsername() + "§a.");
            } else {
                BridgeShared.sendLog("§cFailed to save §f" + getUsername() + "§c.");
            }
        }, true);
    }

    public List<Grant> getActiveGrants() {
        List<Grant> active = new ArrayList<>();

        grants.removeIf(grant -> grant.getRank() == null || SharedAPI.getRank(grant.getRank().getUuid()) == null);
        for (Grant grant : grants) {

            if(grant.isStillActive() && grant.isGrantActiveOnScope()) {
                active.add(grant);
            }
        }
        active.sort(Comparator.comparingInt(o -> o.getRank().getPriority()));
        return active;
    }

    public Grant getCurrentGrant() {
        Grant grant = null;

        for (Grant activeGrant : getActiveGrants()) {
            if (grant == null) {
                grant = activeGrant;
                continue;
            }
            if (activeGrant.getRank().getPriority() > grant.getRank().getPriority()) {
                grant = activeGrant;
            }
        }

        if (grant == null) {
            applyGrant(new Grant(BridgeShared.getRankManager().getDefaultRank(), Long.MAX_VALUE, Collections.singletonList("GLOBAL"), "Automatically granted.", "N/A", BridgeShared.getSystemName()), null, false);
            for (Grant activeGrant : getActiveGrants()) {
                if (grant == null) {
                    grant = activeGrant;
                    continue;
                }
                if (activeGrant.getRank().getPriority() > grant.getRank().getPriority()) {
                    grant = activeGrant;
                }
            }
        }

        return grant;
    }

    public boolean isOnline() {
        return ServerInfo.findPlayerServer(getUuid()) != null;
    }

    public String getConnectedServer() {
        return (ServerInfo.findPlayerServer(getUuid()) == null ? (this.connectedServer == null ? "N/A" : this.connectedServer) : ServerInfo.findPlayerServer(getUuid()));
    }

    public void applyGrant(Grant grant, UUID executor, boolean shouldGetCurrentGrant) {
        grants.add(grant);
        if (shouldGetCurrentGrant && getCurrentGrant().getUuid().toString().equalsIgnoreCase(grant.getUuid().toString())) {
            refreshCurrentGrant();
        }
        BridgeShared.sendLog("Successfully applied " + getUsername() + "'s Grant of the " + grant.getRank().getName() + " Rank");
    }

    public void refreshCurrentGrant() {
        Set<Grant> refresh = new HashSet<>();
        for(Grant grant : getActiveGrants()) {
            Rank r = grant.getRank();
            if(r == null) {
                refresh.add(grant);
            }
        }
        grants.removeAll(refresh);
        if(BridgeShared.getSystemType() == SystemType.BUKKIT) BridgeBukkitRef.updatePermissions(getUuid());
    }

    public void updateColor() { BridgeBukkitRef.updateColor(getUuid()); }

    public void applyGrant(Grant grant, UUID executor) {
        applyGrant(grant, executor, true);
    }

    public boolean hasGrantOf(Rank rank) {
        return getGrants().stream().filter(grant->grant.getUuid().toString().equalsIgnoreCase(rank.getUuid().toString())).findFirst().orElse(null) != null;
    }

    public boolean hasActiveGrantOf(Rank rank) {
        return getActiveGrants().stream().filter(grant->grant.getUuid().toString().equalsIgnoreCase(rank.getUuid().toString())).findFirst().orElse(null) != null;
    }

    public boolean isMuted() {
        return(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(getUuid(), PunishmentType.MUTE) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(getIP(), PunishmentType.MUTE));
    }

    public Punishment getMute() {
        return(isMuted() ? (Punishment) (BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(getUuid(), PunishmentType.MUTE) ? BridgeShared.getPunishmentManager().getActivePunishmentsByTypes(getUuid(), PunishmentType.MUTE).toArray()[0] : BridgeShared.getPunishmentManager().getActiveIPPunishmentsByTypes(getIP(), PunishmentType.MUTE).toArray()[0]) : null);
    }

    public List<Punishment> getPunishments() {
        return BridgeShared.
                getPunishmentManager()
                .getPunishments()
                .stream()
                .filter(p -> p.getTarget()
                        .getUuid()
                        .toString()
                        .equals(
                                getUuid()
                                        .toString()))
                .collect(
                        Collectors.toList());
    }

    public List<Punishment> getActivePunishments() {
        return BridgeShared.getPunishmentManager().getPunishments().stream().filter(p -> p.isActive() && p.getTarget().getUuid().toString().equals(getUuid().toString())).collect(Collectors.toList());

    }

    public boolean hasPermission(String permisson) {
        return getActiveGrants().parallelStream().filter(grant -> grant.isGrantActiveOnScope() && grant.isStillActive()).anyMatch(grant -> grant.getRank().hasPermission(permisson) || getPermissions().contains(permisson) || getActivePermissions().contains(permisson));
    }


}
