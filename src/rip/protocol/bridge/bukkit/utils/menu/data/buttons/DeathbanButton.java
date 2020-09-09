package rip.protocol.bridge.bukkit.utils.menu.data.buttons;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.shared.status.impltype.FoxtrotHandler;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.util.TimeUtils;
import rip.protocol.plib.util.UUIDUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class DeathbanButton extends Button {

    private String server;
    private UUID uuid;

    @Override
    public String getName(Player player) {
        return BukkitAPI.getColor(uuid) + UUIDUtils.name(uuid);
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        lore.add(ChatColor.YELLOW + "Remaining Time: " + ChatColor.RED + TimeUtils.formatLongIntoHHMMSS(FoxtrotHandler.getDeathbanTime(server, uuid)));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left click to revive");
        lore.add(ChatColor.YELLOW + "Right click to revive and refund");
//        lore.add(ChatColor.YELLOW + "Click to view " + ChatColor.LIGHT_PURPLE + type + ChatColor.YELLOW + " data.");
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        return lore;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.TNT;
    }
}
