package rip.protocol.bridge.bukkit.commands.punishment;

import org.bukkit.entity.Player;
import rip.protocol.bridge.bukkit.utils.menu.punishment.MainPunishmentMenu;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.plib.command.Command;
import rip.protocol.plib.command.Param;

public class CheckCommand {

    @Command(names = { "checkpunishments", "cp", "c" }, permission = "bridge.command.checkpunishments", description = "Check a user's punishments", async = true)
    public static void checkPunishments(final Player sender, @Param(name = "target", extraData = "get") Profile target) {
        new MainPunishmentMenu(target.getUuid().toString(), target.getUsername()).openMenu(sender);
    }

}
