package cz.neumimto.events;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.UUID;

/**
 * Created by NeumimTo on 30.1.2016.
 */
public class PlayerGuiModInitEvent implements Event {

    UUID uuid;

    public PlayerGuiModInitEvent(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Cause getCause() {
        return Cause.source(uuid).build();
    }

}
