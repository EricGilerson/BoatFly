package com.boatfly;

import com.boatfly.config.BoatFlyConfig;
import com.boatfly.network.BoatFlyConfigPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class BoatFly implements ModInitializer {
    private static BoatFlyConfig config;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(BoatFlyConfigPayload.TYPE, BoatFlyConfigPayload.CODEC);

        config = BoatFlyConfig.load(FabricLoader.getInstance().getConfigDir());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player, BoatFlyConfigPayload.TYPE)) {
                ServerPlayNetworking.send(handler.player, toPayload(config));
            }
        });
    }

    private static BoatFlyConfigPayload toPayload(BoatFlyConfig config) {
        return new BoatFlyConfigPayload(
                config.flight.enabled,
                config.speed.allowPlayerChange,
                config.speed.defaultSpeed,
                config.speed.max,
                config.speed.min,
                config.keybinds.flightToggle,
                config.keybinds.speedUp,
                config.keybinds.speedDown,
                config.command.boatspeedEnabled
        );
    }
}
