package rip.protocol.bridge.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.Bridge;
import rip.protocol.bridge.bukkit.BukkitAPI;
import rip.protocol.bridge.bukkit.utils.menu.update.UpdaterMenu;
import rip.protocol.bridge.shared.updater.UpdateStatus;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UpdaterCommand {

    @Command(names = "updater", permission = "bridge.updater", hidden = true, description = "Update the plugins from a set directory")
    public static void home(CommandSender sender) {
        sender.sendMessage(BukkitAPI.LINE);
        sender.sendMessage("§6§lBridge §7❘ §fUpdater");
        sender.sendMessage(BukkitAPI.LINE);
        if(BridgeShared.getUpdaterManager() == null) {
            sender.sendMessage(ChatColor.RED + "The update manager failed to initialised, maybe the directory is not set in the config?");
        }else {
            sender.sendMessage(ChatColor.GOLD + "/updater info" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "Check information about the updater.");
            sender.sendMessage(ChatColor.GOLD + "/updater list" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "List all files in your group.");
            sender.sendMessage(ChatColor.GOLD + "/updater list <group>" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "List all files in a specific group.");
            sender.sendMessage(ChatColor.GOLD + "/updater update" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "Open a GUI and select which plugins to update.");
            sender.sendMessage(ChatColor.GOLD + "/updater update <pluginName>" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "Update a specific plugin.");
            sender.sendMessage(ChatColor.GOLD + "/updater update all" + ChatColor.GRAY + " ❘ " + ChatColor.WHITE + "Updates all plugins.");
        }
        sender.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = "updater info", permission = "bridge.updater", hidden = true, description = "Check information about the updater.")
    public static void info(CommandSender sender) {
        sender.sendMessage(BukkitAPI.LINE);
        sender.sendMessage("§6§lBridge §7❘ §fUpdater Information");
        sender.sendMessage(BukkitAPI.LINE);
        sender.sendMessage(ChatColor.YELLOW + "Updating for Group: " + ChatColor.WHITE + BridgeShared.getGroupName() + " & Global");
        sender.sendMessage(ChatColor.YELLOW + "Using Root Directory: " + ChatColor.WHITE + BridgeShared.getUpdaterManager().getPluginUpdateDir());

        List<File> fileList = BridgeShared.getUpdaterManager().getFilesForGroup(BridgeShared.getGroupName());
        sender.sendMessage(ChatColor.YELLOW + "Plugins available: " + ChatColor.WHITE + (fileList == null || fileList.isEmpty() ? "N/A" : fileList.size()));
        sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Use /updater list to list all available plugins...");
        sender.sendMessage(BukkitAPI.LINE);
    }

    @Command(names = "updater list", permission = "bridge.updater", hidden = true, description = "List all files in your group")
    public static void list(CommandSender sender, @Param(name = "group", defaultValue = "current") String groupName) {
        List<File> files = BridgeShared.getUpdaterManager().getFilesForGroup((groupName.equals("current") ? BridgeShared.getGroupName() : groupName));
        sender.sendMessage(BukkitAPI.LINE);
        sender.sendMessage("§6§lBridge §7❘ §fUpdater List");
        sender.sendMessage(BukkitAPI.LINE);
        if(files == null) {
            sender.sendMessage(ChatColor.RED + "The directory for the group does not exist...");
        }else if(files.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No available plugins for this group...");
        }else {
            files.forEach(file -> {
                UpdateStatus updateStatus = BridgeShared.getUpdaterManager().getStatus(file);
                sender.sendMessage(ChatColor.YELLOW + file.getName()  + ChatColor.GRAY + " (" + file.getPath() + ")" + (file.getAbsolutePath().contains("Global") ? ChatColor.RED + " [GLOBAL]" : "") + ' ' + updateStatus.getPrefix());
            });
        }
        sender.sendMessage(BukkitAPI.LINE);

    }

    @Command(names = "updater update", permission = "bridge.updater", hidden = true, description = "Update specific plugins")
    public static void update(CommandSender sender, @Param(name = "pluginname", defaultValue = "none") String pluginName) {
        String group = BridgeShared.getGroupName();
        List<File> files = BridgeShared.getUpdaterManager().getFilesForGroup(group);
        switch(pluginName) {
            case "all": {
                BridgeShared.getUpdaterManager().updatePlugins(files, cons -> sender.sendMessage(ChatColor.BLUE + cons));
                break;
            }

            case "none": {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Sorry ingame only!");
                    return;
                }
                new UpdaterMenu(group).openMenu((Player)sender);
                break;
            }

            default: {
                File pluginFile = files.stream().filter(file -> file.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
                if(pluginFile == null) {
                    sender.sendMessage(ChatColor.RED + "There is no such plugin file by the name \"" + pluginName + "\", use /updater list to get a list of plugins.");
                    return;
                }

                sender.sendMessage(ChatColor.GREEN + "Attempting to update " + pluginFile.getName() + "!");
                BridgeShared.getUpdaterManager().updatePlugins(Collections.singletonList(pluginFile), cons -> sender.sendMessage(ChatColor.BLUE + cons));
            }

        }

    }

}
