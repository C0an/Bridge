package rip.protocol.bridge.bukkit.events;

import rip.protocol.bridge.shared.grant.Grant;
import rip.protocol.bridge.bukkit.utils.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GrantRemoveEvent extends BaseEvent {

    private UUID uuid;
    private Grant grant;

}
