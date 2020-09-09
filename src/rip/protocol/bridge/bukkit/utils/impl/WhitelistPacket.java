package rip.protocol.bridge.bukkit.utils.impl;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import rip.protocol.plib.xpacket.XPacket;

@AllArgsConstructor
public class WhitelistPacket implements XPacket {

    private String server;
    private boolean whitelist;

    @Override
    public void onReceive() {
        if(Bukkit.getServerName().equalsIgnoreCase(server)) {
            Bukkit.setWhitelist(whitelist);
        }
    }
}
