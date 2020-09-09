package rip.protocol.bridge.shared.packet.listener;

import com.google.gson.Gson;
import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.bukkit.utils.BridgeBukkitRef;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.packet.Packet;
import rip.protocol.bridge.shared.packet.PacketListener;
import rip.protocol.bridge.shared.packets.*;
import rip.protocol.bridge.shared.profile.Profile;
import rip.protocol.bridge.shared.punishment.Punishment;
import rip.protocol.bridge.shared.ranks.Rank;
import rip.protocol.bridge.shared.utils.SystemType;
import rip.protocol.plib.pLib;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChristianPacketListener implements PacketListener {

    @Override
    public void receive(Packet packet) {
        switch (packet.id()) {
            case 0: {
                RankCreatePacket rankCreatePacket = (RankCreatePacket)packet;
                BridgeShared.getRankManager().addRank(new Rank(rankCreatePacket.getRank(), true, callback -> {
                    Rank rank = BridgeShared.getRankManager().getRankByID(rankCreatePacket.getRank());
                    BridgeShared.sendLog(rank.getColor() + rank.getName() + " §fhas been created by " + rankCreatePacket.getCreator() + " §7(" + rankCreatePacket.getServer() + ")");
                }));
                break;
            }

            case 1: {
                RankDeletePacket rankDeletePacket = (RankDeletePacket)packet;
                Rank rank = BridgeShared.getRankManager().getRankByID(rankDeletePacket.getRank());
                BridgeShared.getMongoManager().removeRank(rankDeletePacket.getRank(), callback -> {
                    BridgeShared.getProfileManager().getProfiles().forEach(profile -> {
                        profile.getActiveGrants().forEach(grant -> {
                            if (grant.getRank().getUuid().toString().equals(rank.toString())) {
                                profile.refreshCurrentGrant();
                            }
                        });
                    });
                    BridgeShared.sendLog(rank.getColor() + rank.getName() + " §fhas been deleted by " + rankDeletePacket.getCreator() + " §7(" + rankDeletePacket.getServer() + ")");
                }, true);
                break;
            }

            case 2: {
                RankUpdatePacket rankUpdatePacket = (RankUpdatePacket) packet;
                Rank rank = BridgeShared.getRankManager().getRankByID(rankUpdatePacket.getRank());
                BridgeShared.getMongoManager().loadRank(rankUpdatePacket.getRank(), callback->{

                    BridgeShared.getProfileManager().getProfiles().forEach(profile -> {
                        profile.getActiveGrants().forEach(grant -> {
                            if (grant.getRank().getUuid().toString().equals(rankUpdatePacket.getRank().toString())) {
                                profile.refreshCurrentGrant();
                            }
                        });
                    });
                }, true);
                break;
            }

            case 3: {

                GrantCreatePacket grantCreatePacket = (GrantCreatePacket)packet;
                Grant grant = grantCreatePacket.getGrant();
                Profile pf = BridgeShared.getProfileManager().getProfileByUUID(grantCreatePacket.getTarget());
                if(pf == null) {
                    return;
                }
                pf.applyGrant(grant, null);

                break;
            }


            case 5: {
                PunishmentPacket punishmentPacket = (PunishmentPacket)packet;
                Punishment punishment = new Gson().fromJson(punishmentPacket.getJson(), Punishment.class);
                if(BridgeShared.getSystemType() == SystemType.BUKKIT) BridgeBukkitRef.handlePunishment(punishment, punishment.isPardoned());
                break;
            }
        }
    }
}
