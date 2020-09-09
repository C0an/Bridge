package rip.protocol.bridge.shared.status;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import rip.protocol.bridge.BridgeShared;

import java.util.ArrayList;
import java.util.List;

public class StatusHandler {

    private static List<StatusProvider> providers = new ArrayList<>();
    private static boolean initiated = false;


    public static void init() {
        Preconditions.checkState(!StatusHandler.initiated);
        StatusHandler.initiated = true;
        registerProvider(new StatusProvider.DefaultStatusProvider());
        BridgeShared.statusThread = new StatusThread();
        BridgeShared.statusThread.start();
    }

    public static void registerProvider(StatusProvider newProvider) {
        StatusHandler.providers.add(newProvider);
        StatusHandler.providers.sort((a, b) -> Ints.compare(b.getWeight(), a.getWeight()));
    }

    public static StatusProvider getProvider(String name) {
        return providers.stream().filter(statusProvider -> statusProvider.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static StatusProvider getProvider() {
        return providers.get(0);
    }

    public static void shutdown() {
        StatusThread.shutdownThread();
    }

}
