package rip.protocol.bridge.bukkit.utils.menu.data.submenus;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.utils.menu.data.ServerDataMenu;
import rip.protocol.bridge.bukkit.utils.menu.data.buttons.DeathbanButton;
import rip.protocol.bridge.bukkit.utils.menu.data.buttons.FoxtrotButton;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.bridge.shared.status.ServerProperty;
import rip.protocol.bridge.shared.status.impltype.FoxtrotHandler;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathbansMenu extends Menu {

    private String server;

    public DeathbansMenu(String server) {
        super(ChatColor.GREEN + "Viewing " + server + "'s deathban data");
        setAutoUpdate(true);
        this.server = server;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {

        Map<Integer, Button> buttons = new HashMap<>();

        if (ServerInfo.getProperty(server, ServerProperty.PROVIDERNAME).contains("Foxtrot")) {
            for (int data = 1; data <= FoxtrotHandler.getDeathbannedPlayers(server).size(); data++) {
                for (UUID deathbannedPlayer : FoxtrotHandler.getDeathbannedPlayers(server)) {
                    buttons.put(data - 1, new DeathbanButton(server, deathbannedPlayer));
                }
            }
        }
        return buttons;
    }

}
