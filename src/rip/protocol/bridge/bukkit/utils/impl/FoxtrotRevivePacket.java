package rip.protocol.bridge.bukkit.utils.impl;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.hcf.Foxtrot;
import rip.protocol.plib.util.UUIDUtils;
import rip.protocol.plib.xpacket.XPacket;

import java.util.UUID;

@AllArgsConstructor
public class FoxtrotRevivePacket implements XPacket {

    private String server;
    private UUID player;
    private int lives;

    @Override
    public void onReceive() {
        if(BridgeShared.getServerName().equalsIgnoreCase(server)) {
            if(Bukkit.getPluginManager().getPlugin("HCF") != null && Bukkit.getPluginManager().getPlugin("HCF").isEnabled()) {
                Foxtrot.getInstance().getSoulboundLivesMap().setLives(player, lives);
                Foxtrot.getInstance().getDeathbanMap().revive(player);
                System.out.println("[HCF] " + UUIDUtils.name(player) + " has used a life and revived themselves through Hub. (Lives: " + lives + ")");
            }
        }
    }
}
