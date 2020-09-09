package rip.protocol.bridge.shared;


import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.MojangUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SharedAPI {


    public static Rank getRank(String name) {
        return BridgeShared.getRankManager().getRankByName(name);
    }

    public static Rank getRank(UUID uuid) {
        return BridgeShared.getRankManager().getRankByID(uuid);
    }

    public static Rank createRank(String name) {
        if(BridgeShared.getRankManager().getRankByName(name) != null) {
            return null;
        }
        Rank r = new Rank(UUID.randomUUID(), name, false);
        r.saveRank();
        BridgeShared.getRankManager().addRank(r);
        return r;
    }

    public static Profile getProfile(UUID uuid) {
        if(uuid == null) return Profile.getConsoleProfile();
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid);
    }

    public static Profile getProfile(String uuid) {
        if(uuid.equalsIgnoreCase("Console")) return Profile.getConsoleProfile();
        return BridgeShared.getProfileManager().getProfileByUsernameOrCreate(uuid);
    }


    public static Profile getProfileNotCreate(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUID(uuid);
    }




    public static Profile getProfileOrCreateNew(UUID uuid) {
        String name;
        try {
            name = MojangUtils.fetchName(uuid);
        }catch(Exception e) {
            return null;
        }
        return BridgeShared.getProfileManager().getNewProfileOrCreate(name, uuid);
    }

    public static Profile getProfileOrCreateNew(String username, UUID uuid) {
        return BridgeShared.getProfileManager().getNewProfileOrCreate(username, uuid);
    }


    public static Rank getPlayerRank(UUID uuid) {
        if(getProfile(uuid).getDisguisedRank() != null) {
            return getProfile(uuid).getDisguisedRank();
        }
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getCurrentGrant().getRank();
    }


    public static Rank getPlayerRank(Profile profile) {
        if(profile.getDisguisedRank() != null) {
            return profile.getDisguisedRank();
        }
        return profile.getCurrentGrant().getRank();
    }


    public static Rank getPlayerRank(UUID uuid, boolean ignoreDisguise) {
        if(ignoreDisguise) return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getCurrentGrant().getRank();

        else if(getProfile(uuid).getDisguisedRank() != null) {
            return getProfile(uuid).getDisguisedRank();
        }
        return null;
    }


    public static Rank getPlayerRank(Profile profile, boolean ignoreDisguise) {
        if(ignoreDisguise) return profile.getCurrentGrant().getRank();

        else if(profile.getDisguisedRank() != null) {
            return profile.getDisguisedRank();
        }
        return null;
    }


    public static String getColor(UUID player) {
        if(!getProfile(player).getColor().equals("")) {
            return getProfile(player).getColor();
        }else {
            return getPlayerRank(player).getColor();
        }
    }

    public static String getColor(Profile profile) {
        if(!profile.getColor().equals("")) {
            return profile.getColor();
        }else {
            return getPlayerRank(profile).getColor();
        }
    }

    public static String getPrefix(UUID player) {
        if(!getProfile(player).getPrefix().equals("")) {
            return getProfile(player).getPrefix();
        }else {
            return getPlayerRank(player).getPrefix();
        }
    }

    public static String getPrefix(Profile profile) {
        if(!profile.getPrefix().equals("")) {
            return profile.getPrefix();
        }else {
            return getPlayerRank(profile).getPrefix();
        }
    }


    public static String getSuffix(UUID player) {
        if(!getProfile(player).getSuffix().equals("")) {
            return getProfile(player).getSuffix();
        }else {
            return getPlayerRank(player).getSuffix();
        }
    }

    public static String getSuffix(Profile profile) {
        if(!profile.getSuffix().equals("")) {
            return profile.getSuffix();
        }else {
            return getPlayerRank(profile).getSuffix();
        }
    }


    public static List<Grant> getActiveGrants(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getActiveGrants();
    }


    public static List<Grant> getActiveGrants(Profile profile) {
        return profile.getActiveGrants();
    }

    public static List<Grant> getCurrentScopeRanks(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getActiveGrants().stream().filter(Grant::isGrantActiveOnScope).collect(Collectors.toList());
    }

    public static List<Grant> getAllGrants(UUID uuid) {
        return BridgeShared.getProfileManager().getProfileByUUIDOrCreate(uuid).getGrants();
    }


}