package rip.protocol.bridge.shared.grant;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.JsonChain;
import rip.protocol.bridge.shared.utils.OtherUtils;
import rip.protocol.bridge.shared.utils.TimeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class Grant {

    private UUID uuid;
    private Rank rank;
    private long length, initialTime;
    private List<String> scope;
    private String reason, grantedBy, grantedOn;

    @Setter private boolean removed;
    @Setter private long removedAt;
    @Setter private String removedReason, removedBy, removedOn;

    public Grant(Rank rank, long length, List<String> scope, String reason, String grantedBy, String grantedOn) {
        this.uuid = UUID.randomUUID();
        this.rank = rank;
        this.scope = scope;
        this.length = length;
        this.initialTime = System.currentTimeMillis();
        this.reason = reason;
        this.grantedBy = grantedBy;
        this.grantedOn = grantedOn;
        this.removed = false;
        this.removedAt = -1L;
        this.removedBy = "";
        this.removedOn = "";
        this.removedReason = "";
    }

    public Grant(UUID uuid, Rank rank, long length, long initialTime, List<String> scope, String reason, String grantedBy, String grantedOn) {
        this.uuid = uuid;
        this.rank = rank;
        this.length = length;
        this.initialTime = initialTime;
        this.scope = scope;
        this.reason = reason;
        this.grantedBy = grantedBy;
        this.grantedOn = grantedOn;
    }

    public Grant(UUID uuid, Rank rank, long length, long initialTime, List<String> scope, String reason, String grantedBy, String grantedOn, boolean removed, long removedAt, String removedReason, String removedBy, String removedOn) {
        this.uuid = uuid;
        this.rank = rank;
        this.length = length;
        this.initialTime = initialTime;
        this.scope = scope;
        this.reason = reason;
        this.grantedBy = grantedBy;
        this.grantedOn = grantedOn;
        this.removed = removed;
        this.removedAt = removedAt;
        this.removedReason = removedReason;
        this.removedBy = removedBy;
        this.removedOn = removedOn;
    }

    public static Grant deserialize(String grant) {
        JsonObject object = new JsonParser().parse(grant).getAsJsonObject();
        return new Grant(
                UUID.fromString(object.get("uuid").getAsString()),
                BridgeShared.getRankManager().getRankByID(UUID.fromString(object.get("rank").getAsString())),
                object.get("length").getAsLong(),
                object.get("initialTime").getAsLong(),
                new ArrayList<>(Arrays.asList(object.get("scope").getAsString().split(","))),
                object.get("reason").getAsString(),
                object.get("grantedBy").getAsString(),
                object.get("grantedOn").getAsString(),
                object.get("removed").getAsBoolean(),
                object.get("removedAt").getAsLong(),
                object.get("removedBy").getAsString(),
                object.get("removedOn").getAsString(),
                object.get("removedReason").getAsString());
    }

    public static JsonObject serialize(Grant grant) {

        return new JsonChain()
                .addProperty("uuid", grant.getUuid().toString())
                .addProperty("rank", grant.getRank().getUuid().toString())
                .addProperty("length", grant.getLength())
                .addProperty("initialTime", grant.getInitialTime())
                .addProperty("scope", StringUtils.join(grant.getScope(), ','))
                .addProperty("reason", grant.getReason())
                .addProperty("grantedBy", grant.grantedBy)
                .addProperty("grantedOn", grant.getGrantedOn())
                .addProperty("removed", grant.isRemoved())
                .addProperty("removedAt", grant.getRemovedAt())
                .addProperty("removedReason", grant.getRemovedReason())
                .addProperty("removedBy", grant.removedBy)
                .addProperty("removedOn", grant.getRemovedOn())
                .get();
    }

    public String formatGrantedTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mmaa");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return dateFormat.format(cal.getTime()) + " (" + TimeUtil.getTimeZoneShortName(cal.getTimeZone().getDisplayName()) + ")";
    }

    public long getActiveUntil() {
        return length == Long.MAX_VALUE ? Long.MAX_VALUE : (initialTime + length);
    }

    public boolean isStillActive() {
        return getActiveUntil() > System.currentTimeMillis() && !removed;
    }

    public String getGrantedBy() {
        if(OtherUtils.isUUID(grantedBy)) {
            Profile pf = BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(grantedBy));
            return (pf == Profile.getConsoleProfile() ? pf.getColor() : pf.getCurrentGrant().getRank().getColor()) + pf.getUsername();
        }else {
            return grantedBy;
        }
    }

    public String getRemovedBy() {
        if(OtherUtils.isUUID(removedBy)) {
            Profile pf = BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(removedBy));
            return (pf == Profile.getConsoleProfile() ? pf.getColor(): pf.getCurrentGrant().getRank().getColor() ) + pf.getUsername();
        }else {
            return removedBy;
        }
    }


    public boolean isGrantActiveOnScope() {
        switch (BridgeShared.getSystemType()) {
            case BUNGEE: {
                return getScope().stream().anyMatch(s -> (s.equalsIgnoreCase("global") || s.equalsIgnoreCase("bungeecord") || s.equalsIgnoreCase("gr-bungeecord")));
//                return getScope().contains("GLOBAL") || getScope().contains("BungeeCord") || getScope().contains("GR-BungeeCord");
            }
            case BUKKIT: {
                return getScope().stream().anyMatch(s -> (s.equalsIgnoreCase("global") || s.equalsIgnoreCase(BridgeShared.getSystemName()) || s.equalsIgnoreCase("gr-" + BridgeShared.getFromConfig("servergroup"))));
//                return getScope().contains("GLOBAL") || getScope().contains(BridgeShared.getSystemName());
            }
            default: {
                return getScope().contains("GLOBAL");
            }
        }
    }

}
