package rip.protocol.bridge.bungee.listeners;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.backend.MongoManager;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.UUID;

public class ChristianListener implements Listener {

    @EventHandler
    public void onLogin(PostLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (BridgeShared.getProfileManager().getProfileByUUID(uuid) == null) {
            BridgeShared.getMongoManager().loadProfile(uuid.toString(), callback -> {
                if (callback == null) {
                    BridgeShared.getProfileManager().addProfile(new Profile(e.getPlayer().getName(), uuid, false)).applyGrant(new Grant(BridgeShared.getRankManager().getDefaultRank(), Long.MAX_VALUE, Collections.singletonList("GLOBAL"), "First joined", "", "BungeeCord"), null, false);
//                    BungeeCord.getInstance().getPluginManager().callEvent(new ProfileLoadEvent(e.getPlayer(), ChristianShared.getProfileManager().getProfileByUUID(uuid)));
                } else {
                    BridgeShared.getProfileManager().addProfile(callback);
//                    BungeeCord.getInstance().getPluginManager().callEvent(new ProfileLoadEvent(e.getPlayer(), ChristianShared.getProfileManager().getProfileByUUID(uuid)));
                }
            }, false, MongoManager.LoadType.UUID);
        } else {
            BridgeShared.getProfileManager().getProfileByUUID(uuid).refreshCurrentGrant();
//            BungeeCord.getInstance().getPluginManager().callEvent(new ProfileLoadEvent(e.getPlayer(), ChristianShared.getProfileManager().getProfileByUUID(uuid)));
        }
    }

    @EventHandler(priority = 100)
    public void onJoin(LoginEvent e) {
        Profile profile = BridgeShared.getProfileManager().getProfileByUUID(e.getConnection().getUniqueId());
        if (profile != null) {
            profile.refreshCurrentGrant();
        }

    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        Profile profile = BridgeShared.getProfileManager().getProfileByUUID(e.getPlayer().getUniqueId());
        if (profile != null) {
            BridgeShared.getProfileManager().getProfiles().remove(profile);
        }
    }
    
}
