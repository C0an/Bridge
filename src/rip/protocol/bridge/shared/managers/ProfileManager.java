package rip.protocol.bridge.shared.managers;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;

public class ProfileManager {

    @Getter private Set<Profile> profiles = new HashSet<>();

    public void init() {
        profiles.clear();
        new Thread(() -> {

            while(true) {
                try {
                    sleep(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Iterator<Profile> profileIterator = profiles.iterator();
                while (profileIterator.hasNext()) {
                    Profile profile = profileIterator.next();
                    if(!profile.isOnline()) {
                        profile.saveProfile();
                        profileIterator.remove();
                    }
                }
            }

        }).start();
    }

    public Profile getProfileByUUID(UUID id) {
        if (id == null) return null;
        if(id.toString().equals(Profile.getConsoleProfile().getUuid().toString())) return Profile.getConsoleProfile();
        return profiles.stream().filter(rank->rank.getUuid().toString().equalsIgnoreCase(id.toString())).findFirst().orElse(null);
    }

    public Profile getProfileByUsername(String id) {
        if (id == null) return null;
        if(id.equalsIgnoreCase("Console")) return Profile.getConsoleProfile();
        return profiles.stream().filter(rank->rank.getUsername().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public Profile getProfileByIP(String ip) {
        return profiles.stream().filter(rank->rank.getIP().equals(ip)).findFirst().orElse(null);
    }

    public Profile addProfile(Profile profile) {
        profiles.add(profile);
        return profile;
    }

    public void save() {
        profiles.forEach(profile -> {
            BridgeShared.getMongoManager().saveProfile(profile, callback -> {
                if (callback) {
                    BridgeShared.sendLog("§aSuccessfully saved §f" + profile.getUsername() + "§a.");
                } else {
                    BridgeShared.sendLog("§cFailed to save §f" + profile.getUsername() + "§c.");
                }
            }, true);
        });
    }

    public void saveDisable() {
        profiles.forEach(profile -> {
            profile.setLastQuit(System.currentTimeMillis());
            BridgeShared.getMongoManager().saveProfile(profile, callback -> {
                if (callback) {
                    BridgeShared.sendLog("§aSuccessfully saved §f" + profile.getUsername() + "§a.");
                } else {
                    BridgeShared.sendLog("§cFailed to save §f" + profile.getUsername() + "§c.");
                }
            }, false);
        });
    }

    public Profile getProfileByUUIDOrCreate(UUID id) {
        if(id == null) return null;
        AtomicReference<Profile> prof = new AtomicReference<>(getProfileByUUID(id));
        if (prof.get() == null) {
            try {
                BridgeShared.getMongoManager().loadProfile(id.toString(), prof::set, false, MongoManager.LoadType.UUID);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return prof.get();
    }

    public Profile getProfileByUsernameOrCreate(String id) {
        if(id == null) return null;
        AtomicReference<Profile> prof = new AtomicReference<>(getProfileByUsername(id));
        if (prof.get() == null) {
            try {
                BridgeShared.getMongoManager().loadProfile(id, prof::set, false, MongoManager.LoadType.USERNAME);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return prof.get();
    }

    public Profile getNewProfileOrCreate(String name, UUID op) {

        Profile prof = getProfileByUUIDOrCreate(op);
        if(prof == null) {
            BridgeShared.getProfileManager().addProfile(new Profile(name, op,false)).applyGrant(new Grant(BridgeShared.getRankManager().getDefaultRank(), Long.MAX_VALUE, Collections.singletonList("GLOBAL"), "", "N/A", BridgeShared.getSystemName()), null, false);
            prof = getProfileByUUID(op);
        }
        return prof;
    }

}
