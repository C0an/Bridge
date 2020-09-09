package rip.protocol.bridge.bukkit.utils.menu.update;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.events.GrantCreateEvent;
import rip.protocol.bridge.bukkit.utils.menu.grant.ScopesMenu;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.packets.GrantCreatePacket;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.util.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CompleteButton extends Button {

    private UpdaterMenu parent;
    private List<File> plugins;

    public String getName(Player player) {
        return ChatColor.GREEN + "Confirm and Update";
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        description.add(ChatColor.BLUE + "Click to update " + ChatColor.WHITE + this.plugins.size() + " plugins" + ChatColor.BLUE + " for the group " + ChatColor.WHITE + this.parent.getGroup() + ChatColor.BLUE + ".");
        description.add("");
        plugins.forEach(file -> description.add(ChatColor.GRAY + " * " + ChatColor.WHITE + file.getName() + ' ' + BridgeShared.getUpdaterManager().getStatus(file).getPrefix()));
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 30));
        return description;
    }

    public Material getMaterial(Player player) {
        return Material.DIAMOND_SWORD;
    }

    public byte getDamageValue(Player player) {
        return 0;
    }

    public void clicked(Player player, int i, ClickType clickType) {
        this.update(this.plugins, player);
    }

    private void update(List<File> plugins, Player player) {
        if(plugins.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You must select a plugin you wish to update!");
            return;
        }

        BridgeShared.getUpdaterManager().updatePlugins(plugins, cons -> player.sendMessage(ChatColor.BLUE + cons));
        player.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "! YOU WILL NEED TO RESTART FOR CHANGES TO TAKE PLACE !");
        this.parent.setComplete(true);
        player.closeInventory();
    }
    
}
