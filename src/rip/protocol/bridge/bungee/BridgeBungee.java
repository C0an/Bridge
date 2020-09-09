package rip.protocol.bridge.bungee;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bungee.listeners.ChristianListener;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import rip.protocol.bridge.shared.status.StatusHandler;

import java.io.*;

public class BridgeBungee extends Plugin {

    @Getter private static BridgeBungee instance;
    @Getter private static Configuration config;
    @Getter private ConfigurationProvider configurationProvider;
    @Getter private File configFile;

    @Override
    public void onEnable() {
        instance = this;
        setupConfig();

        BungeeCord.getInstance().getPluginManager().registerListener(this, new ChristianListener());
        StatusHandler.registerProvider(new BungeeImplementer());
    }

    @Override
    public void onDisable() {
        BridgeShared.shutdown();
        instance = null;
    }


    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setupConfig() {
        System.out.println("oof");
        configFile = new File(getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
        if(config != null) {
            new BridgeShared();
        }
    }

}
