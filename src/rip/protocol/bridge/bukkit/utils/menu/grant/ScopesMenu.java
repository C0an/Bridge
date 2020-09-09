package rip.protocol.bridge.bukkit.utils.menu.grant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.status.ServerInfo;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public class ScopesMenu extends Menu {
    private Map<String, Boolean> status;
    private boolean global;
    private boolean complete;
    private Rank rank;
    private String targetName;
    private UUID targetUUID;
    private String reason;
    private long duration;

    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Select the Scopes";
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<String> groups = Lists.newArrayList();
        groups.addAll(ServerInfo.getGroups());

        int i = 0;
        for (String scope : groups) {
            this.status.putIfAbsent(scope, false);
            buttons.put(i, new ScopeButton(this, scope));
            ++i;
        }
        List<String> scopes = Lists.newArrayList();
        scopes.addAll(this.status.keySet().stream().filter(this.status::get).collect(Collectors.toList()));
        buttons.put(22, new GlobalButton(this));
        buttons.put(31, new GrantButton(this.rank, this.targetName, this.targetUUID, this.reason, this, scopes, this.duration));
        return buttons;
    }

    public void onClose(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (!Menu.currentlyOpenedMenus.containsKey(player.getName()) && !ScopesMenu.this.complete) {
                    player.sendMessage(ChatColor.RED + "Granting cancelled.");
                }
            }
        }.runTaskLater(Bridge.getInstance(), 1L);
    }

    public ScopesMenu(boolean global, boolean complete, Rank rank, String targetName, UUID targetUUID, String reason, long duration) {
        this.status = Maps.newHashMap();
        this.global = false;
        this.global = global;
        this.complete = complete;
        this.rank = rank;
        this.targetName = targetName;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.duration = duration;
    }

}
