package rip.protocol.bridge.shared.ranks;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.SharedAPI;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.utils.Callback;
import rip.protocol.bridge.shared.utils.SystemType;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class Rank {

    private UUID uuid;
    private String name = "", prefix = "§7", suffix = "§7", color = "§7", displayName = "";
    private int priority = 0;
    private boolean staff = false, hidden = false, grantable = true, defaultRank = false;
    private ArrayList<Rank> inherits;
    private ArrayList<String> permissions;

    public Rank(UUID id, boolean imp) {
        this(id, imp, callback -> {});
    }

    public Rank(UUID id, boolean imp, Callback<Boolean> callback) {
        this.uuid = id;
        this.permissions = new ArrayList<>();
        this.inherits = new ArrayList<>();
        if (imp) {
            BridgeShared.getMongoManager().loadRank(this.uuid, rank -> {
                if (rank != null) {
                    this.importSettings(rank);
                    callback.call(true);
                } else {
                    callback.call(false);
                }
            }, true);
        } else {
            callback.call(true);
        }
    }

    public Rank(UUID id, String name, boolean imp) {
        this.uuid = id;
        this.name = name;
        this.displayName = name;
        this.permissions = new ArrayList<>();
        this.inherits = new ArrayList<>();
        if (imp) {
            BridgeShared.getMongoManager().loadRank(this.uuid, rank -> {
                if (rank != null) {
                    this.importSettings(rank);
                }
            }, true);
        }
    }



    public Rank(UUID id, String name, String prefix, String suffix, String displayName, int priority, boolean staff, boolean hidden, boolean grantable, boolean defaultRank, String color) {
        this.uuid = id;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.priority = priority;
        this.staff = staff;
        this.hidden = hidden;
        this.grantable = grantable;
        this.displayName = displayName;
        this.defaultRank = defaultRank;
        this.color = color;
        this.permissions = new ArrayList<>();
        this.inherits = new ArrayList<>();
    }

    public void load() {

    }

    public void importSettings(Rank rank) {
        if (rank == null) return;
        this.name = rank.getName();
        this.prefix = rank.getPrefix();
        this.suffix = rank.getSuffix();
        this.priority = rank.getPriority();
        this.staff = rank.isStaff();
        this.hidden = rank.isHidden();
        this.grantable = rank.isGrantable();
        this.displayName = rank.getDisplayName();
        this.defaultRank = rank.isDefaultRank();
        this.color = rank.getColor();
        this.permissions = rank.getPermissions();
        this.inherits = rank.getInherits();
    }

    public void removeRank() {

        BridgeShared.getMongoManager().removeRank(this.getUuid(), callback -> {
            if(callback != null) {
                BridgeShared.sendLog("§aSuccessfully deleted rank §r" + this.getColor() + this.getName() + "§a.");
                for (Profile profile : BridgeShared.getProfileManager().getProfiles()) {
                    profile.getGrants().removeIf(grant -> grant.getRank() == null);
                    profile.refreshCurrentGrant();
                }
            }else {
                BridgeShared.sendLog("§cFailed to delete rank §r" + this.getColor() + this.getName() + "§a.");
            }
        }, true);

    }

    public boolean hasPermission(String str) {
        return getPermissions().contains(str);
    }

    public boolean hasInherit(Rank inhr) {
        return getInherits().contains(inhr);
    }


    //True: added false : removed
    public boolean togglePerm(String str) {
        if(hasPermission(str)) {
            getPermissions().remove(str);
            return false;
        }else {
            getPermissions().add(str);
            return true;
        }
    }

    //True: added false : removed
    public boolean toggleInherit(Rank inhr) {
        if(hasInherit(inhr)) {
            getInherits().remove(inhr);
            return false;
        }else {
            getInherits().add(inhr);
            return true;
        }
    }

    public void saveRank() {
        if(BridgeShared.getSystemType() == SystemType.BUKKIT) BridgeBukkitRef.refreshPlayersInRank(this);
        BridgeShared.getMongoManager().saveRank(this, callback -> {
            if (callback != null) {
                BridgeShared.sendLog("§aSuccessfully saved rank §r" + this.getColor() + this.getName() + "§a.");
            } else {
                BridgeShared.sendLog("§cFailed to save rank §r" + this.getColor() + this.getName() + "§c.");
            }
        }, true);
    }

}
