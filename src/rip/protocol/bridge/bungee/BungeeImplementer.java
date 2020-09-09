package rip.protocol.bridge.bungee;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.md_5.bungee.BungeeCord;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.status.StatusProvider;
import rip.protocol.bridge.shared.utils.JsonChain;

import java.util.List;
import java.util.stream.Collectors;

public class BungeeImplementer extends StatusProvider {

    public BungeeImplementer() {
        super("BungeeCord", 10);
    }

    @Override
    public String serverName() {
        return BridgeShared.getSystemName();
    }

    @Override
    public String serverStatus() {
        return "ONLINE";
    }

    @Override
    public int online() {
        return BungeeCord.getInstance().getOnlineCount();
    }

    @Override
    public int maximum() {
        return BungeeCord.getInstance().getConfig().getPlayerLimit();
    }

    @Override
    public String motd() {
        return "";
    }

    @Override
    public double tps() {
        return 0;
    }

    @Override
    public List<String> players() {
        return BungeeCord.getInstance().getPlayers().stream().map(proxiedPlayer -> proxiedPlayer.getUniqueId().toString()).collect(Collectors.toList());
    }

    @Override
    public JsonObject dataPassthrough() {
        return null;
    }
}
