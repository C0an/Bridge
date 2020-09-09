package rip.protocol.bridge.shared.packet;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.redis.ChristianSubscriber;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PacketHandler {

    @Getter private JedisPool jedisPool;
    @Getter private Map<Integer, Class> packets = new HashMap<>();
    @Getter private Set<PacketListener> listeners = new HashSet<>();
    @Getter private String channel;

    public PacketHandler(String channel) {
        this.channel = channel;
        this.jedisPool = new JedisPool(new JedisPoolConfig(), BridgeShared.getRedisHost(), BridgeShared.getRedisPort(), 0, (BridgeShared.isRedisAuth() ? BridgeShared.getRedisPassword() : null), BridgeShared.getRedisDB());
        new Thread(() -> {
            Jedis jedis = jedisPool.getResource();
            jedis.subscribe(new ChristianSubscriber(), channel);
        }).start();
    }

    public void sendPacket(Packet packet) {
        new Thread(() -> {
            Jedis jedis = jedisPool.getResource();
            jedis.publish(channel, packet.id() + "¸" + packet.serialize());
            jedis.close();
        }).start();
        BridgeShared.sendLog("Sent Packet: " + packet.id() + " - " + packet.getClass().getSimpleName() );

    }

    public void handlePacket(Packet packet) {
        boolean cancel = packet.sentFrom().equals(BridgeShared.getSystemName()) && !packet.selfRecieve();
        BridgeShared.sendLog("Incoming Packet §7(" + packet.id() + ") §f - Detected Class: " + getPacketByID(packet.id()).getClass().getSimpleName() + (cancel ? " §c[CANCELLED]" : ""));
        if(!cancel) this.listeners.forEach(packetListener -> packetListener.receive(packet));
    }

    public Packet getPacketByID(int id) {
        if (!packets.containsKey(id)) {
            return null;
        }
        try {
            return (Packet) packets.get(id).newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void registerPacket(Class packet) {
        try {
            int id = (int)packet.getDeclaredMethod("id", (Class<?>[])new Class[0]).invoke(packet.newInstance(), null);
            if (packets.containsKey(id)) {
                throw new IllegalStateException("Packet with that ID already exists");
            }
            packets.put(id, packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerPackets(Class... packets) {
        for (final Class packet : packets) {
            this.registerPacket(packet);
        }
    }

    public void registerListener(PacketListener packetListener) {
        try {
            if (listeners.contains(packetListener)) {
                throw new IllegalStateException("Packet listener already registered");
            }
            listeners.add(packetListener);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
