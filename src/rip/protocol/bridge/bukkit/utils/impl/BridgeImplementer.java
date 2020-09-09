package rip.protocol.bridge.bukkit.utils.impl;

import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.plib.nametag.NametagInfo;
import rip.protocol.plib.nametag.NametagProvider;
import org.bukkit.entity.Player;

public class BridgeImplementer extends NametagProvider {

    public BridgeImplementer() {
        super("Bridge", 1);
    }

    @Override
    public NametagInfo fetchNametag(Player player, Player viewer) {
        return createNametag(BukkitAPI.getColor(player), "");
    }
}
