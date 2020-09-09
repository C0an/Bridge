package rip.protocol.bridge.shared.disguise;

import lombok.Data;
import lombok.Getter;
import rip.protocol.bridge.BridgeShared;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Skin {

    @Getter private static List<Skin> skins = new ArrayList<>();

    private UUID uuid;
    private String skinName, texture, signature;

    public Skin() {
        skins.clear();

        List<Skin> skinList = new ArrayList<>();
        try {
            BridgeShared.getMongoManager().getSkinsInDB(callback-> {

                BridgeShared.sendLog("§aFound " + callback.size() + " skins in database.");


                for (int i = 0; i < callback.size(); i++) {
                    UUID uuid = callback.get(i);
                    BridgeShared.getMongoManager().loadSkin(uuid, cback -> {
                        if (cback == null) {
                            System.out.println("Welp. thats a null.");
                            return;
                        }

                        skinList.add(cback);
                    }, false);

                    if(callback.size() == skinList.size()) {
                        skins.addAll(skinList);
                        BridgeShared.sendLog("§aLoaded all skins.");
                    }
                }

            });

        } catch (Exception ex) {
            BridgeShared.sendLog("§cFailed to initialize the skin manager.");
            ex.printStackTrace();
            BridgeShared.sendLog(ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }

    }

    public Skin(UUID uuid, String skinName, String texture, String signature) {
        this.uuid = uuid;
        this.skinName = skinName;
        this.texture = texture;
        this.signature = signature;
    }

    public Skin(String skinName, String texture, String signature) {
        this.uuid = UUID.randomUUID();
        this.skinName = skinName;
        this.texture = texture;
        this.signature = signature;
        skins.add(this);
    }

    public void save() {
        BridgeShared.getMongoManager().saveSkin(this, callback -> {
            if (callback) {
                BridgeShared.sendLog("§aSuccessfully saved the skin §f" + getSkinName() + "§a.");
            } else {
                BridgeShared.sendLog("§cFailed to save the skin §f" + getSkinName() + "§c.");
            }
        }, true);
    }

    public void delete() {
        BridgeShared.getMongoManager().removeSkin(this.uuid, callback -> {
            if (callback) {
                BridgeShared.sendLog("§aSuccessfully removed the skin §f" + getSkinName() + "§a.");
                skins.remove(this);
            } else {
                BridgeShared.sendLog("§cFailed to remove the skin §f" + getSkinName() + "§c.");
            }
        }, true);
    }

    public static Skin getSkin(String name) {
        return skins.stream().filter(skin -> skin.getSkinName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }


}
