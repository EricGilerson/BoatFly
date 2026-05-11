package com.boatfly;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class BoatFlyClient implements ClientModInitializer {
	public static KeyMapping BoatFlight;
	public static boolean BoatFlyOn;
	public static KeyMapping BoatSpeedInc;
	public static KeyMapping BoatSpeedDec;
	public static double BoatSpeed = 1;
	public static double boatVelocity = 8;
	private static Vec3 boat;
	private static final KeyMapping.Category BOATFLY_CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("boatfly", "main"));

	@Override
	public void onInitializeClient() {
		BoatFlyOn = false;
		BoatFlight   = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.boatfly.toggle",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, BOATFLY_CATEGORY));
		BoatSpeedInc = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.boatfly.speed_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, BOATFLY_CATEGORY));
		BoatSpeedDec = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.boatfly.speed_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, BOATFLY_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.isWindowActive()) {
				onRenderTick(client);
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal("boatspeed")
					.then(ClientCommands.argument("value", FloatArgumentType.floatArg())
							.executes(context -> {
								final double value = Math.round((FloatArgumentType.getFloat(context, "value")) * 1000.0) / 1000.0;
								changeSpeed(Minecraft.getInstance(), (float) value, false);
								return (int) value;
							})));
		});
	}

	private void onRenderTick(Minecraft client) {
		if (client.player == null) return;

		if (BoatFlight.consumeClick()) {
			BoatFlyOn = !BoatFlyOn;
			BoatSpeed = 1;
			if (BoatFlyOn) {
				client.player.sendOverlayMessage(Component.literal("Boat Fly is now On and Boat Speed is Set to 8 blocks/s"));
				changeSpeed(client, 8, true);
			} else {
				client.player.sendOverlayMessage(Component.literal("Boat Fly is now Off"));
			}
		}
		if (BoatSpeedDec.consumeClick()) {
			if (boatVelocity > 0) {
				changeSpeed(client, Math.max(0, boatVelocity - 1), false);
			}
		}
		if (BoatSpeedInc.consumeClick()) {
			changeSpeed(client, boatVelocity + 1, false);
		}

		Entity vehicle = client.player.getVehicle();
		if (vehicle == null) return;

		if (client.options.keyJump.isDown() && BoatFlyOn) {
			Vec3 velocity = vehicle.getDeltaMovement();
			vehicle.setDeltaMovement(new Vec3(velocity.x, 0.3, velocity.z));
		}
		if (client.options.keyUp.isDown() && BoatSpeed != 1) {
			Vec3 velocity = vehicle.getDeltaMovement();
			boat = new Vec3(velocity.x * BoatSpeed, velocity.y, velocity.z * BoatSpeed);
			vehicle.setDeltaMovement(boat);
		}
	}
	public void changeSpeed(Minecraft client, double speed, boolean fly){
		BoatSpeed = multiplier(speed);
		double scale = Math.pow(10, 5);
		boatVelocity = Math.round(speed * scale) / scale;
		String StringBoatSpeed = Double.toString(speed);
		if (!fly && client.player != null) {
			client.player.sendOverlayMessage(Component.literal("Your Boat Speed is now " + StringBoatSpeed + " blocks/s"));
		}
	}

	private double multiplier(double velocity){
		if (velocity <= 0) return 0;
		double logInput = velocity - 8 + 11.9072;
		if (logInput <= 0) return 1.0;
		double base = -5.33893 * Math.pow(Math.log(logInput), -3.31832) + 1.26253;
		if (base <= 0) return 0;
		return Math.pow(base, 0.470998);
	}
}
