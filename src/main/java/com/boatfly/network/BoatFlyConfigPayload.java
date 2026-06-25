package com.boatfly.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BoatFlyConfigPayload(
        boolean flightEnabled,
        boolean allowPlayerSpeedChange,
        double defaultSpeed,
        double maxSpeed,
        double minSpeed,
        boolean keybindFlightToggle,
        boolean keybindSpeedUp,
        boolean keybindSpeedDown,
        boolean commandEnabled
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BoatFlyConfigPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("boatfly", "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoatFlyConfigPayload> CODEC =
            StreamCodec.of(BoatFlyConfigPayload::write, BoatFlyConfigPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, BoatFlyConfigPayload payload) {
        buf.writeBoolean(payload.flightEnabled);
        buf.writeBoolean(payload.allowPlayerSpeedChange);
        buf.writeDouble(payload.defaultSpeed);
        buf.writeDouble(payload.maxSpeed);
        buf.writeDouble(payload.minSpeed);
        buf.writeBoolean(payload.keybindFlightToggle);
        buf.writeBoolean(payload.keybindSpeedUp);
        buf.writeBoolean(payload.keybindSpeedDown);
        buf.writeBoolean(payload.commandEnabled);
    }

    private static BoatFlyConfigPayload read(RegistryFriendlyByteBuf buf) {
        return new BoatFlyConfigPayload(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
