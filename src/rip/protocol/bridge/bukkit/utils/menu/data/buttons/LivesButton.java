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
public class LivesButton extends Button {

    private String server;
    private UUID uuid;
    private String type;

    @Override
    public String getName(Player player) {
        return  UUIDUtils.name(uuid) + ChatColor.GRAY + "'s " + type + " lives";
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        lore.add(ChatColor.YELLOW + "Amount: " + ChatColor.RED + (type.equalsIgnoreCase("soulbound") ? FoxtrotHandler.getSoulboundLives(server, uuid) : FoxtrotHandler.getFriendLives(server, uuid)));
        lore.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------------------");
        return lore;
    }

    @Override
    public Material getMaterial(Player player) {
        return type.equalsIgnoreCase("soulbound") ? Material.SOUL_SAND : Material.REDSTONE_BLOCK;
    }
}
