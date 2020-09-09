package rip.protocol.bridge.bukkit.utils.impl;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.command.ParameterType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RankParamater implements ParameterType<Rank> {

    public static boolean isUUID(String str) {
        try {
            UUID uuid = UUID.fromString(str);
            return true;
        }catch (Exception e) {
            return false;
        }
    }


    @Override
    public Rank transform(CommandSender sender, String source) {
        Rank r;
        if(isUUID(source)) {
            r = BridgeShared.getRankManager().getRankByID(UUID.fromString(source));
        }else {
            r = BridgeShared.getRankManager().getRankByName(source);
        }
        if(r == null) {
            sender.sendMessage("§cThere is no such rank with the " + (isUUID(source) ? "uuid" : "name") + " \"" + source + "\".");
            return null;
        }
        return r;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> ranks = new ArrayList<>();
        BridgeShared.getRankManager().getRanks().forEach(rank -> ranks.add(rank.getName()));
        return ranks;
    }
}
