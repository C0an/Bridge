package rip.protocol.bridge.shared.redis;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.packet.Packet;
import com.google.gson.JsonParser;
import redis.clients.jedis.JedisPubSub;

public class ChristianSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {

        new Thread(() -> {
            if (channel.equals(BridgeShared.getPacketHandler().getChannel())) {
                String[] args = message.split("¸");
                int id;
                try {
                    id = Integer.parseInt(args[0]);
                } catch (Exception ignored) {
                    return;
                }

                Packet packet = BridgeShared.getPacketHandler().getPacketByID(id);
                if (packet == null) {
                    BridgeShared.sendLog("[Packet Handler] Failed to find a packet with the ID: " + id);
                    return;
                }
                packet.deserialize(new JsonParser().parse(args[1]).getAsJsonObject());
                BridgeShared.getPacketHandler().handlePacket(packet);
            }
        }).start();
    }

}
