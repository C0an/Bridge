package rip.protocol.bridge.bukkit.utils.menu.punishment;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.pagination.PaginatedMenu;

import java.util.List;
import java.util.Map;

public class PunishmentMenu extends PaginatedMenu
{
    private String targetUUID;
    private String targetName;
    private PunishmentType type;
    private Map<Punishment, String> punishments;

    public String getPrePaginatedTitle(Player player) {
        return ChatColor.RED + this.type.getDisplayName() + "s";
    }

    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        buttons.put(4, new Button() {
            public String getName(Player player) {
                return ChatColor.YELLOW + "Back";
            }

            public List<String> getDescription(Player player) {
                return null;
            }

            public Material getMaterial(Player player) {
                return Material.PAPER;
            }

            public byte getDamageValue(Player player) {
                return 0;
            }

            public void clicked(Player player, int i, ClickType clickType) {
                player.closeInventory();
                new MainPunishmentMenu(PunishmentMenu.this.targetUUID, PunishmentMenu.this.targetName).openMenu(player);
            }
        });
        return buttons;
    }

    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        int index = 0;
        for (Map.Entry<Punishment, String> entry : this.punishments.entrySet()) {
            buttons.put(index, new PunishmentButton(entry.getKey()));
            ++index;
        }
        return buttons;
    }

    public PunishmentMenu(String targetUUID, String targetName, PunishmentType type, Map<Punishment, String> punishments) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.type = type;
        this.punishments = punishments;
    }
}
