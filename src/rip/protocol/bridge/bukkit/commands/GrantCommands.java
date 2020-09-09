package rip.protocol.bridge.bukkit.commands;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.utils.menu.grant.RanksMenu;
import rip.protocol.bridge.bukkit.utils.menu.grants.GrantsMenu;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.TimeUtil;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;

import java.util.Arrays;
import java.util.List;

public class GrantCommands {

    @Command(names = "grant", permission = "bridge.command.grant", async = true)
    public static void grantCmd(Player p, @Param(name = "profile") Profile target) {

        new RanksMenu(target.getUsername(), target.getUuid()).openMenu(p);

//        Profile pf = BukkitAPI.getProfile(p);
//        if(BukkitAPI.getPlayerRank(target, true).getPriority() > BukkitAPI.getPlayerRank(pf, true).getPriority()) {
//            p.sendMessage(ChatColor.RED + "You cannot modify the grants of \"" + target.getUsername() + "\".");
//            return;
//        }
//        GrantProcess process = pf.getGrantProcess();
//        if (process != null) {
//            pf.setGrantProcess(null);
//            p.sendMessage("§cCancelled your current grant process.");
//        }
//        process = new GrantProcess(p, target.getUuid(), GrantStage.RANKSELECT);
//        pf.setGrantProcess(process);
//        process.executeCurrentState();
    }

    @Command(names = "consolegrant", permission = "console", async = true)
    public static void consolegrantCmd(CommandSender s, @Param(name = "profile") Profile pf, @Param(name = "rank") Rank r, @Param(name = "scopes") String scope, @Param(name = "length") String l, @Param(name = "reason", wildcard = true) String reason) {
        if(!r.isGrantable()) {
            s.sendMessage("§cThis rank is not grantable.");
            return;
        }
        List<String> scopes = Arrays.asList(scope.split(","));
        long length = (l.equalsIgnoreCase("Permanent") ? Long.MAX_VALUE : TimeUtil.parseTime(l));
        pf.applyGrant(new Grant(r, length, scopes, reason, Profile.getConsoleProfile().getUuid().toString(), BridgeShared.getSystemName()), null);
        pf.saveProfile();
        s.sendMessage("§aSuccessfully granted " + pf.getUsername() + " the rank " + r.getDisplayName() + " on the scopes: " + StringUtils.join(scopes, ", "));
    }

    @Command(names = "grants", permission = "bridge.command.grants", async = true)
    public static void grantsCmd(Player p, @Param(name = "profile") Profile profile) {
        if(BukkitAPI.getPlayerRank(profile, true).getPriority() > BukkitAPI.getPlayerRank(p, true).getPriority()) {
            p.sendMessage(ChatColor.RED + "You cannot view the grants of \"" + profile.getUsername() + "\".");
            return;
        }
        List<Grant> allGrants = profile.getGrants();
        allGrants.sort((first, second) -> {
            if (first.getInitialTime() > second.getInitialTime()) {
                return -1;
            }
            else {
                return 1;
            }
        });
        new GrantsMenu(allGrants).openMenu(p);
    }

}
