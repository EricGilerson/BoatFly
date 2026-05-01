package com.boatfly;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
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
	private static double lastPlayerX;
	private static double lastPlayerZ;
	private static long lastCheckTime;
	private static double playerSpeed;
	private static Vec3 boat;
	private static final Minecraft client = Minecraft.getInstance();
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
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("boatspeed")
					.then(Commands.argument("value", FloatArgumentType.floatArg())
							.executes(context -> {
								final double value = Math.round((FloatArgumentType.getFloat(context, "value")) * 1000.0) / 1000.0;
								changeSpeed(Minecraft.getInstance(), (float) value, false);
								return (int) value;
							})));
		});
	}

	private void onRenderTick(Minecraft client) {
		if (client.player != null) {
			if (BoatFlight.consumeClick()) {
				BoatFlyOn = !BoatFlyOn;
				System.out.println(BoatFlyOn);
				BoatSpeed = 1;
				if (BoatFlyOn) {
					client.player.sendOverlayMessage(Component.literal("Boat Fly is now On and Boat Speed is Set to 8 blocks/s"));
					changeSpeed(client, 8, true);
				} else {
					client.player.sendOverlayMessage(Component.literal("Boat Fly is now Off"));
				}
			}
			if (BoatSpeedDec.consumeClick()) {
				if(boatVelocity != 0) {
					changeSpeed(client, boatVelocity + -1, false);
				}

			}
			if (BoatSpeedInc.consumeClick()) {
				changeSpeed(client, boatVelocity + 1, false);
			}
			if (client.options.keyJump.isDown()) {
				if (BoatFlyClient.BoatFlyOn) {
					if (!client.player.isPassenger())
						return;
					Entity vehicle = client.player.getVehicle();
					assert vehicle != null;
					Vec3 velocity = vehicle.getDeltaMovement();
					double motionY = client.options.keyJump.isDown() ? 0.3 : 0;
					vehicle.setDeltaMovement(new Vec3(velocity.x, motionY, velocity.z));
				}

			}
			if (client.options.keyUp.isDown()) {
				if (BoatFlyClient.BoatSpeed != 1) {
					if (!client.player.isPassenger())
						return;
					Entity vehicle = client.player.getVehicle();
					assert vehicle != null;
					Vec3 velocity = vehicle.getDeltaMovement();
					boat = new Vec3(velocity.x * BoatSpeed, velocity.y, velocity.z * BoatSpeed);
					vehicle.setDeltaMovement(boat);
					assert BoatFlyClient.client.player != null;
				}
			}

		}


	}
	public void changeSpeed(Minecraft client, double speed, boolean fly){

		BoatSpeed = multiplier(speed);
		double scale = Math.pow(10, 5);
		boatVelocity = Math.round(speed * scale) / scale;
		double BoatSpeedRound = Math.round(BoatSpeed * scale) / scale;
		System.out.println(BoatSpeedRound);
		String StringBoatSpeed = Double.toString(speed);
		assert client.player != null;
		if(!fly) {
			client.player.sendOverlayMessage(Component.literal("Your Boat Speed is now " + StringBoatSpeed + " blocks/s"));
		}
	}

	private double multiplier(double velocity){
		if (velocity <= 0) return 0;
		double finalMultiplier;
		finalMultiplier = Math.pow((-5.33893*Math.pow(Math.log(velocity-8+11.9072),-3.31832)+1.26253),0.470998);
		return finalMultiplier;
	}
}
