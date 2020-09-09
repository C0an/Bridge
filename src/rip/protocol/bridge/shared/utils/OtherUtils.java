package rip.protocol.bridge.shared.utils;

import java.util.UUID;

public class OtherUtils {

    public static boolean isUUID(String str) {
        try {
            UUID uuid = UUID.fromString(str);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

}
