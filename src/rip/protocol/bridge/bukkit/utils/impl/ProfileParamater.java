package rip.protocol.bridge.bukkit.utils.impl;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.utils.MojangUtils;
import rip.protocol.plib.command.ParameterType;
import rip.protocol.plib.pLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProfileParamater implements ParameterType<Profile> {

    public static boolean isUUID(String str) {
        try {
            UUID uuid = UUID.fromString(str);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    public Profile transform(CommandSender sender, String source) {

        String fixedSource = source.replace("get/", "");

        Profile pf;

        if(fixedSource.equals("self")) {
            pf = BukkitAPI.getProfile(sender);
        }
        else if(isUUID(fixedSource)) {
            pf = BridgeShared.getProfileManager().getProfileByUUIDOrCreate(UUID.fromString(fixedSource));
        }else {

            pf = BridgeShared.getProfileManager().getProfileByUsernameOrCreate(fixedSource);

            if(!source.startsWith("get/")) {

                if(pf == null) {
                    UUID playerUUID = null;
                    try {
                        playerUUID = MojangUtils.fetchUUID(fixedSource);
                        if(playerUUID != null) {
                            pf = BridgeShared.getProfileManager().getNewProfileOrCreate(fixedSource, playerUUID);
                        }else {
                            pf = null;
                        }
                    } catch (Exception e) {
                        pf = null;
                        e.printStackTrace();
                    }
                }
            }



        }
        if(pf == null) {
            sender.sendMessage("§cNo such player with the " + (isUUID(fixedSource) ? "uuid" : "name") + " \"" + fixedSource + "\".");
            return null;
        }
        return pf;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Player player : pLib.getInstance().getServer().getOnlinePlayers()) {
            if (StringUtils.startsWithIgnoreCase(player.getName(), source) && sender.canSee(player) && BridgeShared.getProfileManager().getProfileByUUID(player.getUniqueId()) != null) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}
