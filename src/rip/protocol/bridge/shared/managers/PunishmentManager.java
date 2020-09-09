package rip.protocol.bridge.shared.managers;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.punishment.PunishmentType;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.utils.Callback;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class PunishmentManager {

    @Getter private ArrayList<Punishment> punishments = new ArrayList<>();


    public void init() {
//        punishments.clear();
        List<Punishment> punishmentList = new ArrayList<>();

        try {
            BridgeShared.getMongoManager().getPunishmentsInDB(callback-> {

                BridgeShared.sendLog("§aFound " + callback.size() + " punishments in database.");


                for (int i = 0; i < callback.size(); i++) {
                    UUID uuid = callback.get(i);
                    BridgeShared.getMongoManager().loadPunishment(uuid, cback -> {
                        if (cback == null) {
                            System.out.println("Welp. thats a null.");
                            return;
                        }

                        if(cback.getTarget() == null) {
                            BridgeShared.sendLog("§aDeleted punishment due to non-existant profile.");
                            cback.delete();
                            return;
                        }

                        punishmentList.add(cback);
                    }, false);

                    if(callback.size() == punishmentList.size()) {
                        punishments.addAll(punishmentList);
                        BridgeShared.sendLog("§aLoaded all punishments.");
                    }
                }
//                for (Iterator<UUID> iterator = callback.iterator(); iterator.hasNext();) {
//                    UUID uuid = iterator.next();
//                    BridgeShared.getMongoManager().loadPunishment(uuid, cback -> {
//                        if (cback == null) {
//                            return;
//                        }
//                        punishmentList.add(cback);
//                        System.out.println(punishmentList);
//                    }, true);
//
//                }



            });

        } catch (Exception ex) {
            BridgeShared.sendLog("§cFailed to initialize the punishment manager.");
            ex.printStackTrace();
            BridgeShared.sendLog(ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }


    public Punishment getPunishmentByID(UUID id) {
        return punishments.stream().filter(punishment -> punishment.getUuid().toString().equals(id.toString())).findFirst().orElse(null);
    }

    public void addPunishment(Punishment punishment) {
        if(punishments.contains(punishment)) return;
        punishments.add(punishment);
    }

    public void load(String json) {

    }

    public void save() {
        punishments.forEach(punishment -> {
            BridgeShared.getMongoManager().savePunishment(punishment, callback -> {
                if (callback) {
                    BridgeShared.sendLog("§aSuccessfully saved punishment §f" + punishment.getUuid().toString() + "§a.");
                } else {
                    BridgeShared.sendLog("§cFailed to save punishment §f" + punishment.getUuid().toString() + "§c.");
                }
            }, true);
        });
    }

    public Set<Punishment> getActivePunishmentsByTypes(UUID uuid, PunishmentType... punishmentType) {
        return this.getPunishments().stream().filter((punishment) -> punishment.isActive() && !punishment.isIP() &&punishment.getTarget().getUuid().toString().equals(uuid.toString()) && Arrays.asList(punishmentType).contains(punishment.getPunishmentType())).collect(Collectors.toSet());
    }

    public boolean isCurrentlyPunishedByTypes(UUID uuid, PunishmentType... punishmentType) {
        return this.getActivePunishmentsByTypes(uuid, punishmentType).size() > 0;
    }

    public Set<Punishment> getActiveIPPunishmentsByTypes(String IP, PunishmentType... punishmentType) {
        return this.getPunishments().stream().filter((punishment) -> punishment.isActive() && punishment.isIP() && punishment.getTarget().getIP().equals(IP) && Arrays.asList(punishmentType).contains(punishment.getPunishmentType())).collect(Collectors.toSet());
    }

    public boolean isCurrentlyIPPunishedByTypes(String IP, PunishmentType... punishmentType) {
        return this.getActiveIPPunishmentsByTypes(IP, punishmentType).size() > 0;
    }

    public void isBanEvading(Profile profile, Callback<Boolean> cbck) {
        List<Profile> banEvading  = new ArrayList<>();

        BridgeShared.getMongoManager().getProfiles(profile.getIP(), callback -> {
            if (callback == null || callback.isEmpty() || callback.size() == 1) {
                return;
            }
            callback.forEach(pr -> {
                if(pr.getUuid().toString().equals(profile.getUuid().toString())) return;
                if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BLACKLIST) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BLACKLIST)) banEvading.add(pr);
                if(BridgeShared.getPunishmentManager().isCurrentlyPunishedByTypes(pr.getUuid(), PunishmentType.BAN) || BridgeShared.getPunishmentManager().isCurrentlyIPPunishedByTypes(pr.getIP(), PunishmentType.BAN)) banEvading.add(pr);
            });
            cbck.call(!banEvading.isEmpty());
        }, true);

    }


}
