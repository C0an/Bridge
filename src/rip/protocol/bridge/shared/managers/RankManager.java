package rip.protocol.bridge.shared.managers;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.ranks.Rank;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RankManager {

    @Getter private Set<Rank> ranks = new HashSet<>();

    public void init() {
        ranks.clear();

        try {
            BridgeShared.getMongoManager().getRanksInDB(callback-> {

                BridgeShared.sendLog("§aFound " + callback.size() + " ranks in database.");

                List<Rank> rankList = new ArrayList<>();

                AtomicInteger done = new AtomicInteger();

                Rank rank;
                for (UUID uuid : callback) {
                    rankList.add(new Rank(uuid, true, cbck -> {
                        Rank r = getRankByID(uuid);
                        if (r != null) {
                            r.load();
                            done.getAndIncrement();
                        }
                    }));
                }
                ranks.addAll(rankList);
                if (done.get() == callback.size() && getDefaultRank() == null) {
                    Rank defaultRank = new Rank(UUID.randomUUID(), "Default", false);
                    defaultRank.setDefaultRank(true);
                    defaultRank.setHidden(false);
                    defaultRank.setColor("§7");
                    ranks.add(defaultRank);
                }
            });
            BridgeShared.sendLog("§aSuccessfully loaded all ranks.");
        } catch (Exception ex) {
            BridgeShared.sendLog("§cFailed to initialize the rank manager.");
            ex.printStackTrace();
            BridgeShared.sendLog(ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }

    public Rank getRankByName(String name) {
        return ranks.stream().filter(rank->rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Rank getRankByDisplayName(String name) {
        return ranks.stream().filter(rank->rank.getDisplayName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Rank getRankByID(UUID id) {
        return ranks.stream().filter(rank->rank.getUuid().toString().equalsIgnoreCase(id.toString())).findFirst().orElse(null);
    }

    public Rank addRank(Rank rank) {
        if(ranks.contains(rank)) return rank;
        ranks.add(rank);
        return rank;
    }

    public void save() {
        ranks.forEach(rank -> {
            BridgeShared.getMongoManager().saveRank(rank, callback -> {
                if (callback) {
                    BridgeShared.sendLog("§aSuccessfully saved rank §f" + rank.getColor() + rank.getName() + "§a.");
                } else {
                    BridgeShared.sendLog("§cFailed to save rank §f" + rank.getColor() + rank.getName() + "§c.");
                }
            }, true);
        });
    }

    public void saveDisable() {
        ranks.forEach(rank -> {
            if(rank.getName().equals("")) return;
            BridgeShared.getMongoManager().saveRank(rank, callback -> {
                if (callback) {
                    BridgeShared.sendLog("§aSuccessfully saved rank §f" + rank.getColor() + rank.getName() + "§a.");
                } else {
                    BridgeShared.sendLog("§cFailed to save rank §f" + rank.getColor() + rank.getName() + "§c.");
                }
            }, false);
        });
    }

    public Rank getDefaultRank() {
        return ranks.stream().filter(Rank::isDefaultRank).findFirst().orElse(null);
    }
}
