package rip.protocol.bridge.bukkit.utils.menu.disguise;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.utils.menu.grant.RankButton;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.plib.menu.Button;
import rip.protocol.plib.menu.Menu;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DisguiseRankMenu extends Menu {

    private String nickName;

    public DisguiseRankMenu(String nickName) {
        super(ChatColor.GOLD + "Select a Rank");
        this.nickName = nickName;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttonMap = new HashMap<>();

        List<Rank> rankList = BridgeShared.getRankManager().getRanks().stream().filter(rank -> {
            if(BukkitAPI.getProfile(player).hasPermission("bridge.disguise.all")) {
                return true;
            }else {
                return !rank.isStaff();
            }
        }).sorted(Comparator.comparingInt(Rank::getPriority).reversed()).collect(Collectors.toList());

        AtomicInteger integer = new AtomicInteger(0);
        rankList.forEach(rank -> {
            buttonMap.put(integer.get(), new Button() {
                @Override
                public String getName(Player player) {
                    return rank.getColor() + rank.getDisplayName();
                }

                @Override
                public List<String> getDescription(Player player) {
                    return Arrays.asList(
                            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------",
                            ChatColor.YELLOW + "Click to disguise your rank as " + rank.getColor() + rank.getDisplayName() + ChatColor.YELLOW + ".",
                            "",
                            rank.getPrefix() + rank.getColor() + nickName + ChatColor.WHITE + ": Hello World \\o/",
                            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------"
                    );
                }

                @Override
                public Material getMaterial(Player player) {
                    return Material.WOOL;
                }

                @Override
                public byte getDamageValue(Player player) {
                    return RankButton.getColor(rank.getColor().charAt(1)).getWoolData();
                }

                @Override
                public void clicked(Player player, int slot, ClickType clickType) {
                    player.closeInventory();
                    new DisguiseSkinMenu(nickName, rank).openMenu(player);
                }
            });
            integer.getAndIncrement();
        });

        return buttonMap;
    }
}
