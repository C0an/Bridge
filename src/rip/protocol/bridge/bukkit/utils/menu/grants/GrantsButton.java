package rip.protocol.bridge.bukkit.utils.menu.grants;


import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.util.TimeUtils;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
public class GrantsButton extends Button {

    private Grant grant;

    public String getName(Player player) {
        return ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.grant.getInitialTime()));
    }

    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        String by = this.grant.getGrantedBy();
        description.add(ChatColor.YELLOW + "By: " + ChatColor.RED + by);
        description.add(ChatColor.YELLOW + "Reason: " + ChatColor.RED + this.grant.getReason());
        description.add(ChatColor.YELLOW + "Scopes: " + ChatColor.RED + (this.grant.getScope().isEmpty() ? "Global" : this.grant.getScope()));
        description.add(ChatColor.YELLOW + "Rank: " + ChatColor.RED + this.grant.getRank().getDisplayName());
        if (this.grant.isStillActive()) {
            if (this.grant.getActiveUntil() != Long.MAX_VALUE) {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "Time remaining: " + ChatColor.RED + TimeUtils.formatIntoDetailedString((int) ((this.grant.getActiveUntil() - System.currentTimeMillis()) / 1000)));
            }
            else {
                description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
                description.add(ChatColor.YELLOW + "This is a permanent grant.");
            }
            if (player.hasPermission("bridge.grant.remove." + this.grant.getRank())) {
                description.add("");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "Click to remove");
                description.add(ChatColor.RED.toString() + ChatColor.BOLD + "this grant");
            }
        }
        else if (this.grant.isRemoved()) {
            String removedBy = this.grant.getRemovedBy();
            description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
            description.add(ChatColor.RED + "Removed:");
            description.add(ChatColor.YELLOW + removedBy + ": " + ChatColor.RED + this.grant.getRemovedReason());
            description.add(ChatColor.RED + "at " + ChatColor.YELLOW + TimeUtils.formatIntoCalendarString(new Date(this.grant.getRemovedAt())));
            if (this.grant.getActiveUntil() != Long.MAX_VALUE) {
                description.add("");
                description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int) (this.grant.getLength() / 1000)));
            }
        }
        else if (!this.grant.isStillActive() && this.grant.getActiveUntil() <= Long.MAX_VALUE) {
            description.add(ChatColor.YELLOW + "Duration: " + TimeUtils.formatIntoDetailedString((int)((this.grant.getLength()) / 1000L)));
            description.add(ChatColor.GREEN + "Expired");
        }
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 25));
        return description;
    }

    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    public byte getDamageValue(Player player) {
        return this.grant.isStillActive() ? DyeColor.LIME.getWoolData() : DyeColor.RED.getWoolData();
    }

    public void clicked(Player player, int i, ClickType clickType) {
        if (!player.hasPermission("minehq.grant.remove." + this.grant.getRank()) || !this.grant.isStillActive()) {
            return;
        }
        player.closeInventory();
        ConversationFactory factory = new ConversationFactory(Bridge.getInstance()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            public String getPromptText(ConversationContext context) {
                return "§aType a reason to be used when removing this grant. Type §cno§a to quit.";
            }

            public Prompt acceptInput(ConversationContext cc, String s) {
                if (s.equalsIgnoreCase("no")) {
                    cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Grant removal aborted.");
                }
                else {
                    grant.setRemoved(true);
                    grant.setRemovedAt(System.currentTimeMillis());
                    grant.setRemovedBy(player.getUniqueId().toString());
                    grant.setRemovedOn(BridgeShared.getSystemName());
                    grant.setRemovedReason(s);

                    cc.getForWhom().sendRawMessage(ChatColor.GREEN + "You have removed the grant.");
                }
                return Prompt.END_OF_CONVERSATION;
            }
        }).withLocalEcho(false).withEscapeSequence("/no").withTimeout(60).thatExcludesNonPlayersWithMessage("Go away evil console!");
        Conversation con = factory.buildConversation(player);
        player.beginConversation(con);
    }
}
