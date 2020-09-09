package rip.protocol.bridge.shared.punishment;

import com.google.gson.Gson;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.utils.TimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class Punishment {

    private UUID uuid;
    private Profile target, executor, pardonedBy;
    private PunishmentType punishmentType;
    private boolean pardoned, isIP, silent, clear;
    private String punishedServer, reason, pardonedServer, pardonedReason;
    private long time, duration, pardonedAt;

    public Punishment(Profile target, Profile executor, String punishedServer, String reason, PunishmentType punishmentType, boolean isIP, boolean silent, boolean clear, long duration) {
        this.uuid = UUID.randomUUID();
        this.target = target;
        this.executor = executor;
        this.punishedServer = punishedServer;
        this.reason = reason;
        this.punishmentType = punishmentType;
        this.isIP = isIP;
        this.silent = silent;
        this.clear = clear;
        this.time = System.currentTimeMillis();
        this.duration = duration;
    }

    public Punishment(UUID uuid, Profile target, Profile executor, Profile pardonedBy, PunishmentType punishmentType, boolean pardoned, boolean isIP, boolean silent, boolean clear, String punishedServer, String reason, String pardonedServer, String pardonedReason, long time, long duration, long pardonedAt) {
        this.uuid = uuid;
        this.target = target;
        this.executor = executor;
        this.pardonedBy = pardonedBy;
        this.punishmentType = punishmentType;
        this.pardoned = pardoned;
        this.isIP = isIP;
        this.silent = silent;
        this.clear = clear;
        this.punishedServer = punishedServer;
        this.reason = reason;
        this.pardonedServer = pardonedServer;
        this.pardonedReason = pardonedReason;
        this.time = time;
        this.duration = duration;
        this.pardonedAt = pardonedAt;
    }


    public boolean isPermanent() {
        return this.punishmentType == PunishmentType.BLACKLIST || this.duration == Long.MAX_VALUE;
    }

    public long getRemainingTime() {
        return System.currentTimeMillis() - (this.time + this.duration);
    }

    public boolean isActive() {
        return !this.pardoned && (this.isPermanent() || this.getRemainingTime() < 0L);
    }

    public void pardon(Profile pardonedBy, String pardonedServer, String pardonedReason) {
        this.pardoned = true;
        this.pardonedAt = System.currentTimeMillis();
        this.pardonedBy = pardonedBy;
        this.pardonedServer = pardonedServer;
        this.pardonedReason = pardonedReason;
    }

    public String getRemainingString() {
        if (this.pardoned) {
            return "Pardoned";
        }
        if (this.isPermanent()) {
            return "Permanent";
        }
        if (!this.isActive()) {
            return "Expired";
        }
        return TimeUtil.millisToRoundedTime(this.time + this.duration - System.currentTimeMillis());
    }

    public String getStatusString() {
        if (this.pardoned) {
            return "Pardoned";
        }
        if (this.isPermanent()) {
            return "Permanent";
        }
        if (!this.isActive()) {
            return "Expired";
        }
        return "Active";
    }

    public String getDurationString() {
        if (this.isPermanent()) {
            return "Permanent";
        }
        return TimeUtil.millisToRoundedTime(this.duration);
    }

    public String getDisplayString() {
        if (this.punishmentType != PunishmentType.BAN && this.punishmentType != PunishmentType.MUTE) {
            return this.pardoned ? this.punishmentType.getUndoPunishmentName() : this.punishmentType.getPunishmentName();
        }
        if (this.isPermanent()) {
            return this.pardoned ? this.punishmentType.getUndoPunishmentName() : (this.punishmentType.getPunishmentName());
        }
        return this.pardoned ? this.punishmentType.getUndoPunishmentName() : ("temporarily " + this.punishmentType.getPunishmentName());
    }

    public void save() {
        BridgeShared.getMongoManager().savePunishment(this, callback -> {
            BridgeShared.sendLog((callback ? "Successfully " : "Failed to ") + "save" + (callback ? "d" : "") + " the punishment " + this.getUuid());
        }, true);
    }

    public void delete() {
        BridgeShared.getMongoManager().removePunishment(this.getUuid(), callback -> {
            BridgeShared.sendLog((callback ? "Successfully " : "Failed to ") + "delete" + (callback ? "d" : "") + " the punishment " + this.getUuid());
        }, true);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
