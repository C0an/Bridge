package rip.protocol.bridge.bukkit.utils.menu.grant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.util.*;

@AllArgsConstructor
public class RanksMenu extends Menu {
    private static String CREATE_GRANT_PERMISSION = "bridge.grant.create";
    private String targetName;
    private UUID targetUUID;

    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Choose a Rank";
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<Rank> ranks = this.getAllowedRanks(player);
        for (int i = 0; i < ranks.size(); ++i) {
            buttons.put(i, new RankButton(this.targetName, this.targetUUID, ranks.get(i)));
        }
        return buttons;
    }

    private List<Rank> getAllowedRanks(Player player) {
        List<Rank> allRanks = new ArrayList<>(BridgeShared.getRankManager().getRanks());
        List<Rank> ranks = Lists.newArrayList();
        for (int i = 0; i < allRanks.size(); ++i) {
            if (i != 0) {
                if (this.isAllowed(allRanks.get(i), player)) {
                    ranks.add(allRanks.get(i));
                }
            }
        }
        ranks.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
        return ranks;
    }

    private boolean isAllowed(Rank rank, Player player) {
        return player.hasPermission("bridge.grant.create." + rank.getName());
    }

    public void onClose(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (!Menu.currentlyOpenedMenus.containsKey(player.getName())) {
                    player.sendMessage(ChatColor.RED + "Granting cancelled.");
                }
            }
        }.runTaskLater(Bridge.getInstance(), 1L);
    }

}
