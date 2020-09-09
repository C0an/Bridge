package rip.protocol.bridge.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.listeners.BridgeListener;
import rip.protocol.bridge.bukkit.utils.BukkitUtils;
import rip.protocol.bridge.bukkit.utils.impl.BridgeImplementer;
import rip.protocol.bridge.bukkit.utils.impl.BukkitStatusImplementer;
import rip.protocol.bridge.bukkit.utils.impl.ProfileParamater;
import rip.protocol.bridge.bukkit.utils.impl.RankParamater;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.status.StatusHandler;
import rip.protocol.plib.command.FrozenCommandHandler;
import rip.protocol.plib.nametag.FrozenNametagHandler;

public class Bridge extends JavaPlugin {

    @Getter private static Bridge instance;
    @Getter private boolean isBooted = false;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        new BridgeShared();
        StatusHandler.registerProvider(new BukkitStatusImplementer());
    }

    @Override
    public void onEnable() {
        FrozenCommandHandler.registerAll(this);
        FrozenCommandHandler.registerParameterType(Rank.class, new RankParamater());
        FrozenCommandHandler.registerParameterType(Profile.class, new ProfileParamater());
        FrozenNametagHandler.registerProvider(new BridgeImplementer());

        BukkitUtils.registerListeners(BridgeListener.class);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> isBooted = true);
    }

    @Override
    public void onDisable() {
        BridgeShared.shutdown();
    }

}
