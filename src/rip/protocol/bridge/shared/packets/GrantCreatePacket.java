package rip.protocol.bridge.shared.packets;

import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.packet.Packet;
import rip.protocol.bridge.shared.utils.JsonChain;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GrantCreatePacket implements Packet {

    private Grant grant;
    private UUID target;
    private String granter, server;

    public GrantCreatePacket() {}

    public GrantCreatePacket(Grant grant, UUID target, String granter, String server) {
        this.grant = grant;
        this.target = target;
        this.granter = granter;
        this.server = server;
    }


    @Override
    public int id() {
        return 3;
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
                .addProperty("grant", Grant.serialize(grant).toString())
                .addProperty("target", target.toString())
                .addProperty("granter", granter)
                .addProperty("server", server)
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        this.grant = Grant.deserialize(jsonObject.get("grant").getAsString());
        this.target = UUID.fromString(jsonObject.get("target").getAsString());
        this.granter = jsonObject.get("granter").getAsString();
        this.server = jsonObject.get("server").getAsString();
    }
}
