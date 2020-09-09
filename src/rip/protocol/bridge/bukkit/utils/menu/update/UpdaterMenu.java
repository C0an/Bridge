package rip.protocol.bridge.bukkit.utils.menu.update;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.bukkit.utils.menu.grant.GlobalButton;
import rip.protocol.bridge.bukkit.utils.menu.grant.GrantButton;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.updater.UpdateStatus;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public class UpdaterMenu extends Menu {

    private String group;
    private Map<File, Boolean> status;
    private boolean complete;

    public String getTitle(Player player) {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "Updater - " + group;
    }

    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        List<File> plugins = BridgeShared.getUpdaterManager().getFilesForGroup(group).stream().filter(file -> {
            UpdateStatus updateStatus = BridgeShared.getUpdaterManager().getStatus(file);
            return updateStatus != UpdateStatus.ERROR && updateStatus != UpdateStatus.LATEST;
        }).collect(Collectors.toList());

        int i = 0;
        for (File file : plugins) {
            this.status.putIfAbsent(file, false);
            buttons.put(i, new PluginButton(this, file));
            ++i;
        }
        List<File> scopes = Lists.newArrayList();
        scopes.addAll(this.status.keySet().stream().filter(this.status::get).collect(Collectors.toList()));
        buttons.put(31, new CompleteButton(this, scopes));
        return buttons;
    }

    public void onClose(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (!Menu.currentlyOpenedMenus.containsKey(player.getName()) && !UpdaterMenu.this.complete) {
                    player.sendMessage(ChatColor.RED + "Updating cancelled.");
                }
            }
        }.runTaskLater(Bridge.getInstance(), 1L);
    }

    public UpdaterMenu(String group) {
        this.status = Maps.newHashMap();
        this.group = group;
    }
}
