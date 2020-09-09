package rip.protocol.bridge.shared.packets;

import rip.protocol.bridge.BridgeShared;
import rip.protocol.bridge.shared.packet.Packet;
import rip.protocol.bridge.shared.utils.JsonChain;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class PunishmentPacket implements Packet {

    private String json;

    public PunishmentPacket() {}

    @Override
    public int id() {
        return 5;
    }

    @Override
    public String sentFrom() {
        return BridgeShared.getSystemName();
    }

    @Override
    public boolean selfRecieve() {
        return true;
    }

    @Override
    public JsonObject serialize() {
        return new JsonChain()
                .addProperty("json", json)
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        this.json = jsonObject.get("json").getAsString();
    }
}
