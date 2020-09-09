package rip.protocol.bridge.bukkit.utils.menu.data;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.utils.menu.data.buttons.FoxtrotButton;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.bridge.shared.status.ServerProperty;
import rip.protocol.bridge.shared.status.impltype.FoxtrotHandler;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.util.HashMap;
import java.util.Map;

public class ServerDataMenu extends Menu {

    private String server;

    public ServerDataMenu(String server) {
        super(ChatColor.GREEN + "Viewing " + server + "'s data");
        setAutoUpdate(true);
        this.server = server;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        if(ServerInfo.getProperty(server, ServerProperty.PROVIDERNAME).contains("Foxtrot")) {
            buttons.put(11, new FoxtrotButton(server, "lives"));
            buttons.put(16, new FoxtrotButton(server, "deathban"));
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        if(ServerInfo.getProperty(server, ServerProperty.DATA) == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "This server has no generated data in the database, try again later?");
            return;
        }
        if(getButtons(player).isEmpty()) {
            player.sendMessage(ChatColor.RED + "No formatted data was found for the server, try /serverinfo rawdata " + server + " instead.");
            player.closeInventory();
            return;
        }
    }
}
