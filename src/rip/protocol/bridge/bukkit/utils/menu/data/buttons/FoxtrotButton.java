package rip.protocol.bridge.bukkit.utils.menu.data.buttons;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.bukkit.utils.menu.data.submenus.DeathbansMenu;
import rip.protocol.bridge.bukkit.utils.menu.data.submenus.LivesMenu;
import rip.protocol.plib.menu.Button;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class FoxtrotButton extends Button {

    private String server, type;

    @Override
    public String getName(Player player) {
        return ChatColor.AQUA + type;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        lore.add(ChatColor.YELLOW + "Click to view " + ChatColor.LIGHT_PURPLE + type + ChatColor.YELLOW + " data.");
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        return lore;
    }

    @Override
    public Material getMaterial(Player player) {
        return (type.equalsIgnoreCase("lives") ? Material.BEACON : Material.TNT);
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        if(type.equalsIgnoreCase("lives")) {
            if(new LivesMenu(server).getButtons(player).isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no active lives data at the moment.");
                return;
            }
            new LivesMenu(server).openMenu(player);
        }else {
            if(new DeathbansMenu(server).getButtons(player).isEmpty()) {
                player.sendMessage(ChatColor.RED + "There are no active deathban data at the moment.");
                return;
            }
            new DeathbansMenu(server).openMenu(player);
        }
    }
}
