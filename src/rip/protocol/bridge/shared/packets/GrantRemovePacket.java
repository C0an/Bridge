package rip.protocol.bridge.shared.packets;

import com.google.gson.JsonObject;
import lombok.Getter;
import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.shared.packet.Packet;
import rip.protocol.bridge.shared.utils.JsonChain;

import java.util.UUID;

@Getter
public class GrantRemovePacket implements Packet {

    private Grant grant;
    private UUID target;
    private String granter, server;

    public GrantRemovePacket() {}

    public GrantRemovePacket(Grant grant, UUID target, String remover, String server) {
        this.grant = grant;
        this.target = target;
        this.granter = granter;
        this.server = server;
    }


    @Override
    public int id() {
        return 911;
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
