package rip.protocol.bridge.bukkit.utils.menu.punishment;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.utils.TimeUtil;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.util.TimeUtils;

import java.util.Date;
import java.util.List;

public class PunishmentButton extends Button
{
    private Punishment punishment;

    public String getName(Player player) {
        return ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.punishment.getTime()));
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 25));
        String by = punishment.getExecutor().getUsername();
        int seconds = (this.punishment.getRemainingTime() > 0L) ? TimeUtils.getSecondsBetween(new Date(), new Date(this.punishment.getRemainingTime())) : 0;
        String actor = "Server" + ChatColor.YELLOW + " : " + ChatColor.RED + this.punishment.getPunishedServer();

        description.add(ChatColor.YELLOW + "By: " + ChatColor.RED + by);
        description.add(ChatColor.YELLOW + "Added on: " + ChatColor.RED + actor);
        description.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + this.punishment.getReason());


        if (this.punishment.isActive()) {
            if (!this.punishment.isPermanent()) {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 25));
                description.add(ChatColor.YELLOW + "Time remaining: " + ChatColor.RED + TimeUtils.formatIntoDetailedString(seconds));
            }
            else {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 25));
                description.add(ChatColor.YELLOW + "This is a permanent punishment.");
            }
        }
        else if (this.punishment.isPardoned()) {
            String removedBy = this.punishment.getPardonedBy().getUsername();
            description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 25));
            description.add(ChatColor.RED + "Removed:");
            description.add(ChatColor.YELLOW + removedBy + ": " + ChatColor.RED + this.punishment.getPardonedReason());
            description.add(ChatColor.RED + "at " + ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.punishment.getPardonedAt())));
            if (!this.punishment.isPermanent()) {
                description.add("");
                description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int)((this.punishment.getRemainingTime()) / 1000L) + 1));
            }
        }
        else if (!this.punishment.isPermanent() && this.punishment.getRemainingTime() <= 0) {
            description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int)((this.punishment.getRemainingTime()) / 1000L) + 1));
            description.add(ChatColor.GREEN + "Expired");
        }
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 25));
        return description;
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return !this.punishment.isActive() ? DyeColor.RED.getWoolData() : DyeColor.LIME.getWoolData();
    }

    public void clicked(Player player, int i, ClickType clickType) {
    }

    public PunishmentButton(Punishment punishment) {
        this.punishment = punishment;
    }
}
