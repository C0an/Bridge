package rip.protocol.bridge.shared.packets;

import rip.protocol.bridge.shared.packet.Packet;
import rip.protocol.bridge.shared.utils.JsonChain;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RankUpdatePacket implements Packet {

    private UUID rank;
    private String creator, server;

    public RankUpdatePacket() {}

    public RankUpdatePacket(UUID rank, String creator, String server) {
        this.rank = rank;
        this.creator = creator;
        this.server = server;
    }


    @Override
    public int id() {
        return 2;
    }

    @Override
    public String sentFrom() {
        return server;
    }

    @Override
    public boolean selfRecieve() {
        return false;
    }


    @Override
    public JsonObject serialize() {
        return new JsonChain()
                .addProperty("rank", rank.toString())
                .addProperty("creator", creator)
                .addProperty("server", server)
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        this.rank = UUID.fromString(jsonObject.get("rank").getAsString());
        this.creator = jsonObject.get("creator").getAsString();
        this.server = jsonObject.get("server").getAsString();
    }
}
